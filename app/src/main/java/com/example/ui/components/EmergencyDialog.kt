package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.EmergencyReport
import com.example.ui.theme.EmergencyRed
import com.google.android.gms.location.LocationServices

@Composable
fun EmergencyDialog(
    currentUserName: String,
    currentUserHouse: String,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmitEmergency: (EmergencyReport) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Keamanan") }
    var description by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    userLocation = loc
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    userLocation = loc
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    val categories = listOf(
        Pair("Keamanan", Icons.Default.Shield),
        Pair("Kebakaran", Icons.Default.LocalFireDepartment),
        Pair("Kecelakaan", Icons.Default.CarCrash),
        Pair("Medis", Icons.Default.LocalHospital),
        Pair("Bencana", Icons.Default.Flood),
        Pair("Lainnya", Icons.Default.Warning)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = EmergencyRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "TOMBOL DARURAT (SOS)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EmergencyRed
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih kategori situasi darurat untuk memanggil bantuan security dan pengurus RW secara instan.",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Kategori Darurat:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { (cat, icon) ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmergencyRed.copy(alpha = 0.15f) else Color.Transparent,
                            border = ButtonDefaults.outlinedButtonBorder(isSelected),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmergencyRed else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Tambahan (opsional)") },
                    placeholder = { Text("Contoh: Orang mencurigakan di gang, asap tebal, dll.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Location info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lokasi GPS:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (userLocation != null)
                                    "Lat: ${"%.4f".format(userLocation!!.latitude)}, Long: ${"%.4f".format(userLocation!!.longitude)}"
                                else if (hasLocationPermission)
                                    "Mendeteksi koordinat..."
                                else
                                    "Izin lokasi belum diberikan",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!hasLocationPermission) {
                            TextButton(onClick = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }) {
                                Text("IZINKAN", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showConfirmationDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text("KIRIM DARURAT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL")
            }
        }
    )

    // Mandatory confirmation modal
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            icon = { Icon(Icons.Default.ReportProblem, contentDescription = null, tint = EmergencyRed) },
            title = { Text("Konfirmasi Sinyal Darurat") },
            text = {
                Text(
                    "Apakah Anda yakin ingin mengirim sinyal DARURAT kategori $selectedCategory ke Pos Security dan Pengurus komplek sekarang?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        val report = EmergencyReport(
                            userId = currentUserId,
                            userName = currentUserName,
                            houseNumber = currentUserHouse,
                            category = selectedCategory,
                            description = description,
                            latitude = userLocation?.latitude,
                            longitude = userLocation?.longitude
                        )
                        onSubmitEmergency(report)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("YA, KIRIM SEKARANG!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("CEK LAGI")
                }
            }
        )
    }
}
