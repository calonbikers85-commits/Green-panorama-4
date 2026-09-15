package com.example.ui.screens.admin

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    var selectedBottomNav by remember { mutableStateOf(0) }
    var activeAdminSubMenu by remember { mutableStateOf<String?>(null) }
    var showEmergencyBroadcastDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val users by repository.users.collectAsState()
    val paymentBills by repository.paymentBills.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val facilityReports by repository.facilityReports.collectAsState()
    val guestPasses by repository.guestPasses.collectAsState()
    val announcements by repository.announcements.collectAsState()
    val emergencyReports by repository.emergencyReports.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val complexInfo by repository.complexInfo.collectAsState()

    val pendingUsers = remember(users) { users.filter { it.status == AccountStatus.MENUNGGU_VERIFIKASI } }
    val pendingPayments = remember(paymentBills) { paymentBills.filter { it.status == "MENUNGGU VERIFIKASI" } }
    val activeEmergencies = remember(emergencyReports) { emergencyReports.filter { it.status == "AKTIF" } }

    if (activeAdminSubMenu != null) {
        when (activeAdminSubMenu) {
            "WARGA" -> AdminCitizensSubScreen(
                repository = repository,
                users = users,
                adminName = currentUser.name,
                onBack = { activeAdminSubMenu = null }
            )
            "KEUANGAN" -> AdminFinancesSubScreen(
                repository = repository,
                bills = paymentBills,
                expenses = expenses,
                adminName = currentUser.name,
                onBack = { activeAdminSubMenu = null }
            )
            "LAPORAN" -> AdminReportsSubScreen(
                repository = repository,
                reports = facilityReports,
                adminName = currentUser.name,
                onBack = { activeAdminSubMenu = null }
            )
            "PENGUMUMAN" -> AdminAnnouncementsSubScreen(
                repository = repository,
                announcements = announcements,
                adminName = currentUser.name,
                onBack = { activeAdminSubMenu = null }
            )
            "FASILITAS" -> AdminFacilitiesSubScreen(
                repository = repository,
                onBack = { activeAdminSubMenu = null }
            )
            "TAMU" -> AdminGuestLogsSubScreen(
                guestPasses = guestPasses,
                onBack = { activeAdminSubMenu = null }
            )
            "AUDIT" -> AdminAuditLogsSubScreen(
                auditLogs = auditLogs,
                onBack = { activeAdminSubMenu = null }
            )
            "PENGATURAN" -> AdminSettingsSubScreen(
                complexInfo = complexInfo,
                repository = repository,
                onBack = { activeAdminSubMenu = null }
            )
        }
        return
    }

    Scaffold(
        topBar = {
            GpTopAppBar(
                title = "PANEL PENGURUS RW",
                subtitle = "Administrator: ${currentUser.name}",
                roleBadge = "ADMIN PUSAT GP4",
                unreadNotifCount = pendingUsers.size + pendingPayments.size + activeEmergencies.size,
                onNotifClick = { activeAdminSubMenu = "WARGA" },
                onEmergencyClick = { showEmergencyBroadcastDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = { selectedBottomNav = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Ringkasan") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = { selectedBottomNav = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingUsers.isNotEmpty()) Badge { Text("${pendingUsers.size}") }
                        }) {
                            Icon(Icons.Default.People, contentDescription = null)
                        }
                    },
                    label = { Text("Warga") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = { selectedBottomNav = 2 },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingPayments.isNotEmpty()) Badge { Text("${pendingPayments.size}") }
                        }) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                        }
                    },
                    label = { Text("Keuangan") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 3,
                    onClick = { selectedBottomNav = 3 },
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    label = { Text("Menu RW") }
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
            when (selectedBottomNav) {
                0 -> AdminOverviewTab(
                    users = users,
                    paymentBills = paymentBills,
                    facilityReports = facilityReports,
                    guestPasses = guestPasses,
                    activeEmergencies = activeEmergencies,
                    onNavigate = { key -> activeAdminSubMenu = key }
                )
                1 -> AdminCitizensSubScreen(
                    repository = repository,
                    users = users,
                    adminName = currentUser.name,
                    onBack = { selectedBottomNav = 0 }
                )
                2 -> AdminFinancesSubScreen(
                    repository = repository,
                    bills = paymentBills,
                    expenses = expenses,
                    adminName = currentUser.name,
                    onBack = { selectedBottomNav = 0 }
                )
                3 -> AdminMenuTab(
                    onSelectMenu = { key -> activeAdminSubMenu = key },
                    onLogout = onLogout
                )
            }
        }
    }

    if (showEmergencyBroadcastDialog) {
        EmergencyBroadcastDialog(
            adminName = currentUser.name,
            repository = repository,
            onDismiss = { showEmergencyBroadcastDialog = false }
        )
    }
}

