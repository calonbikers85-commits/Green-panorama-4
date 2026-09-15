package com.example.ui.screens.auth

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.AccountStatus
import com.example.data.model.UserProfile
import com.example.ui.components.CameraPhotoPicker
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B3820),
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.gp4_icon_1789428576769),
                        contentDescription = "Logo Green Panorama 4",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GREEN PANORAMA 4",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sistem Layanan Digital & Keamanan Perumahan",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFD54F),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Versi 1.0.0 • Online Smart Residential",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun LoginScreen(
    repository: FirebaseRepository,
    onLoginSuccess: (UserProfile) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Complex Banner / Logo
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.gp4_icon_1789428576769),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp).clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GREEN PANORAMA 4",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Silakan masuk dengan akun warga atau petugas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Role Quick Selectors (Convenience for testing RBAC)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Akses Cepat Pengujian (RBAC Demo):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = email == "admin@greenpanorama.com",
                        onClick = {
                            email = "admin@greenpanorama.com"
                            password = "password123"
                        },
                        label = { Text("ADMIN", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = email == "security@greenpanorama.com",
                        onClick = {
                            email = "security@greenpanorama.com"
                            password = "password123"
                        },
                        label = { Text("SECURITY", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = email == "warga@greenpanorama.com",
                        onClick = {
                            email = "warga@greenpanorama.com"
                            password = "password123"
                        },
                        label = { Text("WARGA", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            placeholder = { Text("contoh@greenpanorama.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Lupa Password?", style = MaterialTheme.typography.bodySmall)
            }
        }

        if (errorMessage != null) {
            Surface(
                color = EmergencyRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = EmergencyRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email dan password wajib diisi."
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    val result = repository.login(email, password)
                    isLoading = false
                    result.fold(
                        onSuccess = { user -> onLoginSuccess(user) },
                        onFailure = { err -> errorMessage = err.localizedMessage ?: "Gagal masuk." }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text("MASUK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Belum punya akun warga?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "Daftar Di Sini",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    repository: FirebaseRepository,
    onRegisterSuccess: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("A") }
    var houseStatus by remember { mutableStateOf("Pemilik") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var vehicleType by remember { mutableStateOf("Mobil") }
    var vehiclePlate by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val blocks = listOf("A", "B", "C", "D", "E")
    val houseStatuses = listOf("Pemilik", "Penyewa")
    val vehicleTypes = listOf("Mobil", "Motor", "Tidak Ada")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendaftaran Warga GP4") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mohon isi data tempat tinggal Anda dengan benar. Akun warga memerlukan verifikasi Pengurus RW/Admin sebelum dapat mengakses penuh.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap*") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP / WhatsApp*") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email*") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password* (Min 6 Karakter)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Blok Rumah*", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        blocks.forEach { b ->
                            FilterChip(
                                selected = block == b,
                                onClick = { block = b },
                                label = { Text(b) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = houseNumber,
                    onValueChange = { houseNumber = it },
                    label = { Text("No. Rumah*") },
                    placeholder = { Text("01") },
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                Text("Status Rumah*", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    houseStatuses.forEach { st ->
                        FilterChip(
                            selected = houseStatus == st,
                            onClick = { houseStatus = st },
                            label = { Text(st) }
                        )
                    }
                }
            }

            // Photo picker with Camera & Gallery
            CameraPhotoPicker(
                label = "Foto Profil Warga / Identitas:",
                currentPhotoUrl = photoUri?.toString(),
                onPhotoSelected = { uri -> photoUri = uri }
            )

            // Vehicle info
            Text("Data Kendaraan Utama:", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vehicleTypes.forEach { vt ->
                    FilterChip(
                        selected = vehicleType == vt,
                        onClick = { vehicleType = vt },
                        label = { Text(vt) }
                    )
                }
            }

            if (vehicleType != "Tidak Ada") {
                OutlinedTextField(
                    value = vehiclePlate,
                    onValueChange = { vehiclePlate = it.uppercase() },
                    label = { Text("Nomor Polisi / Plat Kendaraan") },
                    placeholder = { Text("B 1234 KLA") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = EmergencyRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() || houseNumber.isBlank()) {
                        errorMessage = "Harap lengkapi semua data bertanda bintang (*)."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password minimal 6 karakter."
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        var uploadedPhoto = ""
                        if (photoUri != null) {
                            uploadedPhoto = repository.uploadImage("profiles", photoUri!!)
                        }

                        val result = repository.registerWarga(
                            name = name,
                            phone = phone,
                            email = email,
                            pass = password,
                            houseNumber = houseNumber,
                            block = block,
                            houseStatus = houseStatus,
                            profilePhotoUrl = uploadedPhoto,
                            vehicleType = vehicleType,
                            vehiclePlate = vehiclePlate
                        )
                        isLoading = false
                        result.fold(
                            onSuccess = { user -> onRegisterSuccess(user) },
                            onFailure = { err -> errorMessage = err.localizedMessage }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("DAFTAR SEBAGAI WARGA", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    repository: FirebaseRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lupa Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Atur Ulang Kata Sandi",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Masukkan alamat email Anda yang terdaftar di sistem Green Panorama 4. Kami akan mengirimkan tautan untuk mengatur ulang kata sandi Anda.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = null
                },
                label = { Text("Email Terdaftar") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            if (message != null) {
                Surface(
                    color = if (isSuccess) SuccessGreen.copy(alpha = 0.1f) else EmergencyRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message ?: "",
                        color = if (isSuccess) SuccessGreen else EmergencyRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (email.isBlank()) {
                        message = "Email tidak boleh kosong."
                        isSuccess = false
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        val res = repository.forgotPassword(email)
                        isLoading = false
                        res.fold(
                            onSuccess = { msg ->
                                isSuccess = true
                                message = msg
                            },
                            onFailure = { err ->
                                isSuccess = false
                                message = err.localizedMessage
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("KIRIM LINK RESET")
                }
            }
        }
    }
}

@Composable
fun AccountStatusScreen(
    user: UserProfile,
    onLogout: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val (icon, title, desc, color) = when (user.status) {
                AccountStatus.MENUNGGU_VERIFIKASI -> Quadruple(
                    Icons.Default.HourglassTop,
                    "MENUNGGU VERIFIKASI ADMIN",
                    "Akun Anda untuk rumah Blok ${user.block}-${user.houseNumber} telah berhasil dibuat dan saat ini sedang dalam proses verifikasi oleh Pengurus RW Green Panorama 4 untuk keamanan lingkungan komplek.",
                    WarningAmber
                )
                AccountStatus.DITOLAK -> Quadruple(
                    Icons.Default.Cancel,
                    "AKUN TIDAK DISETUJUI",
                    "Pengajuan akun warga Anda tidak disetujui oleh admin. Silakan hubungi pengurus RT/RW atau sekretariat komplek Green Panorama 4.",
                    EmergencyRed
                )
                AccountStatus.NONAKTIF -> Quadruple(
                    Icons.Default.Block,
                    "AKUN DINONAKTIFKAN",
                    "Akun Anda dinonaktifkan sementara oleh Pengurus komplek. Hubungi admin untuk aktivasi kembali.",
                    Color.Gray
                )
                else -> Quadruple(
                    Icons.Default.CheckCircle,
                    "AKUN AKTIF",
                    "Akun Anda sudah aktif.",
                    SuccessGreen
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(76.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = color
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nama: ${user.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                    Text("Rumah: Blok ${user.block} No. ${user.houseNumber}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status Rumah: ${user.houseStatus}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PERIKSA STATUS TERKINI")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("KELUAR (LOGOUT)")
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
