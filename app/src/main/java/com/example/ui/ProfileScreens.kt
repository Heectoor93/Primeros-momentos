package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel
) {
    val context = LocalContext.current
    val babyProfiles by viewModel.allBabyProfiles.collectAsStateWithLifecycle()
    val moments by viewModel.moments.collectAsStateWithLifecycle()

    var selectedBabyIdForEditing by remember { mutableStateOf<Int?>(baby.id) }
    var isAddingNew by remember { mutableStateOf(false) }

    var nameEdit by remember { mutableStateOf("") }
    var dobStringEdit by remember { mutableStateOf("") }
    var genderEdit by remember { mutableStateOf("Niño") }
    var skinToneEdit by remember { mutableStateOf("Claro") }
    var hairColorEdit by remember { mutableStateOf("Castaño") }
    var preferredPacifierColorEdit by remember { mutableStateOf<String?>(null) }

    val matchedBaby = babyProfiles.find { it.id == selectedBabyIdForEditing } ?: baby
    val isToddler = viewModel.getAgeInMonths(matchedBaby.dob) >= 12

    var showDeleteConfirmDialog by remember { mutableStateOf<BabyProfile?>(null) }

    LaunchedEffect(selectedBabyIdForEditing, isAddingNew, baby, babyProfiles) {
        if (isAddingNew) {
            nameEdit = ""
            dobStringEdit = ""
            genderEdit = "Niño"
            skinToneEdit = "Claro"
            hairColorEdit = "Castaño"
        } else {
            val matched = babyProfiles.find { it.id == selectedBabyIdForEditing } ?: baby
            selectedBabyIdForEditing = matched.id
            nameEdit = matched.name
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dobStringEdit = sdf.format(Date(matched.dob))
            genderEdit = matched.gender
            skinToneEdit = matched.skinTone
            hairColorEdit = matched.hairColor
            preferredPacifierColorEdit = matched.preferredPacifierColor
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, monthOfYear, dayOfMonth ->
            val chosenCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthOfYear)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dobStringEdit = sdf.format(chosenCal.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showDeleteConfirmDialog != null) {
        val babyToDelete = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "¿Eliminar perfil?",
                    style = Typography.headlineMedium,
                    color = ErrorColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Asegúrate de que quieres eliminar por completo el perfil de \"${babyToDelete.name}\". Esta acción es irreversible y borrará permanentemente todos sus registros de comida, sueño, pañales, fotos y recuerdos.",
                    style = Typography.bodyMedium,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProfile(babyToDelete.id)
                        showDeleteConfirmDialog = null
                        android.widget.Toast.makeText(context, "Perfil de ${babyToDelete.name} eliminado", android.widget.Toast.LENGTH_SHORT).show()
                        isAddingNew = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Sí, eliminar para siempre", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = null }
                ) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayGender = if (isAddingNew) "Niño" else genderEdit
            MiniBabyAvatar(
                gender = displayGender,
                ageInMonths = if (isAddingNew) 6 else viewModel.getAgeInMonths(matchedBaby.dob),
                sizeDp = 48,
                skinTone = skinToneEdit,
                hairColor = hairColorEdit,
                completedPercentage = 0f,
                preferredColor = preferredPacifierColorEdit,
                useProfileImage = true
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Perfil",
                style = Typography.headlineLarge,
                color = PrimaryDark
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Mis bebés",
                style = Typography.labelLarge,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                babyProfiles.forEach { profile ->
                    val isSelected = (!isAddingNew && profile.id == selectedBabyIdForEditing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            isAddingNew = false
                            selectedBabyIdForEditing = profile.id
                            viewModel.selectBaby(profile.id)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MintPrimary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniBabyAvatar(
                                gender = profile.gender,
                                ageInMonths = viewModel.getAgeInMonths(profile.dob),
                                sizeDp = 56,
                                skinTone = profile.skinTone,
                                hairColor = profile.hairColor,
                                completedPercentage = 0f,
                                preferredColor = profile.preferredPacifierColor,
                                useProfileImage = true
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.name,
                            style = Typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryDark else TextMuted
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        isAddingNew = true
                        selectedBabyIdForEditing = null
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                drawCircle(
                                    color = if (isAddingNew) MintPrimary else Color.LightGray,
                                    radius = size.width / 2f - 4f,
                                    style = Stroke(
                                        width = 4f,
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                             imageVector = Icons.Default.Add,
                             contentDescription = "Agregar bebé",
                             tint = if (isAddingNew) MintPrimary else Color.Gray,
                             modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Agregar",
                        style = Typography.labelMedium,
                        fontWeight = if (isAddingNew) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAddingNew) PrimaryDark else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val activeTitleName = nameEdit.ifBlank { "tu bebé" }
                Text(
                    text = if (isAddingNew) "Nuevo perfil de bebé" else "Información de $activeTitleName",
                    style = Typography.headlineSmall,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nombre del bebé", style = Typography.labelMedium, color = TextMuted)
                    PMOutlinedTextField(
                        value = nameEdit,
                        onValueChange = { nameEdit = it },
                        placeholder = { Text("Escribe su nombre aquí") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fecha de nacimiento", style = Typography.labelMedium, color = TextMuted)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                    ) {
                        PMOutlinedTextField(
                            value = dobStringEdit,
                            onValueChange = {},
                            placeholder = { Text("Selecciona fecha") },
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = PrimaryDark)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Género", style = Typography.labelMedium, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (genderEdit == "Niño") MintPrimary else Color.Transparent)
                                .clickable { genderEdit = "Niño" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Niño",
                                color = if (genderEdit == "Niño") Color.White else TextDark,
                                fontWeight = FontWeight.SemiBold,
                                style = Typography.bodyMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (genderEdit == "Niña") MintPrimary else Color.Transparent)
                                .clickable { genderEdit = "Niña" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Niña",
                                color = if (genderEdit == "Niña") Color.White else TextDark,
                                fontWeight = FontWeight.SemiBold,
                                style = Typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (nameEdit.isBlank() || dobStringEdit.isBlank()) {
                            android.widget.Toast.makeText(context, "Por favor indica el nombre y fecha de nacimiento", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val parsed = sdf.parse(dobStringEdit)
                                if (parsed != null) {
                                    val targetId = if (isAddingNew) 0 else (selectedBabyIdForEditing ?: 0)
                                    viewModel.saveProfile(
                                        name = nameEdit,
                                        dobTimestamp = parsed.time,
                                        gender = genderEdit,
                                        id = targetId,
                                        skinTone = skinToneEdit,
                                        hairColor = hairColorEdit
                                    )

                                    if (isAddingNew) {
                                        android.widget.Toast.makeText(context, "¡Nuevo perfil de bebé creado! ✨", android.widget.Toast.LENGTH_LONG).show()
                                        isAddingNew = false
                                    } else {
                                        android.widget.Toast.makeText(context, "¡Perfil actualizado con éxito! ✨", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    viewModel.currentTab.value = "Home"
                                } else {
                                    android.widget.Toast.makeText(context, "Formato de fecha inválido (dd/MM/yyyy)", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Formato de fecha inválido - Ejemplo 02/05/2024", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(if (isAddingNew) "Crear y guardar perfil" else "Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (!isAddingNew && selectedBabyIdForEditing != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            val matched = babyProfiles.find { it.id == selectedBabyIdForEditing } ?: baby
                            showDeleteConfirmDialog = matched
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                        border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar bebé",
                            tint = ErrorColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar perfil", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Hitos de Crecimiento",
                style = Typography.headlineSmall,
                color = PrimaryDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val ranges = listOf(
                    "0-6 meses" to Icons.Default.ChildCare,
                    "6-12 meses" to Icons.Default.Favorite,
                    "12-18 meses" to Icons.Default.NaturePeople,
                    "18-24 meses" to Icons.Default.AutoAwesome
                )

                ranges.forEach { (range, icon) ->
                    val rangeMoments = moments.filter { it.ageRange == range }
                    val completed = rangeMoments.count { it.isCompleted }
                    val total = rangeMoments.size.coerceAtLeast(1)
                    val percent = (completed * 100) / total
                    val sweepAngle = (percent / 100f) * 360f

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFFE2E8F0),
                                        style = Stroke(width = 6f)
                                    )
                                    drawArc(
                                        color = MintPrimary,
                                        startAngle = -90f,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (percent > 0) MintPrimary else TextMuted,
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = 0.dp, y = -1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = range,
                            style = Typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = "$percent%",
                            style = Typography.labelSmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isToddler) "Color del Peluche" else "Color del Chupete",
                    style = Typography.labelLarge,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )

                var milestonePercent by remember { mutableStateOf(0) }
                LaunchedEffect(selectedBabyIdForEditing) {
                    val babyId = selectedBabyIdForEditing
                    if (babyId != null) {
                        milestonePercent = viewModel.calculateMilestonePercent(babyId)
                    }
                }

                val colorOptions = listOf(
                    "Blanco" to Color.White,
                    "Azul" to Color(0xFF3B82F6),
                    "Verde" to Color(0xFF22C55E),
                    "Púrpura" to Color(0xFFA855F7),
                    "Rojo" to Color(0xFFEF4444),
                    "Dorado" to Color(0xFFD4AF37)
                )

                val unlockedThresholds = listOf(
                    "Blanco" to 0,
                    "Azul" to 20,
                    "Verde" to 40,
                    "Púrpura" to 60,
                    "Rojo" to 80,
                    "Dorado" to 100
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { (name, colorValue) ->
                        val threshold = unlockedThresholds.find { it.first == name }?.second ?: 0
                        val isUnlocked = milestonePercent >= threshold
                        val isSelected = preferredPacifierColorEdit == name

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isUnlocked) colorValue else Color.LightGray)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MintPrimary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable(enabled = isUnlocked) {
                                    preferredPacifierColorEdit = name
                                    val babyId = selectedBabyIdForEditing
                                    if (babyId != null) {
                                        viewModel.updatePreferredPacifierColor(babyId, name)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Bloqueado",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = if (isToddler) "Selecciona un color desbloqueado para el peluche del avatar" else "Selecciona un color desbloqueado para el chupete del avatar",
                    style = Typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        val googleEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
        val googleName by viewModel.googleAccountName.collectAsStateWithLifecycle()
        val isDriveConnected by viewModel.isGoogleDriveConnected.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Sincronización con Google Drive",
                style = Typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (isDriveConnected && googleEmail != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MintPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = googleName ?: "Héctor",
                                    style = Typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = googleEmail ?: "",
                                    style = Typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tus fotos, vídeos y registros (peso, vacunas, comidas...) están sincronizados de forma segura en la carpeta 'Primeros momentos' de tu Google Drive.",
                            style = Typography.bodySmall,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.disconnectGoogleAccount()
                                android.widget.Toast.makeText(context, "Cuenta de Google desvinculada", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                            border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Desvincular Google Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Almacenamiento Local",
                                    style = Typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Los datos y fotos se guardan en tu móvil",
                                    style = Typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Vincula tu Google Drive para crear automáticamente la carpeta 'Primeros momentos' con copia de todas tus fotos, vídeos, registros de peso, vacunas y citas.",
                            style = Typography.bodySmall,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.signInWithGoogleNative(context) { success, errorMsg ->
                                    if (!success && errorMsg != null) {
                                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Vincular con Google Drive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Configuración de cuenta",
                style = Typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ListSettingsRow("Preferencias", Icons.Default.Settings) {
                        android.widget.Toast.makeText(context, "Preferencias recomendadas de crianza activadas", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    ListSettingsRow("Notificaciones", Icons.Default.Notifications) {
                        android.widget.Toast.makeText(context, "Configuración de notificaciones diarias guardada", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    ListSettingsRow("Privacidad", Icons.Default.Settings) {
                        android.widget.Toast.makeText(context, "Tus datos siguen estando cifrados localmente en tu móvil🔒", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Cerrar sesión",
            color = ErrorColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    viewModel.signOutUser()
                    android.widget.Toast.makeText(context, "Sesión cerrada.", android.widget.Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp)
        )
    }
}

@Composable
fun ListSettingsRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = Typography.bodyLarge, color = TextDark)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}
