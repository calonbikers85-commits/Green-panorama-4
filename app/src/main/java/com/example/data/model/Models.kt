package com.example.data.model

enum class UserRole {
    WARGA,
    SECURITY,
    ADMIN
}

enum class AccountStatus {
    MENUNGGU_VERIFIKASI,
    DISETUJUI,
    DITOLAK,
    NONAKTIF
}

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.WARGA,
    val houseNumber: String = "",
    val block: String = "",
    val houseStatus: String = "Pemilik", // Pemilik / Penyewa
    val profilePhotoUrl: String = "",
    val vehicleType: String = "",
    val vehiclePlate: String = "",
    val status: AccountStatus = AccountStatus.MENUNGGU_VERIFIKASI,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HouseItem(
    val id: String = "",
    val block: String = "A",
    val number: String = "01",
    val ownerName: String = "",
    val occupantStatus: String = "Pemilik",
    val phone: String = "",
    val totalResidents: Int = 1
)

data class VehicleItem(
    val id: String = "",
    val userId: String = "",
    val type: String = "Mobil", // Mobil / Motor
    val plateNumber: String = "",
    val model: String = "",
    val color: String = ""
)

data class AnnouncementItem(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val photoUrl: String = "",
    val category: String = "Umum", // Umum, Keamanan, Kebersihan, Keuangan, Kegiatan, Darurat
    val priority: String = "Normal", // Normal, Penting, Mendesak
    val createdBy: String = "Admin",
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String = "",
    val userId: String = "", // Empty means broadcast
    val title: String = "",
    val message: String = "",
    val type: String = "INFO", // INFO, DARURAT, TAMU, IURAN, LAPORAN
    val isRead: Boolean = false,
    val targetRole: String = "ALL", // ALL, WARGA, SECURITY, ADMIN
    val createdAt: Long = System.currentTimeMillis()
)

data class GuestPass(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHouse: String = "",
    val guestName: String = "",
    val guestPhone: String = "",
    val date: String = "",
    val arrivalTime: String = "",
    val departureTime: String = "",
    val vehicleType: String = "Mobil",
    val plateNumber: String = "",
    val purpose: String = "",
    val notes: String = "",
    val qrCode: String = "",
    val status: String = "MENUNGGU", // MENUNGGU, MASUK, KELUAR, DITOLAK
    val checkedInAt: Long? = null,
    val checkedOutAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class FacilityReport(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val houseNumber: String = "",
    val category: String = "Lampu jalan mati",
    val title: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val locationBlock: String = "",
    val status: String = "DIAJUKAN", // DIAJUKAN, DIPROSES, DIKERJAKAN, SELESAI
    val adminComment: String = "",
    val repairPhotoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class EmergencyReport(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val houseNumber: String = "",
    val category: String = "Keamanan", // Keamanan, Kebakaran, Kecelakaan, Medis, Bencana, Lainnya
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "AKTIF", // AKTIF, DITANGANI, SELESAI
    val handledBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentBill(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val houseNumber: String = "",
    val userHouse: String = houseNumber,
    val title: String = "Iuran Bulanan",
    val category: String = "Keamanan & Kebersihan",
    val amount: Long = 150000L,
    val period: String = "September 2026",
    val dueDate: String = "10 September 2026",
    val status: String = "BELUM BAYAR", // BELUM BAYAR, MENUNGGU VERIFIKASI, LUNAS, DITOLAK
    val proofPhotoUrl: String = "",
    val paymentProofUrl: String = proofPhotoUrl,
    val adminNote: String = "",
    val paidAt: Long? = null,
    val verifiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExpenseItem(
    val id: String = "",
    val title: String = "",
    val category: String = "Operasional",
    val amount: Long = 0L,
    val date: String = "",
    val description: String = "",
    val proofPhotoUrl: String = "",
    val createdBy: String = "Admin",
    val recordedBy: String = createdBy,
    val createdAt: Long = System.currentTimeMillis()
)

data class CommunityEvent(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val description: String = "",
    val posterUrl: String = "",
    val registeredUserIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ForumPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val userHouse: String = "",
    val category: String = "Umum",
    val title: String = "",
    val content: String = "",
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class ForumComment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MarketplaceProduct(
    val id: String = "",
    val userId: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val sellerHouse: String = "",
    val title: String = "",
    val price: Long = 0L,
    val category: String = "Makanan",
    val photoUrl: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SecurityEvent(
    val id: String = "",
    val reportedBy: String = "Security",
    val title: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val location: String = "",
    val status: String = "SELESAI",
    val createdAt: Long = System.currentTimeMillis()
)

data class AuditLogItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val performedBy: String = userName,
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ComplexInfo(
    val complexName: String = "GREEN PANORAMA 4",
    val address: String = "Jl. Panorama Raya No. 4, Komplek Green Panorama 4",
    val totalBlocks: String = "5 Blok",
    val blocksList: String = "Blok A, B, C, D, E",
    val totalHouses: String = "120 Unit",
    val securityHotline: String = "0812-9900-8822 (Pos Gerbang)",
    val rwHotline: String = "0813-8877-6655 (Ketua RW 04)",
    val ambulanceHotline: String = "119 / 0811-2233-4455"
)
