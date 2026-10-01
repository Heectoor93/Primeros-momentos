package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackingScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    activities: List<ActivityRecord>,
    healthRecords: List<HealthRecord>
) {
    var selectedTab by remember { mutableStateOf("Actividades") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seguimiento",
                style = Typography.headlineLarge,
                color = PrimaryDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    android.widget.Toast.makeText(context, "Generando e informando tu reporte de crecimiento de ${baby.name}...", android.widget.Toast.LENGTH_LONG).show()
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir reporte", tint = PrimaryDark)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clickable { viewModel.currentTab.value = "Perfil" }
                ) {
                    MiniBabyAvatar(
                        gender = baby.gender,
                        ageInMonths = viewModel.getAgeInMonths(baby.dob),
                        sizeDp = 44,
                        skinTone = baby.skinTone,
                        hairColor = baby.hairColor,
                        completedPercentage = 0f,
                        preferredColor = baby.preferredPacifierColor,
                        useProfileImage = true
                    )
                }
            }
        }

        Text(
            text = "${baby.name} • ${viewModel.getAgeDisplayString(baby.dob)}",
            style = Typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5EEFF), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { selectedTab = "Actividades" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "Actividades") MintPrimary else Color.Transparent,
                    contentColor = if (selectedTab == "Actividades") Color.White else TextMuted
                ),
                elevation = null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Actividades", style = Typography.labelLarge)
            }

            Button(
                onClick = { selectedTab = "Salud" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "Salud") MintPrimary else Color.Transparent,
                    contentColor = if (selectedTab == "Salud") Color.White else TextMuted
                ),
                elevation = null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Salud", style = Typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("7 días", "14 días", "1 mes", "3 meses").forEachIndexed { idx, range ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (idx == 0) PrimaryDark else Color(0xFFE5EEFF), RoundedCornerShape(12.dp))
                        .padding(vertical = 6.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(range, style = Typography.labelSmall, color = if (idx == 0) Color.White else TextDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            if (selectedTab == "Actividades") {
                val sleepRecs = activities.filter { it.type == "sueno" }
                val diaperRecs = activities.filter { it.type == "panal" }
                val foodRecs = activities.filter { it.type == "comida" }

                if (activities.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("📊", fontSize = 48.sp)
                            Text(
                                "Sin datos de actividades",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Los gráficos de sueño, pañales y comidas se generarán de forma autónoma una vez empieces a introducir los datos de ${baby.name} desde la pantalla de Inicio.",
                                style = Typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Horas de Sueño", style = Typography.headlineSmall, color = TextDark)
                                    if (sleepRecs.isNotEmpty()) {
                                        val avg = sleepRecs.map { (it.durationMinutes ?: 0) / 60.0 }.average()
                                        Text(String.format("Media: %.1fh", avg), style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (sleepRecs.isEmpty()) {
                                    Text(
                                        text = "Aún no hay registros de sueño. Añade un registro de sueño para ver la gráfica.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    SleepDurationChart(sleepRecs, 30)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Frecuencia de Pañal", style = Typography.headlineSmall, color = TextDark)
                                    if (diaperRecs.isNotEmpty()) {
                                        Text("Total registros: ${diaperRecs.size}", style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (diaperRecs.isEmpty()) {
                                    Text(
                                        text = "Aún no hay registros de pañal. Añade un cambio de pañal para ver el gráfico.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    DiaperLineChart(diaperRecs, 7)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Historial de Comida", style = Typography.headlineSmall, color = TextDark)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (foodRecs.isEmpty()) {
                                    Text("No hay registros de comida hoy aún.", style = Typography.bodyMedium, color = TextMuted)
                                } else {
                                    foodRecs.forEach { rec ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(rec.extraType ?: "Comida", fontWeight = FontWeight.Bold, color = TextDark)
                                                Text(rec.notes, style = Typography.bodyMedium, color = TextMuted)
                                            }
                                            Text(rec.startTime, color = MintPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Divider(color = CreamBg, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val pesoRecs = healthRecords.filter { it.type == "peso" }
                val vaccines = healthRecords.filter { it.type == "vacuna" }
                val appts = healthRecords.filter { it.type == "cita" }

                if (healthRecords.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🩺", fontSize = 48.sp)
                            Text(
                                "Sin datos de salud",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "La evolución del peso, registro de vacunas y próximas citas se dibujarán de forma interactiva una vez las agregues desde la pantalla de Inicio.",
                                style = Typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Evolución del Peso", style = Typography.headlineSmall, color = TextDark)
                                    IconButton(
                                        onClick = {
                                            viewModel.healthHistoryType.value = "peso"
                                            viewModel.currentTab.value = "HealthHistory"
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = "Historial", tint = PrimaryDark, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (pesoRecs.isEmpty()) {
                                    Text(
                                        text = "Aún no hay registros de peso. Añade mediciones de peso para ver el gráfico.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    HealthLineChart(pesoRecs, "Peso", "kg", 30)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Evolución de la Altura", style = Typography.headlineSmall, color = TextDark)
                                    IconButton(
                                        onClick = {
                                            viewModel.healthHistoryType.value = "altura"
                                            viewModel.currentTab.value = "HealthHistory"
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = "Historial", tint = PrimaryDark, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val alturaRecs = healthRecords.filter { it.type == "altura" }
                                if (alturaRecs.isEmpty()) {
                                    Text(
                                        text = "Aún no hay registros de altura. Añade mediciones de altura para ver el gráfico.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    HealthLineChart(alturaRecs, "Altura", "cm", 30)
                                }
                            }
                        }

                        val pendingVaccines = vaccines.filter {
                            it.status == "Falta por vacunar" || it.status == "Pendiente más tomas"
                        }
                        val completedVaccines = vaccines.filter {
                            it.status == "Vacunado completamente" || (it.status != "Falta por vacunar" && it.status != "Pendiente más tomas")
                        }
                        var vaccineListFilter by remember { mutableStateOf("todas") }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Registro de Vacunas", style = Typography.headlineSmall, color = PrimaryDark, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = MintPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "${completedVaccines.size}/${vaccines.size} aplicadas",
                                            style = Typography.labelSmall,
                                            color = MintPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = vaccineListFilter == "todas",
                                        onClick = { vaccineListFilter = "todas" },
                                        label = { Text("Todas (${vaccines.size})", style = Typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = vaccineListFilter == "pendientes",
                                        onClick = { vaccineListFilter = "pendientes" },
                                        label = { Text("Pendientes (${pendingVaccines.size})", style = Typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = vaccineListFilter == "completadas",
                                        onClick = { vaccineListFilter = "completadas" },
                                        label = { Text("Completadas (${completedVaccines.size})", style = Typography.labelSmall) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (vaccines.isEmpty()) {
                                    Text(
                                        text = "Aún no se han registrado vacunas. Añádelas desde Registrar > Salud > Vacunas.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    val displayedVaccines = when (vaccineListFilter) {
                                        "pendientes" -> pendingVaccines
                                        "completadas" -> completedVaccines
                                        else -> vaccines
                                    }

                                    if (displayedVaccines.isEmpty()) {
                                        Text(
                                            text = if (vaccineListFilter == "pendientes") "¡Al día! No hay vacunas pendientes." else "No hay vacunas en esta lista.",
                                            style = Typography.bodyMedium,
                                            color = TextMuted,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            displayedVaccines.forEach { vac ->
                                                val isMoreDoses = vac.status == "Pendiente más tomas"
                                                val isNotVaccinated = vac.status == "Falta por vacunar"
                                                val isCompleted = !isMoreDoses && !isNotVaccinated

                                                val statusBadgeColor = when {
                                                    isMoreDoses -> Color(0xFFD97706)
                                                    isNotVaccinated -> Color(0xFFDC2626)
                                                    else -> MintPrimary
                                                }
                                                val statusBadgeBg = when {
                                                    isMoreDoses -> Color(0xFFFEF3C7)
                                                    isNotVaccinated -> Color(0xFFFEE2E2)
                                                    else -> Color(0xFFE6F7F3)
                                                }
                                                val statusIcon = when {
                                                    isMoreDoses -> Icons.Default.Pending
                                                    isNotVaccinated -> Icons.Default.Schedule
                                                    else -> Icons.Default.CheckCircle
                                                }
                                                val statusText = when {
                                                    isMoreDoses -> "Pendiente más tomas"
                                                    isNotVaccinated -> "Falta por vacunar"
                                                    else -> "Vacunado completamente"
                                                }

                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = Color(0xFFF8FAFC),
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = vac.title,
                                                                style = Typography.bodyLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextDark,
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Surface(
                                                                color = statusBadgeBg,
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = statusIcon,
                                                                        contentDescription = null,
                                                                        tint = statusBadgeColor,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Text(
                                                                        text = statusText,
                                                                        style = Typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = statusBadgeColor
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        val dateToShow = vac.dateString ?: if (vac.timestamp > 0) {
                                                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(vac.timestamp))
                                                        } else null

                                                        if (!dateToShow.isNullOrBlank()) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.CalendarToday,
                                                                    contentDescription = null,
                                                                    tint = TextMuted,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                                Text(
                                                                    text = if (isCompleted) "Administrada: $dateToShow" else "Fecha prevista: $dateToShow",
                                                                    style = Typography.labelSmall,
                                                                    color = TextMuted
                                                                )
                                                            }
                                                        }

                                                        if (vac.notes.isNotBlank()) {
                                                            Text(
                                                                text = vac.notes,
                                                                style = Typography.bodySmall,
                                                                color = TextMuted
                                                            )
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            if (!isCompleted) {
                                                                TextButton(
                                                                    onClick = {
                                                                        viewModel.updateVaccineStatus(vac, "Vacunado completamente")
                                                                        Toast.makeText(context, "¡Vacuna marcada como completada!", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Check, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(16.dp))
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text("Marcar completada", style = Typography.labelSmall, color = MintPrimary, fontWeight = FontWeight.Bold)
                                                                }
                                                            } else {
                                                                TextButton(
                                                                    onClick = {
                                                                        viewModel.updateVaccineStatus(vac, "Pendiente más tomas")
                                                                        Toast.makeText(context, "Estado actualizado a pendiente de más tomas", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text("Cambiar estado", style = Typography.labelSmall, color = TextMuted)
                                                                }
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    viewModel.deleteHealthRecord(vac.id)
                                                                    Toast.makeText(context, "Registro eliminado", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.DeleteOutline,
                                                                    contentDescription = "Eliminar",
                                                                    tint = TextMuted.copy(alpha = 0.7f),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Citas Médicas", style = Typography.headlineSmall, color = PrimaryDark, fontWeight = FontWeight.Bold)
                                    if (appts.isNotEmpty()) {
                                        Surface(
                                            color = PrimaryDark.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "${appts.size} programada(s)",
                                                style = Typography.labelSmall,
                                                color = PrimaryDark,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (appts.isEmpty()) {
                                    Text(
                                        text = "No tienes citas médicas registradas. Puedes añadir consultas o revisiones pediátricas desde Registrar > Salud > Citas Médicas.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        appts.forEach { cita ->
                                            val apptDateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(cita.timestamp))

                                            val openCalendar = {
                                                try {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                                                        data = android.provider.CalendarContract.Events.CONTENT_URI
                                                        putExtra(android.provider.CalendarContract.Events.TITLE, cita.title)
                                                        putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, cita.timestamp)
                                                        putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Cita de ${baby.name}: ${cita.notes}")
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    try {
                                                        val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                            data = android.net.Uri.parse("content://com.android.calendar/time/${cita.timestamp}")
                                                        }
                                                        context.startActivity(viewIntent)
                                                    } catch (e2: Exception) {
                                                        Toast.makeText(context, "Cita: ${cita.title} el $apptDateFormatted", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }

                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = PrimaryDark.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, PrimaryDark.copy(alpha = 0.15f))
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = cita.title,
                                                                style = Typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = PrimaryDark
                                                            )
                                                            if (cita.hasReminder) {
                                                                Text(
                                                                    text = "🔔 Recordatorio activo",
                                                                    style = Typography.labelSmall,
                                                                    color = MintPrimary,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                viewModel.deleteHealthRecord(cita.id)
                                                                Toast.makeText(context, "Cita eliminada", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.DeleteOutline,
                                                                contentDescription = "Eliminar cita",
                                                                tint = TextMuted.copy(alpha = 0.7f),
                                                                modifier = Modifier.size(18.dp)
                                                                )
                                                        }
                                                    }

                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { openCalendar() },
                                                        color = Color.White,
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.dp, MintPrimary.copy(alpha = 0.4f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(34.dp)
                                                                            .clip(CircleShape)
                                                                            .background(MintPrimary.copy(alpha = 0.15f)),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        Icons.Default.CalendarMonth,
                                                                        contentDescription = "Calendario",
                                                                        tint = MintPrimary,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                                Column {
                                                                    Text(
                                                                        text = "Fecha y hora de la cita",
                                                                        style = Typography.labelSmall,
                                                                        color = TextMuted
                                                                    )
                                                                    Text(
                                                                        text = apptDateFormatted,
                                                                        style = Typography.bodyMedium,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = PrimaryDark
                                                                    )
                                                                }
                                                            }

                                                            Surface(
                                                                color = MintPrimary,
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Icon(
                                                                        Icons.Default.Event,
                                                                        contentDescription = null,
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Text(
                                                                        text = "Abrir Calendario",
                                                                        style = Typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color.White
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if (cita.notes.isNotBlank() && cita.notes != "Cita programada con recordatorio automático") {
                                                        Text(
                                                            text = cita.notes,
                                                            style = Typography.bodySmall,
                                                            color = TextMuted
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleepDurationChart(records: List<ActivityRecord>, rangeDays: Int) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val totalWidth = constraints.maxWidth.toFloat()
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val rangeMillis = rangeDays.toLong() * 24 * 60 * 60 * 1000L
        val cutoffTimestamp = todayStart - rangeMillis

        val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val displaySdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        val filteredAndGrouped = records
            .filter { it.timestamp >= cutoffTimestamp }
            .groupBy { dateSdf.format(java.util.Date(it.timestamp)) }
            .mapValues { it.value.sumOf { rec -> rec.durationMinutes ?: 0 } }
            .toList()
            .sortedBy { it.first }

        if (filteredAndGrouped.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos en este rango", style = Typography.bodySmall, color = TextMuted)
            }
            return@BoxWithConstraints
        }

        val sleepData = filteredAndGrouped.map { (it.second / 60f).coerceIn(0f, 24f) }
        val timestamps = filteredAndGrouped.map {
            dateSdf.parse(it.first)!!.time
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records, rangeDays) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val leftMargin = 60f
                        val chartWidth = width - leftMargin

                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE

                        timestamps.forEachIndexed { idx, ts ->
                            val px = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                            val dist = kotlin.math.abs(offset.x - px)
                            if (dist < minDist) {
                                minDist = dist
                                closestIdx = idx
                            }
                        }
                        if (minDist < 30f) selectedPoint = closestIdx else selectedPoint = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftMargin = 60f
            val chartWidth = width - leftMargin

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val maxSleep = maxOf(10f, sleepData.maxOrNull() ?: 10f)

            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = (maxSleep / labelCount * i).toInt()
                val yPos = height - (i.toFloat() / labelCount) * height * 0.65f - 40f
                val axisPaint = android.graphics.Paint(textPaint).apply { textAlign = android.graphics.Paint.Align.RIGHT }
                drawContext.canvas.nativeCanvas.drawText("$labelVal h", 30f, yPos, axisPaint)
            }

            sleepData.forEachIndexed { idx, value ->
                val barHeight = (value / maxSleep) * height * 0.65f
                val ts = timestamps[idx]
                val barCenterX = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                val barLeft = barCenterX - 5f
                val y = height - barHeight - 40f

                if (idx < sleepData.size - 1) {
                    val nextTs = timestamps[idx + 1]
                    val nextCenterX = leftMargin + ((nextTs - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                    val nextBarHeight = (sleepData[idx + 1] / maxSleep) * height * 0.65f
                    val nextY = height - nextBarHeight - 40f

                    drawLine(
                        color = SkyBlue.copy(alpha = 0.5f),
                        start = Offset(barCenterX, y),
                        end = Offset(nextCenterX, nextY),
                        strokeWidth = 2f
                    )
                }

                drawRoundRect(
                    color = if (idx == sleepData.size - 1) MintPrimary else SkyBlue,
                    topLeft = Offset(barLeft, y),
                    size = Size(10f, barHeight),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                drawContext.canvas.nativeCanvas.drawText(displaySdf.format(java.util.Date(ts)), barCenterX, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val idx = selectedPoint!!
            if (idx < sleepData.size) {
                val value = sleepData[idx]
                val ts = timestamps[idx]
                val leftMargin = 60f
                val chartWidth = totalWidth - leftMargin
                val px = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth

                Box(
                    modifier = Modifier
                        .offset { IntOffset((px - 40f).toInt(), 20) }
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("${String.format("%.1f", value)} h", color = Color.White, style = Typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun DiaperLineChart(records: List<ActivityRecord>, rangeDays: Int) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val totalWidth = constraints.maxWidth.toFloat()
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val rangeMillis = rangeDays.toLong() * 24 * 60 * 60 * 1000L
        val cutoffTimestamp = todayStart - rangeMillis

        val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val displaySdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        val filteredAndGrouped = records
            .filter { it.timestamp >= cutoffTimestamp }
            .groupBy { dateSdf.format(java.util.Date(it.timestamp)) }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.first }

        if (filteredAndGrouped.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos en este rango", style = Typography.bodySmall, color = TextMuted)
            }
            return@BoxWithConstraints
        }

        val diaperCounts = filteredAndGrouped.map { it.second.toFloat() }
        val timestamps = filteredAndGrouped.map {
            dateSdf.parse(it.first)!!.time
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records, rangeDays) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val leftMargin = 60f
                        val chartWidth = width - leftMargin

                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE

                        timestamps.forEachIndexed { idx, ts ->
                            val px = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                            val dist = kotlin.math.abs(offset.x - px)
                            if (dist < minDist) {
                                minDist = dist
                                closestIdx = idx
                            }
                        }
                        if (minDist < 30f) selectedPoint = closestIdx else selectedPoint = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftMargin = 60f
            val chartWidth = width - leftMargin

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val maxCount = diaperCounts.maxOrNull()?.coerceAtLeast(2f) ?: 2f

            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = (maxCount / labelCount * i).toInt()
                val yPos = height - (i.toFloat() / labelCount) * height * 0.6f - 40f
                val axisPaint = android.graphics.Paint(textPaint).apply { textAlign = android.graphics.Paint.Align.RIGHT }
                drawContext.canvas.nativeCanvas.drawText("$labelVal", 30f, yPos, axisPaint)
            }

            val points = diaperCounts.mapIndexed { idx, count ->
                val ts = timestamps[idx]
                val x = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                val y = height - (count / maxCount) * height * 0.6f - 40f
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = SkyBlue, start = points[i], end = points[i + 1], strokeWidth = 6f, cap = StrokeCap.Round)
            }

            points.forEachIndexed { idx, pt ->
                drawCircle(color = if (idx == points.size - 1) PrimaryDark else SkyBlue, radius = 8f, center = pt)
                val ts = timestamps[idx]
                drawContext.canvas.nativeCanvas.drawText(displaySdf.format(java.util.Date(ts)), pt.x, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val idx = selectedPoint!!
            if (idx < filteredAndGrouped.size) {
                val count = filteredAndGrouped[idx].second
                val ts = timestamps[idx]
                val leftMargin = 60f
                val chartWidth = totalWidth - leftMargin
                val px = leftMargin + ((ts - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth

                Box(
                    modifier = Modifier
                        .offset { IntOffset((px - 40f).toInt(), 20) }
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("$count pañales", color = Color.White, style = Typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun HealthLineChart(records: List<HealthRecord>, label: String, unit: String, rangeDays: Int) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val totalWidth = constraints.maxWidth.toFloat()
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val rangeMillis = rangeDays.toLong() * 24 * 60 * 60 * 1000L
        val cutoffTimestamp = todayStart - rangeMillis

        val filteredRecords = records
            .filter { it.timestamp >= cutoffTimestamp }
            .sortedBy { it.timestamp }

        if (filteredRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos en este rango", style = Typography.bodySmall, color = TextMuted)
            }
            return@BoxWithConstraints
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records, rangeDays) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val leftMargin = 60f
                        val chartWidth = width - leftMargin

                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE

                        filteredRecords.forEachIndexed { idx, record ->
                            val px = leftMargin + ((record.timestamp - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                            val dist = kotlin.math.abs(offset.x - px)
                            if (dist < minDist) {
                                minDist = dist
                                closestIdx = idx
                            }
                        }
                        if (minDist < 30f) selectedPoint = closestIdx else selectedPoint = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftMargin = 60f
            val chartWidth = width - leftMargin

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

            val data = filteredRecords.map { it.value.toFloat() }
            val maxVal = maxOf(10f, data.maxOrNull() ?: 10f)
            val minVal = minOf(1f, data.minOrNull() ?: 1f)
            val range = (maxVal - minVal).coerceAtLeast(1f)

            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = minVal + (range / labelCount * i)
                val yPos = height - (i.toFloat() / labelCount) * height * 0.6f - 50f
                val axisPaint = android.graphics.Paint(textPaint).apply { textAlign = android.graphics.Paint.Align.RIGHT }
                drawContext.canvas.nativeCanvas.drawText("${String.format("%.1f", labelVal)}", 30f, yPos, axisPaint)
            }

            val points = filteredRecords.map { record ->
                val x = leftMargin + ((record.timestamp - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth
                val y = height - ((record.value.toFloat() - minVal) / range) * height * 0.6f - 50f
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = MintPrimary, start = points[i], end = points[i + 1], strokeWidth = 6f, cap = StrokeCap.Round)
            }

            points.forEachIndexed { idx, pt ->
                drawCircle(color = if (idx == points.size - 1) PrimaryDark else MintPrimary, radius = 8f, center = pt)
                val dateStr = sdf.format(java.util.Date(filteredRecords[idx].timestamp))
                drawContext.canvas.nativeCanvas.drawText(dateStr, pt.x, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val idx = selectedPoint!!
            if (idx < filteredRecords.size) {
                val record = filteredRecords[idx]
                val leftMargin = 60f
                val chartWidth = totalWidth - leftMargin
                val px = leftMargin + ((record.timestamp - cutoffTimestamp).toFloat() / rangeMillis.toFloat()) * chartWidth

                Box(
                    modifier = Modifier
                        .offset { IntOffset((px - 40f).toInt(), 20) }
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("${record.value} $unit", color = Color.White, style = Typography.labelSmall)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HealthHistoryScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    healthRecords: List<HealthRecord>
) {
    val currentType = viewModel.healthHistoryType.collectAsStateWithLifecycle().value
    val filteredRecords = healthRecords.filter { it.type == currentType }.sortedBy { it.timestamp }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (currentType == "peso") "Historial de Peso" else "Historial de Altura",
                        style = Typography.headlineSmall,
                        color = PrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentTab.value = "Seguimiento" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(PremiumBackgroundBrush)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (filteredRecords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📉", fontSize = 64.sp)
                    Text(
                        "No hay registros de $currentType",
                        style = Typography.headlineMedium,
                        color = PrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Añade mediciones desde la pantalla de registrar para ver la evolución.",
                        style = Typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    filteredRecords.reversed().forEach { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
                                    Text(
                                        text = dateStr,
                                        style = Typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${record.value} ${if (currentType == "peso") "kg" else "cm"}",
                                        style = Typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteHealthRecord(record.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Eliminar",
                                        tint = TextMuted.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
