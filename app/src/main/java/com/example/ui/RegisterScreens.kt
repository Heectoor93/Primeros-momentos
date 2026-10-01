package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ActivityRecord
import com.example.data.BabyProfile
import com.example.data.HealthRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@Composable
fun RegisterScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    activities: List<ActivityRecord>,
    healthRecords: List<HealthRecord>
) {
    var activeTab by remember { mutableStateOf("Actividad") }

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
                text = "Registro de Actividad",
                style = Typography.headlineLarge,
                color = PrimaryDark
            )
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5EEFF), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = "Actividad" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "Actividad") MintPrimary else Color.Transparent,
                    contentColor = if (activeTab == "Actividad") Color.White else TextMuted
                ),
                elevation = null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Actividad", style = Typography.labelLarge)
            }

            Button(
                onClick = { activeTab = "Salud" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "Salud") MintPrimary else Color.Transparent,
                    contentColor = if (activeTab == "Salud") Color.White else TextMuted
                ),
                elevation = null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Salud", style = Typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activeTab == "Actividad") {
            ActividadTabContent(viewModel, baby)
        } else {
            SaludTabContent(viewModel)
        }
    }
}

@Composable
fun ActividadTabContent(viewModel: BabyViewModel, baby: BabyProfile) {
    val context = LocalContext.current
    val targetType by viewModel.targetActivityType.collectAsStateWithLifecycle()
    var selectedActivityType by remember { mutableStateOf("comida") }

    LaunchedEffect(targetType) {
        if (targetType != null) {
            selectedActivityType = targetType!!
            viewModel.targetActivityType.value = null
        }
    }

    var mealType by remember { mutableStateOf("Comida") }
    var foodName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sleepStartTime by remember { mutableStateOf("20:00") }
    var sleepEndTime by remember { mutableStateOf("07:00") }
    var sleepNotes by remember { mutableStateOf("") }
    var diaperCondition by remember { mutableStateOf("Seco") }
    var diaperQuantity by remember { mutableStateOf(1) }
    var activityDate by remember { mutableStateOf("") }

    val datePickerDialog = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                activityDate = sdf.format(chosenCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                BentoChip(
                    label = "Comida",
                    selected = selectedActivityType == "comida",
                    onClick = { selectedActivityType = "comida" }
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                BentoChip(
                    label = "Sueño",
                    selected = selectedActivityType == "sueno",
                    onClick = { selectedActivityType = "sueno" }
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                BentoChip(
                    label = "Pañal",
                    selected = selectedActivityType == "panal",
                    onClick = { selectedActivityType = "panal" }
                )
            }
        }

        if (selectedActivityType == "sueno") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFF1E5B94),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Registro de Sueño",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Horas y duración de descanso",
                                style = Typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamBg)
                            .clickable { datePickerDialog.show() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fecha del registro: ${if (activityDate.isNotBlank()) activityDate else "Hoy"}",
                                style = Typography.bodyMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    SleepManualRegistrationForm(
                        baby = baby,
                        activityDateStr = activityDate,
                        onDateChange = { activityDate = it },
                        startTime = sleepStartTime,
                        onStartTimeChange = { sleepStartTime = it },
                        endTime = sleepEndTime,
                        onEndTimeChange = { sleepEndTime = it },
                        notes = sleepNotes,
                        onNotesChange = { sleepNotes = it }
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (selectedActivityType) {
                        "comida" -> {
                            Text("Registro de Comida", style = Typography.headlineMedium, color = PrimaryDark)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CreamBg)
                                    .clickable { datePickerDialog.show() }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fecha del registro: ${if (activityDate.isNotBlank()) activityDate else "Hoy"}",
                                        style = Typography.bodyMedium,
                                        color = PrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }

                            Text("Categoría de comida:", style = Typography.labelLarge)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Desayuno", "Almuerzo", "Comida").forEach { cat ->
                                        FilterChip(
                                            selected = mealType == cat,
                                            onClick = { mealType = cat },
                                            label = { Text(cat, maxLines = 1, softWrap = false, style = Typography.labelSmall) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MintPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Merienda", "Cena").forEach { cat ->
                                        FilterChip(
                                            selected = mealType == cat,
                                            onClick = { mealType = cat },
                                            label = { Text(cat, maxLines = 1, softWrap = false, style = Typography.labelSmall) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MintPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            PMOutlinedTextField(
                                value = foodName,
                                onValueChange = { foodName = it },
                                label = { Text("Alimentos:") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "panal" -> {
                            Text("Registro de Pañal", style = Typography.headlineMedium, color = PrimaryDark)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CreamBg)
                                    .clickable { datePickerDialog.show() }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fecha del registro: ${if (activityDate.isNotBlank()) activityDate else "Hoy"}",
                                        style = Typography.bodyMedium,
                                        color = PrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }

                            Text("Estado del pañal:", style = Typography.labelLarge)
                            val conditions = listOf("Seco", "Mojado", "Sucio", "Ambos")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                conditions.forEach { cond ->
                                    Button(
                                        onClick = { diaperCondition = cond },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (diaperCondition == cond) MintPrimary else CreamBg,
                                            contentColor = if (diaperCondition == cond) Color.White else TextDark
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(cond, style = Typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cantidad de pañales:", style = Typography.labelLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (diaperQuantity > 1) diaperQuantity-- }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Menos", tint = PrimaryDark)
                                    }
                                    Text(
                                        text = diaperQuantity.toString(),
                                        style = Typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDark,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    IconButton(onClick = { diaperQuantity++ }) {
                                        Icon(Icons.Default.Add, contentDescription = "Más", tint = PrimaryDark)
                                    }
                                }
                            }

                            PMOutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notas adicionales") },
                                placeholder = { Text("Ej. Comió con buen apetito...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                when (selectedActivityType) {
                    "comida" -> {
                        viewModel.addMealRecord(mealType, foodName, notes)
                        foodName = ""
                        notes = ""
                    }
                    "sueno" -> {
                        viewModel.addManualSleepRecord(activityDate, sleepStartTime, sleepEndTime, sleepNotes)
                        sleepStartTime = "20:00"
                        sleepEndTime = "07:00"
                        sleepNotes = ""
                    }
                    "panal" -> {
                        viewModel.addDiaperRecord(diaperCondition, notes, activityDate, diaperQuantity)
                        notes = ""
                        diaperQuantity = 1
                    }
                }
                viewModel.currentTab.value = "Home"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(27.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Registro", style = Typography.headlineSmall)
        }
    }
}

@Composable
fun SaludTabContent(viewModel: BabyViewModel) {
    val context = LocalContext.current
    var selectedHealthSubTab by remember { mutableStateOf("peso_altura") }

    var inputWeight by remember { mutableStateOf("") }
    var inputHeight by remember { mutableStateOf("") }

    var vaccineName by remember { mutableStateOf("") }
    var vaccineNotes by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf("") }
    var vaccineStatus by remember { mutableStateOf("Vacunado completamente") }

    var appointmentPediatra by remember { mutableStateOf("") }
    val defaultApptDate = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
    }
    var appointmentDate by remember { mutableStateOf(defaultApptDate) }
    var appointmentTime by remember { mutableStateOf("10:00") }
    var hasReminder by remember { mutableStateOf(true) }

    val vaccineDatePickerDialog = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                vaccineDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val appointmentDatePickerDialog = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                appointmentDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val appointmentTimePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                appointmentTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            },
            10,
            0,
            true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedHealthSubTab == "peso_altura",
                onClick = { selectedHealthSubTab = "peso_altura" },
                label = { Text("Peso / Altura") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedHealthSubTab == "vacunas",
                onClick = { selectedHealthSubTab = "vacunas" },
                label = { Text("Vacunas") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedHealthSubTab == "citas",
                onClick = { selectedHealthSubTab = "citas" },
                label = { Text("Citas Médicas") },
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (selectedHealthSubTab) {
                    "peso_altura" -> {
                        Text("Registrar Peso y Altura", style = Typography.headlineMedium, color = PrimaryDark)

                        PMOutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text("Peso (kg) (Ej: 6.20)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        PMOutlinedTextField(
                            value = inputHeight,
                            onValueChange = { inputHeight = it },
                            label = { Text("Altura (cm) (Ej: 61.0)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "vacunas" -> {
                        Text("Registrar Vacuna", style = Typography.headlineMedium, color = PrimaryDark, fontWeight = FontWeight.Bold)

                        PMOutlinedTextField(
                            value = vaccineName,
                            onValueChange = { vaccineName = it },
                            label = { Text("Nombre de la Vacuna (Ej: Rotavirus, Meningococo)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Estado de la vacuna:", style = Typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
                        val statusOptions = listOf(
                            Triple("Falta por vacunar", Icons.Default.Schedule, Color(0xFFD9534F)),
                            Triple("Pendiente más tomas", Icons.Default.Pending, Color(0xFFE67E22)),
                            Triple("Vacunado completamente", Icons.Default.CheckCircle, MintPrimary)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            statusOptions.forEach { (statusTitle, statusIcon, statusColor) ->
                                val isSelected = vaccineStatus == statusTitle
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vaccineStatus = statusTitle },
                                    color = if (isSelected) statusColor.copy(alpha = 0.12f) else SurfaceMuted.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) statusColor else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) statusColor else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = statusTitle,
                                            style = Typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryDark else TextDark
                                        )
                                    }
                                }
                            }
                        }

                        Text("Fecha (opcional):", style = Typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vaccineDatePickerDialog.show() },
                            color = SurfaceMuted.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendario",
                                        tint = MintPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (vaccineDate.isNotBlank()) vaccineDate else "Sin fecha asignada (Toca para elegir)",
                                        style = Typography.bodyMedium,
                                        color = if (vaccineDate.isNotBlank()) PrimaryDark else TextMuted,
                                        fontWeight = if (vaccineDate.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (vaccineDate.isNotBlank()) {
                                    IconButton(
                                        onClick = { vaccineDate = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Borrar fecha",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MintPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Abrir Calendario",
                                            style = Typography.labelSmall,
                                            color = MintPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PMOutlinedTextField(
                            value = vaccineNotes,
                            onValueChange = { vaccineNotes = it },
                            label = { Text("Detalles o notas adicionales (opcional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    }

                    "citas" -> {
                        Text("Registrar Cita Médica", style = Typography.headlineMedium, color = PrimaryDark, fontWeight = FontWeight.Bold)

                        PMOutlinedTextField(
                            value = appointmentPediatra,
                            onValueChange = { appointmentPediatra = it },
                            label = { Text("Especialidad / Pediatra (Ej: Pediatría revisión 6 meses)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Fecha de la cita:", style = Typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { appointmentDatePickerDialog.show() },
                            color = SurfaceMuted.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                        .size(36.dp)
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
                                            text = "Fecha seleccionada",
                                            style = Typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = appointmentDate.ifBlank { "Toca para elegir fecha" },
                                            style = Typography.bodyLarge,
                                            color = PrimaryDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Surface(
                                    color = MintPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Abrir Calendario",
                                            style = Typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Text("Hora de la cita:", style = Typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { appointmentTimePickerDialog.show() },
                            color = SurfaceMuted.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = "Hora",
                                        tint = MintPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = appointmentTime.ifBlank { "Seleccionar hora" },
                                        style = Typography.bodyLarge,
                                        color = PrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "Schedules",
                                            tint = MintPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Cambiar hora",
                                            style = Typography.labelSmall,
                                            color = PrimaryDark,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recordatorio automático", style = Typography.labelLarge)
                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MintPrimary)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                when (selectedHealthSubTab) {
                    "peso_altura" -> {
                        val parsedWeight = inputWeight.replace(',', '.').toDoubleOrNull()
                        val parsedHeight = inputHeight.replace(',', '.').toDoubleOrNull()
                        if (parsedWeight != null) viewModel.addWeightRecord(parsedWeight)
                        if (parsedHeight != null) viewModel.addHeightRecord(parsedHeight)
                        Toast.makeText(context, "Medición registrada con éxito", Toast.LENGTH_SHORT).show()
                    }
                    "vacunas" -> {
                        viewModel.addVaccineRecord(
                            name = vaccineName,
                            notes = vaccineNotes,
                            dateString = vaccineDate.ifBlank { null },
                            status = vaccineStatus
                        )
                        Toast.makeText(context, "¡Vacuna registrada con éxito! 💉", Toast.LENGTH_SHORT).show()
                        vaccineName = ""
                        vaccineNotes = ""
                        vaccineDate = ""
                        vaccineStatus = "Vacunado completamente"
                    }
                    "citas" -> {
                        if (appointmentPediatra.isNotBlank() && appointmentDate.isNotBlank()) {
                            viewModel.addAppointmentRecord(
                                pediatra = appointmentPediatra,
                                dateString = appointmentDate,
                                timeString = if (appointmentTime.isBlank()) "10:00" else appointmentTime,
                                hasReminder = hasReminder
                            )
                            Toast.makeText(context, "¡Cita médica guardada con recordatorio! 📅", Toast.LENGTH_SHORT).show()
                            appointmentPediatra = ""
                        }
                    }
                }
                viewModel.currentTab.value = "Home"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(27.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Registro", style = Typography.headlineSmall)
        }
    }
}

@Composable
fun QuickMealRegistrationDialog(
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, mealType: String, foodName: String, notes: String) -> Unit
) {
    var mealType by remember { mutableStateOf("Comida") }
    var foodName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val context = LocalContext.current
    var dateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    val datePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(chosenCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape).background(MintPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Registro de Comida", style = Typography.headlineMedium, color = PrimaryDark, fontWeight = FontWeight.Bold)
                            Text("Añade la alimentación de tu bebé", style = Typography.bodySmall, color = TextMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CreamBg).clickable { datePickerDialog.show() }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fecha: $dateStr", style = Typography.bodyMedium, color = PrimaryDark, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                Text("Categoría:", style = Typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Desayuno", "Almuerzo", "Comida").forEach { cat ->
                            FilterChip(
                                selected = mealType == cat,
                                onClick = { mealType = cat },
                                label = { Text(cat, style = Typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MintPrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Merienda", "Cena").forEach { cat ->
                            FilterChip(
                                selected = mealType == cat,
                                onClick = { mealType = cat },
                                label = { Text(cat, style = Typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MintPrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }

                PMOutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Alimentos:") },
                    modifier = Modifier.fillMaxWidth()
                )

                PMOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("Cancelar", color = TextDark)
                    }
                    Button(
                        onClick = { onConfirm(dateStr, mealType, foodName, notes) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickDiaperRegistrationDialog(
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, condition: String, quantity: Int, notes: String) -> Unit
) {
    var condition by remember { mutableStateOf("Seco") }
    var quantity by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }
    val context = LocalContext.current
    var dateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    val datePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(chosenCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp).wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape).background(PeachWarm.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = PeachWarm, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Registro de Pañal", style = Typography.headlineMedium, color = PrimaryDark, fontWeight = FontWeight.Bold)
                            Text("Cambio de pañal y estado", style = Typography.bodySmall, color = TextMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CreamBg).clickable { datePickerDialog.show() }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fecha: $dateStr", style = Typography.bodyMedium, color = PrimaryDark, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                Text("Estado del pañal:", style = Typography.labelLarge)
                val conditions = listOf("Seco", "Mojado", "Sucio", "Ambos")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    conditions.forEach { cond ->
                        Button(
                            onClick = { condition = cond },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (condition == cond) MintPrimary else CreamBg,
                                contentColor = if (condition == cond) Color.White else TextDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(cond, style = Typography.labelSmall, maxLines = 1)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cantidad de pañales:", style = Typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos", tint = PrimaryDark)
                        }
                        Text(text = quantity.toString(), style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = PrimaryDark, modifier = Modifier.padding(horizontal = 12.dp))
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Más", tint = PrimaryDark)
                        }
                    }
                }

                PMOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("Cancelar", color = TextDark)
                    }
                    Button(
                        onClick = { onConfirm(dateStr, condition, quantity, notes) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SleepManualRegistrationForm(
    baby: BabyProfile,
    activityDateStr: String,
    onDateChange: (String) -> Unit,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val context = LocalContext.current
    var localDate by remember { mutableStateOf(activityDateStr) }
    var localStartTime by remember { mutableStateOf(startTime) }
    var localEndTime by remember { mutableStateOf(endTime) }
    var localNotes by remember { mutableStateOf(notes) }

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                localDate = sdf.format(cal.time)
                onDateChange(localDate)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    val startTimePicker = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                localStartTime = formatted
                onStartTimeChange(formatted)
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            false
        )
    }

    val endTimePicker = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                localEndTime = formatted
                onEndTimeChange(formatted)
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            false
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CreamBg).clickable { datePicker.show() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fecha del registro: ${if (localDate.isNotBlank()) localDate else "Hoy"}",
                    style = Typography.bodyMedium,
                    color = PrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(CreamBg).clickable { startTimePicker.show() }.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inicio: $localStartTime",
                        style = Typography.bodyMedium,
                        color = PrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(CreamBg).clickable { endTimePicker.show() }.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fin: $localEndTime",
                        style = Typography.bodyMedium,
                        color = PrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        PMOutlinedTextField(
            value = localNotes,
            onValueChange = { localNotes = it },
            label = { Text("Notas adicionales") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SleepManualRegistrationDialog(
    baby: BabyProfile,
    initialStartTime: String? = null,
    initialEndTime: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, startTime: String, endTime: String, notes: String) -> Unit
) {
    var startTime by remember { mutableStateOf(initialStartTime ?: "20:00") }
    var endTime by remember { mutableStateOf(initialEndTime ?: "07:00") }
    var notes by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFF1E5B94),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Registrar Sueño",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Horas y duración de descanso",
                                style = Typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SleepManualRegistrationForm(
                    baby = baby,
                    activityDateStr = dateStr,
                    onDateChange = { dateStr = it },
                    startTime = startTime,
                    onStartTimeChange = { startTime = it },
                    endTime = endTime,
                    onEndTimeChange = { endTime = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = TextDark)
                    }
                    Button(
                        onClick = { onConfirm(dateStr, startTime, endTime, notes) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        }
    }
}