// ----------------------------------------------------------------------
// OVERVIEW TAB: KPIS & METRICS
// ----------------------------------------------------------------------
@Composable
fun AdminOverviewTab(
    users: List<UserProfile>,
    paymentBills: List<PaymentBill>,
    facilityReports: List<FacilityReport>,
    guestPasses: List<GuestPass>,
    activeEmergencies: List<EmergencyReport>,
    onNavigate: (String) -> Unit
) {
    val citizens = remember(users) { users.filter { it.role == UserRole.WARGA } }
    val occupiedHouses = remember(citizens) { citizens.map { "${it.block}-${it.houseNumber}" }.distinct().size }
    val totalHousesCapacity = 120 // Komplek Green Panorama 4 total plots
    val paidThisMonth = remember(paymentBills) {
        paymentBills.filter { it.status == "LUNAS" }.sumOf { it.amount }
    }
    val pendingReports = remember(facilityReports) {
        facilityReports.filter { it.status != "SELESAI" }
    }
    val guestsToday = remember(guestPasses) { guestPasses.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeEmergencies.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onNavigate("TAMU") }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🚨 ADA ${activeEmergencies.size} SINYAL DARURAT AKTIF!",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text("Periksa lokasi dan koordinasikan dengan regu security.", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Text(
            text = "Indikator Utama Komplek (Real-time)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // KPI Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Warga",
                value = "${citizens.size} Orang",
                subtitle = "$occupiedHouses Terisi / ${totalHousesCapacity - occupiedHouses} Kosong",
                icon = Icons.Default.People,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("WARGA") }
            )
            KpiCard(
                title = "Iuran Terkumpul",
                value = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(paidThisMonth)}",
                subtitle = "${paymentBills.count { it.status == "MENUNGGU VERIFIKASI" }} Menunggu Cek",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("KEUANGAN") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Laporan Fasilitas",
                value = "${pendingReports.size} Aktif",
                subtitle = "${facilityReports.count { it.status == "SELESAI" }} Sudah Tuntas",
                icon = Icons.Default.Campaign,
                color = Color(0xFFE65100),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("LAPORAN") }
            )
            KpiCard(
                title = "Tamu Hari Ini",
                value = "$guestsToday Kunjungan",
                subtitle = "${guestPasses.count { it.status == "MASUK" }} Sedang di Komplek",
                icon = Icons.Default.Badge,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("TAMU") }
            )
        }

        // Quick verification alert cards
        val pendingUsers = citizens.filter { it.status == AccountStatus.MENUNGGU_VERIFIKASI }
        if (pendingUsers.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onNavigate("WARGA") }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚠️ Ada ${pendingUsers.size} Warga Baru Menunggu Verifikasi",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "Verifikasi data tempat tinggal agar warga dapat menggunakan aplikasi.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { onNavigate("WARGA") }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("VERIFIKASI", fontSize = 11.sp)
                    }
                }
            }
        }

        // Action shortcuts
        Text(
            text = "Akses Cepat Manajemen RW:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = Icons.Default.Campaign,
                label = "Broadcast",
                iconColor = Color(0xFF1565C0),
                onClick = { onNavigate("PENGUMUMAN") }
            )
            QuickActionButton(
                icon = Icons.Default.AddCard,
                label = "Buat Iuran",
                iconColor = Color(0xFF2E7D32),
                onClick = { onNavigate("KEUANGAN") }
            )
            QuickActionButton(
                icon = Icons.Default.Apartment,
                label = "Fasilitas",
                iconColor = Color(0xFF6A1B9A),
                onClick = { onNavigate("FASILITAS") }
            )
            QuickActionButton(
                icon = Icons.Default.History,
                label = "Audit Log",
                iconColor = Color(0xFF455A64),
                onClick = { onNavigate("AUDIT") }
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = color))
        }
    }
}

