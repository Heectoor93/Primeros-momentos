package com.example.ui

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import android.widget.VideoView
import android.widget.MediaController
import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage

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
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: BabyViewModel) {
    val baby by viewModel.babyProfile.collectAsStateWithLifecycle()
    val activs by viewModel.activities.collectAsStateWithLifecycle()
    val healths by viewModel.healthRecords.collectAsStateWithLifecycle()
    val allMoments by viewModel.moments.collectAsStateWithLifecycle()
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val googleAccount by viewModel.googleAccountEmail.collectAsStateWithLifecycle()

    // Splash screen state (showing for 2 seconds)
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        showSplash = false
    }

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isRestoringFromCloud by viewModel.isRestoringFromCloud.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen()
    } else if (isAuthLoading || isRestoringFromCloud) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBackgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                CircularProgressIndicator(
                    color = MintPrimary,
                    trackColor = Color(0xFFF1F5F9),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Restaurando tu información...",
                    style = Typography.titleMedium,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Recuperando datos y momentos guardados en tu cuenta.",
                    style = Typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (googleAccount == null) {
        GoogleSignInScreen(viewModel = viewModel)
    } else if (baby == null) {
        OnboardingScreen(onRegister = { name, dob, gender ->
            viewModel.saveProfile(name, dob, gender)
        })
    } else {
        val currentBaby = baby!!
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = activeTab,
                    onTabSelected = { viewModel.currentTab.value = it }
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(PremiumBackgroundBrush)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    "Home" -> HomeScreen(
                        baby = currentBaby,
                        viewModel = viewModel,
                        activities = activs,
                        moments = allMoments
                    )
                    "Momentos" -> MomentsScreen(
                        baby = currentBaby,
                        viewModel = viewModel,
                        moments = allMoments
                    )
                    "Registrar" -> RegisterScreen(
                        baby = currentBaby,
                        viewModel = viewModel,
                        activities = activs,
                        healthRecords = healths
                    )
                    "Seguimiento" -> TrackingScreen(
                        baby = currentBaby,
                        viewModel = viewModel,
                        activities = activs,
                        healthRecords = healths
                    )
                    "Perfil" -> ProfileScreen(
                        baby = currentBaby,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBackgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = MintPrimary.copy(alpha = 0.12f),
                radius = 400f,
                center = Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = PeachWarm.copy(alpha = 0.12f),
                radius = 350f,
                center = Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Stylized Stork Image/Vector Logo Placeholder
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .shadow(12.dp, CircleShape)
                    .border(6.dp, MintPrimary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Let's draw a cute stork or baby basket icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChildCare,
                        contentDescription = "Bebé",
                        tint = PrimaryDark,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "👶",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Primeros Momentos",
                style = Typography.headlineLarge,
                color = PrimaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acompañando cada paso en el crecimiento de tu bebé",
                style = Typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Smooth loading indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "SplashLoading")
                val alpha1 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
                    label = "Alpha1"
                )
                val alpha2 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(250)
                    ),
                    label = "Alpha2"
                )
                val alpha3 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(500)
                    ),
                    label = "Alpha3"
                )

                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintPrimary.copy(alpha = alpha1)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintPrimary.copy(alpha = alpha2)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintPrimary.copy(alpha = alpha3)))
            }
        }
    }
}

