package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.Shape
import com.example.ui.theme.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*

fun getElapsedTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "hace ${seconds}s"
        minutes < 60 -> "hace ${minutes}m"
        hours < 24 -> "hace ${hours}h ${minutes % 60}m"
        else -> "hace ${days}d"
    }
}

val PremiumBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE5F4F0), // Soft cream-mint top
        Color(0xFFF9FCFB), // Pristine light middle
        Color(0xFFFCFDFB)  // Soft warm base
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PMOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp),
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val mergedKeyboardOptions = if (keyboardOptions.imeAction == ImeAction.Default) {
        keyboardOptions.copy(imeAction = ImeAction.Done)
    } else {
        keyboardOptions
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = mergedKeyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onNext = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onSearch = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onGo = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onSend = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            onPrevious = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        singleLine = singleLine,
        shape = shape,
        readOnly = readOnly,
        enabled = enabled,
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedBorderColor = MintPrimary,
            unfocusedBorderColor = MintPrimary.copy(alpha = 0.3f),
            disabledBorderColor = MintPrimary.copy(alpha = 0.3f),
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
            disabledLabelColor = Color.Black.copy(alpha = 0.6f),
            focusedPlaceholderColor = Color.Black.copy(alpha = 0.4f),
            unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.4f),
            disabledPlaceholderColor = Color.Black.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun MiniBabyAvatar(
    gender: String,
    ageInMonths: Int,
    sizeDp: Int,
    skinTone: String,
    hairColor: String,
    completedPercentage: Float,
    preferredColor: String?,
    useProfileImage: Boolean = false
) {
    val baseScale = 0.85f + (ageInMonths * 0.015f).coerceAtMost(0.25f)

    val babyDrawableRes = if (gender == "Niña") {
        when {
            preferredColor == "Dorado" && completedPercentage >= 1.0f -> com.example.R.drawable.img_baby_girl_pacifier_gold
            preferredColor == "Rojo" && completedPercentage >= 0.8f -> com.example.R.drawable.img_baby_girl_pacifier_red
            preferredColor == "Púrpura" && completedPercentage >= 0.6f -> com.example.R.drawable.img_baby_girl_pacifier_purple
            preferredColor == "Verde" && completedPercentage >= 0.4f -> com.example.R.drawable.img_baby_girl_pacifier_green
            preferredColor == "Azul" && completedPercentage >= 0.2f -> com.example.R.drawable.img_baby_girl_pacifier_blue
            else -> com.example.R.drawable.img_baby_girl_under_one_1780948526412
        }
    } else {
        when {
            preferredColor == "Dorado" && completedPercentage >= 1.0f -> com.example.R.drawable.img_baby_boy_pacifier_gold
            preferredColor == "Rojo" && completedPercentage >= 0.8f -> com.example.R.drawable.img_baby_boy_pacifier_red
            preferredColor == "Púrpura" && completedPercentage >= 0.6f -> com.example.R.drawable.img_baby_boy_pacifier_purple
            preferredColor == "Verde" && completedPercentage >= 0.4f -> com.example.R.drawable.img_baby_boy_pacifier_green
            preferredColor == "Azul" && completedPercentage >= 0.2f -> com.example.R.drawable.img_baby_boy_pacifier_blue
            else -> com.example.R.drawable.img_baby_boy_under_one_1780845155346
        }
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(SurfaceWhite),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = babyDrawableRes),
            contentDescription = "Mini Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun InteractiveBabyAvatar(
    gender: String,
    ageInMonths: Int,
    skinTone: String,
    hairColor: String,
    pacifierColor: Color,
    currentMilestonePercent: Int = 0,
    preferredPacifierColorName: String? = null,
    onClick: () -> Unit
) {
    val baseScale = 0.85f + (ageInMonths * 0.015f).coerceAtMost(0.25f)

    val skinToneColor = when (skinTone) {
        "Moreno" -> Color(0xFFE5A65D)
        "Oscuro" -> Color(0xFF81523F)
        else -> Color(0xFFFFE4D6) // "Claro"
    }
    val earColor = when (skinTone) {
        "Moreno" -> Color(0xFFD5944B)
        "Oscuro" -> Color(0xFF704332)
        else -> Color(0xFFFFD5C2) // "Claro"
    }
    val hairColorValue = when (hairColor) {
        "Rubio" -> Color(0xFFF4D068)
        "Pelirrojo" -> Color(0xFFE67E22)
        "Sin pelo" -> Color.Transparent
        else -> Color(0xFF6B4226) // "Castaño"
    }

    val rabbitColor = when {
        currentMilestonePercent < 30 -> Color(0xFFBDBDBD) // Grayish
        currentMilestonePercent < 60 -> Color(0xFFF8BBD0) // Pale Pink
        else -> MintPrimary
    }

    Box(
        modifier = Modifier
            .size(240.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (ageInMonths < 12) {
            Box(
                modifier = Modifier
                    .size((220 * baseScale).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (gender == "Niña") Color(0xFFFFCAD4).copy(alpha = 0.35f) else MintPrimary.copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset.Zero,
                                radius = 200f
                            )
                        )
                )

                val babyDrawableRes = if (gender == "Niña") {
                    when {
                        preferredPacifierColorName == "Dorado" && currentMilestonePercent >= 100 -> com.example.R.drawable.img_baby_girl_pacifier_gold
                        preferredPacifierColorName == "Rojo" && currentMilestonePercent >= 80 -> com.example.R.drawable.img_baby_girl_pacifier_red
                        preferredPacifierColorName == "Púrpura" && currentMilestonePercent >= 60 -> com.example.R.drawable.img_baby_girl_pacifier_purple
                        preferredPacifierColorName == "Verde" && currentMilestonePercent >= 40 -> com.example.R.drawable.img_baby_girl_pacifier_green
                        preferredPacifierColorName == "Azul" && currentMilestonePercent >= 20 -> com.example.R.drawable.img_baby_girl_pacifier_blue
                        preferredPacifierColorName == "Blanco" -> com.example.R.drawable.img_baby_girl_under_one_1780948526412
                        currentMilestonePercent >= 100 -> com.example.R.drawable.img_baby_girl_pacifier_gold
                        currentMilestonePercent >= 80 -> com.example.R.drawable.img_baby_girl_pacifier_red
                        currentMilestonePercent >= 60 -> com.example.R.drawable.img_baby_girl_pacifier_purple
                        currentMilestonePercent >= 40 -> com.example.R.drawable.img_baby_girl_pacifier_green
                        currentMilestonePercent >= 20 -> com.example.R.drawable.img_baby_girl_pacifier_blue
                        else -> com.example.R.drawable.img_baby_girl_under_one_1780948526412
                    }
                } else {
                    when {
                        preferredPacifierColorName == "Dorado" && currentMilestonePercent >= 100 -> com.example.R.drawable.img_baby_boy_pacifier_gold
                        preferredPacifierColorName == "Rojo" && currentMilestonePercent >= 80 -> com.example.R.drawable.img_baby_boy_pacifier_red
                        preferredPacifierColorName == "Púrpura" && currentMilestonePercent >= 60 -> com.example.R.drawable.img_baby_boy_pacifier_purple
                        preferredPacifierColorName == "Verde" && currentMilestonePercent >= 40 -> com.example.R.drawable.img_baby_boy_pacifier_green
                        preferredPacifierColorName == "Azul" && currentMilestonePercent >= 20 -> com.example.R.drawable.img_baby_boy_pacifier_blue
                        preferredPacifierColorName == "Blanco" -> com.example.R.drawable.img_baby_boy_under_one_1780845155346
                        currentMilestonePercent >= 100 -> com.example.R.drawable.img_baby_boy_pacifier_gold
                        currentMilestonePercent >= 80 -> com.example.R.drawable.img_baby_boy_pacifier_red
                        currentMilestonePercent >= 60 -> com.example.R.drawable.img_baby_boy_pacifier_purple
                        currentMilestonePercent >= 40 -> com.example.R.drawable.img_baby_boy_pacifier_green
                        currentMilestonePercent >= 20 -> com.example.R.drawable.img_baby_boy_pacifier_blue
                        else -> com.example.R.drawable.img_baby_boy_under_one_1780845155346
                    }
                }

                Image(
                    painter = painterResource(id = babyDrawableRes),
                    contentDescription = if (gender == "Niña") "Bebé Niña" else "Bebé Martín",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            val toddlerDrawableRes = when {
                preferredPacifierColorName == "Dorado" && currentMilestonePercent >= 100 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_lion else com.example.R.drawable.img_baby_toddler_lion
                preferredPacifierColorName == "Rojo" && currentMilestonePercent >= 80 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_crab else com.example.R.drawable.img_baby_toddler_crab
                preferredPacifierColorName == "Púrpura" && currentMilestonePercent >= 60 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_octopus else com.example.R.drawable.img_baby_toddler_octopus
                preferredPacifierColorName == "Verde" && currentMilestonePercent >= 40 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_turtle else com.example.R.drawable.img_baby_toddler_turtle
                preferredPacifierColorName == "Azul" && currentMilestonePercent >= 20 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_dolphin else com.example.R.drawable.img_baby_toddler_dolphin
                preferredPacifierColorName == "Blanco" -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_rabbit else com.example.R.drawable.img_baby_toddler_rabbit
                currentMilestonePercent >= 100 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_lion else com.example.R.drawable.img_baby_toddler_lion
                currentMilestonePercent >= 80 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_crab else com.example.R.drawable.img_baby_toddler_crab
                currentMilestonePercent >= 60 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_octopus else com.example.R.drawable.img_baby_toddler_octopus
                currentMilestonePercent >= 40 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_turtle else com.example.R.drawable.img_baby_toddler_turtle
                currentMilestonePercent >= 20 -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_dolphin else com.example.R.drawable.img_baby_toddler_dolphin
                else -> if (gender == "Niña") com.example.R.drawable.img_baby_toddler_girl_rabbit else com.example.R.drawable.img_baby_toddler_rabbit
            }

            Box(
                modifier = Modifier.size((200 * baseScale).dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = toddlerDrawableRes),
                    contentDescription = "Bebé Mayor con animal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun BentoCard(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(108.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MintPrimary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = Typography.labelMedium,
                color = TextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CreamBg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = Typography.bodyMedium, color = TextMuted)
        }

        Text(text = value, style = Typography.labelLarge, color = color)
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf<Pair<String, ImageVector>>(
        "Home" to Icons.Default.Home,
        "Momentos" to Icons.Default.Favorite,
        "Registrar" to Icons.Default.AddCircle,
        "Seguimiento" to Icons.Default.Analytics,
        "Perfil" to Icons.Default.Person
    )

    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        tonalElevation = 0.dp
    ) {
        tabs.forEach { (tabName, icon) ->
            NavigationBarItem(
                selected = selectedTab == tabName,
                onClick = { onTabSelected(tabName) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tabName,
                        tint = if (selectedTab == tabName) MintPrimary else TextMuted
                    )
                },
                label = {
                    Text(
                        text = tabName,
                        style = Typography.labelSmall,
                        color = if (selectedTab == tabName) MintPrimary else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MintPrimary.copy(alpha = 0.15f),
                    selectedIconColor = MintPrimary,
                    selectedTextColor = MintPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
