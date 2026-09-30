package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

// Data classes for responses
data class GoogleTokens(
    val access_token: String,
    val refresh_token: String?,
    val expires_in: Int?,
    val token_type: String?
)

data class GoogleProfile(
    val name: String?,
    val email: String?,
    val picture: String?
)

data class DriveFile(
    val id: String = "",
    val name: String? = null,
    val mimeType: String? = null
)

data class DriveFileListResponse(
    val files: List<DriveFile>? = null
)

sealed class DriveResult {
    data class Success(val folderId: String) : DriveResult()
    data class Error(val httpCode: Int, val message: String) : DriveResult()
    data class Exception(val exception: kotlin.Exception) : DriveResult()
}

sealed class UploadResult {
    data class Success(val fileId: String) : UploadResult()
    data class Error(val httpCode: Int, val message: String) : UploadResult()
    data class Exception(val exception: kotlin.Exception) : UploadResult()
}

class GoogleDriveService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.i("GoogleDriveService", "HTTP ${request.method} ${request.url}")
            val response = chain.proceed(request)
            Log.i("GoogleDriveService", "HTTP ${response.code} ${response.message} for ${request.url}")
            response
        }
        .build()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // OAuth 2.0 URLs
    private val TOKEN_URL = "https://oauth2.googleapis.com/token"
    private val USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
    private val DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
    private val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"

    fun getAuthUrl(clientId: String): String {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId" +
                "&redirect_uri=https://localhost/oauth2redirect" +
                "&response_type=code" +
                "&scope=https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email openid" +
                "&access_type=offline" +
                "&prompt=consent"
    }

    suspend fun exchangeCodeForTokens(code: String, clientId: String, clientSecret: String): GoogleTokens? {
        val formBody = ("code=$code" +
                "&client_id=$clientId" +
                "&client_secret=$clientSecret" +
                "&redirect_uri=https://localhost/oauth2redirect" +
                "&grant_type=authorization_code")
            .toByteArray(Charsets.UTF_8)
            .toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GoogleDriveService", "Code exchange failed: ${response.code} ${response.message}")
                    val errBody = response.body?.string()
                    Log.e("GoogleDriveService", "Error body: $errBody")
                    null
                } else {
                    val bodyString = response.body?.string() ?: return null
                    moshi.adapter(GoogleTokens::class.java).fromJson(bodyString)
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Code exchange exception", e)
            null
        }
    }

    suspend fun refreshAccessToken(refreshToken: String, clientId: String, clientSecret: String): GoogleTokens? {
        val formBody = ("refresh_token=$refreshToken" +
                "&client_id=$clientId" +
                "&client_secret=$clientSecret" +
                "&grant_type=refresh_token")
            .toByteArray(Charsets.UTF_8)
            .toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GoogleDriveService", "Refresh token failed: ${response.code}")
                    null
                } else {
                    val bodyString = response.body?.string() ?: return null
                    moshi.adapter(GoogleTokens::class.java).fromJson(bodyString)
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Refresh token exception", e)
            null
        }
    }

    suspend fun getUserProfile(accessToken: String): GoogleProfile? {
        val request = Request.Builder()
            .url(USER_INFO_URL)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return null
                    moshi.adapter(GoogleProfile::class.java).fromJson(bodyString)
                } else {
                    Log.e("GoogleDriveService", "Failed to get user profile: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "User profile exception", e)
            null
        }
    }

    /**
     * Returns DriveResult with the folder ID or detailed error info.
     */
    suspend fun getOrCreateFolderResult(accessToken: String, folderName: String = "Primeros momentos", parentFolderId: String? = null): DriveResult {
        // Query to check if the folder already exists
        val query = StringBuilder("name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
        if (parentFolderId != null) {
            query.append(" and '$parentFolderId' in parents")
        }

        val queryUrl = "$DRIVE_FILES_URL?q=" +
                java.net.URLEncoder.encode(query.toString(), "UTF-8").replace("+", "%20") +
                "&fields=files(id,name,mimeType)"

        val searchRequest = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        try {
            client.newCall(searchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    Log.i("GoogleDriveService", "Folder search response body: $bodyString")
                    val listResponse = moshi.adapter(DriveFileListResponse::class.java).fromJson(bodyString)
                    val existingFolder = listResponse?.files?.firstOrNull()
                    if (existingFolder != null && existingFolder.id.isNotBlank()) {
                        Log.i("GoogleDriveService", "Found existing folder ${existingFolder.name} with ID: ${existingFolder.id}")
                        return DriveResult.Success(existingFolder.id)
                    }
                } else {
                    val err = response.body?.string() ?: "sin cuerpo"
                    Log.e("GoogleDriveService", "Folder search failure: ${response.code} ${response.message} - $err")
                    // Return error immediately for 401/403 — don't try to create folder with a bad token
                    if (response.code == 401 || response.code == 403) {
                        return DriveResult.Error(response.code, "Búsqueda carpeta: ${response.code} - $err")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Folder search exception", e)
            return DriveResult.Exception(e)
        }

        // Create folder if not found
        Log.i("GoogleDriveService", "Folder '$folderName' not found, creating a new one")
        val parentPart = if (parentFolderId != null) ", \"parents\": [\"$parentFolderId\"]" else ""
        val createBody = """{"name": "$folderName", "mimeType": "application/vnd.google-apps.folder"$parentPart }"""
            .toByteArray(Charsets.UTF_8)
            .toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())

        val createRequest = Request.Builder()
            .url("$DRIVE_FILES_URL?fields=id,name,mimeType")
            .header("Authorization", "Bearer $accessToken")
            .post(createBody)
            .build()

        return try {
            client.newCall(createRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    Log.i("GoogleDriveService", "Folder create response body: $bodyString")
                    val driveFile = moshi.adapter(DriveFile::class.java).fromJson(bodyString)
                    if (driveFile != null && driveFile.id.isNotBlank()) {
                        Log.i("GoogleDriveService", "Folder '$folderName' created successfully with ID: ${driveFile.id}")
                        DriveResult.Success(driveFile.id)
                    } else {
                        DriveResult.Error(response.code, "Carpeta creada pero sin ID en respuesta: $bodyString")
                    }
                } else {
                    val err = response.body?.string() ?: "sin cuerpo"
                    Log.e("GoogleDriveService", "Folder creation failed: ${response.code} ${response.message} - $err")
                    DriveResult.Error(response.code, "Crear carpeta: ${response.code} - $err")
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Folder creation exception", e)
            DriveResult.Exception(e)
        }
    }

    /**
     * Backward-compatible wrapper that returns just the folder ID or null.
     */
    suspend fun getOrCreateFolder(accessToken: String, folderName: String = "Primeros momentos", parentFolderId: String? = null): String? {
        return when (val result = getOrCreateFolderResult(accessToken, folderName, parentFolderId)) {
            is DriveResult.Success -> result.folderId
            else -> null
        }
    }

    suspend fun uploadFileToFolder(
        accessToken: String,
        folderId: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String
    ): UploadResult {
        // Upload with multipart / related (Required by Google Drive API for multiplayer format)
        val metadata = """{"name": "$fileName", "parents": ["$folderId"]}"""
        val relatedType = "multipart/related".toMediaTypeOrNull() ?: MultipartBody.MIXED

        val multipartBody = MultipartBody.Builder()
            .setType(relatedType)
            .addPart(
                null,
                metadata.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())
            )
            .addPart(
                null,
                fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("$DRIVE_UPLOAD_URL?uploadType=multipart")
            .header("Authorization", "Bearer $accessToken")
            .post(multipartBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val driveFile = moshi.adapter(DriveFile::class.java).fromJson(bodyString)
                    val fileId = driveFile?.id ?: "unknown"
                    Log.i("GoogleDriveService", "Successfully uploaded visual file $fileName with ID $fileId to folder $folderId")
                    UploadResult.Success(fileId)
                } else {
                    val err = response.body?.string() ?: "sin cuerpo"
                    Log.e("GoogleDriveService", "File upload failed: ${response.code} ${response.message} - $err")
                    UploadResult.Error(response.code, err)
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "File upload threw exception", e)
            UploadResult.Exception(e)
        }
    }

    suspend fun findFileInFolder(accessToken: String, folderId: String, fileName: String): String? {
        val queryUrl = "$DRIVE_FILES_URL?q=" +
                java.net.URLEncoder.encode("name = '$fileName' and '$folderId' in parents and trashed = false", "UTF-8").replace("+", "%20")

        val searchRequest = Request.Builder()
            .url(queryUrl)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            client.newCall(searchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val listResponse = moshi.adapter(DriveFileListResponse::class.java).fromJson(bodyString)
                    listResponse?.files?.firstOrNull()?.id
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "File search exception", e)
            null
        }
    }

    suspend fun downloadFileContent(accessToken: String, fileId: String): String? {
        val request = Request.Builder()
            .url("$DRIVE_FILES_URL/$fileId?alt=media")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.e("GoogleDriveService", "Download file failed: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Download file exception", e)
            null
        }
    }

    suspend fun updateFileContent(
        accessToken: String,
        fileId: String,
        fileBytes: ByteArray,
        mimeType: String
    ): Boolean {
        val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$DRIVE_UPLOAD_URL/$fileId?uploadType=media")
            .header("Authorization", "Bearer $accessToken")
            .patch(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i("GoogleDriveService", "Successfully updated file $fileId")
                    true
                } else {
                    Log.e("GoogleDriveService", "Update file failed: ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Update file exception", e)
            false
        }
    }

    suspend fun uploadOrUpdateFileInFolder(
        accessToken: String,
        folderId: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String
    ): Boolean {
        val existingFileId = findFileInFolder(accessToken, folderId, fileName)
        return if (existingFileId != null) {
            updateFileContent(accessToken, existingFileId, fileBytes, mimeType)
        } else {
            val result = uploadFileToFolder(accessToken, folderId, fileName, fileBytes, mimeType)
            result is UploadResult.Success
        }
    }

    suspend fun deleteFile(accessToken: String, fileId: String): Boolean {
        val request = Request.Builder()
            .url("$DRIVE_FILES_URL/$fileId")
            .header("Authorization", "Bearer $accessToken")
            .delete()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i("GoogleDriveService", "Successfully deleted file $fileId")
                    true
                } else {
                    Log.e("GoogleDriveService", "Delete file failed: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Delete file exception", e)
            false
        }
    }
}

