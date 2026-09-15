package com.example.ui.screens.security

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.EmergencyReport
import com.example.data.model.GuestPass
import com.example.data.model.SecurityEvent
import com.example.data.model.UserProfile
import com.example.ui.components.CameraPhotoPicker
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GpTopAppBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityMainScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val guestPasses by repository.guestPasses.collectAsState()
    val emergencyReports by repository.emergencyReports.collectAsState()
    val securityEvents by repository.securityEvents.collectAsState()
    val activeEmergencies = remember(emergencyReports) { emergencyReports.filter { it.status == "AKTIF" } }

    var scannedPassCode by remember { mutableStateOf<String?>(null) }
    var showScanQrDialog by remember { mutableStateOf(false) }
    var showAddIncidentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GpTopAppBar(
                title = "POS KEAMANAN UTAMA",
                subtitle = "Komandan: ${currentUser.name}",
                roleBadge = "REGU SECURITY GP4",
                unreadNotifCount = activeEmergencies.size,
                onNotifClick = { selectedTab = 3 }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    label = { Text("Scan QR") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    label = { Text("Tamu") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Kendaraan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeEmergencies.isNotEmpty()) {
                                    Badge(containerColor = EmergencyRed) { Text("${activeEmergencies.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                        }
                    },
                    label = { Text("Darurat") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                    label = { Text("Insiden") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Urgent SOS Alert Bar if active
            if (activeEmergencies.isNotEmpty()) {
                Surface(
                    color = EmergencyRed,
                    modifier = Modifier.fillMaxWidth().clickable { selectedTab = 3 }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚨 PERINGATAN DARURAT AKTIF (${activeEmergencies.size})",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            val first = activeEmergencies.first()
                            Text(
                                text = "${first.userName} (Rumah ${first.houseNumber}) - ${first.category}",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { selectedTab = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmergencyRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("LIHAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> SecurityScannerTab(
                    guestPasses = guestPasses,
                    onQrScanned = { code -> scannedPassCode = code },
                    onOpenScannerCamera = { showScanQrDialog = true }
                )
                1 -> SecurityGuestsTab(
                    guestPasses = guestPasses,
                    onUpdateStatus = { passId, newStatus ->
                        repository.updateGuestPassStatus(passId, newStatus)
                    }
                )
                2 -> SecurityVehiclesTab(guestPasses = guestPasses)
                3 -> SecurityEmergencyTab(
                    emergencies = emergencyReports,
                    onResolve = { id -> repository.resolveEmergency(id, currentUser.name) }
                )
                4 -> SecurityIncidentsTab(
                    incidents = securityEvents,
                    onAddIncident = { showAddIncidentDialog = true },
                    onLogout = onLogout
                )
            }
        }
    }

    // Modal Camera QR Scanner Dialog
    if (showScanQrDialog) {
        CameraQrScannerDialog(
            onDismiss = { showScanQrDialog = false },
            onCodeDetected = { code ->
                scannedPassCode = code
                showScanQrDialog = false
            }
        )
    }

    // Verified Pass Detail Bottom Sheet / Dialog
    if (scannedPassCode != null) {
        val foundPass = guestPasses.find { it.id == scannedPassCode || it.qrCode == scannedPassCode }
        GuestPassValidationDialog(
            passCode = scannedPassCode!!,
            pass = foundPass,
            onDismiss = { scannedPassCode = null },
            onAllowEntry = { passId ->
                repository.updateGuestPassStatus(passId, "MASUK")
                scannedPassCode = null
            },
            onRecordExit = { passId ->
                repository.updateGuestPassStatus(passId, "KELUAR")
                scannedPassCode = null
            }
        )
    }

    if (showAddIncidentDialog) {
        AddSecurityIncidentDialog(
            reportedBy = currentUser.name,
            repository = repository,
            onDismiss = { showAddIncidentDialog = false }
        )
    }
}

// ----------------------------------------------------------------------
// TAB 0: SCANNER & MANUAL LOOKUP
// ----------------------------------------------------------------------
@Composable
fun SecurityScannerTab(
    guestPasses: List<GuestPass>,
    onQrScanned: (String) -> Unit,
    onOpenScannerCamera: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Scan QR Tamu Masuk / Keluar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Arahkan kamera ke QR Code tamu warga untuk verifikasi izin masuk otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onOpenScannerCamera,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BUKA KAMERA SCANNER", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Manual Pass Code Search
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Input Kode Tamu Manual",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it.uppercase() },
                        placeholder = { Text("Contoh: GP4-PASS-8821") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                onQrScanned(manualInput.trim())
                            }
                        }
                    ) {
                        Text("CEK")
                    }
                }
            }
        }

        // Quick active list
        Text(
            text = "Daftar Tamu Siap Diperiksa:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )

        val pendingOrIn = guestPasses.filter { it.status == "MENUNGGU" || it.status == "MASUK" }
        if (pendingOrIn.isEmpty()) {
            EmptyStateView(
                title = "Tidak Ada Tamu Aktif",
                description = "Saat ini belum ada tamu dengan status menunggu atau berada di dalam komplek."
            )
        } else {
            pendingOrIn.forEach { pass ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQrScanned(pass.qrCode) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pass.guestName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Tujuan: ${pass.userHouse} (${pass.userName})", style = MaterialTheme.typography.bodySmall)
                            Text("Plat: ${pass.plateNumber} • ${pass.vehicleType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        StatusBadge(status = pass.status)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// CAMERA QR SCANNER MODAL (CameraX Preview)
// ----------------------------------------------------------------------
@Composable
fun CameraQrScannerDialog(
    onDismiss: () -> Unit,
    onCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview
                                    )
                                } catch (exc: Exception) {
                                    exc.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Target scanning frame overlay
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                    ) {
                        Surface(
                            color = Color.Transparent,
                            border = ButtonDefaults.outlinedButtonBorder(true),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Izin kamera diperlukan untuk memindai QR.", color = Color.White)
                    }
                }

                // Top bar in dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Arahkan ke QR Code Tamu",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                // Demo trigger button to simulate scanning a pass
                Button(
                    onClick = {
                        // Scan detected sample code
                        onCodeDetected("GP4-GUEST-8821")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("DETEKSI QR OTOMATIS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// VALIDATION DIALOG
// ----------------------------------------------------------------------
@Composable
fun GuestPassValidationDialog(
    passCode: String,
    pass: GuestPass?,
    onDismiss: () -> Unit,
    onAllowEntry: (String) -> Unit,
    onRecordExit: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (pass != null) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (pass != null) SuccessGreen else EmergencyRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (pass != null) "Izin Tamu Valid" else "Data Tidak Ditemukan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            if (pass != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nama Tamu: ${pass.guestName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Rumah Tujuan: ${pass.userHouse}", fontWeight = FontWeight.SemiBold)
                            Text("Warga Tuan Rumah: ${pass.userName}")
                            Text("Tanggal: ${pass.date}")
                            Text("Jam Kunjungan: ${pass.arrivalTime} - ${pass.departureTime}")
                            Text("Kendaraan: ${pass.vehicleType} (${pass.plateNumber})")
                            Text("Keperluan: ${pass.purpose}")
                            if (pass.notes.isNotEmpty()) {
                                Text("Catatan: ${pass.notes}", color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Status Saat Ini: ")
                                StatusBadge(status = pass.status)
                            }
                        }
                    }
                }
            } else {
                Text("Kode QR '$passCode' tidak terdaftar di sistem Green Panorama 4. Mohon periksa kembali atau minta warga membuat izin baru.")
            }
        },
        confirmButton = {
            if (pass != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pass.status != "MASUK") {
                        Button(
                            onClick = { onAllowEntry(pass.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("IZINKAN MASUK")
                        }
                    }
                    if (pass.status == "MASUK") {
                        Button(
                            onClick = { onRecordExit(pass.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text("CATAT TAMU KELUAR")
                        }
                    }
                }
            } else {
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("TUTUP") }
        }
    )
}

// ----------------------------------------------------------------------
// TAB 1: GUEST LIST
// ----------------------------------------------------------------------
@Composable
fun SecurityGuestsTab(
    guestPasses: List<GuestPass>,
    onUpdateStatus: (String, String) -> Unit
) {
    var filterStatus by remember { mutableStateOf("ALL") }

    val filtered = remember(guestPasses, filterStatus) {
        if (filterStatus == "ALL") guestPasses else guestPasses.filter { it.status == filterStatus }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "MENUNGGU", "MASUK", "KELUAR").forEach { st ->
                FilterChip(
                    selected = filterStatus == st,
                    onClick = { filterStatus = st },
                    label = { Text(st, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            EmptyStateView(
                title = "Tidak Ada Data Tamu",
                description = "Daftar tamu dengan filter $filterStatus kosong."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { pass ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pass.guestName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                StatusBadge(status = pass.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tujuan: ${pass.userHouse} (${pass.userName})", style = MaterialTheme.typography.bodyMedium)
                            Text("🚗 ${pass.vehicleType} • Plat: ${pass.plateNumber}", style = MaterialTheme.typography.bodySmall)
                            Text("Waktu: ${pass.date} (${pass.arrivalTime} - ${pass.departureTime})", style = MaterialTheme.typography.bodySmall)

                            if (pass.checkedInAt != null) {
                                Text(
                                    "Waktu Masuk: ${SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date(pass.checkedInAt))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                            if (pass.checkedOutAt != null) {
                                Text(
                                    "Waktu Keluar: ${SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date(pass.checkedOutAt))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (pass.status == "MENUNGGU") {
                                    Button(
                                        onClick = { onUpdateStatus(pass.id, "MASUK") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("IZINKAN MASUK", fontSize = 11.sp)
                                    }
                                }
                                if (pass.status == "MASUK") {
                                    Button(
                                        onClick = { onUpdateStatus(pass.id, "KELUAR") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("CATAT KELUAR", fontSize = 11.sp)
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

// ----------------------------------------------------------------------
// TAB 2: KENDARAAN (VEHICLES LOG)
// ----------------------------------------------------------------------
@Composable
fun SecurityVehiclesTab(guestPasses: List<GuestPass>) {
    var searchQuery by remember { mutableStateOf("") }

    val vehicleLogs = remember(guestPasses, searchQuery) {
        val list = guestPasses.filter { it.plateNumber.isNotEmpty() }
        if (searchQuery.isBlank()) list else list.filter {
            it.plateNumber.contains(searchQuery, ignoreCase = true) ||
            it.guestName.contains(searchQuery, ignoreCase = true) ||
            it.userHouse.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Cari Nomor Plat / Nama") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (vehicleLogs.isEmpty()) {
            EmptyStateView(
                title = "Kendaraan Tidak Ditemukan",
                description = "Tidak ada riwayat kendaraan yang sesuai dengan pencarian."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vehicleLogs) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.plateNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "${item.vehicleType} • Pengemudi: ${item.guestName}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Tujuan: ${item.userHouse}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(status = item.status)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// TAB 3: DARURAT (EMERGENCY ALERTS)
// ----------------------------------------------------------------------
@Composable
fun SecurityEmergencyTab(
    emergencies: List<EmergencyReport>,
    onResolve: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Laporan Sinyal Darurat (SOS)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (emergencies.isEmpty()) {
            EmptyStateView(
                title = "Situasi Aman Terkendali",
                description = "Tidak ada laporan sinyal darurat dari warga saat ini.",
                icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(56.dp)) }
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(emergencies) { emg ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (emg.status == "AKTIF") Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (emg.status == "AKTIF") EmergencyRed else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        emg.category.uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (emg.status == "AKTIF") EmergencyRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                StatusBadge(status = emg.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Warga: ${emg.userName} (Rumah ${emg.houseNumber})",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (emg.description.isNotEmpty()) {
                                Text("Keterangan: ${emg.description}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (emg.latitude != null && emg.longitude != null) {
                                Text("📍 Koordinat GPS: ${emg.latitude}, ${emg.longitude}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID")).format(Date(emg.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (emg.status == "AKTIF") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onResolve(emg.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TANDAI SUDAH DITANGANI / SELESAI", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// TAB 4: LAPORAN KEAMANAN & INSIDEN
// ----------------------------------------------------------------------
@Composable
fun SecurityIncidentsTab(
    incidents: List<SecurityEvent>,
    onAddIncident: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Buku Catatan Keamanan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Button(onClick = onAddIncident) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("CATAT KEJADIAN")
            }
        }

        // Roster / Jadwal Regu Security
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("📋 Jadwal Piket Pos Gerbang GP4:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Shift Pagi (07:00 - 15:00): Slamet Riyadi & Joko Santoso", style = MaterialTheme.typography.bodySmall)
                Text("• Shift Sore (15:00 - 23:00): Rudi Hartono & Agus Salim", style = MaterialTheme.typography.bodySmall)
                Text("• Shift Malam (23:00 - 07:00): Herman & Dedi Prasetya", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text(
            text = "Riwayat Kejadian / Patroli:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        if (incidents.isEmpty()) {
            EmptyStateView(
                title = "Belum Ada Catatan Kejadian",
                description = "Gunakan tombol 'Catat Kejadian' untuk merekam patroli atau temuan keamanan."
            )
        } else {
            incidents.forEach { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text("📍 ${item.location} • Oleh: ${item.reportedBy}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("KELUAR DARI AKUN SECURITY")
        }
    }
}

@Composable
fun AddSecurityIncidentDialog(
    reportedBy: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Gerbang Utama GP4") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Kejadian Keamanan") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Kejadian*") },
                    placeholder = { Text("Contoh: Patroli Malam Blok A-C Aman") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi Temuan / Patroli*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Rincian Kejadian*") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                CameraPhotoPicker(
                    label = "Foto Dokumentasi Kejadian:",
                    currentPhotoUrl = photoUri?.toString(),
                    onPhotoSelected = { uri -> photoUri = uri }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        isUploading = true
                        coroutineScope.launch {
                            var uploaded = ""
                            if (photoUri != null) {
                                uploaded = repository.uploadImage("security", photoUri!!)
                            }
                            val event = SecurityEvent(
                                id = "SEC-" + UUID.randomUUID().toString().take(6).uppercase(),
                                reportedBy = reportedBy,
                                title = title,
                                location = location,
                                description = description,
                                photoUrl = uploaded
                            )
                            // Log and notify
                            repository.logAudit(reportedBy, "LAPORAN_KEAMANAN", "$title di $location")
                            isUploading = false
                            onDismiss()
                        }
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("SIMPAN CATATAN")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}
