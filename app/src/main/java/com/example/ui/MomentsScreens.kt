package com.example.ui

import android.widget.Toast
import android.net.Uri
import android.app.DatePickerDialog
import android.widget.VideoView
import android.widget.MediaController
import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.BabyProfile
import com.example.ui.theme.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import com.example.data.MomentRecord
import com.example.ui.BabyViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MomentsScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    moments: List<MomentRecord>
) {
    var showAddMomentDialog by remember { mutableStateOf(false) }
    var showEditMomentDetail by remember { mutableStateOf<MomentRecord?>(null) }

    // Sync with ViewModel trigger from Bento Grid
    LaunchedEffect(Unit) {
        viewModel.showAddMomentDialog.collect { shouldShow ->
            if (shouldShow) {
                showAddMomentDialog = true
                viewModel.showAddMomentDialog.value = false
            }
        }
    }

    var autoOpenMediaSelector by remember { mutableStateOf(false) }
    var showCustomMediaOptionsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Dialog state controllers
    var customName by remember { mutableStateOf("") }
    var customAgeRange by remember { mutableStateOf("0-6 meses") }
    var customDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var customLocation by remember { mutableStateOf("") }
    var customDetails by remember { mutableStateOf("") }
    var customPhotoPath by remember { mutableStateOf<String?>(null) }

    val customDatePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                customDate = sdf.format(chosenCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }


    // Pulled-up state controllers for editing a moment
    var nameEdit by remember { mutableStateOf("") }
    var dateEdit by remember { mutableStateOf("") }
    var locEdit by remember { mutableStateOf("") }
    var detEdit by remember { mutableStateOf("") }
    var localPhotoPath by remember { mutableStateOf<String?>(null) }
    var videoThumbnailBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Pulled-up state for media choice dialog
    var showMediaOptionsDialog by remember { mutableStateOf(false) }
    var showDeletePhotoConfirm by remember { mutableStateOf(false) }
    var showFullscreenPhoto by remember { mutableStateOf(false) }

    // DatePicker for editing moment date
    val momentCalendar = Calendar.getInstance()
    val momentDatePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, monthOfYear, dayOfMonth ->
            val chosenCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthOfYear)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateEdit = sdf.format(chosenCal.time)
        },
        momentCalendar.get(Calendar.YEAR),
        momentCalendar.get(Calendar.MONTH),
        momentCalendar.get(Calendar.DAY_OF_MONTH)
    )

    // Sync video thumbnail whenever localPhotoPath changes
    LaunchedEffect(localPhotoPath) {
        val path = localPhotoPath
        if (path != null && (path.endsWith(".mp4", ignoreCase = true) || path.contains("video", ignoreCase = true))) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val retriever = MediaMetadataRetriever()
                    if (path.startsWith("content://")) {
                        retriever.setDataSource(context, Uri.parse(path))
                    } else {
                        retriever.setDataSource(path)
                    }
                    val frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    retriever.release()
                    videoThumbnailBitmap = frame
                }
            } catch (e: Exception) {
                videoThumbnailBitmap = null
            }
        } else {
            videoThumbnailBitmap = null
        }
    }

    // Sync state when details dialog is shown or auto-opened
    LaunchedEffect(showEditMomentDetail) {
        val selected = showEditMomentDetail
        if (selected != null) {
            nameEdit = selected.name
            dateEdit = selected.dateHappened ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            locEdit = selected.location ?: ""
            detEdit = selected.details ?: ""
            localPhotoPath = selected.photoPath
            if (autoOpenMediaSelector) {
                showMediaOptionsDialog = true
                autoOpenMediaSelector = false
            }
        }
    }

    // Camera photo taker launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val file = tempPhotoFile
            if (file != null && file.exists()) {
                val absolutePath = file.absolutePath
                if (showEditMomentDetail != null) {
                    localPhotoPath = absolutePath
                    Toast.makeText(context, "¡Foto de bebé capturada! 👶📸", Toast.LENGTH_SHORT).show()
                    viewModel.completeMoment(
                        moment = showEditMomentDetail!!,
                        dateHappened = dateEdit,
                        location = locEdit,
                        details = detEdit,
                        photoPath = absolutePath,
                        newName = nameEdit
                    )
                } else if (showAddMomentDialog) {
                    customPhotoPath = absolutePath
                    Toast.makeText(context, "¡Foto de bebé capturada! 👶📸", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Cámara cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    val momentsDir = remember {
        java.io.File(context.filesDir, "moments").apply { if (!exists()) mkdirs() }
    }

    // Permission launcher for accessing the camera (photo)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = java.io.File.createTempFile("baby_moment_", ".jpg", momentsDir).apply {
                    createNewFile()
                }
                tempPhotoFile = file
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                tempPhotoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error cámara: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para hacer la foto.", Toast.LENGTH_LONG).show()
        }
    }

    // Camera video recorder launcher
    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            val file = tempPhotoFile
            if (file != null && file.exists()) {
                val absolutePath = file.absolutePath
                if (showEditMomentDetail != null) {
                    localPhotoPath = absolutePath
                    Toast.makeText(context, "¡Vídeo de bebé capturado! 👶🎥", Toast.LENGTH_SHORT).show()
                    viewModel.completeMoment(
                        moment = showEditMomentDetail!!,
                        dateHappened = dateEdit,
                        location = locEdit,
                        details = detEdit,
                        photoPath = absolutePath,
                        newName = nameEdit
                    )
                } else if (showAddMomentDialog) {
                    customPhotoPath = absolutePath
                    Toast.makeText(context, "¡Vídeo de bebé capturado! 👶🎥", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Grabación de vídeo cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for recording video
    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = java.io.File.createTempFile("baby_moment_video_", ".mp4", momentsDir).apply {
                    createNewFile()
                }
                tempPhotoFile = file
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                tempPhotoUri = uri
                captureVideoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error cámara vídeo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para grabar vídeo.", Toast.LENGTH_LONG).show()
        }
    }

    // Photo & Video picker from phone gallery launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val type = contentResolver.getType(uri) ?: ""
                val isVideo = type.startsWith("video") || uri.toString().contains("video", ignoreCase = true)
                val extension = if (isVideo) ".mp4" else ".jpg"
                val prefix = if (isVideo) "baby_moment_video_" else "baby_moment_photo_"
                val file = java.io.File.createTempFile(prefix, extension, momentsDir)

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.io.FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val absolutePath = file.absolutePath
                if (showEditMomentDetail != null) {
                    localPhotoPath = absolutePath
                    Toast.makeText(context, if (isVideo) "¡Vídeo de la galería seleccionado! 👶🎥" else "¡Foto de la galería seleccionada! 👶📸", Toast.LENGTH_SHORT).show()
                } else if (showAddMomentDialog) {
                    customPhotoPath = absolutePath
                    val msg = if (isVideo) "¡Vídeo de la galería seleccionado! 👶🎥" else "¡Foto de la galería seleccionada! 👶📸"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al importar de la galería: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diario de Momentos",
                    style = Typography.headlineLarge,
                    color = PrimaryDark
                )
                Box(
                    modifier = Modifier.clickable { viewModel.currentTab.value = "Perfil" }
                ) {
                    // Calculate baby's current milestone status for avatar
                    val babyAgeMonths = viewModel.getAgeInMonths(baby.dob)
                    val currentMilestoneRange = when {
                        babyAgeMonths < 6 -> "0-6 meses"
                        babyAgeMonths < 12 -> "6-12 meses"
                        babyAgeMonths < 18 -> "12-18 meses"
                        else -> "18-24 meses"
                    }
                    val levelMoments = moments.filter { it.ageRange == currentMilestoneRange && !it.isCustom }
                    val completedCount = levelMoments.count { it.isCompleted }
                    val totalCount = levelMoments.size.coerceAtLeast(1)
                    val percent = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

                    MiniBabyAvatar(
                        gender = baby.gender,
                        ageInMonths = babyAgeMonths,
                        sizeDp = 44,
                        skinTone = baby.skinTone,
                        hairColor = baby.hairColor,
                        completedPercentage = percent,
                        preferredColor = baby.preferredPacifierColor,
                        useProfileImage = true
                    )
                }
            }

            // Age category accordion lists
            val ageCategories = listOf("0-6 meses", "6-12 meses", "12-18 meses", "18-24 meses")

            val babyAgeMonths = viewModel.getAgeInMonths(baby.dob)
            val defaultAgeRange = when {
                babyAgeMonths < 6 -> "0-6 meses"
                babyAgeMonths < 12 -> "6-12 meses"
                babyAgeMonths < 18 -> "12-18 meses"
                else -> "18-24 meses"
            }

            val targetSelectedRange by viewModel.selectedMomentAgeRange.collectAsStateWithLifecycle()
            val expandedRange = remember { mutableStateOf(targetSelectedRange ?: defaultAgeRange) }

            // If user clicked a specific range on Home, expand that range
            LaunchedEffect(targetSelectedRange) {
                if (targetSelectedRange != null) {
                    expandedRange.value = targetSelectedRange!!
                    viewModel.selectedMomentAgeRange.value = null
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(ageCategories) { range ->
                    val filtered = moments.filter { it.ageRange == range }
                    val completed = filtered.count { it.isCompleted }
                    val total = filtered.size

                    val isExpanded = expandedRange.value == range

                    Card(
                        modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, MintPrimary.copy(alpha = 0.1f))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedRange.value = if (isExpanded) "" else range
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (range) {
                                    "0-6 meses" -> Icons.Default.ChildCare
                                    "6-12 meses" -> Icons.Default.DirectionsRun
                                    "12-18 meses" -> Icons.Default.NaturePeople
                                    else -> Icons.Default.AutoAwesome
                                }
                                val iconColor = when (range) {
                                    "0-6 meses" -> MintPrimary
                                    "6-12 meses" -> SkyBlue
                                    "12-18 meses" -> LavenderSoft
                                    else -> GoldenMilestone
                                }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(text = range, style = Typography.headlineSmall, color = TextDark)
                                    Text(
                                        text = "$completed/$total momentos completados",
                                        style = Typography.labelSmall,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                            HorizontalDivider(color = CreamBg, thickness = 1.dp)
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (filtered.isEmpty()) {
                                    Text(
                                        text = "Aún no hay momentos definidos para esta etapa.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                filtered.forEach { moment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showEditMomentDetail = moment }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = moment.name,
                                                style = if (moment.isCompleted) Typography.bodyLarge else Typography.bodyMedium,
                                                color = if (moment.isCompleted) TextDark else TextMuted,
                                                fontWeight = if (moment.isCompleted) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            if (moment.isCustom) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Personalizado",
                                                    style = Typography.labelSmall,
                                                    color = MintPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    showEditMomentDetail = moment
                                                    autoOpenMediaSelector = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "Añadir foto o vídeo",
                                                    tint = if (!moment.photoPath.isNullOrBlank()) MintPrimary else TextMuted.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.toggleMomentStatus(moment)
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (moment.isCompleted) {
                                                        if (moment.isCustom) Icons.Default.Favorite else Icons.Default.CheckCircle
                                                        } else {
                                                        Icons.Default.RadioButtonUnchecked
                                                        },
                                                    contentDescription = if (moment.isCompleted) "Desmarcar momento" else "Marcar momento",
                                                    tint = if (moment.isCompleted) {
                                                        if (moment.isCustom) PrimaryDark else MintPrimary
                                                        } else TextMuted.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = CreamBg.copy(alpha = 0.5f), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button (FAB) to add custom moments
        FloatingActionButton(
            onClick = { showAddMomentDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp),
            containerColor = PrimaryDark,
            contentColor = SurfaceWhite
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Momento Personalizado")
        }
    }

    // Custom moments creator dialog
    if (showAddMomentDialog) {
        AlertDialog(
            onDismissRequest = { showAddMomentDialog = false },
            title = { Text("Añadir Momento Customizado", style = Typography.headlineMedium, color = PrimaryDark) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Personaliza recuerdos y momentos hermosos junto a tu bebé. (No cuentan para objetivos oficiales de edad).", style = Typography.labelSmall, color = TextMuted)

                    PMOutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nombre del momento") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Age range selection
                    Text("Rango de edad:", style = Typography.labelLarge)
                    var expandedRange by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedRange = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(customAgeRange, color = TextDark)
                        }
                        DropdownMenu(
                            expanded = expandedRange,
                            onDismissRequest = { expandedRange = false }
                        ) {
                            listOf("0-6 meses", "6-12 meses", "12-18 meses", "18-24 meses").forEach { selectRange ->
                                DropdownMenuItem(
                                    text = { Text(selectRange) },
                                    onClick = {
                                        customAgeRange = selectRange
                                        expandedRange = false
                                    }
                                )
                            }
                        }
                    }

                    PMOutlinedTextField(
                        value = customDate,
                        onValueChange = { customDate = it },
                        label = { Text("¿Cuándo paso? (Ej. 15/05/2024)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    PMOutlinedTextField(
                        value = customLocation,
                        onValueChange = { customLocation = it },
                        label = { Text("¿Dónde fue? (Ej. Baño principal)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    PMOutlinedTextField(
                        value = customDetails,
                        onValueChange = { customDetails = it },
                        label = { Text("Detalles del momento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            viewModel.addCustomMoment(
                                name = customName,
                                ageRange = customAgeRange,
                                dateHappened = customDate,
                                location = customLocation,
                                details = customDetails,
                                photoPath = "mock_photo_drive"
                            )
                            customName = ""
                            customDate = ""
                            customLocation = ""
                            customDetails = ""
                            showAddMomentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Guardar Momento")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMomentDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}
