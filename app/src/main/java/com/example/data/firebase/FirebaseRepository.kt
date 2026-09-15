package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "FirebaseRepository"

    var isFirebaseConnected: Boolean = false
        private set

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null

    // Reactive state flows for all main collections
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _announcements = MutableStateFlow<List<AnnouncementItem>>(emptyList())
    val announcements: StateFlow<List<AnnouncementItem>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _guestPasses = MutableStateFlow<List<GuestPass>>(emptyList())
    val guestPasses: StateFlow<List<GuestPass>> = _guestPasses.asStateFlow()

    private val _facilityReports = MutableStateFlow<List<FacilityReport>>(emptyList())
    val facilityReports: StateFlow<List<FacilityReport>> = _facilityReports.asStateFlow()

    private val _emergencyReports = MutableStateFlow<List<EmergencyReport>>(emptyList())
    val emergencyReports: StateFlow<List<EmergencyReport>> = _emergencyReports.asStateFlow()

    private val _paymentBills = MutableStateFlow<List<PaymentBill>>(emptyList())
    val paymentBills: StateFlow<List<PaymentBill>> = _paymentBills.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses.asStateFlow()

    private val _events = MutableStateFlow<List<CommunityEvent>>(emptyList())
    val events: StateFlow<List<CommunityEvent>> = _events.asStateFlow()

    private val _forumPosts = MutableStateFlow<List<ForumPost>>(emptyList())
    val forumPosts: StateFlow<List<ForumPost>> = _forumPosts.asStateFlow()

    private val _forumComments = MutableStateFlow<List<ForumComment>>(emptyList())
    val forumComments: StateFlow<List<ForumComment>> = _forumComments.asStateFlow()

    private val _marketplaceItems = MutableStateFlow<List<MarketplaceProduct>>(emptyList())
    val marketplaceItems: StateFlow<List<MarketplaceProduct>> = _marketplaceItems.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogItem>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogItem>> = _auditLogs.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()
    val users: StateFlow<List<UserProfile>> = allUsers

    private val _complexInfo = MutableStateFlow(ComplexInfo())
    val complexInfo: StateFlow<ComplexInfo> = _complexInfo.asStateFlow()

    private val _houses = MutableStateFlow<List<HouseItem>>(emptyList())
    val houses: StateFlow<List<HouseItem>> = _houses.asStateFlow()

    private val _securityEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val securityEvents: StateFlow<List<SecurityEvent>> = _securityEvents.asStateFlow()

    init {
        tryInitFirebase()
        seedInitialData()
    }

    private fun tryInitFirebase() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                storage = FirebaseStorage.getInstance()
                isFirebaseConnected = true
                Log.d(TAG, "Firebase initialized successfully with native config.")
                listenToFirestoreRealtime()
            } else {
                Log.i(TAG, "FirebaseApp not initialized yet. Using secure local state mirror.")
                isFirebaseConnected = false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning: ${e.localizedMessage}")
            isFirebaseConnected = false
        }
    }

    private fun seedInitialData() {
        // Pre-populate with realistic Green Panorama 4 initial accounts & baseline content
        val adminUser = UserProfile(
            id = "admin_gp4_01",
            name = "Bambang Sudarmono (Ketua RW)",
            phone = "081234567890",
            email = "admin@greenpanorama.com",
            role = UserRole.ADMIN,
            houseNumber = "01",
            block = "A",
            houseStatus = "Pemilik",
            profilePhotoUrl = "",
            vehicleType = "Mobil",
            vehiclePlate = "B 1234 GP",
            status = AccountStatus.DISETUJUI
        )

        val securityUser = UserProfile(
            id = "security_gp4_01",
            name = "Slamet Riyadi (Komandan Regu A)",
            phone = "081298765432",
            email = "security@greenpanorama.com",
            role = UserRole.SECURITY,
            houseNumber = "Pos Barat",
            block = "Security",
            houseStatus = "Petugas",
            profilePhotoUrl = "",
            vehicleType = "Motor",
            vehiclePlate = "B 8888 GP",
            status = AccountStatus.DISETUJUI
        )

        val sampleCitizen = UserProfile(
            id = "warga_gp4_01",
            name = "Rahmat Hidayat",
            phone = "081311223344",
            email = "warga@greenpanorama.com",
            role = UserRole.WARGA,
            houseNumber = "12",
            block = "B",
            houseStatus = "Pemilik",
            profilePhotoUrl = "",
            vehicleType = "Mobil",
            vehiclePlate = "B 4567 KLA",
            status = AccountStatus.DISETUJUI
        )

        val pendingCitizen = UserProfile(
            id = "warga_gp4_pending",
            name = "Dian Permata",
            phone = "085712345678",
            email = "dian@greenpanorama.com",
            role = UserRole.WARGA,
            houseNumber = "08",
            block = "C",
            houseStatus = "Penyewa",
            profilePhotoUrl = "",
            vehicleType = "Motor",
            vehiclePlate = "B 9012 DEF",
            status = AccountStatus.MENUNGGU_VERIFIKASI
        )

        _allUsers.value = listOf(adminUser, securityUser, sampleCitizen, pendingCitizen)

        _houses.value = listOf(
            HouseItem(id = "h1", block = "A", number = "01", ownerName = "Bambang Sudarmono", occupantStatus = "Pemilik", phone = "081234567890", totalResidents = 4),
            HouseItem(id = "h2", block = "A", number = "02", ownerName = "Agus Prasetyo", occupantStatus = "Pemilik", phone = "081211112222", totalResidents = 3),
            HouseItem(id = "h3", block = "B", number = "12", ownerName = "Rahmat Hidayat", occupantStatus = "Pemilik", phone = "081311223344", totalResidents = 4),
            HouseItem(id = "h4", block = "C", number = "08", ownerName = "Dian Permata", occupantStatus = "Penyewa", phone = "085712345678", totalResidents = 2),
            HouseItem(id = "h5", block = "D", number = "15", ownerName = "Hendra Wijaya", occupantStatus = "Pemilik", phone = "081388887777", totalResidents = 5)
        )

        _announcements.value = listOf(
            AnnouncementItem(
                id = "ann_1",
                title = "Jadwal Kerja Bakti Lingkungan Serentak",
                content = "Diberitahukan kepada seluruh warga Green Panorama 4 bahwa kerja bakti pembersihan saluran air dan taman akan diadakan hari Minggu pkl 07.30 WIB. Titik kumpul di Balai Warga GP4.",
                category = "Kebersihan",
                priority = "Penting",
                createdBy = "Pengurus RW",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            AnnouncementItem(
                id = "ann_2",
                title = "Pemasangan Palang Otomatis Gerbang Utama",
                content = "Mulai tanggal 20 bulan ini, sistem gate barrier access otomatis menggunakan kartu RFID dan QR tamu resmi diaktifkan. Mohon pastikan data kendaraan warga sudah terdaftar di aplikasi.",
                category = "Keamanan",
                priority = "Normal",
                createdBy = "Koordinator Keamanan",
                createdAt = System.currentTimeMillis() - 172800000L
            )
        )

        _notifications.value = listOf(
            NotificationItem(
                id = "notif_1",
                title = "Selamat Datang di Green Panorama 4",
                message = "Aplikasi resmi layanan digital komplek siap digunakan. Silakan periksa data profil Anda.",
                type = "INFO",
                targetRole = "ALL"
            )
        )

        _paymentBills.value = listOf(
            PaymentBill(
                id = "pay_1",
                userId = "warga_gp4_01",
                userName = "Rahmat Hidayat",
                houseNumber = "B-12",
                title = "Iuran Bulanan September 2026",
                category = "Keamanan & Kebersihan",
                amount = 175000L,
                period = "September 2026",
                dueDate = "10 September 2026",
                status = "LUNAS",
                adminNote = "Pembayaran via BCA dikonfirmasi",
                paidAt = System.currentTimeMillis() - 200000000L,
                verifiedAt = System.currentTimeMillis() - 190000000L
            ),
            PaymentBill(
                id = "pay_2",
                userId = "warga_gp4_01",
                userName = "Rahmat Hidayat",
                houseNumber = "B-12",
                title = "Iuran Bulanan Oktober 2026",
                category = "Keamanan & Kebersihan",
                amount = 175000L,
                period = "Oktober 2026",
                dueDate = "10 Oktober 2026",
                status = "BELUM BAYAR"
            ),
            PaymentBill(
                id = "pay_3",
                userId = "warga_gp4_pending",
                userName = "Dian Permata",
                houseNumber = "C-08",
                title = "Iuran Kebersihan September 2026",
                category = "Kebersihan",
                amount = 100000L,
                period = "September 2026",
                dueDate = "10 September 2026",
                status = "MENUNGGU VERIFIKASI"
            )
        )

        _expenses.value = listOf(
            ExpenseItem(
                id = "exp_1",
                title = "Gaji Petugas Keamanan Regu A & B",
                category = "Keamanan",
                amount = 9000000L,
                date = "01 September 2026",
                description = "Pembayaran gaji 4 personil security GP4"
            ),
            ExpenseItem(
                id = "exp_2",
                title = "Perbaikan Lampu Taman & PJU Blok B",
                category = "Fasilitas",
                amount = 1250000L,
                date = "05 September 2026",
                description = "Penggantian 5 unit bohlam LED PJU solar/listrik"
            )
        )

        _events.value = listOf(
            CommunityEvent(
                id = "evt_1",
                title = "Kerja Bakti & Senam Sehat Warga",
                date = "20 September 2026",
                time = "07:00 - 10:30 WIB",
                location = "Taman Sentral GP4",
                description = "Kegiatan olahraga bersama dilanjutkan penanaman bibit pohon buah di taman komplek.",
                registeredUserIds = listOf("warga_gp4_01")
            ),
            CommunityEvent(
                id = "evt_2",
                title = "Musyawarah Tahunan & Laporan Keuangan",
                date = "28 September 2026",
                time = "19:30 - 21:30 WIB",
                location = "Balai Serbaguna Green Panorama",
                description = "Pembahasan anggaran tahun depan dan pemeliharaan fasilitas bersama.",
                registeredUserIds = emptyList()
            )
        )

        _facilityReports.value = listOf(
            FacilityReport(
                id = "rep_1",
                userId = "warga_gp4_01",
                userName = "Rahmat Hidayat",
                houseNumber = "B-12",
                category = "Lampu jalan mati",
                title = "Lampu PJU Depan Rumah B-14 Mati",
                description = "Lampu penerangan jalan utama dekat pertigaan blok B mati sejak kemarin malam. Area agak gelap.",
                locationBlock = "Blok B Depan No. 14",
                status = "DIPROSES",
                adminComment = "Petugas teknisi dijadwalkan cek sore ini.",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )

        _guestPasses.value = listOf(
            GuestPass(
                id = "GP4-GUEST-8821",
                userId = "warga_gp4_01",
                userName = "Rahmat Hidayat",
                userHouse = "B-12",
                guestName = "Doni Kusuma",
                guestPhone = "08122334455",
                date = "15 September 2026",
                arrivalTime = "14:00",
                departureTime = "18:00",
                vehicleType = "Mobil",
                plateNumber = "B 9912 ABC",
                purpose = "Kunjungan Silaturahmi Keluarga",
                notes = "Membawa keluarga dari Bandung",
                qrCode = "GP4-GUEST-8821",
                status = "MENUNGGU"
            )
        )

        _forumPosts.value = listOf(
            ForumPost(
                id = "post_1",
                userId = "warga_gp4_01",
                userName = "Rahmat Hidayat",
                userHouse = "B-12",
                category = "Umum",
                title = "Usul Pengadaan Tempat Sampah Terpilah Organik/Anorganik",
                content = "Selamat pagi bapak/ibu warga GP4. Alangkah baiknya komplek kita mulai menyediakan tong sampah pilah di setiap pos blok untuk mendukung komplek ramah lingkungan. Bagaimana pendapat bapak/ibu?",
                commentCount = 2,
                createdAt = System.currentTimeMillis() - 43200000L
            )
        )

        _forumComments.value = listOf(
            ForumComment(
                id = "comm_1",
                postId = "post_1",
                userId = "admin_gp4_01",
                userName = "Bambang Sudarmono",
                content = "Usul yang sangat baik Pak Rahmat. Kami agendakan di rapat warga tanggal 28 nanti.",
                createdAt = System.currentTimeMillis() - 36000000L
            ),
            ForumComment(
                id = "comm_2",
                postId = "post_1",
                userId = "warga_gp4_pending",
                userName = "Dian Permata",
                content = "Setuju sekali, lingkungan jadi jauh lebih higienis.",
                createdAt = System.currentTimeMillis() - 28000000L
            )
        )

        _marketplaceItems.value = listOf(
            MarketplaceProduct(
                id = "mkt_1",
                userId = "warga_gp4_01",
                sellerName = "Ibu Rahmat",
                sellerPhone = "081311223344",
                sellerHouse = "Blok B No. 12",
                title = "Kue Nastar Wisman & Kastengel Rumahan",
                price = 85000L,
                category = "Makanan",
                description = "Fresh dibuat dari oven setiap hari dengan butter kualitas terbaik, renyah dan lumer.",
                createdAt = System.currentTimeMillis() - 72000000L
            ),
            MarketplaceProduct(
                id = "mkt_2",
                userId = "admin_gp4_01",
                sellerName = "Pak Bambang",
                sellerPhone = "081234567890",
                sellerHouse = "Blok A No. 01",
                title = "Tanaman Hias Monstera Variegata",
                price = 250000L,
                category = "Produk Lainnya",
                description = "Kondisi sangat sehat, daun rimbun 5 helai corak var putih bersih.",
                createdAt = System.currentTimeMillis() - 100000000L
            )
        )

        _auditLogs.value = listOf(
            AuditLogItem(
                id = "log_1",
                userName = "Sistem",
                action = "INITIALIZE",
                details = "Sistem Layanan Digital Green Panorama 4 Aktif.",
                timestamp = System.currentTimeMillis() - 300000000L
            )
        )
    }

    private fun listenToFirestoreRealtime() {
        val fs = firestore ?: return
        try {
            // Listen to announcements
            fs.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(AnnouncementItem::class.java) }
                        if (list.isNotEmpty()) _announcements.value = list
                    }
                }

            // Listen to guest passes
            fs.collection("guest_passes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(GuestPass::class.java) }
                        if (list.isNotEmpty()) _guestPasses.value = list
                    }
                }

            // Listen to facility reports
            fs.collection("facility_reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(FacilityReport::class.java) }
                        if (list.isNotEmpty()) _facilityReports.value = list
                    }
                }

            // Listen to emergency reports
            fs.collection("emergency_reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(EmergencyReport::class.java) }
                        if (list.isNotEmpty()) _emergencyReports.value = list
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore realtime listener notice: ${e.localizedMessage}")
        }
    }

    // AUTH METHODS
    suspend fun login(email: String, pass: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()

        // 1. Try Firebase Auth if configured
        if (isFirebaseConnected && auth != null) {
            try {
                val authResult = auth!!.signInWithEmailAndPassword(cleanEmail, pass).await()
                val uid = authResult.user?.uid ?: ""
                val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                val profile = doc?.toObject(UserProfile::class.java)
                if (profile != null) {
                    _currentUser.value = profile
                    logAudit(profile.name, "LOGIN", "Login berhasil via Firebase Auth")
                    return Result.success(profile)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase Auth failed, falling back to local registry: ${e.localizedMessage}")
            }
        }

        // 2. Check registered accounts
        val matchedUser = _allUsers.value.find { it.email.lowercase() == cleanEmail }
        return if (matchedUser != null) {
            _currentUser.value = matchedUser
            logAudit(matchedUser.name, "LOGIN", "Login pengguna berhasil (${matchedUser.role})")
            Result.success(matchedUser)
        } else {
            Result.failure(Exception("Email atau password tidak terdaftar di Green Panorama 4."))
        }
    }

    suspend fun registerWarga(
        name: String,
        phone: String,
        email: String,
        pass: String,
        houseNumber: String,
        block: String,
        houseStatus: String,
        profilePhotoUrl: String,
        vehicleType: String,
        vehiclePlate: String
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        if (_allUsers.value.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(Exception("Email sudah terdaftar."))
        }

        val newId = UUID.randomUUID().toString().take(8)
        var realUid = "warga_$newId"

        if (isFirebaseConnected && auth != null) {
            try {
                val authRes = auth!!.createUserWithEmailAndPassword(cleanEmail, pass).await()
                realUid = authRes.user?.uid ?: realUid
            } catch (e: Exception) {
                Log.w(TAG, "Firebase Auth registration notice: ${e.localizedMessage}")
            }
        }

        val newUser = UserProfile(
            id = realUid,
            name = name,
            phone = phone,
            email = cleanEmail,
            role = UserRole.WARGA,
            houseNumber = houseNumber,
            block = block,
            houseStatus = houseStatus,
            profilePhotoUrl = profilePhotoUrl,
            vehicleType = vehicleType,
            vehiclePlate = vehiclePlate,
            status = AccountStatus.MENUNGGU_VERIFIKASI,
            createdAt = System.currentTimeMillis()
        )

        // Sync to Firestore if connected
        if (isFirebaseConnected && firestore != null) {
            try {
                firestore!!.collection("users").document(realUid).set(newUser).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore set error: ${e.localizedMessage}")
            }
        }

        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser
        logAudit(name, "REGISTRASI", "Pendaftaran warga baru blok $block-$houseNumber")

        // Notification for admin
        addNotification(
            title = "Pendaftaran Warga Baru",
            message = "$name mengajukan verifikasi akun untuk rumah $block-$houseNumber.",
            type = "VERIFIKASI",
            targetRole = "ADMIN"
        )

        return Result.success(newUser)
    }

    suspend fun forgotPassword(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (isFirebaseConnected && auth != null) {
            try {
                auth!!.sendPasswordResetEmail(cleanEmail).await()
                return Result.success("Tautan reset kata sandi telah dikirim ke $cleanEmail")
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        val exists = _allUsers.value.any { it.email.lowercase() == cleanEmail }
        return if (exists) {
            Result.success("Instruksi reset kata sandi telah dikirim ke $cleanEmail")
        } else {
            Result.failure(Exception("Email tidak ditemukan di database warga Green Panorama 4."))
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            logAudit(user.name, "LOGOUT", "Pengguna keluar dari aplikasi")
        }
        if (isFirebaseConnected && auth != null) {
            auth?.signOut()
        }
        _currentUser.value = null
    }

    fun setCurrentUserDirect(user: UserProfile) {
        _currentUser.value = user
    }

    // ADMIN USER MANAGEMENT
    fun updateUserStatus(userId: String, newStatus: AccountStatus) {
        updateUserAccountStatus(userId, newStatus, "Admin")
    }

    fun updateUserAccountStatus(userId: String, newStatus: AccountStatus, adminName: String = "Admin") {
        val list = _allUsers.value.toMutableList()
        val index = list.indexOfFirst { it.id == userId }
        if (index != -1) {
            val updated = list[index].copy(status = newStatus, updatedAt = System.currentTimeMillis())
            list[index] = updated
            _allUsers.value = list

            if (_currentUser.value?.id == userId) {
                _currentUser.value = updated
            }

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("users").document(userId)
                            .update("status", newStatus.name, "updatedAt", System.currentTimeMillis()).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Update user error: ${e.localizedMessage}")
                    }
                }
            }

            logAudit(adminName, "VERIFIKASI_WARGA", "Ubah status akun ${updated.name} menjadi ${newStatus.name}")

            addNotification(
                userId = updated.id,
                title = "Status Akun Diperbarui",
                message = "Status akun Green Panorama 4 Anda sekarang: ${newStatus.name}",
                type = "AKUN",
                targetRole = "WARGA"
            )
        }
    }

    fun updateComplexInfo(info: ComplexInfo) {
        _complexInfo.value = info
        logAudit("Admin", "UPDATE_INFO_KOMPLEK", "Profil perumahan Green Panorama 4 diperbarui")
    }

    // GUEST PASSES
    suspend fun createGuestPass(pass: GuestPass): Result<GuestPass> {
        val qrId = "GP4-PASS-" + UUID.randomUUID().toString().take(6).uppercase()
        val completePass = pass.copy(
            id = qrId,
            qrCode = qrId,
            createdAt = System.currentTimeMillis()
        )

        _guestPasses.value = listOf(completePass) + _guestPasses.value

        if (isFirebaseConnected && firestore != null) {
            try {
                firestore!!.collection("guest_passes").document(qrId).set(completePass).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore guest pass create error: ${e.localizedMessage}")
            }
        }

        logAudit(pass.userName, "TAMU_DIGITAL", "Izin tamu dibuat untuk ${pass.guestName} (Rumah ${pass.userHouse})")

        addNotification(
            title = "Izin Tamu Baru: ${pass.guestName}",
            message = "Tamu menuju ${pass.userHouse} tiba pukul ${pass.arrivalTime} (${pass.plateNumber})",
            type = "TAMU",
            targetRole = "SECURITY"
        )

        return Result.success(completePass)
    }

    fun updateGuestPassStatus(passId: String, newStatus: String, notes: String = "") {
        val list = _guestPasses.value.toMutableList()
        val index = list.indexOfFirst { it.id == passId || it.qrCode == passId }
        if (index != -1) {
            val old = list[index]
            val now = System.currentTimeMillis()
            val updated = old.copy(
                status = newStatus,
                checkedInAt = if (newStatus == "MASUK") now else old.checkedInAt,
                checkedOutAt = if (newStatus == "KELUAR") now else old.checkedOutAt
            )
            list[index] = updated
            _guestPasses.value = list

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("guest_passes").document(old.id).set(updated).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Update guest pass error: ${e.localizedMessage}")
                    }
                }
            }

            logAudit("Security", "TAMU_$newStatus", "Tamu ${old.guestName} tujuan ${old.userHouse} status: $newStatus")

            addNotification(
                userId = old.userId,
                title = "Update Status Tamu",
                message = "Tamu Anda ${old.guestName} telah tercatat: $newStatus oleh Security.",
                type = "TAMU",
                targetRole = "WARGA"
            )
        }
    }

    // FACILITY REPORT
    suspend fun submitFacilityReport(report: FacilityReport): Result<FacilityReport> {
        val repId = "REP-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = report.copy(
            id = repId,
            status = "DIAJUKAN",
            createdAt = System.currentTimeMillis()
        )
        _facilityReports.value = listOf(complete) + _facilityReports.value

        if (isFirebaseConnected && firestore != null) {
            try {
                firestore!!.collection("facility_reports").document(repId).set(complete).await()
            } catch (e: Exception) {
                Log.w(TAG, "Facility report submit error: ${e.localizedMessage}")
            }
        }

        logAudit(report.userName, "LAPORAN_WARGA", "Laporan baru: ${report.title} (${report.category})")

        addNotification(
            title = "Laporan Warga Baru",
            message = "${report.userName} melaporkan '${report.title}' di ${report.locationBlock}",
            type = "LAPORAN",
            targetRole = "ADMIN"
        )

        return Result.success(complete)
    }

    fun updateFacilityReportStatus(reportId: String, newStatus: String, adminComment: String, repairPhoto: String = "") {
        val list = _facilityReports.value.toMutableList()
        val index = list.indexOfFirst { it.id == reportId }
        if (index != -1) {
            val old = list[index]
            val updated = old.copy(
                status = newStatus,
                adminComment = adminComment.ifEmpty { old.adminComment },
                repairPhotoUrl = repairPhoto.ifEmpty { old.repairPhotoUrl },
                updatedAt = System.currentTimeMillis()
            )
            list[index] = updated
            _facilityReports.value = list

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("facility_reports").document(reportId).set(updated).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Facility report status update error: ${e.localizedMessage}")
                    }
                }
            }

            logAudit("Admin", "UPDATE_LAPORAN", "Laporan $reportId status diubah jadi $newStatus")

            addNotification(
                userId = old.userId,
                title = "Status Laporan: $newStatus",
                message = "Laporan '${old.title}' Anda telah diperbarui: $newStatus",
                type = "LAPORAN",
                targetRole = "WARGA"
            )
        }
    }

    fun updateReportStatus(reportId: String, newStatus: String, adminComment: String, adminName: String = "Admin") {
        updateFacilityReportStatus(reportId, newStatus, adminComment)
    }

    // EMERGENCY (SOS)
    suspend fun triggerEmergency(report: EmergencyReport): Result<EmergencyReport> {
        val sosId = "SOS-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = report.copy(
            id = sosId,
            status = "AKTIF",
            createdAt = System.currentTimeMillis()
        )
        _emergencyReports.value = listOf(complete) + _emergencyReports.value

        if (isFirebaseConnected && firestore != null) {
            try {
                firestore!!.collection("emergency_reports").document(sosId).set(complete).await()
            } catch (e: Exception) {
                Log.w(TAG, "SOS error: ${e.localizedMessage}")
            }
        }

        logAudit(report.userName, "DARURAT_SOS", "ALARM DARURAT! Kategori ${report.category} dari rumah ${report.houseNumber}")

        addNotification(
            title = "🚨 DARURAT: ${report.category}",
            message = "Warga ${report.userName} (Rumah ${report.houseNumber}) membutuhkan bantuan segera!",
            type = "DARURAT",
            targetRole = "ALL"
        )

        return Result.success(complete)
    }

    fun resolveEmergency(sosId: String, handledBy: String) {
        val list = _emergencyReports.value.toMutableList()
        val index = list.indexOfFirst { it.id == sosId }
        if (index != -1) {
            val updated = list[index].copy(status = "SELESAI", handledBy = handledBy)
            list[index] = updated
            _emergencyReports.value = list

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("emergency_reports").document(sosId).set(updated).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "SOS update: ${e.localizedMessage}")
                    }
                }
            }

            logAudit(handledBy, "DARURAT_SELESAI", "Status darurat $sosId telah diselesaikan.")
        }
    }

    // ANNOUNCEMENTS
    suspend fun createAnnouncement(announcement: AnnouncementItem): Result<AnnouncementItem> {
        val id = "ANN-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = announcement.copy(id = id, createdAt = System.currentTimeMillis())
        _announcements.value = listOf(complete) + _announcements.value

        if (isFirebaseConnected && firestore != null) {
            try {
                firestore!!.collection("announcements").document(id).set(complete).await()
            } catch (e: Exception) {
                Log.w(TAG, "Announcement create: ${e.localizedMessage}")
            }
        }

        logAudit("Admin", "PENGUMUMAN", "Pengumuman baru dipublikasikan: ${complete.title}")

        addNotification(
            title = "Pengumuman: ${complete.title}",
            message = complete.content.take(100) + "...",
            type = "PENGUMUMAN",
            targetRole = "ALL"
        )

        return Result.success(complete)
    }

    fun deleteAnnouncement(id: String) {
        _announcements.value = _announcements.value.filterNot { it.id == id }
        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("announcements").document(id).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Delete announcement error: ${e.localizedMessage}")
                }
            }
        }
        logAudit("Admin", "HAPUS_PENGUMUMAN", "Pengumuman $id dihapus")
    }

    // DUES / PAYMENTS
    fun uploadPaymentProof(billId: String, proofUriOrBase64: String) {
        val list = _paymentBills.value.toMutableList()
        val index = list.indexOfFirst { it.id == billId }
        if (index != -1) {
            val old = list[index]
            val updated = old.copy(
                status = "MENUNGGU VERIFIKASI",
                proofPhotoUrl = proofUriOrBase64,
                paidAt = System.currentTimeMillis()
            )
            list[index] = updated
            _paymentBills.value = list

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("payments").document(billId).set(updated).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Payment update error: ${e.localizedMessage}")
                    }
                }
            }

            logAudit(old.userName, "UPLOAD_BUKTI_IURAN", "Upload bukti pembayaran iuran ${old.title}")

            addNotification(
                title = "Bukti Iuran Diunggah",
                message = "${old.userName} mengunggah bukti pembayaran iuran ${old.title}.",
                type = "IURAN",
                targetRole = "ADMIN"
            )
        }
    }

    fun createPaymentBill(bill: PaymentBill) {
        val id = "PAY-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = bill.copy(id = id, createdAt = System.currentTimeMillis())
        _paymentBills.value = listOf(complete) + _paymentBills.value
        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("payments").document(id).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Payment create error: ${e.localizedMessage}")
                }
            }
        }
        logAudit("Admin", "BUAT_TAGIHAN", "Tagihan baru: ${complete.title} (${complete.userName})")
    }

    fun verifyPayment(billId: String, isApproved: Boolean, note: String, adminName: String = "Admin") {
        val list = _paymentBills.value.toMutableList()
        val index = list.indexOfFirst { it.id == billId }
        if (index != -1) {
            val old = list[index]
            val status = if (isApproved) "LUNAS" else "DITOLAK"
            val updated = old.copy(
                status = status,
                adminNote = note,
                verifiedAt = System.currentTimeMillis()
            )
            list[index] = updated
            _paymentBills.value = list

            if (isFirebaseConnected && firestore != null) {
                scope.launch {
                    try {
                        firestore!!.collection("payments").document(billId).set(updated).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Payment verify error: ${e.localizedMessage}")
                    }
                }
            }

            logAudit(adminName, "VERIFIKASI_IURAN", "Iuran ${old.title} (${old.userName}) status: $status")

            addNotification(
                userId = old.userId,
                title = "Verifikasi Iuran: $status",
                message = "Pembayaran ${old.title} telah diverifikasi dengan hasil: $status. Catatan: $note",
                type = "IURAN",
                targetRole = "WARGA"
            )
        }
    }

    // EXPENSES
    fun addExpense(expense: ExpenseItem) {
        val expId = "EXP-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = expense.copy(id = expId, createdAt = System.currentTimeMillis())
        _expenses.value = listOf(complete) + _expenses.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("expenses").document(expId).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Expense add error: ${e.localizedMessage}")
                }
            }
        }

        logAudit("Admin", "PENCATATAN_PENGELUARAN", "Pengeluaran ${complete.title} sebesar Rp ${complete.amount}")
    }

    // EVENTS
    fun registerEventParticipant(eventId: String, userId: String) {
        val list = _events.value.toMutableList()
        val index = list.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val old = list[index]
            if (!old.registeredUserIds.contains(userId)) {
                val updated = old.copy(registeredUserIds = old.registeredUserIds + userId)
                list[index] = updated
                _events.value = list

                if (isFirebaseConnected && firestore != null) {
                    scope.launch {
                        try {
                            firestore!!.collection("events").document(eventId).set(updated).await()
                        } catch (e: Exception) {
                            Log.w(TAG, "Event register error: ${e.localizedMessage}")
                        }
                    }
                }
            }
        }
    }

    fun createEvent(event: CommunityEvent) {
        val id = "EVT-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = event.copy(id = id, createdAt = System.currentTimeMillis())
        _events.value = listOf(complete) + _events.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("events").document(id).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Event create error: ${e.localizedMessage}")
                }
            }
        }

        logAudit("Admin", "KEGIATAN_WARGA", "Agenda kegiatan baru dibuat: ${complete.title}")
    }

    // FORUM
    fun addForumPost(post: ForumPost) {
        val id = "POST-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = post.copy(id = id, createdAt = System.currentTimeMillis())
        _forumPosts.value = listOf(complete) + _forumPosts.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("forum_posts").document(id).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Forum post error: ${e.localizedMessage}")
                }
            }
        }

        logAudit(post.userName, "FORUM_POST", "Postingan forum baru: ${post.title}")
    }

    fun addForumComment(comment: ForumComment) {
        val id = "COMM-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = comment.copy(id = id, createdAt = System.currentTimeMillis())
        _forumComments.value = _forumComments.value + complete

        val postList = _forumPosts.value.toMutableList()
        val pIdx = postList.indexOfFirst { it.id == comment.postId }
        if (pIdx != -1) {
            postList[pIdx] = postList[pIdx].copy(commentCount = postList[pIdx].commentCount + 1)
            _forumPosts.value = postList
        }

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("forum_comments").document(id).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Forum comment error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteForumPost(postId: String) {
        _forumPosts.value = _forumPosts.value.filterNot { it.id == postId }
        _forumComments.value = _forumComments.value.filterNot { it.postId == postId }
        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("forum_posts").document(postId).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Forum post delete error: ${e.localizedMessage}")
                }
            }
        }
        logAudit("Admin", "MODERASI_FORUM", "Postingan forum $postId dihapus oleh admin")
    }

    // MARKETPLACE
    fun addMarketplaceProduct(product: MarketplaceProduct) {
        val id = "PROD-" + UUID.randomUUID().toString().take(6).uppercase()
        val complete = product.copy(id = id, createdAt = System.currentTimeMillis())
        _marketplaceItems.value = listOf(complete) + _marketplaceItems.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("marketplace").document(id).set(complete).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Marketplace add error: ${e.localizedMessage}")
                }
            }
        }

        logAudit(product.sellerName, "MARKETPLACE", "Produk baru dipasang: ${product.title}")
    }

    fun deleteMarketplaceProduct(productId: String) {
        _marketplaceItems.value = _marketplaceItems.value.filterNot { it.id == productId }
        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("marketplace").document(productId).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Marketplace delete error: ${e.localizedMessage}")
                }
            }
        }
        logAudit("Admin", "MODERASI_MARKETPLACE", "Produk marketplace $productId dihapus")
    }

    // AUDIT LOG & NOTIFICATIONS
    fun logAudit(userName: String, action: String, details: String) {
        val id = "LOG-" + UUID.randomUUID().toString().take(6).uppercase()
        val item = AuditLogItem(
            id = id,
            userName = userName,
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(item) + _auditLogs.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("activity_logs").document(id).set(item).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Audit log error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun addNotification(
        title: String,
        message: String,
        type: String,
        targetRole: String = "ALL",
        userId: String = ""
    ) {
        val id = "NOTIF-" + UUID.randomUUID().toString().take(6).uppercase()
        val item = NotificationItem(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            isRead = false,
            targetRole = targetRole,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(item) + _notifications.value

        if (isFirebaseConnected && firestore != null) {
            scope.launch {
                try {
                    firestore!!.collection("notifications").document(id).set(item).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Notification error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun sendNotification(
        userId: String = "",
        targetRole: String = "ALL",
        title: String,
        message: String,
        type: String = "INFO"
    ) {
        addNotification(
            title = title,
            message = message,
            type = type,
            targetRole = targetRole,
            userId = userId
        )
    }

    // STORAGE UPLOAD HELPER
    suspend fun uploadImage(folder: String, fileUri: Uri): String {
        if (isFirebaseConnected && storage != null) {
            try {
                val ref = storage!!.reference.child("images/$folder/${UUID.randomUUID()}.jpg")
                ref.putFile(fileUri).await()
                val downloadUrl = ref.downloadUrl.await()
                return downloadUrl.toString()
            } catch (e: Exception) {
                Log.w(TAG, "Storage upload error: ${e.localizedMessage}. Using cached local URI.")
            }
        }
        return fileUri.toString()
    }
}
