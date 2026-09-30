package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    activities: List<ActivityRecord>,
    moments: List<MomentRecord>
) {
    val context = LocalContext.current
    var showManualSleepDialog by remember { mutableStateOf(false) }
    var showQuickMealDialog by remember { mutableStateOf(false) }
    var showQuickDiaperDialog by remember { mutableStateOf(false) }
    var sleepNotes by remember { mutableStateOf("") }
    val isTimerRunning by viewModel.isSleepTimerActive.collectAsStateWithLifecycle()

    var showGeneralSyncInfo by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Calculate baby's current milestone status for avatar and progress box
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

            // Profile & Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${baby.name} • ${viewModel.getAgeDisplayString(baby.dob)}",
                        style = Typography.headlineMedium,
                        color = PrimaryDark
                    )
                    Text(
                        text = "Acompañando su crecimiento paso a paso",
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                }

                // Mini profile avatar clickable
                Box(
                    modifier = Modifier
                        .clickable { viewModel.currentTab.value = "Perfil" }
                ) {
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

            // Milestone Progress Box based on baby's current age
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable {
                        viewModel.selectedMomentAgeRange.value = currentMilestoneRange
                        viewModel.currentTab.value = "Momentos"
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = MintPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Momentos de $currentMilestoneRange: $completedCount/$totalCount",
                            style = Typography.labelLarge,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MintPrimary,
                            trackColor = CreamBg,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calculate milestone completion percentage based on baby's current growth stage
            val ageMonths = viewModel.getAgeInMonths(baby.dob)
            val relevantAgeRanges = if (ageMonths < 12) {
                listOf("0-6 meses", "6-12 meses")
            } else {
                listOf("12-18 meses", "18-24 meses")
            }

            val relevantMoments = moments.filter {
                relevantAgeRanges.contains(it.ageRange) && !it.isCustom
            }
            val completedMoments = relevantMoments.count { it.isCompleted }
            val totalMoments = relevantMoments.size.coerceAtLeast(1)
            val milestonePercent = (completedMoments * 100) / totalMoments


            // Central Smart Baby Avatar Custom Drawing!
            var showPacifierLegend by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Information button for pacifier colors
                IconButton(
                    onClick = { showPacifierLegend = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .background(SurfaceWhite.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = if (ageMonths < 12) "Información de chupete" else "Información de peluches",
                        tint = PrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Growth and Gender Adaptive baby drawing
                // Pacifier Color scale based on completed moments percent
                val pacifierColor = when {
                    (milestonePercent / 100f) <= 0.25f -> Color(0xFF94A3B8) // Soft Gray
                    (milestonePercent / 100f) <= 0.50f -> PeachWarm       // Peach/Orange
                    (milestonePercent / 100f) <= 0.75f -> MintPrimary     // Mint green
                    else -> GoldenMilestone             // Golden milestone!
                }

                InteractiveBabyAvatar(
                    gender = baby.gender,
                    ageInMonths = ageMonths,
                    skinTone = baby.skinTone,
                    hairColor = baby.hairColor,
                    pacifierColor = pacifierColor,
                    currentMilestonePercent = milestonePercent,
                    preferredPacifierColorName = baby.preferredPacifierColor,
                    onClick = { showGeneralSyncInfo = true }
                )
            }

            if (showPacifierLegend) {
                AlertDialog(
                    onDismissRequest = { showPacifierLegend = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            if (ageMonths < 12) "Leyenda del Chupete" else "Leyenda de Peluches",
                            style = Typography.headlineSmall,
                            color = PrimaryDark
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                if (ageMonths < 12)
                                    "El color del chupete refleja el progreso de los hitos del primer año:"
                                else
                                    "El peluche refleja el progreso de los hitos según la edad:",
                                style = Typography.bodyMedium,
                                color = TextDark
                            )

                            val legendItems = if (ageMonths < 12) {
                                listOf(
                                    "Blanco" to (Color.White to "0% - 19%"),
                                    "Azul" to (Color(0xFF3B82F6) to "20% - 39%"),
                                    "Verde" to (Color(0xFF22C55E) to "40% - 59%"),
                                    "Púrpura" to (Color(0xFFA855F7) to "60% - 79%"),
                                    "Rojo" to (Color(0xFFEF4444) to "80% - 99%"),
                                    "Dorado" to (Color(0xFFD4AF37) to "100%")
                                )
                            } else {
                                listOf(
                                    "Conejo" to (Color.White to "0% - 19%"),
                                    "Delfín" to (Color(0xFF3B82F6) to "20% - 39%"),
                                    "Tortuga" to (Color(0xFF22C55E) to "40% - 59%"),
                                    "Pulpo" to (Color(0xFFA855F7) to "60% - 79%"),
                                    "Cangrejo" to (Color(0xFFEF4444) to "80% - 99%"),
                                    "León" to (Color(0xFFD4AF37) to "100%")
                                )
                            }

                            legendItems.forEach { (_, details) ->
                                val (colorValue, percentage) = details
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(colorValue)
                                            .border(1.dp, Color.LightGray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "$percentage",
                                        style = Typography.bodySmall,
                                        color = TextDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (ageMonths < 12)
                                    "¡Sigue completando momentos para desbloquear más colores!"
                                else
                                    "¡Sigue completando momentos para desbloquear más peluches!",
                                style = Typography.labelSmall,
                                color = MintPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPacifierLegend = false }) {
                            Text("Entendido", color = PrimaryDark)
                        }
                    }
                )
            }

    // Quick Actions Bento Grid (comida, sueño, pañal, nuevo momento)
    Text(
        text = "Registro Rápido",
        style = Typography.headlineSmall,
        color = PrimaryDark,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dar de comer
        BentoCard(
            modifier = Modifier.weight(1f),
            title = "Dar de comer",
            icon = Icons.Default.Restaurant,
            iconColor = PrimaryDark,
            bgColor = PrimaryDark.copy(alpha = 0.08f),
            onClick = {
                showQuickMealDialog = true
            }
        )
        // Dormir
        BentoCard(
            modifier = Modifier.weight(1f),
            title = "Dormir",
            icon = Icons.Default.Bedtime,
            iconColor = SkyBlue,
            bgColor = SkyBlue.copy(alpha = 0.15f),
            onClick = {
                showManualSleepDialog = true
            }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cambio Pañal
        BentoCard(
            modifier = Modifier.weight(1f),
            title = "Cambio pañal",
            icon = Icons.Default.ChildCare,
            iconColor = PeachWarm,
            bgColor = PeachWarm.copy(alpha = 0.15f),
            onClick = {
                showQuickDiaperDialog = true
            }
        )
        // Nuevo Momento (Custom)
        BentoCard(
            modifier = Modifier.weight(1f),
            title = "Nuevo Momento",
            icon = Icons.Default.AddCircle,
            iconColor = GoldenMilestone,
            bgColor = GoldenMilestone.copy(alpha = 0.15f),
            onClick = {
                viewModel.showAddMomentDialog.value = true
                viewModel.currentTab.value = "Momentos"
            }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Resumen de Hoy Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Resumen de hoy",
                style = Typography.headlineSmall,
                color = PrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Latest feeding details
            val mealsToday = activities.filter { it.type == "comida" }
            val latestMealText = if (mealsToday.isNotEmpty()) {
                "hace " + getElapsedTime(mealsToday.first().timestamp)
            } else "No registrado"

            SummaryItem(
                icon = Icons.Default.Timer,
                label = "Última comida",
                value = latestMealText,
                color = MintPrimary
            )

            // Latest sleep details
            val sleepsToday = activities.filter {
                it.type == "sueno" &&
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)) == SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }
            val latestSleepText = if (sleepsToday.isNotEmpty()) {
                "hace " + getElapsedTime(sleepsToday.first().timestamp)
            } else "No registrado"

            SummaryItem(
                icon = Icons.Default.Bedtime,
                label = "Último sueño",
                value = latestSleepText,
                color = SkyBlue
            )

            // Total sleep duration today
            val totalSleepMins = sleepsToday.sumOf { it.durationMinutes ?: 0 }
            val totalSleepHours = totalSleepMins / 60
            val totalSleepRemMins = totalSleepMins % 60
            val totalSleepText = if (totalSleepMins > 0) "${totalSleepHours}h ${totalSleepRemMins}m" else "0h"

            SummaryItem(
                icon = Icons.Default.History,
                label = "Duración sueño",
                value = totalSleepText,
                color = SkyBlue
            )

            // Diapers count today
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val diapersToday = activities.filter {
                it.type == "panal" &&
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.timestamp)) == todayStr
            }.size
            val diaperText = if (diapersToday == 1) "1 pañal" else "$diapersToday pañales"

            SummaryItem(
                icon = Icons.Default.ChildCare,
                label = "Pañales de hoy",
                value = diaperText,
                color = PeachWarm
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
        }

        // Live timer floating bar if running
        if (isTimerRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .background(PrimaryDark, RoundedCornerShape(16.dp))
                    .border(2.dp, MintPrimary, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MintPrimary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Su bebé está durmiendo...", color = Color.White, style = Typography.labelLarge)
                            Text("Calculando siesta actual", color = MintPrimary, style = Typography.labelSmall)
                        }
                    }

                    Button(
                        onClick = { showManualSleepDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Detener", style = Typography.labelLarge)
                    }
                }
            }
        }

        // 1. Manual Sleep Registration Dialog
        if (showManualSleepDialog) {
            val timerStart = viewModel.sleepTimerStart.collectAsStateWithLifecycle().value
            val initialStart = if (timerStart != null && isTimerRunning) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timerStart))
            } else null
            val initialEnd = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            SleepManualRegistrationDialog(
                baby = baby,
                initialStartTime = initialStart,
                initialEndTime = initialEnd,
                onDismiss = { showManualSleepDialog = false },
                onConfirm = { dateStr: String, startTime: String, endTime: String, notes: String ->
                    viewModel.addManualSleepRecord(dateStr, startTime, endTime, notes)
                    if (isTimerRunning) {
                        viewModel.cancelSleepTimer()
                    }
                    showManualSleepDialog = false
                    Toast.makeText(context, "¡Sueño registrado con éxito! 🌙💤", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showQuickMealDialog) {
            QuickMealRegistrationDialog(
                onDismiss = { showQuickMealDialog = false },
                onConfirm = { dateStr, mealType, foodName, notes ->
                    viewModel.addMealRecord(mealType, foodName, notes)
                    showQuickMealDialog = false
                    Toast.makeText(context, "¡Comida registrada con éxito! 🍼", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showQuickDiaperDialog) {
            QuickDiaperRegistrationDialog(
                onDismiss = { showQuickDiaperDialog = false },
                onConfirm = { dateStr, condition, quantity, notes ->
                    viewModel.addDiaperRecord(condition, notes, dateStr, quantity)
                    showQuickDiaperDialog = false
                    Toast.makeText(context, "¡Cambio de pañal registrado! 👶", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 2. Google Drive info popup
        if (showGeneralSyncInfo) {
            val syncState by viewModel.googleDriveSyncStatus.collectAsStateWithLifecycle()
            val photosCount by viewModel.totalSyncedPhotosCount.collectAsStateWithLifecycle()
            val googleEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
            val googleName by viewModel.googleAccountName.collectAsStateWithLifecycle()

            AlertDialog(
                onDismissRequest = { showGeneralSyncInfo = false },
                containerColor = Color.White,
                title = { Text("Google Drive Backup", style = Typography.headlineMedium, color = PrimaryDark) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¡Tus recuerdos están a salvo!", fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Cada vez que completas un momento especial y subes una foto, 'Primeros Momentos' la guarda automáticamente en una carpeta dedicada de Google Drive.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MintPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Estado actual: $syncState", color = PrimaryDark, fontWeight = FontWeight.Bold)
                        }
                        if (googleEmail != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MintPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cuenta: ${googleName ?: ""} ($googleEmail)", color = TextDark)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = MintPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carpeta: Google Drive/Primeros Momentos/", color = TextDark)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SkyBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fotos sincronizadas: $photosCount recuerdos", color = TextDark)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showGeneralSyncInfo = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}