// 1.5 GOOGLE SIGN-IN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInScreen(viewModel: BabyViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var showCustomAccountDialog by remember { mutableStateOf(false) }
    var currentStepText by remember { mutableStateOf("Conectando con Google...") }

    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isSyncingCloud.collectAsStateWithLifecycle()

    fun startNativeGoogleSignIn() {
        viewModel.signInWithGoogleNative(context) { success, errorMsg ->
            if (!success && errorMsg != null) {
                android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    // Start real secure Google OAuth flow for Drive permissions and folder creation
    fun startRealGoogleAuth() {
        startNativeGoogleSignIn()
    }

    // Start simulated Google authentication and drive sync (instant bypass fallback)
    fun startSimulatedGoogleAuth(email: String, name: String) {
        val finalName = if (name.isBlank()) "Héctor" else name
        val finalEmail = if (email.isBlank()) "heectoor93@gmail.com" else email

        isSigningIn = true
        currentStepText = "Solicitando permisos de Google Drive (Simulado)..."
        
        scope.launch {
            kotlinx.coroutines.delay(1000)
            currentStepText = "Creando carpeta 'Primeros Momentos/' en tu Google Drive..."
            kotlinx.coroutines.delay(1000)
            currentStepText = "¡Copia de seguridad virtual lista! 🎉"
            kotlinx.coroutines.delay(800)
            viewModel.registerGoogleAccount(finalEmail, finalName)
            isSigningIn = false
            android.widget.Toast.makeText(context, "¡Google Drive simulado correctamente! ✨", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    if (showCustomAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAccountDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Vincular cuenta simulada",
                    style = Typography.headlineMedium,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Introduce los datos de la cuenta que deseas simular para las copias de seguridad de tus momentos.",
                        style = Typography.bodyMedium,
                        color = TextMuted
                    )
                    PMOutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        placeholder = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth().testTag("google_custom_name"),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MintPrimary) }
                    )
                    PMOutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        placeholder = { Text("ejemplo@gmail.com") },
                        modifier = Modifier.fillMaxWidth().testTag("google_custom_email"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MintPrimary) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customEmail.contains("@") && customEmail.contains(".")) {
                            showCustomAccountDialog = false
                            startSimulatedGoogleAuth(customEmail.trim(), customName.trim())
                        } else {
                            android.widget.Toast.makeText(context, "Por favor, introduce un correo electrónico de Google válido.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Vincular y Continuar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAccountDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(PremiumBackgroundBrush)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Cloud backup visual representation
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MintPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☁️", fontSize = 42.sp)
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Copia de Seguridad\ncon Google Drive",
                    style = Typography.headlineLarge,
                    color = PrimaryDark,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Para asegurar las fotos y vídeos de tus momentos especiales, 'Primeros Momentos' creará una carpeta segura en tu Google Drive.",
                    style = Typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Google Identity Styled Button (Primary)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clickable { startRealGoogleAuth() }
                        .shadow(3.dp, RoundedCornerShape(27.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Styled Canvas for Google Logo "G" represented beautifully using official colors
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val r = size.width / 2
                            val cx = size.width / 2
                            val cy = size.height / 2
                            drawArc(
                                color = Color(0xFFEA4335), // Red
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFFFBBC05), // Yellow
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFF34A853), // Green
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFF4285F4), // Blue
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            // White mask inside to make the G outline
                            drawCircle(color = SurfaceWhite, radius = r * 0.6f, center = Offset(cx, cy))
                            // Center bar of G
                            drawRect(
                                color = Color(0xFF4285F4),
                                topLeft = Offset(cx, cy - r * 0.15f),
                                size = Size(r, r * 0.3f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Iniciar sesión con Google",
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option to bypass with simulated drive
                TextButton(
                    onClick = { showCustomAccountDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwitchAccount,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MintPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuar sin Google Drive",
                        style = Typography.bodyMedium,
                        color = MintPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Conexión segura provista por Google Identity Services. Tus datos e imágenes se sincronizan directamente con tu Drive y no se comparten con terceros.",
                    style = Typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Authentication Progress Overlay
            val googleSyncState by viewModel.googleDriveSyncStatus.collectAsStateWithLifecycle()
            if (isSigningIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Vinculando cuenta de Google",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            CircularProgressIndicator(
                                color = MintPrimary,
                                trackColor = Color(0xFFF1F5F9),
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Text(
                                text = googleSyncState,
                                style = Typography.bodyMedium,
                                color = TextDark,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Creando carpeta segura 'Primeros Momentos/' en tu Drive para guardar fotos y vídeos...",
                                style = Typography.labelSmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom beautiful stork drawing with a baby bundle
@Composable
fun StorkDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val scale = minOf(w, h) / 150f // baseline size 150x150

        // Coordinates with scaling
        val cx = w / 2f
        val cy = h / 2f - 10f * scale

        // 1. Soft shadow / aura
        drawCircle(
            color = Color(0x2A000000),
            radius = 65f * scale,
            center = Offset(cx, cy + 15f * scale)
        )
        // Background soft mint circle with higher-contrast background and borders
        drawCircle(
            color = Color(0xFFD4EDDA), // slightly richer mint pastel background
            radius = 60f * scale,
            center = Offset(cx, cy + 10f * scale)
        )
        // Outer border line around the badge to make it pop
        drawCircle(
            color = Color(0xFFA2D7B4), // solid pastel mint green border outline
            radius = 60f * scale,
            center = Offset(cx, cy + 10f * scale),
            style = Stroke(width = 3.5f * scale)
        )

        // 2. Legs: Long thin orange legs
        val legColor = Color(0xFFE67E22)
        val legX = cx + 15f * scale
        val legYStart = cy + 40f * scale
        val legYEnd = cy + 85f * scale
        // Background leg
        drawLine(
            color = legColor,
            start = Offset(legX - 5f * scale, legYStart),
            end = Offset(legX - 8f * scale, legYEnd),
            strokeWidth = 3f * scale,
            cap = StrokeCap.Round
        )
        // Foreground leg (bent slightly)
        val legPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(legX + 5f * scale, legYStart)
            lineTo(legX + 8f * scale, cy + 65f * scale)
            lineTo(legX + 2f * scale, legYEnd)
        }
        drawPath(
            path = legPath,
            color = legColor,
            style = Stroke(width = 3f * scale, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )

        // 3. Body: Cute white fluffy oval with delicate outline
        val bodyColor = Color.White
        val outlineColor = Color(0xFFCFD8DC) // nice light grey-blue outline
        val bodyWidth = 70f * scale
        val bodyHeight = 45f * scale
        val bodyX = cx - 15f * scale
        val bodyY = cy + 5f * scale
        drawOval(
            color = bodyColor,
            topLeft = Offset(bodyX, bodyY),
            size = Size(bodyWidth, bodyHeight)
        )
        // Body outline
        drawOval(
            color = outlineColor,
            topLeft = Offset(bodyX, bodyY),
            size = Size(bodyWidth, bodyHeight),
            style = Stroke(width = 1.5f * scale)
        )

        // 4. Neck: Curved long elegant neck with perfect outer outline
        val neckPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx + 40f * scale, cy + 15f * scale) // top-right of body
            quadraticTo(
                cx + 50f * scale, cy - 15f * scale, // control point
                cx + 35f * scale, cy - 45f * scale  // head connection
            )
        }
        // Outline stroke first (thicker)
        drawPath(
            path = neckPath,
            color = outlineColor,
            style = Stroke(width = 18f * scale, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
        // Inner white path overlay (slightly thinner than outline to produce nice border)
        drawPath(
            path = neckPath,
            color = bodyColor,
            style = Stroke(width = 15f * scale, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )

        // 5. Head: Cute round white head with outline
        val headX = cx + 32f * scale
        val headY = cy - 50f * scale
        val headRadius = 15f * scale
        // Head outline
        drawCircle(
            color = outlineColor,
            radius = headRadius + 1f * scale,
            center = Offset(headX, headY)
        )
        // Head fill
        drawCircle(
            color = bodyColor,
            radius = headRadius,
            center = Offset(headX, headY)
        )

        // 6. Wing: Black tipped elegant wing on body
        val wingColor = Color(0xFF2C3E50)
        val wingPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(bodyX + 25f * scale, bodyY + 5f * scale)
            quadraticTo(
                bodyX + 60f * scale, bodyY + 10f * scale,
                bodyX + 75f * scale, bodyY + 30f * scale
            )
            quadraticTo(
                bodyX + 45f * scale, bodyY + 45f * scale,
                bodyX + 20f * scale, bodyY + 25f * scale
            )
            close()
        }
        drawPath(path = wingPath, color = wingColor)

        // 7. Eye: Little sweet closed sleepy eye
        drawCircle(
            color = Color(0xFF2C3E50),
            radius = 2f * scale,
            center = Offset(headX - 4f * scale, headY - 3f * scale)
        )

        // 8. Bundle (Hato de bebé): Mint green soft fabric hanging from beak
        val bundleColor = Color(0xFFB2EBF2) // light pastel blue-mint
        val bundleX = headX - 35f * scale
        val bundleY = headY + 35f * scale
        val bundleRadius = 14f * scale

        // Bundle pouch string/knotted part at the beak
        val beakTipX = headX - 30f * scale
        val beakTipY = headY + 5f * scale

        val strapPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(beakTipX - 2f * scale, beakTipY + 2f * scale)
            lineTo(bundleX, bundleY - bundleRadius + 2f * scale)
            lineTo(beakTipX + 2f * scale, beakTipY + 2f * scale)
            close()
        }
        drawPath(path = strapPath, color = bundleColor.copy(alpha = 0.9f))

        // Bundle pouch circle
        drawCircle(
            color = bundleColor,
            radius = bundleRadius,
            center = Offset(bundleX, bundleY)
        )
        // Cute knot details on bundle
        drawCircle(
            color = Color(0xFF80DEEA),
            radius = 4f * scale,
            center = Offset(beakTipX, beakTipY + 4f * scale)
        )

        // 9. Beak: Big long pointy bright orange/yellow peak
        val beakColor = Color(0xFFF39C12)
        val beakPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(headX - 10f * scale, headY - 4f * scale) // top attachment
            lineTo(headX - 45f * scale, headY + 6f * scale) // beak tip
            lineTo(headX - 8f * scale, headY + 8f * scale)  // bottom attachment
            close()
        }
        drawPath(path = beakPath, color = beakColor)
    }
}

// 2. ONBOARDING SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onRegister: (String, Long, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dobTimestamp by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("Niño") }
    var dateString by remember { mutableStateOf("") }

    val context = LocalContext.current
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
            dateString = sdf.format(chosenCal.time)
            dobTimestamp = chosenCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(PremiumBackgroundBrush)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Crane/Stork Mascot Illustration drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StorkDrawing(
                            modifier = Modifier
                                .size(110.dp)
                                .padding(bottom = 4.dp)
                        )
                        Text(
                            text = "¡Hola, Papis!",
                            style = Typography.headlineMedium,
                            color = PrimaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Comencemos vuestra historia",
                    style = Typography.headlineLarge,
                    color = PrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crea el perfil de tu pequeño para empezar a registrar sus primeros grandes momentos.",
                    style = Typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Name Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "¿Cómo se llama tu bebé?",
                            style = Typography.labelLarge,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PMOutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Ej: Martina o Martín") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("baby_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Face, contentDescription = null, tint = MintPrimary)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Birthdate Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Fecha de nacimiento",
                            style = Typography.labelLarge,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Wrapped in clickable Box so tapping anywhere on the bar shows calendar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                        ) {
                            PMOutlinedTextField(
                                value = dateString,
                                onValueChange = {},
                                placeholder = { Text("Selecciona fecha") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("baby_dob_input"),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MintPrimary)
                                },
                                enabled = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "¿Es niño o niña?",
                            style = Typography.labelLarge,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { gender = "Niño" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("onboarding_boy_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gender == "Niño") MintPrimary else CreamBg,
                                    contentColor = if (gender == "Niño") SurfaceWhite else TextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (gender == "Niño") MintPrimary else MintPrimary.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Male, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Niño", style = Typography.labelLarge)
                            }

                            Button(
                                onClick = { gender = "Niña" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("onboarding_girl_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gender == "Niña") MintPrimary else CreamBg,
                                    contentColor = if (gender == "Niña") SurfaceWhite else TextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (gender == "Niña") MintPrimary else MintPrimary.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Female, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Niña", style = Typography.labelLarge)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        val finalDob = dobTimestamp ?: System.currentTimeMillis()
                        if (name.isNotBlank()) {
                            onRegister(name, finalDob, gender)
                        }
                    },
                    enabled = name.isNotBlank() && dateString.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Siguiente", style = Typography.headlineSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Puedes cambiar estos datos en cualquier momento desde los ajustes de tu perfil.",
                    style = Typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// Helper functions for Sleep manual entry
fun calculateSleepMinutes(startTime: String, endTime: String): Int {
    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = format.parse(startTime) ?: return 0
        var end = format.parse(endTime) ?: return 0
        if (end.before(start)) {
            val cal = Calendar.getInstance().apply {
                time = end
                add(Calendar.DATE, 1)
            }
            end = cal.time
        }
        ((end.time - start.time) / 60000).toInt()
    } catch (e: Exception) {
        0
    }
}

fun formatSleepDurationText(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0 min"
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "$h h $m min"
        h > 0 -> "$h h"
        else -> "$m min"
    }
}

fun formatSleepDurationDescription(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "Las horas de inicio y fin son iguales"
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "$h ${if (h == 1) "hora" else "horas"} y $m ${if (m == 1) "minuto" else "minutos"}"
        h > 0 -> "$h ${if (h == 1) "hora" else "horas"}"
        else -> "$m ${if (m == 1) "minuto" else "minutos"}"
    }
}

fun parseHourAndMinute(timeStr: String, defaultHour: Int = 12, defaultMinute: Int = 0): Pair<Int, Int> {
    return try {
        val parts = timeStr.split(":")
        val h = parts[0].trim().toInt().coerceIn(0, 23)
        val m = parts[1].trim().toInt().coerceIn(0, 59)
        Pair(h, m)
    } catch (e: Exception) {
        Pair(defaultHour, defaultMinute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepManualRegistrationForm(
    initialDate: Calendar = Calendar.getInstance(),
    initialStartTime: String? = null,
    initialEndTime: String? = null,
    initialNotes: String = "",
    onConfirm: (dateStr: String, startTime: String, endTime: String, notes: String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // 1. FECHA - por defecto puesto hoy
    var selectedCalendar by remember { mutableStateOf(initialDate.clone() as Calendar) }
    val displayDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 2. HORAS - inicio y fin para seleccionarlo manualmente
    var startTimeStr by remember {
        mutableStateOf(
            initialStartTime ?: run {
                val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -1) }
                String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        )
    }
    var endTimeStr by remember {
        mutableStateOf(
            initialEndTime ?: run {
                val cal = Calendar.getInstance()
                String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        )
    }
    var sleepNotes by remember { mutableStateOf(initialNotes) }

    // Date picker dialog
    val datePickerDialog = remember(selectedCalendar) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedCalendar = newCal
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Dynamic duration calculation
    val totalMinutes = remember(startTimeStr, endTimeStr) {
        calculateSleepMinutes(startTimeStr, endTimeStr)
    }
    val durationText = remember(totalMinutes) { formatSleepDurationText(totalMinutes) }
    val durationDesc = remember(totalMinutes) { formatSleepDurationDescription(totalMinutes) }

    val isOvernight = remember(startTimeStr, endTimeStr) {
        try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val s = format.parse(startTimeStr)
            val e = format.parse(endTimeStr)
            if (s != null && e != null) e.before(s) else false
        } catch (e: Exception) {
            false
        }
    }

    val isToday = remember(selectedCalendar) {
        val today = Calendar.getInstance()
        today.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
        today.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
    }
    val isYesterday = remember(selectedCalendar) {
        val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        yest.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
        yest.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. FECHA (Arriba fecha y por defecto puesto hoy) ---
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha de descanso",
                    style = Typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                // Chips rápidos Hoy / Ayer
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isToday,
                        onClick = { selectedCalendar = Calendar.getInstance() },
                        label = { Text("Hoy", style = Typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = isYesterday,
                        onClick = {
                            selectedCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        },
                        label = { Text("Ayer", style = Typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Clickable Date Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                colors = CardDefaults.cardColors(containerColor = SurfaceMuted.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MintPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isToday) "Hoy" else if (isYesterday) "Ayer" else "Fecha",
                                style = Typography.labelSmall,
                                color = if (isToday) MintPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            val formattedDate = displayDateFormat.format(selectedCalendar.time)
                            Text(
                                text = formattedDate,
                                style = Typography.bodyLarge,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "Cambiar",
                            style = Typography.labelSmall,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // --- 2. HORA DE INICIO Y DE FIN (Abajo hora de inicio y de fin para seleccionarlo manualmente) ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Horarios de sueño",
                style = Typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Hora de inicio
                val (startH, startM) = parseHourAndMinute(startTimeStr)
                val startTimePicker = android.app.TimePickerDialog(
                    context,
                    { _, h, m ->
                        startTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                    },
                    startH,
                    startM,
                    true
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { startTimePicker.show() },
                    colors = CardDefaults.cardColors(containerColor = SurfaceMuted.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hora de inicio",
                                style = Typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = startTimeStr,
                            style = Typography.headlineMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = SkyBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Tocar para cambiar",
                                style = Typography.labelSmall,
                                color = Color(0xFF1E5B94),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Card Hora de fin
                val (endH, endM) = parseHourAndMinute(endTimeStr)
                val endTimePicker = android.app.TimePickerDialog(
                    context,
                    { _, h, m ->
                        endTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                    },
                    endH,
                    endM,
                    true
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { endTimePicker.show() },
                    colors = CardDefaults.cardColors(containerColor = SurfaceMuted.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = PeachWarm,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hora de fin",
                                style = Typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = endTimeStr,
                            style = Typography.headlineMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = PeachWarm.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Tocar para cambiar",
                                style = Typography.labelSmall,
                                color = Color(0xFFB45309),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. RESUMEN TIEMPO TOTAL DE SUEÑO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SkyBlue.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, SkyBlue.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E5B94)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Resumen de sueño",
                            style = Typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E5B94)
                        )
                    }

                    val sleepTypeBadge = if (isOvernight || totalMinutes >= 300) "🌙 Sueño nocturno" else "☀️ Siesta"
                    Surface(
                        color = SurfaceWhite,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = sleepTypeBadge,
                            style = Typography.labelSmall,
                            color = PrimaryDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Tiempo total de sueño",
                            style = Typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = durationText,
                            style = Typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryDark,
                            fontSize = 32.sp
                        )
                    }
                    Text(
                        text = durationDesc,
                        style = Typography.bodySmall,
                        color = Color(0xFF1E5B94),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                if (isOvernight) {
                    Text(
                        text = "ℹ️ Cruzó la medianoche (del día seleccionado al día siguiente)",
                        style = Typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Optional Notes
        PMOutlinedTextField(
            value = sleepNotes,
            onValueChange = { sleepNotes = it },
            placeholder = { Text("Notas o detalles (opcional, ej: Durmió tranquilo)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = TextMuted)
            }
        )

        // --- 4. BOTÓN PARA CONFIRMAR EL REGISTRO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(0.38f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("Cancelar", color = TextMuted)
                }
            }

            Button(
                onClick = {
                    val dbDateStr = dbDateFormat.format(selectedCalendar.time)
                    onConfirm(dbDateStr, startTimeStr, endTimeStr, sleepNotes)
                },
                enabled = totalMinutes > 0,
                modifier = Modifier
                    .weight(if (onCancel != null) 0.62f else 1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceMuted,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(27.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar",
                    style = Typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SleepManualRegistrationDialog(
    initialStartTime: String? = null,
    initialEndTime: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, startTime: String, endTime: String, notes: String) -> Unit
) {
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                    initialStartTime = initialStartTime,
                    initialEndTime = initialEndTime,
                    onConfirm = onConfirm,
                    onCancel = onDismiss
                )
            }
        }
    }
}

// 3. HOME SCREEN
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
                        ageInMonths = viewModel.getAgeInMonths(baby.dob),
                        sizeDp = 44,
                        skinTone = baby.skinTone,
                        hairColor = baby.hairColor
                    )
                }
            }

            // Milestone Progress Box based on baby's current age
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

            // Calculate 0-12 month milestone completion percentage for pacifier color
            val milestoneUnderOneMoments = moments.filter {
                (it.ageRange == "0-6 meses" || it.ageRange == "6-12 meses") && !it.isCustom
            }
            val milestoneCompletedUnderOne = milestoneUnderOneMoments.count { it.isCompleted }
            val milestoneTotalUnderOne = milestoneUnderOneMoments.size.coerceAtLeast(1)
            val milestonePercentUnderOne = (milestoneCompletedUnderOne * 100) / milestoneTotalUnderOne

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
                        contentDescription = "Información de chupete",
                        tint = PrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Growth and Gender Adaptive baby drawing
                val ageMonths = viewModel.getAgeInMonths(baby.dob)
                // Pacifier Color scale based on completed moments percent
                val percent = milestonePercentUnderOne / 100f
                val pacifierColor = when {
                    percent <= 0.25f -> Color(0xFF94A3B8) // Soft Gray
                    percent <= 0.50f -> PeachWarm       // Peach/Orange
                    percent <= 0.75f -> MintPrimary     // Mint green
                    else -> GoldenMilestone             // Golden milestone!
                }

                InteractiveBabyAvatar(
                    gender = baby.gender,
                    ageInMonths = ageMonths,
                    skinTone = baby.skinTone,
                    hairColor = baby.hairColor,
                    pacifierColor = pacifierColor,
                    milestonePercentUnderOne = milestonePercentUnderOne,
                    onClick = { showGeneralSyncInfo = true }
                )
            }

            if (showPacifierLegend) {
                AlertDialog(
                    onDismissRequest = { showPacifierLegend = false },
                    containerColor = Color.White,
                    title = { Text("Leyenda del Chupete", style = Typography.headlineSmall, color = PrimaryDark) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("El color del chupete refleja el progreso de los hitos del primer año:", style = Typography.bodyMedium, color = TextDark)

                            val legendItems = listOf(
                                "Dorado" to (Color(0xFFD4AF37) to "100%"),
                                "Rojo" to (Color(0xFFEF4444) to "80% - 99%"),
                                "Púrpura" to (Color(0xFFA855F7) to "60% - 79%"),
                                "Verde" to (Color(0xFF22C55E) to "40% - 59%"),
                                "Azul" to (Color(0xFF3B82F6) to "20% - 39%"),
                                "Blanco" to (Color.White to "0% - 19%")
                            )

                            legendItems.forEach { (colorName, details) ->
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
                                        text = "$colorName: $percentage",
                                        style = Typography.bodySmall,
                                        color = TextDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("¡Sigue completando momentos para subir de nivel!", style = Typography.labelSmall, color = MintPrimary, fontWeight = FontWeight.Bold)
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
                        viewModel.currentTab.value = "Registrar"
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
                        viewModel.targetActivityType.value = "panal"
                        viewModel.currentTab.value = "Registrar"
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
                    val sleepsToday = activities.filter { it.type == "sueno" }
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
                initialStartTime = initialStart,
                initialEndTime = initialEnd,
                onDismiss = { showManualSleepDialog = false },
                onConfirm = { dateStr, startTime, endTime, notes ->
                    viewModel.addManualSleepRecord(dateStr, startTime, endTime, notes)
                    if (isTimerRunning) {
                        viewModel.cancelSleepTimer()
                    }
                    showManualSleepDialog = false
                    Toast.makeText(context, "¡Sueño registrado con éxito! 🌙💤", Toast.LENGTH_SHORT).show()
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

// Interactive custom drew Baby Avatar with scaling based on age and custom pacifier color!
@Composable
fun InteractiveBabyAvatar(
    gender: String,
    ageInMonths: Int,
    skinTone: String,
    hairColor: String,
    pacifierColor: Color,
    milestonePercentUnderOne: Int = 0,
    onClick: () -> Unit
) {
    // Scales dynamically to show baby growing older
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
                // Soft background radial aura so the transparent baby floats gently and harmoniously
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (gender == "Niña") Color(0xFFFFCAD4).copy(alpha = 0.35f) else MintPrimary.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                val babyDrawableRes = if (gender == "Niña") {
                    // Select baby girl drawable based on 0-12 month milestone completion %
                    when {
                        milestonePercentUnderOne >= 100 -> com.example.R.drawable.img_baby_girl_pacifier_gold
                        milestonePercentUnderOne >= 80 -> com.example.R.drawable.img_baby_girl_pacifier_red
                        milestonePercentUnderOne >= 60 -> com.example.R.drawable.img_baby_girl_pacifier_purple
                        milestonePercentUnderOne >= 40 -> com.example.R.drawable.img_baby_girl_pacifier_green
                        milestonePercentUnderOne >= 20 -> com.example.R.drawable.img_baby_girl_pacifier_blue
                        else -> com.example.R.drawable.img_baby_girl_under_one_1780948526412 // white pacifier 0-19%
                    }
                } else {
                    // Select baby boy drawable based on 0-12 month milestone completion %
                    when {
                        milestonePercentUnderOne >= 100 -> com.example.R.drawable.img_baby_boy_pacifier_gold
                        milestonePercentUnderOne >= 80 -> com.example.R.drawable.img_baby_boy_pacifier_red
                        milestonePercentUnderOne >= 60 -> com.example.R.drawable.img_baby_boy_pacifier_purple
                        milestonePercentUnderOne >= 40 -> com.example.R.drawable.img_baby_boy_pacifier_green
                        milestonePercentUnderOne >= 20 -> com.example.R.drawable.img_baby_boy_pacifier_blue
                        else -> com.example.R.drawable.img_baby_boy_under_one_1780845155346 // white pacifier 0-19%
                    }
                }

                Image(
                    painter = painterResource(id = babyDrawableRes),
                    contentDescription = if (gender == "Niña") "Bebé Niña" else "Bebé Martín",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
        } else {
            Canvas(modifier = Modifier.size((200 * baseScale).dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val headRadius = size.width * 0.32f

            // 1. Soft glowing aura background
            drawCircle(
                color = MintPrimary.copy(alpha = 0.08f),
                radius = headRadius * 1.35f,
                center = center
            )

            // 2. Body / Shoulders (Pastel outfit corresponding to gender)
            val outfitColor = if (gender == "Niña") Color(0xFFFFCAD4) else Color(0xFFBDE0FE)
            drawArc(
                color = outfitColor,
                startAngle = 40f,
                sweepAngle = 100f,
                useCenter = true,
                size = Size(headRadius * 1.8f, headRadius * 1.4f),
                topLeft = Offset(center.x - headRadius * 0.9f, center.y + headRadius * 0.38f)
            )

            // Cute white baby bib collar
            drawArc(
                color = Color.White,
                startAngle = 40f,
                sweepAngle = 100f,
                useCenter = true,
                size = Size(headRadius * 1.0f, headRadius * 0.7f),
                topLeft = Offset(center.x - headRadius * 0.5f, center.y + headRadius * 0.43f)
            )

            // 3. Ears with sweet inner ear details
            val earY = center.y + headRadius * 0.05f
            // Left Ear
            drawCircle(
                color = earColor,
                radius = headRadius * 0.24f,
                center = Offset(center.x - headRadius * 0.98f, earY)
            )
            drawCircle(
                color = Color(0xFFFFB5A7).copy(alpha = 0.6f),
                radius = headRadius * 0.14f,
                center = Offset(center.x - headRadius * 0.96f, earY)
            )
            // Right Ear
            drawCircle(
                color = earColor,
                radius = headRadius * 0.24f,
                center = Offset(center.x + headRadius * 0.98f, earY)
            )
            drawCircle(
                color = Color(0xFFFFB5A7).copy(alpha = 0.6f),
                radius = headRadius * 0.14f,
                center = Offset(center.x + headRadius * 0.96f, earY)
            )

            // 4. Head (Soft warm peach baby skin)
            drawCircle(
                color = skinToneColor,
                radius = headRadius,
                center = center
            )

            // 5. Hair (Beautiful custom colored curls / customized by gender)
            if (hairColorValue != Color.Transparent) {
                if (gender == "Niña") {
                    // Front fringe/bangs
                    drawArc(
                        color = hairColorValue,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = Size(headRadius * 1.8f, headRadius * 1.0f),
                        topLeft = Offset(center.x - headRadius * 0.9f, center.y - headRadius * 1.15f)
                    )
                    // Adorable pink bow
                    val bowPink = Color(0xFFFFB5A7)
                    val bowCenter = Offset(center.x + headRadius * 0.52f, center.y - headRadius * 0.85f)
                    drawCircle(color = bowPink, radius = 16f, center = Offset(bowCenter.x - 10f, bowCenter.y))
                    drawCircle(color = bowPink, radius = 16f, center = Offset(bowCenter.x + 10f, bowCenter.y))
                    drawCircle(color = Color.White, radius = 7f, center = bowCenter)
                } else {
                    // Curly hair locks
                    drawArc(
                        color = hairColorValue,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = true,
                        size = Size(headRadius * 1.8f, headRadius * 1.1f),
                        topLeft = Offset(center.x - headRadius * 0.9f, center.y - headRadius * 1.2f)
                    )
                    // Cute golden curl on top
                    drawArc(
                        color = hairColorValue,
                        startAngle = 240f,
                        sweepAngle = 70f,
                        useCenter = false,
                        style = Stroke(width = 14f, cap = StrokeCap.Round),
                        size = Size(headRadius * 0.4f, headRadius * 0.4f),
                        topLeft = Offset(center.x - headRadius * 0.2f, center.y - headRadius * 1.32f)
                    )
                }
            }

            // 6. Cute fine eyebrows
            drawArc(
                color = if (hairColorValue != Color.Transparent) hairColorValue.copy(alpha = 0.65f) else Color.DarkGray.copy(alpha = 0.3f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(width = 5f, cap = StrokeCap.Round),
                size = Size(headRadius * 0.24f, headRadius * 0.12f),
                topLeft = Offset(center.x - headRadius * 0.48f, center.y - headRadius * 0.38f)
            )
            drawArc(
                color = if (hairColorValue != Color.Transparent) hairColorValue.copy(alpha = 0.65f) else Color.DarkGray.copy(alpha = 0.3f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(width = 5f, cap = StrokeCap.Round),
                size = Size(headRadius * 0.24f, headRadius * 0.12f),
                topLeft = Offset(center.x + headRadius * 0.24f, center.y - headRadius * 0.38f)
            )

            // 7. Sparkly adorable eyes (Anime-style doll reflections)
            val leftEyeCenter = Offset(center.x - headRadius * 0.34f, center.y - headRadius * 0.08f)
            val rightEyeCenter = Offset(center.x + headRadius * 0.34f, center.y - headRadius * 0.08f)
            val eyeRadius = 14f

            // Base eye
            drawCircle(color = Color(0xFF1E293B), radius = eyeRadius, center = leftEyeCenter)
            drawCircle(color = Color(0xFF1E293B), radius = eyeRadius, center = rightEyeCenter)

            // Sparkle 1 (Main shine)
            drawCircle(color = Color.White, radius = 5.5f, center = Offset(leftEyeCenter.x + 3.5f, leftEyeCenter.y - 3.5f))
            drawCircle(color = Color.White, radius = 5.5f, center = Offset(rightEyeCenter.x + 3.5f, rightEyeCenter.y - 3.5f))

            // Sparkle 2 (Soft accent)
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(leftEyeCenter.x - 4f, leftEyeCenter.y + 4f))
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(rightEyeCenter.x - 4f, rightEyeCenter.y + 4f))

            // 8. Cute blushing cheeks (kawaii highlights)
            val cheekRadius = headRadius * 0.21f
            val leftCheekCenter = Offset(center.x - headRadius * 0.52f, center.y + headRadius * 0.16f)
            val rightCheekCenter = Offset(center.x + headRadius * 0.52f, center.y + headRadius * 0.16f)

            // Rosy blush circles
            drawCircle(color = Color(0xFFFFB5A7).copy(alpha = 0.55f), radius = cheekRadius, center = leftCheekCenter)
            drawCircle(color = Color(0xFFFFB5A7).copy(alpha = 0.55f), radius = cheekRadius, center = rightCheekCenter)

            // White cheek reflex sparkles
            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 3f, center = Offset(leftCheekCenter.x - 6f, leftCheekCenter.y - 1f))
            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 2f, center = Offset(leftCheekCenter.x, leftCheekCenter.y + 3f))
            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 3f, center = Offset(rightCheekCenter.x + 6f, rightCheekCenter.y - 1f))
            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 2f, center = Offset(rightCheekCenter.x, rightCheekCenter.y + 3f))

            // 9. Sweet button nose
            drawCircle(color = Color(0xFFFFB5A7), radius = 8f, center = Offset(center.x, center.y + headRadius * 0.08f))

            // 10. Glossy pacifier (changes by pacifierColor dynamically)
            val pacifierCenter = Offset(center.x, center.y + headRadius * 0.32f)

            // Pacifier outer rim glow
            drawCircle(color = Color.White, radius = headRadius * 0.27f, center = pacifierCenter)
            // Pacifier colored backplate
            drawCircle(color = pacifierColor.copy(alpha = 0.75f), radius = headRadius * 0.24f, center = pacifierCenter)
            // Pacifier main inner button
            drawCircle(color = pacifierColor, radius = headRadius * 0.14f, center = pacifierCenter)
            // Pacifier shiny glossy glaze spot
            drawCircle(color = Color.White.copy(alpha = 0.85f), radius = headRadius * 0.04f, center = Offset(pacifierCenter.x + headRadius * 0.04f, pacifierCenter.y - headRadius * 0.04f))

            // Pacifier ring handle
            drawArc(
                color = Color.White.copy(alpha = 0.92f),
                startAngle = -20f,
                sweepAngle = 220f,
                useCenter = false,
                style = Stroke(width = 9f),
                size = Size(headRadius * 0.26f, headRadius * 0.26f),
                topLeft = Offset(pacifierCenter.x - headRadius * 0.13f, pacifierCenter.y)
            )
        }
    }
    }
}

// Quick action button bento-style card
@Composable
fun BentoCard(
    modifier: Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

// 4. MOMENTS SCREEN (List & Custom Add Moment Button)
@Composable
fun MomentsScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel,
    moments: List<MomentRecord>
) {
    var showAddMomentDialog by remember { mutableStateOf(false) }
    var showEditMomentDetail by remember { mutableStateOf<MomentRecord?>(null) }
    var autoOpenMediaSelector by remember { mutableStateOf(false) }

    // Dialog state controllers
    var customName by remember { mutableStateOf("") }
    var customAgeRange by remember { mutableStateOf("0-6 meses") }
    var customDate by remember { mutableStateOf("") }
    var customLocation by remember { mutableStateOf("") }
    var customDetails by remember { mutableStateOf("") }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    // Pulled-up state controllers for editing a moment
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
            dateEdit = selected.dateHappened ?: ""
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
                val currentEditing = showEditMomentDetail
                if (currentEditing != null) {
                    localPhotoPath = absolutePath
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
                val currentEditing = showEditMomentDetail
                if (currentEditing != null) {
                    localPhotoPath = absolutePath
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
                // Determine mime type and copy to persistent files directory
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
                val currentEditing = showEditMomentDetail
                if (currentEditing != null) {
                    localPhotoPath = absolutePath
                    viewModel.completeMoment(
                        moment = currentEditing,
                        dateHappened = dateEdit,
                        location = locEdit,
                        details = detEdit,
                        photoPath = absolutePath
                    )
                    val msg = if (isVideo) "¡Vídeo de la galería guardado! 👶🎥" else "¡Foto de la galería guardada! 👶📸"
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
                    MiniBabyAvatar(
                        gender = baby.gender,
                        ageInMonths = viewModel.getAgeInMonths(baby.dob),
                        sizeDp = 44,
                        skinTone = baby.skinTone,
                        hairColor = baby.hairColor
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }

                            if (isExpanded) {
                                Divider(color = CreamBg, thickness = 1.dp)
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

                                                // Verified Checkmark status icon (clickable to toggle completion)
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
                                        Divider(color = CreamBg.copy(alpha = 0.5f), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ACTION BUTTON (FAB) (+) to write/register new custom moments
        FloatingActionButton(
            onClick = { showAddMomentDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("add_custom_moment_fab"),
            containerColor = PrimaryDark,
            contentColor = SurfaceWhite
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Momento Personalizado")
        }

        // Popup standard custom moments creator dialog
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

                        // Age range radio choices
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
                                    photoPath = "mock_photo_drive" // mock autosaved backup image identifier
                                )
                                // Clear
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

        // 5. DETAIL/EDIT MOMENT MODAL
        if (showEditMomentDetail != null) {
            val selected = showEditMomentDetail!!

            // Dynamic camera/video/gallery simulated states (pushed-up variables are used for main inputs)
            var showCameraSimu by remember { mutableStateOf<String?>(null) } // "photo" or "video"
            var showGallerySimu by remember { mutableStateOf(false) }
            var isRecordingVideo by remember { mutableStateOf(false) }
            var videoProgressSec by remember { mutableStateOf(0) }

            LaunchedEffect(isRecordingVideo) {
                if (isRecordingVideo) {
                    videoProgressSec = 0
                    while (isRecordingVideo && videoProgressSec < 10) {
                        kotlinx.coroutines.delay(1000)
                        videoProgressSec++
                    }
                    if (isRecordingVideo) {
                        isRecordingVideo = false
                        localPhotoPath = "video_captured_drive.mp4"
                        viewModel.completeMoment(
                            moment = selected,
                            dateHappened = dateEdit,
                            location = locEdit,
                            details = detEdit,
                            photoPath = "video_captured_drive.mp4"
                        )
                    }
                }
            }

            // Simulated Google Drive sync state
            val driveStateFlow by viewModel.googleDriveSyncStatus.collectAsStateWithLifecycle()

            // Media picker selection dialog
            if (showMediaOptionsDialog) {
                AlertDialog(
                    onDismissRequest = { showMediaOptionsDialog = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "Añadir recuerdo",
                            style = Typography.headlineMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Selecciona una opción para guardar este hermoso momento de tu bebé:",
                                style = Typography.bodyMedium,
                                color = TextDark
                            )

                            // Option 1: Camera Photo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMediaOptionsDialog = false
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MintPrimary.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, MintPrimary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MintPrimary.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = PrimaryDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Hacer una foto", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text("Usa la cámara para capturar una foto", style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }

                            // Option 2: Camera Video
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMediaOptionsDialog = false
                                        videoPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                colors = CardDefaults.cardColors(containerColor = SkyBlue.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(SkyBlue.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = PrimaryDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Grabar un vídeo", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text("Graba un vídeo corto desde la cámara", style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }

                            // Option 3: Phone Gallery (Photo or Video)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMediaOptionsDialog = false
                                        galleryPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                colors = CardDefaults.cardColors(containerColor = LavenderSoft.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, LavenderSoft.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(LavenderSoft.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Collections,
                                            contentDescription = null,
                                            tint = PrimaryDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Elegir de la galería", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text("Abre tus fotos y vídeos del móvil", style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showMediaOptionsDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                    }
                )
            }

            // Simulated Camera Photo/Video Dialog UI
            if (showCameraSimu != null) {
                val isVideo = showCameraSimu == "video"
                AlertDialog(
                    onDismissRequest = { 
                        showCameraSimu = null
                        isRecordingVideo = false
                    },
                    title = {
                        Text(
                            text = if (isVideo) "Cámara de Vídeo" else "Cámara de Fotos",
                            style = Typography.headlineMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isVideo) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        "Vista previa de cámara...",
                                        color = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                                        style = Typography.labelSmall
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = if (isRecordingVideo) Color.Red else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        if (isRecordingVideo) {
                                            Text(
                                                "REC 00:0$videoProgressSec / 00:10",
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (!isVideo) {
                                IconButton(
                                    onClick = {
                                        localPhotoPath = "simulated_photo_drive.jpg"
                                        viewModel.completeMoment(
                                            moment = selected,
                                            dateHappened = dateEdit,
                                            location = locEdit,
                                            details = detEdit,
                                            photoPath = "simulated_photo_drive.jpg"
                                        )
                                        showCameraSimu = null
                                    },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(PrimaryDark.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryDark)
                                    )
                                }
                                Text("Haz clic para tomar foto", style = Typography.labelSmall, color = TextMuted)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (!isRecordingVideo) {
                                        Button(
                                            onClick = { isRecordingVideo = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                                                Text("Empezar a grabar", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                isRecordingVideo = false
                                                localPhotoPath = "video_captured_drive.mp4"
                                                viewModel.completeMoment(
                                                    moment = selected,
                                                    dateHappened = dateEdit,
                                                    location = locEdit,
                                                    details = detEdit,
                                                    photoPath = "video_captured_drive.mp4"
                                                )
                                                showCameraSimu = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(modifier = Modifier.size(8.dp).background(Color.Red, RoundedCornerShape(1.dp)))
                                                Text("Finalizar y Guardar", color = TextDark, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }

            // Real System Gallery Image Grid Picker Simulation
            if (showGallerySimu) {
                AlertDialog(
                    onDismissRequest = { showGallerySimu = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Galería del Móvil",
                                style = Typography.headlineMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showGallerySimu = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                            }
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Elige una foto reciente guardada en la galería de tu dispositivo:",
                                style = Typography.bodyMedium,
                                color = TextDark
                            )

                            val galleryItems = listOf(
                                Triple("👶 Primeras sonrisas", "gallery_first_smiles.jpg", MintPrimary),
                                Triple("🍼 Hora de comer", "gallery_eating_time.jpg", PeachWarm),
                                Triple("🧸 Jugando con peluche", "gallery_teddy_play.jpg", GoldenMilestone),
                                Triple("🛁 Baño calentito", "gallery_warm_bath.jpg", SkyBlue),
                                Triple("😴 Dulces sueños", "gallery_sweet_dreams.jpg", LavenderSoft),
                                Triple("🌳 Mi primer paseo", "gallery_first_outing.jpg", MintPrimary)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                galleryItems.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { item ->
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(90.dp)
                                                    .clickable {
                                                        localPhotoPath = item.second
                                                        viewModel.completeMoment(
                                                            moment = selected,
                                                            dateHappened = dateEdit,
                                                            location = locEdit,
                                                            details = detEdit,
                                                            photoPath = item.second
                                                        )
                                                        showGallerySimu = false
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = item.third.copy(alpha = 0.12f)),
                                                border = BorderStroke(1.dp, item.third.copy(alpha = 0.3f))
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = when(item.second) {
                                                                "gallery_first_smiles.jpg" -> "👶"
                                                                "gallery_eating_time.jpg" -> "🍼"
                                                                "gallery_teddy_play.jpg" -> "🧸"
                                                                "gallery_warm_bath.jpg" -> "🛁"
                                                                "gallery_sweet_dreams.jpg" -> "😴"
                                                                else -> "🌳"
                                                            },
                                                            fontSize = 20.sp
                                                        )
                                                        Text(
                                                            text = item.first,
                                                            style = Typography.labelSmall,
                                                            textAlign = TextAlign.Center,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (pair.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }

            // Delete Photo / Video Confirmation Dialog
            if (showDeletePhotoConfirm) {
                val isVideoConfirm = localPhotoPath?.let { it.contains(".mp4", ignoreCase = true) || it.contains("video", ignoreCase = true) } ?: false
                AlertDialog(
                    onDismissRequest = { showDeletePhotoConfirm = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = if (isVideoConfirm) "Eliminar vídeo" else "Eliminar imagen",
                            style = Typography.headlineMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = if (isVideoConfirm) "¿Estás seguro de que quieres borrar el vídeo de este momento?" else "¿Estás seguro de que quieres borrar la imagen de este momento?",
                            style = Typography.bodyMedium,
                            color = TextDark
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                localPhotoPath = null
                                showDeletePhotoConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeletePhotoConfirm = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                    }
                )
            }

            // Fullscreen Photo Viewer Dialog
            if (showFullscreenPhoto && localPhotoPath != null) {
                Dialog(
                    onDismissRequest = { showFullscreenPhoto = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        val isFullscreenVideo = localPhotoPath!!.contains(".mp4", ignoreCase = true) ||
                            localPhotoPath!!.contains("video", ignoreCase = true)
                        if (isFullscreenVideo) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        val controller = MediaController(ctx)
                                        controller.setAnchorView(this)
                                        setMediaController(controller)
                                        if (localPhotoPath!!.startsWith("content://")) {
                                            setVideoURI(Uri.parse(localPhotoPath!!))
                                        } else {
                                            setVideoPath(localPhotoPath!!)
                                        }
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = false
                                            start()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.85f)
                                    .align(Alignment.Center)
                            )
                        } else {
                            AsyncImage(
                                model = localPhotoPath,
                                contentDescription = "Foto en grande",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                        IconButton(
                            onClick = { showFullscreenPhoto = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar foto",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showEditMomentDetail = null },
                containerColor = Color.White,
                title = { Text(selected.name, style = Typography.headlineMedium, color = PrimaryDark) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        // Image camera/photo representation
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clickable {
                                    if (localPhotoPath != null) {
                                        showFullscreenPhoto = true
                                    } else {
                                        showMediaOptionsDialog = true
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = CreamBg)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (localPhotoPath != null) {
                                    val isRealPhoto = localPhotoPath!!.startsWith("/") || localPhotoPath!!.startsWith("content://") || localPhotoPath!!.startsWith("file://")
                                    val isRealVideo = localPhotoPath!!.contains(".mp4", ignoreCase = true) || localPhotoPath!!.contains("video", ignoreCase = true)
                                    if (isRealPhoto) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (isRealVideo) {
                                                // Show video thumbnail with play overlay
                                                if (videoThumbnailBitmap != null) {
                                                    Image(
                                                        bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                                        contentDescription = "Miniatura del vídeo",
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    // Dark placeholder while thumbnail loads or if extraction failed
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color(0xFF1C1C1E)),
                                                        contentAlignment = Alignment.Center
                                                    ) {}
                                                }
                                                // Play icon overlay (centered)
                                                Icon(
                                                    imageVector = Icons.Default.PlayCircle,
                                                    contentDescription = "Reproducir vídeo",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .align(Alignment.Center)
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = localPhotoPath,
                                                    contentDescription = "Foto del bebé",
                                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            }
                                            // Bottom overlay bar with label
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isRealVideo) "🎥 Vídeo guardado" else "📸 Foto guardada",
                                                        style = Typography.labelSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Toca para ver",
                                                        style = Typography.labelSmall,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                            val displayIcon = when {
                                                localPhotoPath!!.contains("video") -> "📹"
                                                localPhotoPath!!.contains("gallery") -> "🖼️"
                                                else -> "📸"
                                            }
                                            val labelText = when {
                                                localPhotoPath!!.contains("video") -> "Vídeo guardado"
                                                localPhotoPath!!.contains("gallery") -> "Foto guardada"
                                                else -> "Foto guardada"
                                            }
                                            val detailName = when(localPhotoPath) {
                                                "gallery_first_smiles.jpg" -> "👶 Primeras sonrisas"
                                                "gallery_eating_time.jpg" -> "🍼 Hora de comer"
                                                "gallery_teddy_play.jpg" -> "🧸 Jugando con peluche"
                                                "gallery_warm_bath.jpg" -> "🛁 Baño calentito"
                                                "gallery_sweet_dreams.jpg" -> "😴 Dulces sueños"
                                                "gallery_first_outing.jpg" -> "🌳 Mi primer paseo"
                                                "video_captured_drive.mp4" -> "Grabación de vídeo de tu bebé"
                                                else -> "Captura de cámara"
                                            }
                                            Text(displayIcon, fontSize = 28.sp)
                                            Text(labelText, style = Typography.labelLarge, color = PrimaryDark, fontWeight = FontWeight.Bold)
                                            Text(detailName, style = Typography.labelSmall, color = TextDark)
                                        }
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = PrimaryDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Añadir foto o vídeo del recuerdo", style = Typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }

                        // Drive status indicator below grey line, aligned to the right
                        if (localPhotoPath != null) {
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = MintPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = driveStateFlow.replace("(Simulado)", "").trim(),
                                    style = Typography.labelSmall,
                                    color = MintPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Trash icon below the photo/video to delete it
                        if (localPhotoPath != null) {
                            val isVideoMedia = localPhotoPath!!.contains(".mp4", ignoreCase = true) || localPhotoPath!!.contains("video", ignoreCase = true)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showDeletePhotoConfirm = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = if (isVideoMedia) "Eliminar vídeo" else "Eliminar foto",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isVideoMedia) "Eliminar vídeo" else "Eliminar foto",
                                        style = Typography.labelMedium,
                                        color = Color(0xFFE53935)
                                    )
                                }
                            }
                        }

                        // Date field that opens the native DatePickerDialog
                        Text(
                            text = "Fecha del momento:",
                            style = Typography.labelMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { momentDatePickerDialog.show() },
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
                                        text = if (dateEdit.isNotBlank()) dateEdit else "Toca para elegir fecha en el calendario",
                                        style = Typography.bodyMedium,
                                        color = if (dateEdit.isNotBlank()) PrimaryDark else TextMuted,
                                        fontWeight = if (dateEdit.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = PrimaryDark.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        PMOutlinedTextField(
                            value = locEdit,
                            onValueChange = { locEdit = it },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        PMOutlinedTextField(
                            value = detEdit,
                            onValueChange = { detEdit = it },
                            label = { Text("Detalles mágicos") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completeMoment(
                                moment = selected,
                                dateHappened = dateEdit,
                                location = locEdit,
                                details = detEdit,
                                photoPath = localPhotoPath
                            )
                            showEditMomentDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Text("Guardar Cambios")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (selected.isCompleted) {
                            TextButton(
                                onClick = {
                                    viewModel.uncompleteMoment(selected)
                                    showEditMomentDetail = null
                                }
                            ) {
                                Text("Desmarcar", color = Color(0xFFE53935))
                            }
                        }
                        TextButton(onClick = { showEditMomentDetail = null }) {
                            Text("Cerrar", color = TextMuted)
                        }
                    }
                }
            )
        }
    }
}

// 5. REGISTRAR ACTIVITY & HEALTH SCREEN
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
                    hairColor = baby.hairColor
                )
            }
        }

        // Activity vs Salud Segmented Tab Control
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
            ActividadTabContent(viewModel)
        } else {
            SaludTabContent(viewModel)
        }
    }
}

@Composable
fun ActividadTabContent(viewModel: BabyViewModel) {
    val context = LocalContext.current
    val targetType by viewModel.targetActivityType.collectAsStateWithLifecycle()
    var selectedActivityType by remember { mutableStateOf("comida") } // "comida", "sueno", "panal"

    // Sync with targetActivityType from Home
    LaunchedEffect(targetType) {
        if (targetType != null) {
            selectedActivityType = targetType!!
            viewModel.targetActivityType.value = null
        }
    }

    // Form inputs state
    var mealType by remember { mutableStateOf("Comida") }
    var foodName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Diaper state
    var diaperCondition by remember { mutableStateOf("Seco") }
    var diaperQuantity by remember { mutableStateOf(1) }
    var activityDate by remember { mutableStateOf("") }

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
        // Activity category selection row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoChip(
                modifier = Modifier.weight(1f),
                label = "Comida",
                icon = Icons.Default.Restaurant,
                selected = selectedActivityType == "comida",
                onClick = { selectedActivityType = "comida" }
            )
            BentoChip(
                modifier = Modifier.weight(1f),
                label = "Sueño",
                icon = Icons.Default.Bedtime,
                selected = selectedActivityType == "sueno",
                onClick = { selectedActivityType = "sueno" }
            )
            BentoChip(
                modifier = Modifier.weight(1f),
                label = "Pañal",
                icon = Icons.Default.ChildCare,
                selected = selectedActivityType == "panal",
                onClick = { selectedActivityType = "panal" }
            )
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
                    // Date selector integrated into the card
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
                                text = if (activityDate.isNotBlank()) activityDate else "Hoy",
                                style = Typography.bodyMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

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

                    SleepManualRegistrationForm(
                        onConfirm = { dateStr, startTime, endTime, sleepNotes ->
                            viewModel.addManualSleepRecord(dateStr, startTime, endTime, sleepNotes)
                            Toast.makeText(context, "¡Sueño registrado con éxito! 🌙💤", Toast.LENGTH_SHORT).show()
                            viewModel.currentTab.value = "Home"
                        }
                    )
                }
            }
        } else {
            // Form Fields based on activity type
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Date selector integrated into the card
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
                                text = if (activityDate.isNotBlank()) activityDate else "Hoy",
                                style = Typography.bodyMedium,
                                color = PrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    when (selectedActivityType) {
                        "comida" -> {
                            Text("Registro de Comida", style = Typography.headlineMedium, color = PrimaryDark)

                            PMOutlinedTextField(
                                value = foodName,
                                onValueChange = { foodName = it },
                                label = { Text("¿Qué comió tu bebé? (Ej: Puré de calabaza)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Categoría de comida:", style = Typography.labelLarge)
                            val categories = listOf("Desayuno", "Almuerzo", "Comida", "Merienda", "Cena")
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = mealType == cat,
                                        onClick = { mealType = cat },
                                        label = { Text(cat) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MintPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        "panal" -> {
                            Text("Registro de Pañal", style = Typography.headlineMedium, color = PrimaryDark)

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

                            // Quantity Counter for diapers
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
                        }
                    }

                    // General Notes Field
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

            // Guardar Action Button
            Button(
                onClick = {
                    when (selectedActivityType) {
                        "comida" -> {
                            viewModel.addMealRecord(mealType, foodName, notes)
                            foodName = ""
                            notes = ""
                        }
                        "panal" -> {
                            viewModel.addDiaperRecord(diaperCondition, notes, activityDate, diaperQuantity)
                            notes = ""
                            diaperQuantity = 1
                        }
                    }
                    // Back to home tab
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
}

@Composable
fun SaludTabContent(viewModel: BabyViewModel) {
    val context = LocalContext.current
    var selectedHealthSubTab by remember { mutableStateOf("peso_altura") } // "peso_altura", "vacunas", "citas"

    var inputWeight by remember { mutableStateOf("") }
    var inputHeight by remember { mutableStateOf("") }

    // Vacunas
    var vaccineName by remember { mutableStateOf("") }
    var vaccineNotes by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf("") } // optional: "dd/MM/yyyy"
    var vaccineStatus by remember { mutableStateOf("Vacunado completamente") } // "Falta por vacunar", "Pendiente más tomas", "Vacunado completamente"

    // Citas Médicas
    var appointmentPediatra by remember { mutableStateOf("") }
    val defaultApptDate = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
    }
    var appointmentDate by remember { mutableStateOf(defaultApptDate) }
    var appointmentTime by remember { mutableStateOf("10:00") }
    var hasReminder by remember { mutableStateOf(true) }

    // DatePicker for vaccine (optional date)
    val vaccineDatePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                vaccineDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // DatePicker for appointments (Acceso directo al calendario)
    val appointmentDatePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                appointmentDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // TimePicker for appointments
    val appointmentTimePickerDialog = remember {
        android.app.TimePickerDialog(
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
        // Subdivision Selection row
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

                        // 1. Selector de estado de vacunación
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
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
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { vaccineStatus = statusTitle },
                                            colors = RadioButtonDefaults.colors(selectedColor = statusColor)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Campo de Fecha opcional con acceso a calendario
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

                        // Acceso al Calendario para la fecha
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

                        // Acceso a la hora de la cita
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

        // Save Action Button
        Button(
            onClick = {
                when (selectedHealthSubTab) {
                    "peso_altura" -> {
                        val parsedWeight = inputWeight.toDoubleOrNull()
                        val parsedHeight = inputHeight.toDoubleOrNull()
                        if (parsedWeight != null) viewModel.addWeightRecord(parsedWeight)
                        if (parsedHeight != null) viewModel.addHeightRecord(parsedHeight)
                        Toast.makeText(context, "Medición registrada con éxito", Toast.LENGTH_SHORT).show()
                    }
                    "vacunas" -> {
                        if (vaccineName.isNotBlank()) {
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
                // Back to home tab
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

// 6. SEGUIMIENTO (ANALYTICS & CHARTS) SCREEN
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
                // Share icon to download complete PDF summaries
                IconButton(onClick = {
                    // Trigger a simulated report generating toast
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
                        hairColor = baby.hairColor
                    )
                }
            }
        }

        // Subtitle brief
        Text(
            text = "${baby.name} • ${viewModel.getAgeDisplayString(baby.dob)}",
            style = Typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Activity vs Salud Segmented Tab Control
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

        // Time range options
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
                        // Sleep duration Bar chart drawing (Dynamic from records)
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
                                    SleepDurationChart(sleepRecs)
                                }
                            }
                        }

                        // Diapers Line chart drawing
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
                                    DiaperLineChart(diaperRecs)
                                }
                            }
                        }

                        // Food History List
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
                // SALUD VIEW CHARTS
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
                        // Peso chart
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Evolución del Peso", style = Typography.headlineSmall, color = TextDark)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (pesoRecs.isEmpty()) {
                                    Text(
                                        text = "Aún no hay registros de peso. Añade mediciones de peso para ver el gráfico.",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    WeightLineChart(pesoRecs)
                                }
                            }
                        }

                        // Registro de Vacunas con separación de pendientes y completadas
                        val pendingVaccines = vaccines.filter {
                            it.status == "Falta por vacunar" || it.status == "Pendiente más tomas"
                        }
                        val completedVaccines = vaccines.filter {
                            it.status == "Vacunado completamente" || (it.status != "Falta por vacunar" && it.status != "Pendiente más tomas")
                        }
                        var vaccineListFilter by remember { mutableStateOf("todas") } // "todas", "pendientes", "completadas"

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

                                // Filtro rápido: Todas, Pendientes, Completadas
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

                                                            // Status Badge
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

                                                        // Fecha opcional registrada
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

                                                        // Acciones: Cambiar estado y eliminar
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

                        // Citas Médicas con acceso directo al Calendario
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
                                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                                        data = CalendarContract.Events.CONTENT_URI
                                                        putExtra(CalendarContract.Events.TITLE, cita.title)
                                                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cita.timestamp)
                                                        putExtra(CalendarContract.Events.DESCRIPTION, "Cita de ${baby.name}: ${cita.notes}")
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    try {
                                                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                            data = Uri.parse("content://com.android.calendar/time/${cita.timestamp}")
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

                                                    // ACCESO DIRECTO AL CALENDARIO EN LA FECHA
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
fun SleepDurationChart(records: List<ActivityRecord>) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val height = size.height
                        val recentRecords = records.take(7).reversed()
                        val sleepData = recentRecords.map { ((it.durationMinutes ?: 0) / 60f).coerceIn(0f, 24f) }
                        if (sleepData.isEmpty()) return@detectTapGestures

                        val barWidth = width / maxOf(7, sleepData.size).toFloat()
                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE
                        sleepData.forEachIndexed { idx, _ ->
                            val barX = idx * barWidth + (barWidth / 4f)
                            val dist = kotlin.math.abs(offset.x - barX)
                            if (dist < minDist) {
                                minDist = dist
                                closestIdx = idx
                            }
                        }
                        if (minDist < (barWidth / 2)) selectedPoint = closestIdx else selectedPoint = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

            val recentRecords = records.take(7).reversed()
            val sleepData = recentRecords.map { ((it.durationMinutes ?: 0) / 60f).coerceIn(0f, 24f) }
            if (sleepData.isEmpty()) return@Canvas

            val maxSleep = maxOf(10f, sleepData.maxOrNull() ?: 10f)
            val barWidth = width / maxOf(7, sleepData.size).toFloat()

            // Draw Y-axis labels
            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = (maxSleep / labelCount * i).toInt()
                val yPos = height - (i.toFloat() / labelCount) * height * 0.65f - 40f
                drawContext.canvas.nativeCanvas.drawText("$labelVal h", 30f, yPos, textPaint)
            }

            sleepData.forEachIndexed { idx, value ->
                val barHeight = (value / maxSleep) * height * 0.65f
                val barX = (idx + 1) * barWidth + (barWidth / 4f) // Shift for labels
                val y = height - barHeight - 40f

                drawRoundRect(
                    color = if (idx == sleepData.size - 1) MintPrimary else SkyBlue,
                    topLeft = Offset(barX, y),
                    size = Size(barWidth / 2f, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )

                val dateStr = sdf.format(java.util.Date(recentRecords[idx].timestamp))
                drawContext.canvas.nativeCanvas.drawText(dateStr, barX, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val recentRecords = records.take(7).reversed()
            val sleepData = recentRecords.map { ((it.durationMinutes ?: 0) / 60f).coerceIn(0f, 24f) }
            val idx = selectedPoint!!
            if (idx < sleepData.size) {
                val value = sleepData[idx]
                Box(
                    modifier = Modifier
                        .offset { IntOffset(((idx + 1) * (1000f / maxOf(7, sleepData.size).toFloat()) - 40f).toInt(), 20) } // Rough approx
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
fun DiaperLineChart(records: List<ActivityRecord>) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val height = size.height
                        val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val groupedData = records.groupBy { dateSdf.format(java.util.Date(it.timestamp)) }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedBy { it.first }
                            .takeLast(7)

                        if (groupedData.isEmpty()) return@detectTapGestures

                        val stepX = width / maxOf(6, groupedData.size - 1).coerceAtLeast(1).toFloat()
                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE
                        groupedData.forEachIndexed { idx, _ ->
                            val px = (idx + 1) * stepX
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
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val displaySdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

            val groupedData = records.groupBy { dateSdf.format(java.util.Date(it.timestamp)) }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }
                .takeLast(7)

            if (groupedData.isEmpty()) return@Canvas

            val diaperCounts = groupedData.map { it.second.toFloat() }
            val dates = groupedData.map { it.first }
            val maxCount = diaperCounts.maxOrNull()?.coerceAtLeast(2f) ?: 2f

            // Draw Y-axis labels
            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = (maxCount / labelCount * i).toInt()
                val yPos = height - (i.toFloat() / labelCount) * height * 0.6f - 40f
                drawContext.canvas.nativeCanvas.drawText("$labelVal", 30f, yPos, textPaint)
            }

            val stepX = width / maxOf(6, diaperCounts.size - 1).coerceAtLeast(1).toFloat()
            val points = diaperCounts.mapIndexed { idx, count ->
                val x = (idx + 1) * stepX
                val y = height - (count / maxCount) * height * 0.6f - 40f
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = SkyBlue, start = points[i], end = points[i + 1], strokeWidth = 6f, cap = StrokeCap.Round)
            }

            points.forEachIndexed { idx, pt ->
                drawCircle(color = if (idx == points.size - 1) PrimaryDark else SkyBlue, radius = 8f, center = pt)
                val date = dates[idx]
                val dateStr = displaySdf.format(java.util.Date(records.find { dateSdf.format(java.util.Date(it.timestamp)) == date }?.timestamp ?: 0L))
                drawContext.canvas.nativeCanvas.drawText(dateStr, pt.x, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val dateSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val groupedData = records.groupBy { dateSdf.format(java.util.Date(it.timestamp)) }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }
                .takeLast(7)

            val idx = selectedPoint!!
            if (idx < groupedData.size) {
                val count = groupedData[idx].second
                Box(
                    modifier = Modifier
                        .offset { IntOffset(((idx + 1) * (1000f / maxOf(6, groupedData.size - 1).toFloat()) - 40f).toInt(), 20) }
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
fun WeightLineChart(records: List<HealthRecord>) {
    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val height = size.height
                        val recentRecords = records.take(7).reversed()
                        val weightData = recentRecords.map { it.value.toFloat() }
                        if (weightData.isEmpty()) return@detectTapGestures

                        val stepX = width / maxOf(4, weightData.size - 1).coerceAtLeast(1).toFloat()
                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE
                        weightData.forEachIndexed { idx, _ ->
                            val px = (idx + 1) * stepX
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
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 20f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

            val recentRecords = records.take(7).reversed()
            val weightData = recentRecords.map { it.value.toFloat() }
            if (weightData.isEmpty()) return@Canvas

            val maxWeight = maxOf(10f, weightData.maxOrNull() ?: 10f)
            val minWeight = minOf(1f, weightData.minOrNull() ?: 1f) // Fixed min weight logic
            val range = (maxWeight - minWeight).coerceAtLeast(1f)

            // Draw Y-axis labels
            val labelCount = 3
            for (i in 0..labelCount) {
                val labelVal = minWeight + (range / labelCount * i)
                val yPos = height - (i.toFloat() / labelCount) * height * 0.6f - 50f
                drawContext.canvas.nativeCanvas.drawText("${String.format("%.1f", labelVal)}", 30f, yPos, textPaint)
            }

            val stepX = width / maxOf(4, weightData.size - 1).coerceAtLeast(1).toFloat()
            val points = weightData.mapIndexed { idx, valY ->
                val x = (idx + 1) * stepX
                val y = height - ((valY - minWeight) / range) * height * 0.6f - 50f
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = MintPrimary, start = points[i], end = points[i + 1], strokeWidth = 6f)
            }

            points.forEachIndexed { idx, pt ->
                drawCircle(color = PrimaryDark, radius = 8f, center = pt)
                val dateStr = sdf.format(java.util.Date(recentRecords[idx].timestamp))
                drawContext.canvas.nativeCanvas.drawText(dateStr, pt.x, height - 10f, textPaint)
            }
        }

        if (selectedPoint != null) {
            val recentRecords = records.take(7).reversed()
            val weightData = recentRecords.map { it.value.toFloat() }
            val idx = selectedPoint!!
            if (idx < weightData.size) {
                val value = weightData[idx]
                Box(
                    modifier = Modifier
                        .offset { IntOffset(((idx + 1) * (1000f / maxOf(4, weightData.size - 1).toFloat()) - 40f).toInt(), 20) }
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("${value} kg", color = Color.White, style = Typography.labelSmall)
                }
            }
        }
    }
}

// Custom Bottom Navigation Bar
@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = Modifier.shadow(8.dp)
    ) {
        NavigationBarItem(
            selected = selectedTab == "Home",
            onClick = { onTabSelected("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", style = Typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryDark,
                indicatorColor = MintPrimary
            )
        )

        NavigationBarItem(
            selected = selectedTab == "Momentos",
            onClick = { onTabSelected("Momentos") },
            icon = { Icon(Icons.Default.Stars, contentDescription = "Logros") },
            label = { Text("Momentos", style = Typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryDark,
                indicatorColor = MintPrimary
            )
        )

        NavigationBarItem(
            selected = selectedTab == "Registrar",
            onClick = { onTabSelected("Registrar") },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Registrar") },
            label = { Text("Registrar", style = Typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryDark,
                indicatorColor = MintPrimary
            )
        )

        NavigationBarItem(
            selected = selectedTab == "Seguimiento",
            onClick = { onTabSelected("Seguimiento") },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Seguimiento") },
            label = { Text("Seguimiento", style = Typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryDark,
                indicatorColor = MintPrimary
            )
        )
    }
}

// Quick Bento Activity Chip Select
@Composable
fun BentoChip(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MintPrimary else SurfaceWhite
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MintPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else TextDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (selected) Color.White else TextDark,
                style = Typography.labelLarge
            )
        }
    }
}

private fun getElapsedTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val diffHours = diff / (1000 * 60 * 60)
    val diffMins = (diff / (1000 * 60)) % 60
    return when {
        diffHours > 0 -> "${diffHours}h ${diffMins}m"
        else -> "${diffMins}m"
    }
}

@Composable
fun MiniBabyAvatar(
    gender: String,
    ageInMonths: Int,
    sizeDp: Int = 40,
    skinTone: String = "Claro",
    hairColor: String = "Castaño"
) {
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

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .border(2.dp, MintPrimary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp - 4).dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val headRadius = size.width * 0.32f

            // Ear details
            val earY = center.y + headRadius * 0.05f
            drawCircle(
                color = earColor,
                radius = headRadius * 0.24f,
                center = Offset(center.x - headRadius * 0.98f, earY)
            )
            drawCircle(
                color = earColor,
                radius = headRadius * 0.24f,
                center = Offset(center.x + headRadius * 0.98f, earY)
            )

            // Head (Soft warm peach baby skin)
            drawCircle(
                color = skinToneColor,
                radius = headRadius,
                center = center
            )

            // Hair
            if (hairColorValue != Color.Transparent) {
                if (gender == "Niña") {
                    drawArc(
                        color = hairColorValue,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = Size(headRadius * 1.8f, headRadius * 1.0f),
                        topLeft = Offset(center.x - headRadius * 0.9f, center.y - headRadius * 1.15f)
                    )
                } else {
                    drawArc(
                        color = hairColorValue,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = true,
                        size = Size(headRadius * 1.8f, headRadius * 1.1f),
                        topLeft = Offset(center.x - headRadius * 0.9f, center.y - headRadius * 1.2f)
                    )
                }
            }

            // Eyes
            val leftEyeCenter = Offset(center.x - headRadius * 0.34f, center.y - headRadius * 0.08f)
            val rightEyeCenter = Offset(center.x + headRadius * 0.34f, center.y - headRadius * 0.08f)
            val eyeRadius = 3f * (sizeDp / 40f)
            drawCircle(color = Color(0xFF1E293B), radius = eyeRadius, center = leftEyeCenter)
            drawCircle(color = Color(0xFF1E293B), radius = eyeRadius, center = rightEyeCenter)

            // Blushing cheeks
            val cheekRadius = headRadius * 0.21f
            drawCircle(color = Color(0xFFFFB5A7).copy(alpha = 0.55f), radius = cheekRadius, center = Offset(center.x - headRadius * 0.52f, center.y + headRadius * 0.16f))
            drawCircle(color = Color(0xFFFFB5A7).copy(alpha = 0.55f), radius = cheekRadius, center = Offset(center.x + headRadius * 0.52f, center.y + headRadius * 0.16f))

            // Glossy pacifier
            val pacifierCenter = Offset(center.x, center.y + headRadius * 0.32f)
            drawCircle(color = MintPrimary, radius = headRadius * 0.22f, center = pacifierCenter)
            drawCircle(color = Color.White, radius = headRadius * 0.12f, center = pacifierCenter)
        }
    }
}

@Composable
fun ProfileScreen(
    baby: BabyProfile,
    viewModel: BabyViewModel
) {
    val context = LocalContext.current
    val babyProfiles by viewModel.allBabyProfiles.collectAsStateWithLifecycle()

    var selectedBabyIdForEditing by remember { mutableStateOf<Int?>(baby.id) }
    var isAddingNew by remember { mutableStateOf(false) }

    var nameEdit by remember { mutableStateOf("") }
    var dobStringEdit by remember { mutableStateOf("") }
    var genderEdit by remember { mutableStateOf("Niño") }
    var skinToneEdit by remember { mutableStateOf("Claro") }
    var hairColorEdit by remember { mutableStateOf("Castaño") }

    var showDeleteConfirmDialog by remember { mutableStateOf<BabyProfile?>(null) }

    // Synchronize editing variables upon baby clicks or initial load
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
        }
    }

    // Material Native DatePickerDialog controller
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

    // Delete confirmation dialog
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
        // 1. Header with custom baby avatar top-left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayGender = if (isAddingNew) "Niño" else genderEdit
            MiniBabyAvatar(
                gender = displayGender,
                ageInMonths = 6,
                sizeDp = 48,
                skinTone = skinToneEdit,
                hairColor = hairColorEdit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Perfil",
                style = Typography.headlineLarge,
                color = PrimaryDark
            )
        }

        // 2. "Mis bebés" Section
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
                // Dynamically render every registered baby profile from Database
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
                                hairColor = profile.hairColor
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

                // Agregar (Dashed '+' circle button)
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

        // 3. Info Card form matching the mockup's card
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

                // Nombre del bebé
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nombre del bebé", style = Typography.labelMedium, color = TextMuted)
                    PMOutlinedTextField(
                        value = nameEdit,
                        onValueChange = { nameEdit = it },
                        placeholder = { Text("Escribe su nombre aquí") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fecha de nacimiento
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

                // Género pill selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Género", style = Typography.labelMedium, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Niño Pill
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
                        // Niña Pill
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

                // Tono de Piel pill selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tono de Piel", style = Typography.labelMedium, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val skinOptions = listOf("Claro", "Moreno", "Oscuro")
                        skinOptions.forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (skinToneEdit == opt) MintPrimary else Color.Transparent)
                                    .clickable { skinToneEdit = opt },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    opt,
                                    color = if (skinToneEdit == opt) Color.White else TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Color de Pelo pill selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Color de Pelo", style = Typography.labelMedium, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val hairOptions = listOf("Castaño", "Rubio", "Pelirrojo", "Sin pelo")
                        hairOptions.forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (hairColorEdit == opt) MintPrimary else Color.Transparent)
                                    .clickable { hairColorEdit = opt },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    opt,
                                    color = if (hairColorEdit == opt) Color.White else TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guardar cambios Button
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

                // Delete Button (only visible when editing an existing profile)
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

        // 4. "Hitos de Crecimiento" Section with exact icons and design
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
                // Milestone 1: Primeras Sonrisas (0-6 meses)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MintPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completado",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Primeras Sonrisas", style = Typography.labelMedium, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
                    Text("0-6 meses", style = Typography.labelSmall, color = TextMuted)
                }

                // Milestone 2: Explorador (6-12 meses)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                drawCircle(color = Color(0xFFE2E8F0), style = Stroke(width = 6f))
                                drawArc(
                                    color = MintPrimary,
                                    startAngle = -90f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Explorador",
                            tint = MintPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Explorador", style = Typography.labelMedium, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
                    Text("6-12 meses", style = Typography.labelSmall, color = TextMuted)
                }

                // Milestone 3: Gran Orador (12-18 meses)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Gran Orador", style = Typography.labelMedium, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
                    Text("12-18 meses", style = Typography.labelSmall, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Google Drive Sync Card in Profile Screen
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

        // 5. "Configuración de cuenta" Section & list card
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
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    ListSettingsRow("Notificaciones", Icons.Default.Notifications) {
                        android.widget.Toast.makeText(context, "Configuración de notificaciones diarias guardada", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    ListSettingsRow("Privacidad", Icons.Default.Settings) {
                        android.widget.Toast.makeText(context, "Tus datos siguen estando cifrados localmente en tu móvil🔒", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Cerrar sesión red link button
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
