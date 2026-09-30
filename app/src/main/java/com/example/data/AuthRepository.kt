package com.example.data

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // Web Client ID from google-services.json (Firebase Auth)
    private val webClientId = context.getString(com.example.R.string.default_web_client_id)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user ?: return Result.failure(Exception("Usuario nulo tras login"))
            Log.i("AuthRepository", "Signed in: ${user.email}")
            Result.success(user)

        } catch (e: GetCredentialException) {
            Log.e("AuthRepository", "Credential error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Requests Drive file-scope authorization using GoogleAuthUtil / Identity.
     * Uses the user's logged in account email to retrieve the OAuth 2.0 Access Token natively from Google Play Services.
     * No Client Secret is needed.
     */
    suspend fun getDriveAccessTokenForAccount(accountEmail: String): Pair<String?, IntentSender?> {
        return withContext(Dispatchers.IO) {
            try {
                val scope = "oauth2:https://www.googleapis.com/auth/drive"
                val account = android.accounts.Account(accountEmail, "com.google")
                val token = GoogleAuthUtil.getToken(context, account, scope)
                Log.i("AuthRepository", "Successfully retrieved Drive OAuth access token for $accountEmail (length=${token?.length})")
                Pair(token, null)
            } catch (e: UserRecoverableAuthException) {
                Log.i("AuthRepository", "User consent required for Drive scope: ${e.message}")
                val pendingIntent = e.intent?.let {
                    PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                }
                Pair(null, pendingIntent?.intentSender)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to get Drive OAuth token: ${e.message}", e)
                Pair(null, null)
            }
        }
    }

    /**
     * Invalidates the cached Drive token so the next getDriveAccessTokenForAccount call
     * fetches a completely fresh token from Google servers.
     */
    suspend fun invalidateDriveToken(token: String) {
        withContext(Dispatchers.IO) {
            try {
                GoogleAuthUtil.clearToken(context, token)
                Log.i("AuthRepository", "Invalidated cached Drive token")
            } catch (e: Exception) {
                Log.w("AuthRepository", "Error invalidating token: ${e.message}")
            }
        }
    }

    suspend fun authorizeGoogleDrive(activity: Activity): Pair<String?, IntentSender?> {
        val email = currentUser?.email
        if (!email.isNullOrBlank()) {
            val result = getDriveAccessTokenForAccount(email)
            if (result.first != null || result.second != null) {
                return result
            }
        }
        
        return try {
            val driveScope = Scope("https://www.googleapis.com/auth/drive")
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(driveScope))
                .build()

            val authResult = Identity.getAuthorizationClient(activity)
                .authorize(request)
                .await()

            if (authResult.hasResolution()) {
                Pair(null, authResult.pendingIntent?.intentSender)
            } else {
                Pair(authResult.accessToken, null)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Drive authorization error: ${e.message}", e)
            Pair(null, null)
        }
    }

    /**
     * Extracts access token from an authorization result returned by the Drive consent screen.
     */
    fun getAccessTokenFromAuthResult(authResult: AuthorizationResult): String? {
        return authResult.accessToken
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("AuthRepository", "clearCredentialState error: ${e.message}")
        }
        auth.signOut()
    }
}