// ----------------------------------------------------------------------
// MENU RW TAB
// ----------------------------------------------------------------------
@Composable
fun AdminMenuTab(
    onSelectMenu: (String) -> Unit,
    onLogout: () -> Unit
) {
    val menus = listOf(
        Triple("WARGA", "👥 Manajemen Warga & Hunian", "Verifikasi pendaftar, edit data rumah, nonaktifkan akun."),
        Triple("KEUANGAN", "💰 Iuran, Pembayaran & Kas", "Kelola tagihan warga, cek bukti transfer, catat pengeluaran."),
        Triple("LAPORAN", "📢 Pengaduan & Laporan Warga", "Ubah status pengerjaan, tanggapi laporan kerusakan."),
        Triple("TAMU", "📋 Buku Log Tamu & Kendaraan", "Pantau tamu masuk/keluar di gerbang pos security."),
        Triple("PENGUMUMAN", "📢 Pengumuman & Broadcast Notif", "Sebarkan surat edaran RT/RW langsung ke HP warga."),
        Triple("FASILITAS", "🏛️ Inventaris Fasilitas Komplek", "Status CCTV gerbang, PJU, balai pertemuan, dan taman."),
        Triple("AUDIT", "🛡️ Catatan Audit Aktivitas", "Rekam jejak tindakan admin, login, dan mutasi data."),
        Triple("PENGATURAN", "⚙️ Pengaturan Komplek GP4", "Informasi komplek, nomor darurat, struktur pengurus.")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(menus) { (key, title, desc) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable { onSelectMenu(key) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KELUAR DARI ADMIN")
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-SCREEN 1: MANAJEMEN WARGA
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCitizensSubScreen(
    repository: FirebaseRepository,
    users: List<UserProfile>,
    adminName: String,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForAction by remember { mutableStateOf<UserProfile?>(null) }

    val citizens = remember(users, searchQuery) {
        val list = users.filter { it.role == UserRole.WARGA }
        if (searchQuery.isBlank()) list else list.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.houseNumber.contains(searchQuery, ignoreCase = true) ||
            it.block.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Warga GP4") },
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Warga / No Rumah") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (citizens.isEmpty()) {
                EmptyStateView(title = "Tidak Ada Data Warga", description = "Belum ada warga yang terdaftar.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(citizens) { citizen ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().clickable { selectedUserForAction = citizen }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${citizen.block}${citizen.houseNumber}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(citizen.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text("Blok ${citizen.block}-${citizen.houseNumber} (${citizen.houseStatus})", style = MaterialTheme.typography.bodySmall)
                                        Text("📱 ${citizen.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(status = citizen.status.name)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Detail >", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedUserForAction != null) {
        UserActionDialog(
            user = selectedUserForAction!!,
            adminName = adminName,
            repository = repository,
            onDismiss = { selectedUserForAction = null }
        )
    }
}

@Composable
fun UserActionDialog(
    user: UserProfile,
    adminName: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kelola Akun Warga") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nama: ${user.name}", fontWeight = FontWeight.Bold)
                Text("Email: ${user.email}")
                Text("Rumah: Blok ${user.block} No. ${user.houseNumber}")
                Text("Status Hunian: ${user.houseStatus}")
                Text("Status Akun: ${user.status.name}")
                if (user.vehiclePlate.isNotEmpty()) {
                    Text("Kendaraan: ${user.vehicleType} (${user.vehiclePlate})")
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (user.status != AccountStatus.DISETUJUI) {
                    Button(
                        onClick = {
                            repository.updateUserAccountStatus(user.id, AccountStatus.DISETUJUI, adminName)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SETUJUI & AKTIFKAN AKUN")
                    }
                }
                if (user.status == AccountStatus.MENUNGGU_VERIFIKASI) {
                    Button(
                        onClick = {
                            repository.updateUserAccountStatus(user.id, AccountStatus.DITOLAK, adminName)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TOLAK PENDAFTARAN")
                    }
                }
                if (user.status == AccountStatus.DISETUJUI) {
                    OutlinedButton(
                        onClick = {
                            repository.updateUserAccountStatus(user.id, AccountStatus.NONAKTIF, adminName)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NONAKTIFKAN SEMENTARA")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("TUTUP") }
        }
    )
}

// ----------------------------------------------------------------------
// SUB-SCREEN 2: IURAN & KEUANGAN
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinancesSubScreen(
    repository: FirebaseRepository,
    bills: List<PaymentBill>,
    expenses: List<ExpenseItem>,
    adminName: String,
    onBack: () -> Unit
) {
    var selectedFinanceTab by remember { mutableStateOf(0) }
    var selectedBillForVerification by remember { mutableStateOf<PaymentBill?>(null) }
    var showCreateBillDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keuangan & Kas Komplek") },
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
        ) {
            TabRow(selectedTabIndex = selectedFinanceTab) {
                Tab(
                    selected = selectedFinanceTab == 0,
                    onClick = { selectedFinanceTab = 0 },
                    text = { Text("Iuran Warga (${bills.count { it.status == "MENUNGGU VERIFIKASI" }})") }
                )
                Tab(
                    selected = selectedFinanceTab == 1,
                    onClick = { selectedFinanceTab = 1 },
                    text = { Text("Pengeluaran Kas") }
                )
            }

            if (selectedFinanceTab == 0) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Tagihan Iuran",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(onClick = { showCreateBillDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BUAT TAGIHAN")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(bills) { bill ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().clickable { selectedBillForVerification = bill }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${bill.userName} (${bill.userHouse})", fontWeight = FontWeight.Bold)
                                        StatusBadge(status = bill.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${bill.title} • Periode: ${bill.period}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(bill.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    if (bill.paymentProofUrl.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "📸 Ada Bukti Pembayaran (Klik untuk Verifikasi)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Pengeluaran Kas RW",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(onClick = { showAddExpenseDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CATAT PENGELUARAN")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(expenses) { exp ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(exp.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "- Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(exp.amount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmergencyRed
                                        )
                                    }
                                    Text("Kategori: ${exp.category} • ${exp.date}", style = MaterialTheme.typography.bodySmall)
                                    Text("Dicatat oleh: ${exp.recordedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedBillForVerification != null) {
        VerifyPaymentProofDialog(
            bill = selectedBillForVerification!!,
            adminName = adminName,
            repository = repository,
            onDismiss = { selectedBillForVerification = null }
        )
    }

    if (showCreateBillDialog) {
        CreateBillDialog(
            repository = repository,
            onDismiss = { showCreateBillDialog = false }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            recordedBy = adminName,
            repository = repository,
            onDismiss = { showAddExpenseDialog = false }
        )
    }
}

@Composable
fun VerifyPaymentProofDialog(
    bill: PaymentBill,
    adminName: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    var adminNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verifikasi Bukti Pembayaran") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Warga: ${bill.userName} (${bill.userHouse})", fontWeight = FontWeight.Bold)
                Text("Tagihan: ${bill.title} (${bill.period})")
                Text("Nominal: Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(bill.amount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                if (bill.paymentProofUrl.isNotEmpty()) {
                    Text("Foto Bukti Transfer Warga:", style = MaterialTheme.typography.labelMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = bill.paymentProofUrl,
                            contentDescription = "Bukti Transfer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text("Belum ada foto bukti pembayaran yang diunggah warga.", color = Color.Gray)
                }

                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { adminNote = it },
                    label = { Text("Catatan untuk Warga (opsional)") },
                    placeholder = { Text("Contoh: Dana telah masuk kas BCA / Transfer kurang nominal") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        repository.verifyPayment(bill.id, true, adminNote, adminName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("TERIMA (LUNAS)")
                }
                Button(
                    onClick = {
                        repository.verifyPayment(bill.id, false, adminNote.ifEmpty { "Bukti transfer tidak valid/belum terbaca." }, adminName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("TOLAK BUKTI")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("KEMBALI") }
        }
    )
}

@Composable
fun CreateBillDialog(
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    val users by repository.users.collectAsState()
    val citizens = remember(users) { users.filter { it.role == UserRole.WARGA } }

    var title by remember { mutableStateOf("Iuran Keamanan & Kebersihan") }
    var period by remember { mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())) }
    var amountText by remember { mutableStateOf("150000") }
    var dueDate by remember { mutableStateOf("10 " + SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())) }
    var category by remember { mutableStateOf("Iuran Bulanan") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terbitkan Tagihan Baru ke Warga") },
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
                    label = { Text("Nama Tagihan*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = period,
                    onValueChange = { period = it },
                    label = { Text("Periode*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Jumlah Nominal (Rp)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Jatuh Tempo*") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tagihan akan otomatis diterbitkan ke ${citizens.size} warga komplek terdaftar.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (title.isNotBlank() && amount > 0) {
                        citizens.forEach { citizen ->
                            val bill = PaymentBill(
                                userId = citizen.id,
                                userName = citizen.name,
                                userHouse = "Blok ${citizen.block}-${citizen.houseNumber}",
                                title = title,
                                period = period,
                                amount = amount,
                                dueDate = dueDate,
                                category = category
                            )
                            repository.createPaymentBill(bill)
                        }
                        onDismiss()
                    }
                }
            ) {
                Text("TERBITKAN KE SEMUA WARGA")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}

@Composable
fun AddExpenseDialog(
    recordedBy: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Keamanan") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pengeluaran Kas RW") },
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
                    label = { Text("Keperluan Pengeluaran*") },
                    placeholder = { Text("Contoh: Pembelian Solar Genset Pos Security") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Nominal (Rp)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (Keamanan/Kebersihan/Perbaikan)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (title.isNotBlank() && amount > 0) {
                        val exp = ExpenseItem(
                            title = title,
                            amount = amount,
                            category = category,
                            date = date,
                            recordedBy = recordedBy
                        )
                        repository.addExpense(exp)
                        onDismiss()
                    }
                }
            ) {
                Text("SIMPAN")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}

// ----------------------------------------------------------------------
// SUB-SCREEN 3: LAPORAN FASILITAS ADMIN
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsSubScreen(
    repository: FirebaseRepository,
    reports: List<FacilityReport>,
    adminName: String,
    onBack: () -> Unit
) {
    var selectedReportForAction by remember { mutableStateOf<FacilityReport?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Laporan Fasilitas") },
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
                .padding(16.dp)
        ) {
            if (reports.isEmpty()) {
                EmptyStateView(title = "Tidak Ada Laporan", description = "Belum ada keluhan atau laporan sarana.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reports) { rep ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().clickable { selectedReportForAction = rep }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rep.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    StatusBadge(status = rep.status)
                                }
                                Text("Pelapor: ${rep.userName} (${rep.houseNumber}) • 📍 ${rep.locationBlock}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(rep.description, style = MaterialTheme.typography.bodyMedium)

                                if (rep.adminComment.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Respon RW: ${rep.adminComment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedReportForAction != null) {
        UpdateReportStatusDialog(
            report = selectedReportForAction!!,
            adminName = adminName,
            repository = repository,
            onDismiss = { selectedReportForAction = null }
        )
    }
}

@Composable
fun UpdateReportStatusDialog(
    report: FacilityReport,
    adminName: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(report.status) }
    var comment by remember { mutableStateOf(report.adminComment) }

    val statusOptions = listOf("DIAJUKAN", "DIPROSES", "DIKERJAKAN", "SELESAI")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tindak Lanjut Laporan") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Laporan: ${report.title}", fontWeight = FontWeight.Bold)
                Text("Lokasi: ${report.locationBlock}")
                Text("Pelapor: ${report.userName}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ubah Status:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statusOptions.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Tanggapan Pengurus / Teknisi") },
                    placeholder = { Text("Contoh: Teknisi listrik sudah mengganti bohlam PJU siang ini.") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.updateReportStatus(report.id, status, comment, adminName)
                    onDismiss()
                }
            ) {
                Text("SIMPAN STATUS")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}

// ----------------------------------------------------------------------
// SUB-SCREEN 4: PENGUMUMAN & BROADCAST
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementsSubScreen(
    repository: FirebaseRepository,
    announcements: List<AnnouncementItem>,
    adminName: String,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengumuman & Broadcast RW") },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                text = { Text("BUAT PENGUMUMAN") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(announcements) { ann ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatusBadge(status = ann.priority)
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(ann.createdAt)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ann.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(ann.content, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Diterbitkan oleh: ${ann.createdBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAnnouncementDialog(
            adminName = adminName,
            repository = repository,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun CreateAnnouncementDialog(
    adminName: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Pengumuman Komplek") },
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
                    label = { Text("Judul Pengumuman*") },
                    placeholder = { Text("Contoh: Kerja Bakti Massal Minggu Depan") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Prioritas:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL", "PENTING", "DARURAT").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Pengumuman / Edaran*") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val ann = AnnouncementItem(
                            title = title,
                            content = content,
                            priority = priority,
                            createdBy = adminName
                        )
                        coroutineScope.launch {
                            repository.createAnnouncement(ann)
                        }
                        onDismiss()
                    }
                }
            ) {
                Text("TERBITKAN & BROADCAST")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}

// ----------------------------------------------------------------------
// SUB-SCREEN 5: FASILITAS KOMPLEK
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFacilitiesSubScreen(
    repository: FirebaseRepository,
    onBack: () -> Unit
) {
    val facilities = listOf(
        Triple("Gerbang Utama & Pos Security", "Aktif 24 Jam • Barrier gate & CCTV 4 Channel", "NORMAL"),
        Triple("Lampu PJU Jalan Utama Blok A-E", "Sebagian otomatis sensor cahaya • 36 Titik lampu", "NORMAL"),
        Triple("Balai Warga & Sekretariat RW", "Bisa digunakan untuk musyawarah & kegiatan senam", "TERSEDIA"),
        Triple("Taman Bermain Anak Blok C", "Rumput terawat • Ayunan & perosotan aman", "BAIK"),
        Triple("Lapangan Olahraga Serbaguna", "Bulu tangkis & futsal warga", "TERSEDIA")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fasilitas Green Panorama 4") },
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
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(facilities) { (name, desc, status) ->
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
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(status = status)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-SCREEN 6: LOG TAMU LENGKAP
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGuestLogsSubScreen(
    guestPasses: List<GuestPass>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Lengkap Tamu GP4") },
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
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(guestPasses) { pass ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pass.guestName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                StatusBadge(status = pass.status)
                            }
                            Text("Tujuan: ${pass.userHouse} (${pass.userName})", style = MaterialTheme.typography.bodySmall)
                            Text("Plat: ${pass.plateNumber} • ${pass.vehicleType}", style = MaterialTheme.typography.bodySmall)
                            Text("Waktu: ${pass.date} (${pass.arrivalTime} - ${pass.departureTime})", style = MaterialTheme.typography.bodySmall)
                            if (pass.checkedInAt != null) {
                                Text("Masuk: ${SimpleDateFormat("dd/MM HH:mm", Locale("id", "ID")).format(Date(pass.checkedInAt))}", fontSize = 11.sp, color = SuccessGreen)
                            }
                            if (pass.checkedOutAt != null) {
                                Text("Keluar: ${SimpleDateFormat("dd/MM HH:mm", Locale("id", "ID")).format(Date(pass.checkedOutAt))}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-SCREEN 7: AUDIT LOGS
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsSubScreen(
    auditLogs: List<AuditLogItem>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jejak Audit Aktivitas") },
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
                .padding(16.dp)
        ) {
            Text("Rekam Jejak Keamanan Sistem:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(auditLogs) { log ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                Text(SimpleDateFormat("dd MMM, HH:mm:ss", Locale("id", "ID")).format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(log.details, style = MaterialTheme.typography.bodySmall)
                            Text("Pengguna: ${log.performedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-SCREEN 8: PENGATURAN KOMPLEK
// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsSubScreen(
    complexInfo: ComplexInfo,
    repository: FirebaseRepository,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Komplek GP4") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(complexInfo.complexName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Text("📍 Alamat: ${complexInfo.address}", style = MaterialTheme.typography.bodyMedium)
                    Text("🏡 Total Blok: ${complexInfo.totalBlocks} (${complexInfo.blocksList})", style = MaterialTheme.typography.bodyMedium)
                    Text("🏘️ Total Unit Rumah: ${complexInfo.totalHouses}", style = MaterialTheme.typography.bodyMedium)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("📞 Pos Security 24 Jam: ${complexInfo.securityHotline}", fontWeight = FontWeight.SemiBold)
                    Text("📞 Ketua RW 04: ${complexInfo.rwHotline}", fontWeight = FontWeight.SemiBold)
                    Text("🚑 Ambulans Siaga Desa: ${complexInfo.ambulanceHotline}", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// BROADCAST DARURAT MODAL (ADMIN)
// ----------------------------------------------------------------------
@Composable
fun EmergencyBroadcastDialog(
    adminName: String,
    repository: FirebaseRepository,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("PERINGATAN DARURAT WARGA GP4") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast Sinyal Darurat", color = EmergencyRed, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kirimkan pesan darurat berprioritas tinggi ke seluruh aplikasi warga dan pos security secara serentak.")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Peringatan") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Instruksi / Rincian Darurat*") },
                    placeholder = { Text("Contoh: Waspada cuaca ekstrem angin kencang / Mohon kunci pagar") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        repository.sendNotification(
                            userId = "ALL",
                            targetRole = "ALL",
                            title = "🚨 $title",
                            message = message,
                            type = "DARURAT"
                        )
                        repository.logAudit(adminName, "BROADCAST_DARURAT", message)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text("KIRIM KE SEMUA WARGA")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}
