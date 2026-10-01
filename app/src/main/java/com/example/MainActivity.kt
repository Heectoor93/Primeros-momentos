package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.BabyRepository
import com.example.ui.BabyViewModel
import com.example.ui.BabyViewModelFactory
import com.example.ui.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BabyRepository(database.babyDao())
        val authRepository = com.example.data.AuthRepository(applicationContext)
        val firestoreRepository = com.example.data.FirestoreRepository()

        // Setup ViewModel with Factory
        val viewModel: BabyViewModel by viewModels {
            BabyViewModelFactory(application, repository, authRepository, firestoreRepository)
        }

        // Register Activity Result Launcher for Drive Consent Prompt
        val driveAuthLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                android.util.Log.i("MainActivity", "Google Drive consent granted by user, fetching OAuth token...")
                viewModel.onDriveConsentGranted()
            } else {
                android.util.Log.w("MainActivity", "Drive consent prompt returned result code: ${result.resultCode}")
            }
        }

        // Listen for Drive auth pending intent events from ViewModel
        lifecycleScope.launch {
            viewModel.drivePendingIntentEvent.collect { intentSender ->
                val request = IntentSenderRequest.Builder(intentSender).build()
                driveAuthLauncher.launch(request)
            }
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
