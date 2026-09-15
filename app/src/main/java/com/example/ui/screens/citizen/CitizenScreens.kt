package com.example.ui.screens.citizen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CitizenMainScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    var selectedBottomNav by remember { mutableStateOf(0) }
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val announcements by repository.announcements.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val paymentBills by repository.paymentBills.collectAsState()
    val facilityReports by repository.facilityReports.collectAsState()
    val events by repository.events.collectAsState()
    val guestPasses by repository.guestPasses.collectAsState()

    val myPayments = remember(paymentBills, currentUser) {
        paymentBills.filter { it.userId == currentUser.id }
    }
    val myReports = remember(facilityReports, currentUser) {
        facilityReports.filter { it.userId == currentUser.id }
    }
    val myGuestPasses = remember(guestPasses, currentUser) {
        guestPasses.filter { it.userId == currentUser.id }
    }
    val unreadNotifs = remember(notifications, currentUser) {
        notifications.count { (it.userId == currentUser.id || it.targetRole == "ALL" || it.targetRole == "WARGA") && !it.isRead }
    }

    if (activeSubScreen != null) {
        when (activeSubScreen) {
            "TAMU" -> CitizenGuestPassSubScreen(
                repository = repository,
                currentUser = currentUser,
                guestPasses = myGuestPasses,
                onBack = { activeSubScreen = null }
            )
            "LAPORAN" -> CitizenReportSubScreen(
                repository = repository,
                currentUser = currentUser,
                reports = myReports,
                onBack = { activeSubScreen = null }
            )
            "IURAN" -> CitizenDuesSubScreen(
                repository = repository,
                currentUser = currentUser,
                bills = myPayments,
                onBack = { activeSubScreen = null }
            )
            "KEGIATAN" -> CitizenEventsSubScreen(
                repository = repository,
                currentUser = currentUser,
                events = events,
                onBack = { activeSubScreen = null }
            )
            "FORUM" -> CitizenForumSubScreen(
                repository = repository,
                currentUser = currentUser,
                onBack = { activeSubScreen = null }
            )
            "MARKETPLACE" -> CitizenMarketplaceSubScreen(
                repository = repository,
                currentUser = currentUser,
                onBack = { activeSubScreen = null }
            )
        }
        return
    }

    Scaffold(
        topBar = {
            GpTopAppBar(
                title = "Selamat datang,",
                subtitle = currentUser.name,
                roleBadge = "WARGA BLOK ${currentUser.block}-${currentUser.houseNumber}",
                unreadNotifCount = unreadNotifs,
                onNotifClick = { selectedBottomNav = 2 },
                onEmergencyClick = { showEmergencyDialog = true }
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
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = { selectedBottomNav = 1 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                    label = { Text("Layanan") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = { selectedBottomNav = 2 },
                    icon = {
                        BadgedBox(badge = {
                            if (unreadNotifs > 0) Badge { Text("$unreadNotifs") }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                    },
                    label = { Text("Notifikasi") }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 3,
                    onClick = { selectedBottomNav = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedBottomNav) {
                0 -> CitizenHomeTab(
                    currentUser = currentUser,
                    announcements = announcements,
                    myPayments = myPayments,
                    myReports = myReports,
                    events = events,
                    onNavigateService = { route -> activeSubScreen = route },
                    onEmergency = { showEmergencyDialog = true }
                )
                1 -> CitizenServicesTab(
                    onSelectService = { route -> activeSubScreen = route },
                    onEmergency = { showEmergencyDialog = true }
                )
                2 -> CitizenNotificationsTab(
                    notifications = notifications.filter { it.userId == currentUser.id || it.targetRole == "ALL" || it.targetRole == "WARGA" }
                )
                3 -> CitizenProfileTab(
                    currentUser = currentUser,
                    onLogout = onLogout
                )
            }
        }
    }

    if (showEmergencyDialog) {
        EmergencyDialog(
            currentUserName = currentUser.name,
            currentUserHouse = "${currentUser.block}-${currentUser.houseNumber}",
            currentUserId = currentUser.id,
            onDismiss = { showEmergencyDialog = false },
            onSubmitEmergency = { report ->
                coroutineScope.launch {
                    repository.triggerEmergency(report)
                }
            }
        )
    }
}

@Composable
fun CitizenHomeTab(
    currentUser: UserProfile,
    announcements: List<AnnouncementItem>,
    myPayments: List<PaymentBill>,
    myReports: List<FacilityReport>,
    events: List<CommunityEvent>,
    onNavigateService: (String) -> Unit,
    onEmergency: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Residential Visual Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gp4_banner_1789428588744),
                    contentDescription = "Green Panorama 4 Estate",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(14.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "GREEN PANORAMA 4",
                            color = Color(0xFFFFD54F),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Lingkungan Aman, Asri & Nyaman Bersama",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Quick Action Grid (8 items)
        Text(
            text = "Menu Cepat",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = Icons.Default.Warning,
                label = "🚨 Darurat",
                containerColor = EmergencyRed.copy(alpha = 0.15f),
                iconColor = EmergencyRed,
                onClick = onEmergency
            )
            QuickActionButton(
                icon = Icons.Default.QrCode,
                label = "Tamu Saya",
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigateService("TAMU") }
            )
            QuickActionButton(
                icon = Icons.Default.Campaign,
                label = "Laporan",
                iconColor = Color(0xFFE65100),
                onClick = { onNavigateService("LAPORAN") }
            )
            QuickActionButton(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Iuran",
                iconColor = Color(0xFF2E7D32),
                onClick = { onNavigateService("IURAN") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = Icons.Default.Event,
                label = "Kegiatan",
                iconColor = Color(0xFF1565C0),
                onClick = { onNavigateService("KEGIATAN") }
            )
            QuickActionButton(
                icon = Icons.Default.Forum,
                label = "Forum",
                iconColor = Color(0xFF6A1B9A),
                onClick = { onNavigateService("FORUM") }
            )
            QuickActionButton(
                icon = Icons.Default.Storefront,
                label = "Marketplace",
                iconColor = Color(0xFFD84315),
                onClick = { onNavigateService("MARKETPLACE") }
            )
            QuickActionButton(
                icon = Icons.Default.Person,
                label = "Profil",
                iconColor = Color(0xFF455A64),
                onClick = { onNavigateService("PROFIL") }
            )
        }

        // Status Iuran Card
        val unpaidBills = myPayments.filter { it.status == "BELUM BAYAR" }
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (unpaidBills.isNotEmpty()) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateService("IURAN") }
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (unpaidBills.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (unpaidBills.isNotEmpty()) Color(0xFFE65100) else Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (unpaidBills.isNotEmpty()) "Iuran Belum Dibayar: ${unpaidBills.size}" else "Iuran Anda Lunas",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (unpaidBills.isNotEmpty()) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                        Text(
                            text = if (unpaidBills.isNotEmpty()) "Total: Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(unpaidBills.sumOf { it.amount })}" else "Terima kasih atas partisipasi Anda",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        // Pengumuman Terbaru Carousel / Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📢 Pengumuman Terbaru",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (announcements.isEmpty()) {
            EmptyStateView(
                title = "Belum Ada Pengumuman",
                description = "Pengumuman dari Pengurus RW akan ditampilkan di sini."
            )
        } else {
            announcements.take(3).forEach { ann ->
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
                            StatusBadge(status = ann.priority)
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(ann.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ann.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ann.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Kegiatan Terdekat
        if (events.isNotEmpty()) {
            Text(
                text = "📅 Kegiatan Terdekat",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            val nextEvent = events.first()
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateService("KEGIATAN") }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = nextEvent.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${nextEvent.registeredUserIds.size} Peserta",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "🗓️ ${nextEvent.date} • ⏰ ${nextEvent.time}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "📍 ${nextEvent.location}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Active Reports
        if (myReports.isNotEmpty()) {
            Text(
                text = "Laporan Fasilitas Saya",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            myReports.take(2).forEach { rep ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rep.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(rep.locationBlock, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = rep.status)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(76.dp)
    ) {
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CitizenServicesTab(
    onSelectService: (String) -> Unit,
    onEmergency: () -> Unit
) {
    val services = listOf(
        Triple("TAMU", "👤 Tamu Digital (QR Pass)", "Buat izin kedatangan tamu, hasilkan QR Code untuk gerbang security."),
        Triple("LAPORAN", "📢 Laporan Fasilitas", "Laporkan lampu PJU mati, jalan rusak, sampah, dan sarana umum."),
        Triple("IURAN", "💰 Iuran & Pembayaran", "Cek tagihan iuran keamanan & kebersihan, bayar dan upload bukti transfer."),
        Triple("KEGIATAN", "📅 Kegiatan Warga", "Daftar kerja bakti, rapat musyawarah, senam, dan acara sosial perumahan."),
        Triple("FORUM", "💬 Forum Warga", "Ruang diskusi, saran lingkungan, info kehilangan, dan aspirasi warga."),
        Triple("MARKETPLACE", "🛒 Marketplace Warga", "Jual beli produk kuliner, jasa, perlengkapan sesama tetangga Green Panorama 4.")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onEmergency() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("🚨 Tombol Darurat (SOS)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EmergencyRed)
                        Text("Panggil bantuan darurat pos security dan RT/RW dalam 1 sentuhan.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        items(services) { (key, title, desc) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable { onSelectService(key) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun CitizenNotificationsTab(notifications: List<NotificationItem>) {
    if (notifications.isEmpty()) {
        EmptyStateView(
            title = "Belum Ada Notifikasi",
            description = "Pemberitahuan terkait tamu, iuran, dan keamanan akan muncul di sini."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifications) { notif ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val icon = when (notif.type) {
                            "DARURAT" -> Icons.Default.Warning
                            "TAMU" -> Icons.Default.QrCode
                            "IURAN" -> Icons.Default.AccountBalanceWallet
                            "LAPORAN" -> Icons.Default.Campaign
                            else -> Icons.Default.Notifications
                        }
                        val tint = when (notif.type) {
                            "DARURAT" -> EmergencyRed
                            "IURAN" -> SuccessGreen
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(notif.message, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(notif.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CitizenProfileTab(
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(90.dp)
        ) {
            if (currentUser.profilePhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = currentUser.profilePhotoUrl,
                    contentDescription = currentUser.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = currentUser.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        StatusBadge(status = currentUser.status.name)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileInfoRow("Blok / No. Rumah", "Blok ${currentUser.block} No. ${currentUser.houseNumber}")
                ProfileInfoRow("Status Rumah", currentUser.houseStatus)
                ProfileInfoRow("Email", currentUser.email)
                ProfileInfoRow("Nomor HP", currentUser.phone)
                ProfileInfoRow("Kendaraan", "${currentUser.vehicleType} (${currentUser.vehiclePlate})")
                ProfileInfoRow("Komplek", "Green Panorama 4")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("KELUAR (LOGOUT)")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 1: TAMU DIGITAL (GUEST PASS)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenGuestPassSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    guestPasses: List<GuestPass>,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedQrPass by remember { mutableStateOf<GuestPass?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tamu Saya (QR Pass)") },
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
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("BUAT IZIN TAMU") },
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
            Text(
                text = "Daftar Tamu Terdaftar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (guestPasses.isEmpty()) {
                EmptyStateView(
                    title = "Belum Ada Izin Tamu",
                    description = "Tekan tombol '+ Buat Izin Tamu' untuk mendaftarkan tamu dan menghasilkan kode QR masuk."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(guestPasses) { pass ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQrPass = pass }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pass.guestName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("🗓️ ${pass.date} • ⏰ ${pass.arrivalTime} - ${pass.departureTime}", style = MaterialTheme.typography.bodySmall)
                                    Text("🚗 ${pass.vehicleType} • ${pass.plateNumber}", style = MaterialTheme.typography.bodySmall)
                                    Text("Keperluan: ${pass.purpose}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(status = pass.status)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { selectedQrPass = pass },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("QR KODE", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGuestPassDialog(
            currentUser = currentUser,
            onDismiss = { showCreateDialog = false },
            onSubmit = { pass ->
                coroutineScope.launch {
                    val res = repository.createGuestPass(pass)
                    showCreateDialog = false
                    res.onSuccess { selectedQrPass = it }
                }
            }
        )
    }

    if (selectedQrPass != null) {
        ShowQrDialog(pass = selectedQrPass!!, onDismiss = { selectedQrPass = null })
    }
}

@Composable
fun CreateGuestPassDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onSubmit: (GuestPass) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())) }
    var arrivalTime by remember { mutableStateOf("14:00") }
    var departureTime by remember { mutableStateOf("18:00") }
    var vehicleType by remember { mutableStateOf("Mobil") }
    var plateNumber by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Izin Tamu Digital") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Nama Tamu*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guestPhone,
                    onValueChange = { guestPhone = it },
                    label = { Text("Nomor HP Tamu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal Kedatangan") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = arrivalTime,
                        onValueChange = { arrivalTime = it },
                        label = { Text("Jam Masuk") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        label = { Text("Jam Pulang") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Mobil", "Motor", "Jalan Kaki").forEach { vt ->
                        FilterChip(
                            selected = vehicleType == vt,
                            onClick = { vehicleType = vt },
                            label = { Text(vt) }
                        )
                    }
                }
                if (vehicleType != "Jalan Kaki") {
                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it.uppercase() },
                        label = { Text("Nomor Plat Kendaraan") },
                        placeholder = { Text("B 1234 XYZ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Keperluan / Tujuan*") },
                    placeholder = { Text("Contoh: Bertamu keluarga / Kurir paket / Tukang") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan untuk Security (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (guestName.isNotBlank() && purpose.isNotBlank()) {
                        val pass = GuestPass(
                            userId = currentUser.id,
                            userName = currentUser.name,
                            userHouse = "Blok ${currentUser.block}-${currentUser.houseNumber}",
                            guestName = guestName,
                            guestPhone = guestPhone,
                            date = date,
                            arrivalTime = arrivalTime,
                            departureTime = departureTime,
                            vehicleType = vehicleType,
                            plateNumber = plateNumber,
                            purpose = purpose,
                            notes = notes
                        )
                        onSubmit(pass)
                    }
                }
            ) {
                Text("TERBITKAN QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

@Composable
fun ShowQrDialog(pass: GuestPass, onDismiss: () -> Unit) {
    val qrBitmap = remember(pass.qrCode) {
        QrCodeHelper.generateQrBitmap(pass.qrCode, 480, 480)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QR CODE TAMU",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Perlihatkan ke Petugas Security di Gerbang Masuk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (qrBitmap != null) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code Tamu",
                            modifier = Modifier
                                .size(220.dp)
                                .padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pass.qrCode,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Tamu: ${pass.guestName}", fontWeight = FontWeight.Bold)
                        Text("Tujuan: ${pass.userHouse} (${pass.userName})")
                        Text("Waktu: ${pass.date} (${pass.arrivalTime} - ${pass.departureTime})")
                        Text("Status: ${pass.status}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TUTUP")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 2: LAPORAN WARGA (FACILITY REPORTS)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenReportSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    reports: List<FacilityReport>,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Fasilitas & Warga") },
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
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("BUAT LAPORAN") },
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
            Text(
                text = "Riwayat Laporan Saya",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (reports.isEmpty()) {
                EmptyStateView(
                    title = "Belum Ada Laporan",
                    description = "Laporkan lampu jalan mati, sampah, jalan rusak, atau kendala fasilitas di lingkungan Green Panorama 4."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reports) { rep ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(status = rep.status)
                                    Text(
                                        text = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(rep.createdAt)),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(rep.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("📍 ${rep.locationBlock} • Kategori: ${rep.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(rep.description, style = MaterialTheme.typography.bodyMedium)

                                if (rep.photoUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = rep.photoUrl,
                                            contentDescription = "Foto Laporan",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                if (rep.adminComment.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Respon Pengurus / Teknisi:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                            Text(rep.adminComment, style = MaterialTheme.typography.bodySmall)
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

    if (showAddDialog) {
        CreateReportDialog(
            currentUser = currentUser,
            repository = repository,
            onDismiss = { showAddDialog = false },
            onSubmit = { rep ->
                coroutineScope.launch {
                    repository.submitFacilityReport(rep)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun CreateReportDialog(
    currentUser: UserProfile,
    repository: FirebaseRepository,
    onDismiss: () -> Unit,
    onSubmit: (FacilityReport) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var category by remember { mutableStateOf("Lampu jalan mati") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationBlock by remember { mutableStateOf("Blok ${currentUser.block}") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val categories = listOf(
        "Lampu jalan mati", "Jalan rusak", "Sampah", "Saluran air",
        "Fasilitas umum", "Listrik", "Air", "Keamanan", "Lainnya"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Form Laporan Warga") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Kategori Laporan:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Laporan*") },
                    placeholder = { Text("Contoh: PJU Padam di depan Blok B No. 10") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = locationBlock,
                    onValueChange = { locationBlock = it },
                    label = { Text("Lokasi / Blok*") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Detail Kendala*") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Camera and Gallery photo picker
                CameraPhotoPicker(
                    label = "Foto Bukti Kerusakan / Lokasi:",
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
                                uploaded = repository.uploadImage("reports", photoUri!!)
                            }
                            val rep = FacilityReport(
                                userId = currentUser.id,
                                userName = currentUser.name,
                                houseNumber = "Blok ${currentUser.block}-${currentUser.houseNumber}",
                                category = category,
                                title = title,
                                description = description,
                                locationBlock = locationBlock,
                                photoUrl = uploaded
                            )
                            isUploading = false
                            onSubmit(rep)
                        }
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("KIRIM LAPORAN")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

// -------------------------------------------------------------
// SUB-SCREEN 3: IURAN WARGA (DUES)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDuesSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    bills: List<PaymentBill>,
    onBack: () -> Unit
) {
    var selectedBillToPay by remember { mutableStateOf<PaymentBill?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iuran & Tagihan Warga") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rekening Pembayaran Resmi Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🏦 Rekening Resmi Kas GP4:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("BCA: 8830-1928-11 a.n. KAS WARGA GREEN PANORAMA 4", style = MaterialTheme.typography.bodySmall)
                    Text("Mandiri: 137-00-19283-00 a.n. PENGURUS GP4", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "*Setelah transfer, silakan upload bukti struk/screenshot pembayaran melalui kamera atau galeri.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Daftar Tagihan Iuran Anda",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (bills.isEmpty()) {
                EmptyStateView(
                    title = "Tidak Ada Tagihan",
                    description = "Semua kewajiban iuran Anda telah terselesaikan."
                )
            } else {
                bills.forEach { bill ->
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
                                StatusBadge(status = bill.status)
                                Text("Jatuh Tempo: ${bill.dueDate}", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(bill.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Periode: ${bill.period} • ${bill.category}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(bill.amount)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            if (bill.adminNote.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Catatan Admin: ${bill.adminNote}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
                            }

                            if (bill.status == "BELUM BAYAR" || bill.status == "DITOLAK") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { selectedBillToPay = bill },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("UPLOAD BUKTI PEMBAYARAN")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedBillToPay != null) {
        UploadProofDialog(
            bill = selectedBillToPay!!,
            repository = repository,
            onDismiss = { selectedBillToPay = null },
            onSuccess = { selectedBillToPay = null }
        )
    }
}

@Composable
fun UploadProofDialog(
    bill: PaymentBill,
    repository: FirebaseRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var proofUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Bukti Transfer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tagihan: ${bill.title} (Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(bill.amount)})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Gunakan Kamera untuk memotret struk ATM/struk EDC, atau pilih tangkapan layar m-banking dari galeri Anda.",
                    style = MaterialTheme.typography.bodySmall
                )

                CameraPhotoPicker(
                    label = "Foto Struk / Bukti Transfer:",
                    currentPhotoUrl = proofUri?.toString(),
                    onPhotoSelected = { uri -> proofUri = uri }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (proofUri != null) {
                        isUploading = true
                        coroutineScope.launch {
                            val uploaded = repository.uploadImage("payments", proofUri!!)
                            repository.uploadPaymentProof(bill.id, uploaded)
                            isUploading = false
                            onSuccess()
                        }
                    }
                },
                enabled = proofUri != null && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("KIRIM BUKTI")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

// -------------------------------------------------------------
// SUB-SCREEN 4: KEGIATAN WARGA (EVENTS)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenEventsSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    events: List<CommunityEvent>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kegiatan & Agenda Lingkungan") },
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
            if (events.isEmpty()) {
                EmptyStateView(
                    title = "Belum Ada Kegiatan",
                    description = "Agenda kerja bakti, rapat musyawarah, dan senam akan ditampilkan di sini."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(events) { evt ->
                        val isRegistered = evt.registeredUserIds.contains(currentUser.id)
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
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${evt.registeredUserIds.size} Orang Berpartisipasi",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isRegistered) {
                                        StatusBadge(status = "SUDAH DAFTAR")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(evt.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🗓️ ${evt.date} • ⏰ ${evt.time}", style = MaterialTheme.typography.bodySmall)
                                Text("📍 ${evt.location}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(evt.description, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(10.dp))

                                if (!isRegistered) {
                                    Button(
                                        onClick = { repository.registerEventParticipant(evt.id, currentUser.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("IKUT BERPARTISIPASI")
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ANDA SUDAH TERDAFTAR")
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

// -------------------------------------------------------------
// SUB-SCREEN 5: FORUM WARGA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenForumSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    onBack: () -> Unit
) {
    val forumPosts by repository.forumPosts.collectAsState()
    val forumComments by repository.forumComments.collectAsState()
    var selectedPostForComments by remember { mutableStateOf<ForumPost?>(null) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forum Diskusi Warga GP4") },
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
                onClick = { showCreatePostDialog = true },
                icon = { Icon(Icons.Default.Create, contentDescription = null) },
                text = { Text("POSTING BARU") },
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
            if (forumPosts.isEmpty()) {
                EmptyStateView(
                    title = "Belum Ada Postingan",
                    description = "Mulai diskusi bersama warga mengenai keamanan, saran lingkungan, atau info bersama."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(forumPosts) { post ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPostForComments = post }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${post.userName} (${post.userHouse})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    StatusBadge(status = post.category)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(post.content, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(post.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${post.commentCount} Komentar", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePostDialog) {
        CreateForumPostDialog(
            currentUser = currentUser,
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { post ->
                repository.addForumPost(post)
                showCreatePostDialog = false
            }
        )
    }

    if (selectedPostForComments != null) {
        ForumCommentsDialog(
            post = selectedPostForComments!!,
            currentUser = currentUser,
            comments = forumComments.filter { it.postId == selectedPostForComments!!.id },
            onAddComment = { content ->
                val comm = ForumComment(
                    postId = selectedPostForComments!!.id,
                    userId = currentUser.id,
                    userName = currentUser.name,
                    content = content
                )
                repository.addForumComment(comm)
            },
            onDismiss = { selectedPostForComments = null }
        )
    }
}

@Composable
fun CreateForumPostDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onSubmit: (ForumPost) -> Unit
) {
    var category by remember { mutableStateOf("Umum") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val categories = listOf("Umum", "Keamanan", "Jual beli", "Kehilangan", "Informasi", "Saran", "Kegiatan")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Topik Diskusi") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Kategori:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Topik*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Topik / Diskusi*") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val post = ForumPost(
                            userId = currentUser.id,
                            userName = currentUser.name,
                            userHouse = "Blok ${currentUser.block}-${currentUser.houseNumber}",
                            category = category,
                            title = title,
                            content = content
                        )
                        onSubmit(post)
                    }
                }
            ) {
                Text("POSTING")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}

@Composable
fun ForumCommentsDialog(
    post: ForumPost,
    currentUser: UserProfile,
    comments: List<ForumComment>,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Diskusi & Komentar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(post.title, fontWeight = FontWeight.Bold)
                        Text(post.content, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments) { comm ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(comm.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                Text(comm.content, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Tulis komentar...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 6: MARKETPLACE WARGA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenMarketplaceSubScreen(
    repository: FirebaseRepository,
    currentUser: UserProfile,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val items by repository.marketplaceItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketplace Warga GP4") },
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
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                text = { Text("JUAL PRODUK") },
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
            Text(
                text = "Produk & Jasa Warga Komplek",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (items.isEmpty()) {
                EmptyStateView(
                    title = "Belum Ada Produk",
                    description = "Warga Green Panorama 4 dapat menjual aneka makanan, minuman, jasa, atau produk lainnya di sini."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items) { prod ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (prod.photoUrl.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = prod.photoUrl,
                                            contentDescription = prod.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(80.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    StatusBadge(status = prod.category)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(prod.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(prod.price)}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Text("Penjual: ${prod.sellerName} (${prod.sellerHouse})", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            // Open WhatsApp directly
                                            val cleanPhone = prod.sellerPhone.replace("-", "").replace(" ", "")
                                            val formatted = if (cleanPhone.startsWith("0")) "62" + cleanPhone.drop(1) else cleanPhone
                                            val msg = "Halo ${prod.sellerName}, saya warga GP4 tertarik dengan '${prod.title}' di Marketplace GP4."
                                            val url = "https://wa.me/$formatted?text=${Uri.encode(msg)}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("HUBUNGI PENJUAL", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateMarketplaceProductDialog(
            currentUser = currentUser,
            repository = repository,
            onDismiss = { showAddDialog = false },
            onSubmit = { prod ->
                repository.addMarketplaceProduct(prod)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CreateMarketplaceProductDialog(
    currentUser: UserProfile,
    repository: FirebaseRepository,
    onDismiss: () -> Unit,
    onSubmit: (MarketplaceProduct) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val categories = listOf("Makanan", "Minuman", "Pakaian", "Jasa", "Elektronik", "Barang Bekas", "Produk Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jual Produk Warga") },
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
                    label = { Text("Nama Produk / Jasa*") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Harga (Rp)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Kategori:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Produk") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Photo picker using Camera & Gallery
                CameraPhotoPicker(
                    label = "Foto Produk:",
                    currentPhotoUrl = photoUri?.toString(),
                    onPhotoSelected = { uri -> photoUri = uri }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && priceText.isNotBlank()) {
                        isUploading = true
                        coroutineScope.launch {
                            var uploaded = ""
                            if (photoUri != null) {
                                uploaded = repository.uploadImage("marketplace", photoUri!!)
                            }
                            val product = MarketplaceProduct(
                                userId = currentUser.id,
                                sellerName = currentUser.name,
                                sellerPhone = currentUser.phone,
                                sellerHouse = "Blok ${currentUser.block}-${currentUser.houseNumber}",
                                title = title,
                                price = priceText.toLongOrNull() ?: 0L,
                                category = category,
                                photoUrl = uploaded,
                                description = description
                            )
                            isUploading = false
                            onSubmit(product)
                        }
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("PASANG IKLAN")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("BATAL") } }
    )
}
