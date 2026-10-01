package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class BabyViewModel(
    application: Application,
    private val repository: BabyRepository,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("baby_app_prefs", android.content.Context.MODE_PRIVATE)
    private val driveService = GoogleDriveService()

    // Target activity type for navigation from Home
    val targetActivityType = MutableStateFlow<String?>(null)

    // Current Auth & Sync State
    val isAuthLoading = MutableStateFlow(false)
    val authErrorMessage = MutableStateFlow<String?>(null)
    val isRestoringFromCloud = MutableStateFlow(false)
    val isSyncingCloud = MutableStateFlow(false)

    // Registered Google Account for Drive Backup & Auth
    val googleAccountEmail = MutableStateFlow<String?>(
        authRepository.currentUser?.email ?: sharedPrefs.getString("google_account_email", null)
    )
    val googleAccountName = MutableStateFlow<String?>(
        authRepository.currentUser?.displayName ?: sharedPrefs.getString("google_account_name", null)
    )

    val googleAccessToken = MutableStateFlow(sharedPrefs.getString("google_access_token", null))
    val googleFolderId = MutableStateFlow(sharedPrefs.getString("google_folder_id", null))

    // Helper Date utilities
    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }

    private fun parseDateString(dateStr: String): Long {
        return try {
            val format = if (dateStr.contains("/")) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseDateTimeString(dateStr: String, timeStr: String): Long {
        return try {
            val format = if (dateStr.contains("/")) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            }
            format.parse("$dateStr $timeStr")?.time ?: parseDateString(dateStr)
        } catch (e: Exception) {
            parseDateString(dateStr)
        }
    }

    private fun calculateMinutesDiff(start: String, end: String): Int {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = format.parse(start) ?: return 60
            var endDate = format.parse(end) ?: return 60
            if (endDate.before(startDate)) {
                val cal = Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.DATE, 1)
                }
                endDate = cal.time
            }
            ((endDate.time - startDate.time) / 60000).toInt()
        } catch (e: Exception) {
            60
        }
    }


    fun updatePreferredPacifierColor(babyId: Int, colorName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val profile = repository.getBabyProfileByIdSync(babyId)
            if (profile != null) {
                val updatedProfile = profile.copy(preferredPacifierColor = colorName)
                repository.saveBabyProfile(updatedProfile)
            }
        }
    }

    suspend fun calculateMilestonePercent(babyId: Int): Int {
        val profile = repository.getBabyProfileByIdSync(babyId) ?: return 0
        val age = getAgeInMonths(profile.dob)
        val moments = repository.getAllMomentsSync(babyId)

        val milestones = if (age < 12) {
            moments.filter { (it.ageRange == "0-6 meses" || it.ageRange == "6-12 meses") && !it.isCustom }
        } else {
            moments.filter { (it.ageRange == "12-18 meses" || it.ageRange == "18-24 meses") && !it.isCustom }
        }

        val completed = milestones.count { it.isCompleted }
        val total = milestones.size.coerceAtLeast(1)
        return (completed * 100) / total
    }

    // Drive is "connected" when we have a valid access token persisted
    val isGoogleDriveConnected = googleAccessToken.map { it != null }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val healthHistoryType = MutableStateFlow<String?>(null) // "peso" or "altura"
    val exportHealthData = MutableSharedFlow<String>() // Event for exporting CSV/Excel

    fun exportHealthRecordsToCSV(type: String, records: List<HealthRecord>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val baby = babyProfile.value ?: return@launch
            val fileName = "historial_${type}_${baby.name}.csv"
            val csvContent = StringBuilder()
            csvContent.append("Fecha,Valor\n")

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            records.sortedBy { it.timestamp }.forEach { rec ->
                csvContent.append("${sdf.format(Date(rec.timestamp))},${rec.value}\n")
            }

            try {
                val resolver = getApplication<Application>().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }

                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                uri?.let { fileUri ->
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(csvContent.toString().toByteArray(Charsets.UTF_8))
                    }
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        exportHealthData.emit("Archivo guardado en la carpeta Descargas: $fileName")
                    }
                } ?: throw Exception("No se pudo crear la entrada en MediaStore")

            } catch (e: Exception) {
                Log.e("BabyViewModel", "Error exporting CSV to Downloads", e)
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    exportHealthData.emit("Error al exportar el archivo: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addHealthRecordWithDate(type: String, value: Double, dateStr: String) {
        viewModelScope.launch {
            val timestamp = parseDateString(dateStr)
            val record = HealthRecord(
                babyId = selectedBabyId.value ?: 1,
                type = type,
                timestamp = timestamp,
                value = value,
                title = if (type == "peso") "Registro de Peso" else "Registro de Altura",
                notes = ""
            )
            repository.insertHealthRecord(record)
            syncDataToCloud()
        }
    }

    // Emits a PendingIntent sender when the Drive consent screen must be shown by the Activity
    val drivePendingIntentEvent = kotlinx.coroutines.flow.MutableSharedFlow<android.content.IntentSender>(extraBufferCapacity = 1)

    fun registerGoogleAccount(email: String, name: String) {
        sharedPrefs.edit().apply {
            putString("google_account_email", email)
            putString("google_account_name", name)
            apply()
        }
        googleAccountEmail.value = email
        googleAccountName.value = name
    }

private fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

    fun signInWithGoogleNative(activityContext: android.content.Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            val result = authRepository.signInWithGoogle(activityContext)
            result.onSuccess { user ->
                val email = user.email ?: "usuario@gmail.com"
                val name = user.displayName ?: "Usuario"
                registerGoogleAccount(email, name)

                // Request Drive authorization (native, no Client Secret required)
                val activity = activityContext.findActivity()
                if (activity != null) {
                    val (accessToken, intentSender) = authRepository.authorizeGoogleDrive(activity)
                    when {
                        accessToken != null -> {
                            // Already authorized — save token and set up Drive folder
                            onDriveAccessTokenReceived(accessToken)
                        }
                        intentSender != null -> {
                            // Show consent screen — emit PendingIntent for MainActivity to handle
                            drivePendingIntentEvent.tryEmit(intentSender)
                        }
                        else -> {
                            Log.w("BabyViewModel", "Drive authorization returned nothing — trying token fetch directly")
                            fetchDriveTokenDirectly(email)
                        }
                    }
                } else {
                    fetchDriveTokenDirectly(email)
                }

                isAuthLoading.value = false
                onResult(true, null)
            }.onFailure { error ->
                isAuthLoading.value = false
                authErrorMessage.value = error.localizedMessage ?: "Error al iniciar sesión con Google"
                onResult(false, authErrorMessage.value)
            }
        }
    }

    private fun fetchDriveTokenDirectly(email: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Invalidate any cached token first to force a fresh fetch
            val oldToken = googleAccessToken.value
            if (oldToken != null) {
                authRepository.invalidateDriveToken(oldToken)
            }
            val (accessToken, intentSender) = authRepository.getDriveAccessTokenForAccount(email)
            if (accessToken != null) {
                Log.i("BabyViewModel", "fetchDriveTokenDirectly: got token (length=${accessToken.length})")
                onDriveAccessTokenReceived(accessToken)
            } else if (intentSender != null) {
                Log.i("BabyViewModel", "fetchDriveTokenDirectly: consent needed")
                drivePendingIntentEvent.tryEmit(intentSender)
            } else {
                Log.e("BabyViewModel", "fetchDriveTokenDirectly: no token and no consent intent")
                googleDriveSyncStatus.value = "❌ No se pudo obtener token de Drive"
            }
        }
    }

    /**
     * Called from MainActivity when the Drive consent screen result arrives.
     * authResult comes from Identity.getAuthorizationClient(activity).authorize() PendingIntent.
     */
    fun onDriveConsentGranted() {
        val email = googleAccountEmail.value
        Log.i("BabyViewModel", "Drive consent granted by user for email: $email")
        if (!email.isNullOrBlank()) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val (accessToken, _) = authRepository.getDriveAccessTokenForAccount(email)
                if (accessToken != null) {
                    Log.i("BabyViewModel", "Obtained valid Drive access token post-consent!")
                    onDriveAccessTokenReceived(accessToken)
                } else {
                    Log.e("BabyViewModel", "Failed to retrieve access token post-consent")
                }
            }
        }
    }

    /**
     * Saves the Drive access token and sets up the backup folder.
     * Also restores data from Drive if local DB is empty.
     * On 401, invalidates the cached token and retries with a fresh one.
     */
    fun onDriveAccessTokenReceived(accessToken: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            googleAccessToken.value = accessToken
            sharedPrefs.edit().putString("google_access_token", accessToken).apply()

            // Try to get or create folder with detailed result
            var currentToken = accessToken
            var result = driveService.getOrCreateFolderResult(currentToken)

            // If 401 (expired/invalid token), invalidate and retry with a fresh token
            if (result is DriveResult.Error && result.httpCode == 401) {
                Log.w("BabyViewModel", "Drive token expired (401), invalidating and fetching fresh token...")
                authRepository.invalidateDriveToken(currentToken)
                val email = googleAccountEmail.value
                if (!email.isNullOrBlank()) {
                    val (freshToken, intentSender) = authRepository.getDriveAccessTokenForAccount(email)
                    if (freshToken != null) {
                        currentToken = freshToken
                        googleAccessToken.value = freshToken
                        sharedPrefs.edit().putString("google_access_token", freshToken).apply()
                        Log.i("BabyViewModel", "Got fresh Drive token, retrying folder creation...")
                        result = driveService.getOrCreateFolderResult(currentToken)
                    } else if (intentSender != null) {
                        drivePendingIntentEvent.tryEmit(intentSender)
                        googleDriveSyncStatus.value = "⚠️ Necesita re-autorizar Google Drive"
                        return@launch
                    } else {
                        googleDriveSyncStatus.value = "❌ Error obteniendo token nuevo"
                        return@launch
                    }
                }
            }

            when (result) {
                is DriveResult.Success -> {
                    val folderId = result.folderId
                    googleFolderId.value = folderId
                    sharedPrefs.edit().putString("google_folder_id", folderId).apply()
                    Log.i("BabyViewModel", "Drive folder ID verified/created: $folderId")
                    googleDriveSyncStatus.value = "✅ Carpeta Drive lista"

                    val localBabies = repository.getAllBabyProfilesSync()
                    if (localBabies.isEmpty()) {
                        isRestoringFromCloud.value = true
                        restoreFromDrive(currentToken, folderId)
                        isRestoringFromCloud.value = false
                    } else {
                        syncDataToCloud()
                    }
                }
                is DriveResult.Error -> {
                    val errorMsg = "❌ Drive Error ${result.httpCode}: ${result.message.take(120)}"
                    Log.e("BabyViewModel", errorMsg)
                    googleDriveSyncStatus.value = errorMsg
                }
                is DriveResult.Exception -> {
                    val errorMsg = "❌ Excepción Drive: ${result.exception.message?.take(120)}"
                    Log.e("BabyViewModel", errorMsg, result.exception)
                    googleDriveSyncStatus.value = errorMsg
                }
            }
        }
    }


    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun syncDataToCloud() {
        val email = googleAccountEmail.value
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSyncingCloud.value = true
            googleDriveSyncStatus.value = "Sincronizando con Drive..."
            try {
                var accessToken = googleAccessToken.value
                if (accessToken == null && !email.isNullOrBlank()) {
                    val (freshToken, intentSender) = authRepository.getDriveAccessTokenForAccount(email)
                    if (freshToken != null) {
                        accessToken = freshToken
                        googleAccessToken.value = freshToken
                        sharedPrefs.edit().putString("google_access_token", freshToken).apply()
                    } else if (intentSender != null) {
                        drivePendingIntentEvent.tryEmit(intentSender)
                        googleDriveSyncStatus.value = "⚠️ Necesita autorizar Drive"
                        return@launch
                    }
                }

                if (accessToken == null) {
                    Log.i("BabyViewModel", "Google Drive no conectado: datos guardados en base local SQLite.")
                    googleDriveSyncStatus.value = "Solo guardado local (sin Drive)"
                    return@launch
                }

                // Get or create folder with retry on 401
                var folderId = googleFolderId.value
                if (folderId == null) {
                    var result = driveService.getOrCreateFolderResult(accessToken)

                    // Retry with fresh token on 401
                    if (result is DriveResult.Error && result.httpCode == 401 && !email.isNullOrBlank()) {
                        Log.w("BabyViewModel", "syncDataToCloud: 401, invalidating and retrying...")
                        authRepository.invalidateDriveToken(accessToken)
                        val (freshToken, intentSender) = authRepository.getDriveAccessTokenForAccount(email)
                        if (freshToken != null) {
                            accessToken = freshToken
                            googleAccessToken.value = freshToken
                            sharedPrefs.edit().putString("google_access_token", freshToken).apply()
                            result = driveService.getOrCreateFolderResult(freshToken)
                        } else if (intentSender != null) {
                            drivePendingIntentEvent.tryEmit(intentSender)
                            googleDriveSyncStatus.value = "⚠️ Necesita re-autorizar Drive"
                            return@launch
                        }
                    }

                    when (result) {
                        is DriveResult.Success -> {
                            folderId = result.folderId
                            googleFolderId.value = folderId
                            sharedPrefs.edit().putString("google_folder_id", folderId).apply()
                        }
                        is DriveResult.Error -> {
                            googleDriveSyncStatus.value = "❌ Drive Error ${result.httpCode}: ${result.message.take(120)}"
                            return@launch
                        }
                        is DriveResult.Exception -> {
                            googleDriveSyncStatus.value = "❌ Excepción: ${result.exception.message?.take(120)}"
                            return@launch
                        }
                    }
                }

                if (folderId == null) {
                    googleDriveSyncStatus.value = "❌ No se pudo obtener carpeta Drive"
                    return@launch
                }

                // Serialize all local data into structured JSON
                val allBabies = repository.getAllBabyProfilesSync()
                val backupItems = allBabies.map { baby ->
                    BabyBackupItem(
                        profile = baby,
                        activities = repository.getAllActivitiesSync(baby.id),
                        healthRecords = repository.getAllHealthRecordsSync(baby.id),
                        moments = repository.getAllMomentsSync(baby.id)
                    )
                }

                val backupData = AppBackupData(
                    version = 1,
                    exportedAt = System.currentTimeMillis(),
                    babies = backupItems
                )

                val jsonAdapter = moshi.adapter(AppBackupData::class.java)
                val jsonString = jsonAdapter.toJson(backupData)
                val fileBytes = jsonString.toByteArray(Charsets.UTF_8)

                val success = driveService.uploadOrUpdateFileInFolder(
                    accessToken = accessToken,
                    folderId = folderId,
                    fileName = "datos_bebe_backup.json",
                    fileBytes = fileBytes,
                    mimeType = "application/json"
                )

                if (success) {
                    Log.i("BabyViewModel", "Sincronización completa con Google Drive (datos_bebe_backup.json)")
                    googleDriveSyncStatus.value = "✅ Seguro en Google Drive"
                } else {
                    Log.e("BabyViewModel", "Error al sincronizar datos_bebe_backup.json en Google Drive")
                    googleDriveSyncStatus.value = "❌ Error subiendo datos a Drive"
                }
            } catch (e: Exception) {
                Log.e("BabyViewModel", "Error crítico sincronizando datos con Google Drive", e)
                googleDriveSyncStatus.value = "❌ Error: ${e.message?.take(100)}"
            } finally {
                isSyncingCloud.value = false
            }
        }
    }

    suspend fun restoreFromDrive(accessToken: String, folderId: String): Boolean {
        return try {
            val fileId = driveService.findFileInFolder(accessToken, folderId, "datos_bebe_backup.json")
            if (fileId == null) {
                Log.i("BabyViewModel", "No existe copia previa en Google Drive")
                return false
            }

            val jsonContent = driveService.downloadFileContent(accessToken, fileId)
            if (jsonContent.isNullOrBlank()) return false

            val jsonAdapter = moshi.adapter(AppBackupData::class.java)
            val backup = jsonAdapter.fromJson(jsonContent) ?: return false

            if (backup.babies.isNotEmpty()) {
                repository.clearDatabase()
                for (item in backup.babies) {
                    val savedId = repository.saveBabyProfile(item.profile).toInt()
                    val targetId = if (savedId != 0) savedId else item.profile.id
                    for (act in item.activities) {
                        repository.insertActivity(act.copy(id = 0, babyId = targetId))
                    }
                    for (health in item.healthRecords) {
                        repository.insertHealthRecord(health.copy(id = 0, babyId = targetId))
                    }
                    for (moment in item.moments) {
                        repository.insertMoment(moment.copy(id = 0, babyId = targetId))
                    }
                }
                val restored = repository.getAllBabyProfilesSync()
                if (restored.isNotEmpty()) {
                    selectedBabyId.value = restored.first().id
                }
                Log.i("BabyViewModel", "Restauración exitosa desde Google Drive!")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("BabyViewModel", "Error al restaurar desde Google Drive", e)
            false
        }
    }

    fun disconnectGoogleAccount() {
        viewModelScope.launch {
            authRepository.signOut()
            sharedPrefs.edit().apply {
                remove("google_account_email")
                remove("google_account_name")
                remove("google_access_token")
                remove("google_folder_id")
                apply()
            }
            googleAccountEmail.value = null
            googleAccountName.value = null
            googleAccessToken.value = null
            googleFolderId.value = null
        }
    }


    // Current screen bottom tab
    val currentTab = MutableStateFlow("Home")
    // Target age range to expand in MomentsScreen (e.g. "0-6 meses", "6-12 meses")
    val selectedMomentAgeRange = MutableStateFlow<String?>(null)

    // All registered baby profiles
    val allBabyProfiles: StateFlow<List<BabyProfile>> = repository.allBabyProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active baby profile ID
    val selectedBabyId = MutableStateFlow<Int?>(null)

    // Automatically select the first baby if none selected and profiles list is not empty
    init {
        viewModelScope.launch {
            allBabyProfiles.collect { profiles ->
                if (selectedBabyId.value == null && profiles.isNotEmpty()) {
                    selectedBabyId.value = profiles.first().id
                }
            }
        }

        // Al iniciar la app, si hay cuenta de Google Drive conectada, obtener token fresco y restaurar/sincronizar
        val savedEmail = googleAccountEmail.value
        if (savedEmail != null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Siempre invalidar token viejo y obtener uno fresco
                    val oldToken = googleAccessToken.value
                    if (oldToken != null) {
                        authRepository.invalidateDriveToken(oldToken)
                    }

                    val (freshToken, intentSender) = authRepository.getDriveAccessTokenForAccount(savedEmail)
                    if (freshToken != null) {
                        Log.i("BabyViewModel", "Init: Got fresh Drive token for $savedEmail")
                        googleAccessToken.value = freshToken
                        sharedPrefs.edit().putString("google_access_token", freshToken).apply()

                        val localBabies = repository.getAllBabyProfilesSync()
                        if (localBabies.isEmpty()) {
                            isRestoringFromCloud.value = true
                            try {
                                var folderId = googleFolderId.value
                                if (folderId == null) {
                                    folderId = driveService.getOrCreateFolder(freshToken)
                                    if (folderId != null) {
                                        googleFolderId.value = folderId
                                        sharedPrefs.edit().putString("google_folder_id", folderId).apply()
                                    }
                                }
                                if (folderId != null) {
                                    restoreFromDrive(freshToken, folderId)
                                }
                            } finally {
                                isRestoringFromCloud.value = false
                            }
                        }
                    } else if (intentSender != null) {
                        Log.i("BabyViewModel", "Init: Drive consent needed for $savedEmail")
                        drivePendingIntentEvent.tryEmit(intentSender)
                    } else {
                        Log.w("BabyViewModel", "Init: Could not get Drive token for $savedEmail")
                    }
                } catch (e: Exception) {
                    Log.e("BabyViewModel", "Init: Auto restore from Drive failed", e)
                }
            }
        }
    }


    // UI state flows reactive to selectedBabyId
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val babyProfile: StateFlow<BabyProfile?> = selectedBabyId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getBabyProfileFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activities: StateFlow<List<ActivityRecord>> = selectedBabyId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllActivitiesFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val healthRecords: StateFlow<List<HealthRecord>> = selectedBabyId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllHealthRecordsFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val moments: StateFlow<List<MomentRecord>> = selectedBabyId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllMomentsFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val completedMoments: StateFlow<List<MomentRecord>> = selectedBabyId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getCompletedSpecialMomentsFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Sleep Timer State
    val sleepTimerStart = MutableStateFlow<Long?>(null)
    val isSleepTimerActive = sleepTimerStart.map { it != null }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Google Drive Sync status
    val googleDriveSyncStatus = MutableStateFlow("Sincronizado con Google Drive")
    val totalSyncedPhotosCount = MutableStateFlow(0) // default matching completed moments
    val showAddMomentDialog = MutableStateFlow(false)

    // Select active baby profile
    fun selectBaby(id: Int) {
        selectedBabyId.value = id
    }

    // Reset database
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearDatabase()
            selectedBabyId.value = null
            currentTab.value = "Home"
            disconnectGoogleAccount()
        }
    }

    // Cerrar sesión y limpiar base local para cambio de usuario (sin borrar datos de la nube)
    fun signOutUser() {
        viewModelScope.launch {
            repository.clearDatabase()
            selectedBabyId.value = null
            currentTab.value = "Home"
            disconnectGoogleAccount()
        }
    }

    // Save profile from Onboarding/Edit (with optional custom ID for updates vs insertions)
    fun saveProfile(
        name: String,
        dobTimestamp: Long,
        gender: String,
        id: Int = 0,
        skinTone: String = "Claro",
        hairColor: String = "Castaño"
    ) {
        viewModelScope.launch {
            val profile = BabyProfile(
                id = id,
                name = name,
                dob = dobTimestamp,
                gender = gender,
                skinTone = skinTone,
                hairColor = hairColor
            )
            val savedId = repository.saveBabyProfile(profile)
            // Ensure default moments are pre-populated for this baby
            repository.prepopulateDefaultMomentsIfEmpty(savedId.toInt())
            // Switch selection to this baby profile
            selectedBabyId.value = savedId.toInt()
            // Sincronizar en la nube (Firestore) para persistencia entre reinstalaciones
            syncDataToCloud()
        }
    }

    // Update baby appearance properties specifically
    fun updateBabyAppearance(skinTone: String, hairColor: String) {
        val currentId = selectedBabyId.value ?: return
        viewModelScope.launch {
            val existing = repository.getBabyProfileByIdSync(currentId) ?: return@launch
            val updated = existing.copy(skinTone = skinTone, hairColor = hairColor)
            repository.saveBabyProfile(updated)
        }
    }

    // Delete a baby profile and all associated data
    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteBabyProfile(id)
            val remaining = repository.getAllBabyProfilesSync()
            if (remaining.isNotEmpty()) {
                selectedBabyId.value = remaining.first().id
            } else {
                selectedBabyId.value = null
            }
            
            // Sincronizar actualización con Google Drive
            syncDataToCloud()
        }
    }

    // Add activity records
    fun addMealRecord(mealType: String, foodName: String, notes: String) {
        viewModelScope.launch {
            val record = ActivityRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "comida",
                timestamp = System.currentTimeMillis(),
                startTime = getCurrentTimeString(),
                extraType = mealType,
                notes = if (foodName.isNotBlank()) "Alimento: $foodName. $notes" else notes
            )
            repository.insertActivity(record)
            syncDataToCloud()
        }
    }

    fun startSleepTimer() {
        sleepTimerStart.value = System.currentTimeMillis()
    }

    fun stopAndSaveSleepTimer(notes: String) {
        val start = sleepTimerStart.value ?: return
        val end = System.currentTimeMillis()
        val durationMins = ((end - start) / 60000).toInt().coerceAtLeast(1)
        
        viewModelScope.launch {
            val record = ActivityRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "sueno",
                timestamp = start,
                startTime = formatTime(start),
                endTime = formatTime(end),
                durationMinutes = durationMins,
                notes = notes
            )
            repository.insertActivity(record)
            sleepTimerStart.value = null
            syncDataToCloud()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerStart.value = null
    }

    fun addManualSleepRecord(dateString: String, startTime: String, endTime: String, notes: String) {
        viewModelScope.launch {
            val parsedDate = parseDateTimeString(dateString, startTime)
            val durationMins = calculateMinutesDiff(startTime, endTime)
            val record = ActivityRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "sueno",
                timestamp = parsedDate,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMins,
                notes = notes
            )
            repository.insertActivity(record)
            syncDataToCloud()
        }
    }

    fun addDiaperRecord(condition: String, notes: String, dateString: String? = null, quantity: Int = 1) {
        viewModelScope.launch {
            val timestamp = if (!dateString.isNullOrBlank()) {
                parseDateTimeString(dateString, getCurrentTimeString())
            } else {
                System.currentTimeMillis()
            }

            repeat(quantity) {
                val record = ActivityRecord(
                    babyId = selectedBabyId.value ?: 1,
                    type = "panal",
                    timestamp = timestamp,
                    startTime = getCurrentTimeString(),
                    extraType = condition,
                    notes = notes
                )
                repository.insertActivity(record)
            }
            syncDataToCloud()
        }
    }

    fun deleteActivityRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteActivity(id)
            syncDataToCloud()
        }
    }

    // Health additions
    fun addWeightRecord(weightKg: Double) {
        viewModelScope.launch {
            val record = HealthRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "peso",
                timestamp = System.currentTimeMillis(),
                value = weightKg,
                title = "Registro de Peso",
                notes = ""
            )
            repository.insertHealthRecord(record)
            syncDataToCloud()
        }
    }

    fun addHeightRecord(heightCm: Double) {
        viewModelScope.launch {
            val record = HealthRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "altura",
                timestamp = System.currentTimeMillis(),
                value = heightCm,
                title = "Registro de Altura",
                notes = ""
            )
            repository.insertHealthRecord(record)
            syncDataToCloud()
        }
    }

    fun updateHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            repository.updateHealthRecord(record)
            syncDataToCloud()
        }
    }

    fun addVaccineRecord(name: String, notes: String, dateString: String? = null, status: String = "Vacunado completamente") {
        viewModelScope.launch {
            val recordTimestamp = if (!dateString.isNullOrBlank()) {
                parseDateTimeString(dateString, "12:00")
            } else {
                System.currentTimeMillis()
            }
            val record = HealthRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "vacuna",
                timestamp = recordTimestamp,
                value = 0.0,
                title = name,
                notes = notes,
                status = status,
                dateString = dateString
            )
            repository.insertHealthRecord(record)
            syncDataToCloud()
        }
    }

    fun updateVaccineStatus(record: HealthRecord, newStatus: String) {
        viewModelScope.launch {
            val updated = record.copy(status = newStatus)
            repository.updateHealthRecord(updated)
            syncDataToCloud()
        }
    }

    fun deleteHealthRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteHealthRecord(id)
            syncDataToCloud()
        }
    }

    fun addAppointmentRecord(pediatra: String, dateString: String, timeString: String, hasReminder: Boolean) {
        viewModelScope.launch {
            val combinedDatetime = parseDateTimeString(dateString, timeString)
            val record = HealthRecord(
                babyId = selectedBabyId.value ?: 1,
                type = "cita",
                timestamp = combinedDatetime,
                value = 0.0,
                title = "Pediatra: $pediatra",
                notes = "Cita programada con recordatorio automático",
                hasReminder = hasReminder
            )
            repository.insertHealthRecord(record)
            syncDataToCloud()
        }
    }

    // Moments management
    fun toggleMomentStatus(moment: MomentRecord) {
        viewModelScope.launch {
            val updated = moment.copy(
                isCompleted = !moment.isCompleted
            )
            repository.updateMoment(updated)
            syncDataToCloud()
        }
    }

    fun uncompleteMoment(moment: MomentRecord) {
        viewModelScope.launch {
            val updated = moment.copy(
                isCompleted = false
            )
            repository.updateMoment(updated)
            syncDataToCloud()
        }
    }

    fun completeMoment(moment: MomentRecord, dateHappened: String, location: String, details: String, photoPath: String? = null, newName: String? = null) {
        viewModelScope.launch {
            val finalName = if (moment.isCustom && !newName.isNullOrBlank()) newName else moment.name
            val updated = moment.copy(
                name = finalName,
                isCompleted = true,
                dateHappened = dateHappened,
                location = location,
                details = details,
                photoPath = photoPath
            )

            // Check the latest photo path from DB to avoid duplicates if the UI state is stale
            val currentMoment = repository.getMomentByIdSync(moment.id)
            val currentPhotoPath = currentMoment?.photoPath
            val currentDriveId = currentMoment?.driveFileId

            // Delete from Drive if photo is removed or replaced
            if (currentDriveId != null && (photoPath == null || photoPath != currentPhotoPath)) {
                val token = googleAccessToken.value
                if (token != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        driveService.deleteFile(token, currentDriveId)
                    }
                }
            }

            repository.updateMoment(updated)
            syncDataToCloud()

            // Only upload if a photo is provided AND it's actually different from what's currently in the DB
            if (photoPath != null && photoPath != currentPhotoPath) {
                val isGirl = babyProfile.value?.gender == "Niña"
                val fileId = uploadMomentPhotoToDrive(finalName, photoPath, isGirl, dateHappened, location)
                if (fileId != null) {
                    val momentWithDriveId = updated.copy(driveFileId = fileId)
                    repository.updateMoment(momentWithDriveId)
                }
            }
        }
    }

    fun addCustomMoment(name: String, ageRange: String, dateHappened: String, location: String, details: String, photoPath: String? = null) {
        viewModelScope.launch {
            val custom = MomentRecord(
                babyId = selectedBabyId.value ?: 1,
                name = name,
                ageRange = ageRange,
                dateHappened = dateHappened,
                location = location,
                details = details,
                isCustom = true,
                photoPath = photoPath,
                isCompleted = true
            )
            val momentId = repository.insertMoment(custom).toInt()
            syncDataToCloud()
            if (photoPath != null) {
                val isGirl = babyProfile.value?.gender == "Niña"
                val fileId = uploadMomentPhotoToDrive(name, photoPath, isGirl, dateHappened, location)
                if (fileId != null) {
                    val updated = custom.copy(id = momentId, driveFileId = fileId)
                    repository.updateMoment(updated)
                }
            }
        }
    }

    fun updateMoment(moment: MomentRecord, newName: String, newDate: String, newLoc: String, newDet: String, newPhotoPath: String? = null) {
        viewModelScope.launch {
            val updated = moment.copy(
                name = if (moment.isCustom) newName else moment.name,
                dateHappened = newDate,
                location = newLoc,
                details = newDet,
                photoPath = newPhotoPath,
                driveFileId = if (newPhotoPath == null) null else moment.driveFileId,
                isCompleted = true
            )

            // Check the latest photo path from DB to avoid duplicates if the UI state is stale
            val currentMoment = repository.getMomentByIdSync(moment.id)
            val currentPhotoPath = currentMoment?.photoPath
            val currentDriveId = currentMoment?.driveFileId

            // Handle Drive file deletion if photo is being removed or replaced
            if (currentDriveId != null && (newPhotoPath == null || newPhotoPath != currentPhotoPath)) {
                val token = googleAccessToken.value
                if (token != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        driveService.deleteFile(token, currentDriveId)
                    }
                }
            }

            repository.updateMoment(updated)
            syncDataToCloud()

            // Only upload if a new photo is provided AND it's actually different from what's currently in the DB
            if (newPhotoPath != null && newPhotoPath != currentPhotoPath) {
                val isGirl = babyProfile.value?.gender == "Niña"
                val fileId = uploadMomentPhotoToDrive(updated.name, newPhotoPath, isGirl, newDate, newLoc)
                if (fileId != null) {
                    val momentWithDriveId = updated.copy(driveFileId = fileId)
                    repository.updateMoment(momentWithDriveId)
                }
            }
        }
    }

    fun deleteMoment(moment: MomentRecord) {
        viewModelScope.launch {
            repository.deleteMomentById(moment.id)
            syncDataToCloud()
        }
    }


    fun deleteMomentPhotoFromDrive(momentId: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val moment = repository.getMomentByIdSync(momentId) ?: return@launch
            val fileId = moment.driveFileId

            // 1. Delete from Drive if fileId exists
            if (fileId != null) {
                val token = googleAccessToken.value
                if (token != null) {
                    driveService.deleteFile(token, fileId)
                }
            }

            // 2. Update DB to clear photo path and drive file ID
            val updated = moment.copy(photoPath = null, driveFileId = null)
            repository.updateMoment(updated)

            // Sync general backup state
            syncDataToCloud()
        }
    }

    // Dynamic Image Generator that creates a beautifully branded keepsake card featuring key memory details

    private fun generateMomentImageBytes(
        momentName: String,
        photoPath: String,
        isGirl: Boolean,
        date: String,
        location: String
    ): ByteArray? {
        val width = 800
        val height = 800
        
        try {
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            
            // Background soft gradients
            val startColor = if (isGirl) 0xFFFFF1F2.toInt() else 0xFFECFDF5.toInt() // rose-50 or emerald-50
            val endColor = if (isGirl) 0xFFFFE4E6.toInt() else 0xFFD1FAE5.toInt()   // rose-100 or emerald-100
            
            val gradientPaint = android.graphics.Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    startColor, endColor,
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
            
            // White central keepsake card
            val cardLeft = 60f
            val cardTop = 60f
            val cardRight = width.toFloat() - 60f
            val cardBottom = height.toFloat() - 60f
            
            val cardPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            
            // Card soft shadow
            val shadowPaint = android.graphics.Paint().apply {
                color = 0x1A000000
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            canvas.drawRoundRect(cardLeft + 2f, cardTop + 6f, cardRight + 2f, cardBottom + 6f, 36f, 36f, shadowPaint)
            canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 36f, 36f, cardPaint)
            
            // Elegant colored border
            val borderPaint = android.graphics.Paint().apply {
                color = if (isGirl) 0xFFFDA4AF.toInt() else 0xFF6EE7B7.toInt() // rose-200 or emerald-300
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 5f
                isAntiAlias = true
            }
            canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 36f, 36f, borderPaint)
            
            // Stamp Header: "Primeros Momentos ✨"
            val headerPaint = android.graphics.Paint().apply {
                color = if (isGirl) 0xFFE11D48.toInt() else 0xFF059669.toInt() // rose-600 or emerald-600
                isAntiAlias = true
                textSize = 34f
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("Primeros Momentos ✨", (width / 2).toFloat(), cardTop + 85f, headerPaint)
            
            val emojiPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 140f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val isRealPhoto = photoPath.startsWith("/") || photoPath.startsWith("file://")
            if (isRealPhoto) {
                val realPhotoPath = if (photoPath.startsWith("file://")) photoPath.substring(7) else photoPath
                try {
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val rawBitmap = android.graphics.BitmapFactory.decodeFile(realPhotoPath, options)
                    if (rawBitmap != null) {
                        val rectLeft = (width / 2 - 140).toFloat()
                        val rectTop = (cardTop + 120)
                        val rectRight = (width / 2 + 140).toFloat()
                        val rectBottom = (cardTop + 330)
                        
                        val targetRect = android.graphics.RectF(rectLeft, rectTop, rectRight, rectBottom)
                        
                        val srcWidth = rawBitmap.width
                        val srcHeight = rawBitmap.height
                        val targetWidth = (rectRight - rectLeft).toInt()
                        val targetHeight = (rectBottom - rectTop).toInt()
                        
                        val srcAspectRatio = srcWidth.toFloat() / srcHeight
                        val targetAspectRatio = targetWidth.toFloat() / targetHeight
                        
                        val srcRect = if (srcAspectRatio > targetAspectRatio) {
                            val cropWidth = (srcHeight * targetAspectRatio).toInt()
                            val leftOffset = (srcWidth - cropWidth) / 2
                            android.graphics.Rect(leftOffset, 0, leftOffset + cropWidth, srcHeight)
                        } else {
                            val cropHeight = (srcWidth / targetAspectRatio).toInt()
                            val topOffset = (srcHeight - cropHeight) / 2
                            android.graphics.Rect(0, topOffset, srcWidth, topOffset + cropHeight)
                        }
                        
                        canvas.drawBitmap(rawBitmap, srcRect, targetRect, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG))
                        rawBitmap.recycle()
                    } else {
                        canvas.drawText("📸", (width / 2).toFloat(), cardTop + 270f, emojiPaint)
                    }
                } catch (e: Exception) {
                    Log.e("BabyViewModel", "Failed to paint real baby photo on keepsake card", e)
                    canvas.drawText("📸", (width / 2).toFloat(), cardTop + 270f, emojiPaint)
                }
            } else {
                // Emoji Selector based on photoPath string
                val emoji = when (photoPath) {
                    "gallery_first_smiles.jpg" -> "👶"
                    "gallery_eating_time.jpg" -> "🍼"
                    "gallery_teddy_play.jpg" -> "🧸"
                    "gallery_warm_bath.jpg" -> "🛁"
                    "gallery_sweet_dreams.jpg" -> "😴"
                    "gallery_first_outing.jpg" -> "🌳"
                    "video_captured_drive.mp4" -> "📹"
                    else -> "📸"
                }
                canvas.drawText(emoji, (width / 2).toFloat(), cardTop + 270f, emojiPaint)
            }
            
            // Moment Title
            val titlePaint = android.graphics.Paint().apply {
                color = 0xFF1E293B.toInt() // slate-800
                isAntiAlias = true
                textSize = 40f
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            var nameToShow = momentName
            if (nameToShow.length > 28) {
                nameToShow = nameToShow.substring(0, 25) + "..."
            }
            canvas.drawText(nameToShow, (width / 2).toFloat(), cardTop + 395f, titlePaint)
            
            // Sleek layout separator
            val separatorPaint = android.graphics.Paint().apply {
                color = 0xFFE2E8F0.toInt() // slate-200
                strokeWidth = 3f
            }
            canvas.drawLine((width / 2 - 160).toFloat(), cardTop + 440f, (width / 2 + 160).toFloat(), cardTop + 440f, separatorPaint)
            
            // Sub-details (Date & Location)
            val detailsPaint = android.graphics.Paint().apply {
                color = 0xFF475569.toInt() // slate-600
                isAntiAlias = true
                textSize = 28f
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            val displayDate = if (date.isNotBlank()) "📅 $date" else "📅 Recuerdo Guardado"
            val displayLocation = if (location.isNotBlank()) "📍 $location" else "📍 Hogar"
            
            canvas.drawText(displayDate, (width / 2).toFloat(), cardTop + 500f, detailsPaint)
            canvas.drawText(displayLocation, (width / 2).toFloat(), cardTop + 565f, detailsPaint)
            
            // Footer Branding
            val babyName = babyProfile.value?.name ?: "Mi Bebé"
            val footerPaint = android.graphics.Paint().apply {
                color = if (isGirl) 0xFFFDA4AF.toInt() else 0xFF34D399.toInt()
                isAntiAlias = true
                textSize = 24f
                typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("ÁLBUM DE ${babyName.uppercase()}", (width / 2).toFloat(), cardBottom - 65f, footerPaint)
            
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            return outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("BabyViewModel", "Failed to generate dynamic canvas photo", e)
            return null
        }
    }

    suspend fun uploadMomentPhotoToDrive(
        momentName: String,
        photoPath: String,
        isGirl: Boolean,
        dateHappened: String = "",
        location: String = ""
    ): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val email = googleAccountEmail.value
            val babyName = babyProfile.value?.name ?: "Bebé"
            try {
            var accessToken = googleAccessToken.value
            if (accessToken == null && !email.isNullOrBlank()) {
                val (freshToken, intentSender) = authRepository.getDriveAccessTokenForAccount(email)
                if (freshToken != null) {
                    accessToken = freshToken
                    googleAccessToken.value = freshToken
                    sharedPrefs.edit().putString("google_access_token", freshToken).apply()
                } else if (intentSender != null) {
                    drivePendingIntentEvent.tryEmit(intentSender)
                }
            }

            if (accessToken == null) {
                Log.w("BabyViewModel", "Google Drive not authenticated, showing simulated backup instead.")
                simulateGoogleDriveSync()
                return@withContext null
            }

            googleDriveSyncStatus.value = "Sincronizando con Google Drive..."

            // 2. Ensure Root Folder and then a "Fotos" subfolder
            var rootFolderId = googleFolderId.value
            var retryRoot = true

            while (retryRoot) {
                if (rootFolderId == null) {
                    val result = driveService.getOrCreateFolderResult(accessToken)
                    when (result) {
                        is DriveResult.Success -> {
                            rootFolderId = result.folderId
                            googleFolderId.value = rootFolderId
                            sharedPrefs.edit().putString("google_folder_id", rootFolderId).apply()
                        }
                        is DriveResult.Error -> {
                            if (result.httpCode == 404) {
                                googleFolderId.value = null
                                sharedPrefs.edit().remove("google_folder_id").apply()
                            } else {
                                googleDriveSyncStatus.value = "Error Raíz: ${result.httpCode} ${result.message}"
                                return@withContext null
                            }
                        }
                        is DriveResult.Exception -> {
                            googleDriveSyncStatus.value = "Excepción Raíz: ${result.exception.message}"
                            return@withContext null
                        }
                    }
                } else {
                    retryRoot = false
                }

                if (rootFolderId != null) {
                    val verify = driveService.getOrCreateFolderResult(accessToken, "Primeros momentos", null)
                    if (verify is DriveResult.Error && verify.httpCode == 404) {
                        rootFolderId = null
                        googleFolderId.value = null
                        sharedPrefs.edit().remove("google_folder_id").apply()
                    } else {
                        retryRoot = false
                    }
                }
            }


            if (rootFolderId == null) {
                googleDriveSyncStatus.value = "Error: Sin Carpeta Raíz"
                return@withContext null
            }

            // 2. Ensure Baby's specific folder inside "Primeros momentos"
            val babyFolderResult = driveService.getOrCreateFolderResult(accessToken, babyName, rootFolderId)
            val babyFolderId = when (babyFolderResult) {
                is DriveResult.Success -> babyFolderResult.folderId
                is DriveResult.Error -> {
                    googleDriveSyncStatus.value = "Error Carpeta Bebé: ${babyFolderResult.httpCode}"
                    return@withContext null
                }
                is DriveResult.Exception -> {
                    googleDriveSyncStatus.value = "Excepción Carpeta Bebé: ${babyFolderResult.exception.message}"
                    return@withContext null
                }
            }

            // 3. Determine if it's a photo or video and get the appropriate subfolder ("Fotos" or "Videos")
            val isVideo = photoPath.endsWith(".mp4", ignoreCase = true) || photoPath.contains("video", ignoreCase = true)
            val subFolderName = if (isVideo) "Videos" else "Fotos"

            val mediaFolderResult = driveService.getOrCreateFolderResult(accessToken, subFolderName, babyFolderId)
            val targetFolderId = when (mediaFolderResult) {
                is DriveResult.Success -> mediaFolderResult.folderId
                is DriveResult.Error -> {
                    googleDriveSyncStatus.value = "Error Carpeta $subFolderName: ${mediaFolderResult.httpCode}"
                    return@withContext null
                }
                is DriveResult.Exception -> {
                    googleDriveSyncStatus.value = "Excepción Carpeta $subFolderName: ${mediaFolderResult.exception.message}"
                    return@withContext null
                }
            }

            // 3. Read actual photo or video bytes from photoPath (Content URI, local file, or generated memory photo card)
            var mimeType = if (isVideo) "video/mp4" else "image/jpeg"
            var extension = if (isVideo) ".mp4" else ".jpg"

            val fileBytes: ByteArray? = try {
                val file = java.io.File(photoPath)
                if (photoPath.startsWith("content://")) {
                    val uri = android.net.Uri.parse(photoPath)
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } else if (file.exists()) {
                    file.readBytes()
                } else {
                    extension = ".png"
                    mimeType = "image/png"
                    generateMomentImageBytes(momentName, photoPath, isGirl, dateHappened, location)
                }
            } catch (e: Exception) {
                Log.e("BabyViewModel", "Failed to read media bytes for $photoPath", e)
                extension = ".png"
                mimeType = "image/png"
                generateMomentImageBytes(momentName, photoPath, isGirl, dateHappened, location)
            }

            if (fileBytes == null) {
                googleDriveSyncStatus.value = "Error al procesar archivo"
                return@withContext null
            }

            // 4. Upload raw media file to Google Drive folder
            val cleanMomentName = momentName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "${cleanMomentName}_${System.currentTimeMillis()}$extension"

            val uploadResult = driveService.uploadFileToFolder(
                accessToken = accessToken,
                folderId = targetFolderId,
                fileName = fileName,
                fileBytes = fileBytes,
                mimeType = mimeType
            )

            when (uploadResult) {
                is UploadResult.Success -> {
                    totalSyncedPhotosCount.value += 1
                    googleDriveSyncStatus.value = "Seguro en Google Drive"
                    uploadResult.fileId
                }
                is UploadResult.Error -> {
                    googleDriveSyncStatus.value = "Error subir foto: ${uploadResult.httpCode} ${uploadResult.message}"
                    null
                }
                is UploadResult.Exception -> {
                    googleDriveSyncStatus.value = "Excepción subir foto: ${uploadResult.exception.message}"
                    null
                }
            }
            } catch (e: Exception) {
                Log.e("BabyViewModel", "Critical error in uploadMomentPhotoToDrive", e)
                googleDriveSyncStatus.value = "Error crítico: ${e.message}"
                null
            }
        }
    }

    private fun simulateGoogleDriveSync() {
        googleDriveSyncStatus.value = "Sincronizando (Simulado - Sin Drive)..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            totalSyncedPhotosCount.value += 1
            googleDriveSyncStatus.value = "Guardado localmente (No subido a Drive)"
        }
    }

    // Age Calculation Helpers
    fun getAgeDisplayString(dob: Long): String {
        val birthCalendar = Calendar.getInstance().apply { timeInMillis = dob }
        val today = Calendar.getInstance()

        var years = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val prevMonth = (today.get(Calendar.MONTH) - 1 + 12) % 12
            today.set(Calendar.MONTH, prevMonth)
            days += today.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }

        val totalMonths = years * 12 + months
        return when {
            totalMonths == 0 -> {
                if (days == 1) "1 día" else "$days días"
            }
            days == 0 -> {
                if (totalMonths == 1) "1 Mes" else "$totalMonths Meses"
            }
            else -> {
                val monthsStr = if (totalMonths == 1) "1 Mes" else "$totalMonths Meses"
                val daysStr = if (days == 1) "1 día" else "$days días"
                "$monthsStr y $daysStr"
            }
        }
    }

    fun getAgeInMonths(dob: Long): Int {
        val diff = System.currentTimeMillis() - dob
        val diffDays = diff / (1000 * 60 * 60 * 24)
        return (diffDays / 30).toInt().coerceAtLeast(0)
    }
}
