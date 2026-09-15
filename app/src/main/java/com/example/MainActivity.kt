package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.AccountStatus
import com.example.data.model.UserRole
import com.example.ui.components.OfflineBanner
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.auth.*
import com.example.ui.screens.citizen.CitizenMainScreen
import com.example.ui.screens.security.SecurityMainScreen
import com.example.ui.theme.GreenPanoramaTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = FirebaseRepository(applicationContext)

        setContent {
            GreenPanoramaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isOffline by remember { mutableStateOf(false) }

                    // Monitor Network Connectivity
                    DisposableEffect(Unit) {
                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val callback = object : ConnectivityManager.NetworkCallback() {
                            override fun onAvailable(network: Network) {
                                isOffline = false
                            }
                            override fun onLost(network: Network) {
                                isOffline = true
                            }
                        }

                        val request = NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build()
                        connectivityManager.registerNetworkCallback(request, callback)

                        val activeNet = connectivityManager.activeNetwork
                        val caps = connectivityManager.getNetworkCapabilities(activeNet)
                        isOffline = caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                        onDispose {
                            try {
                                connectivityManager.unregisterNetworkCallback(callback)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        OfflineBanner(
                            isOffline = isOffline,
                            onRetry = {
                                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                val activeNet = connectivityManager.activeNetwork
                                val caps = connectivityManager.getNetworkCapabilities(activeNet)
                                isOffline = caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            GreenPanoramaApp(repository = repository)
                        }
                    }
                }
            }
        }
    }
}

enum class ScreenState {
    SPLASH,
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    MAIN_APP
}

@Composable
fun GreenPanoramaApp(repository: FirebaseRepository) {
    var screenState by remember { mutableStateOf(ScreenState.SPLASH) }
    val currentUser by repository.currentUser.collectAsState()

    Crossfade(targetState = screenState, label = "AppNavCrossfade") { state ->
        when (state) {
            ScreenState.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        if (currentUser != null) {
                            screenState = ScreenState.MAIN_APP
                        } else {
                            screenState = ScreenState.LOGIN
                        }
                    }
                )
            }
            ScreenState.LOGIN -> {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = {
                        screenState = ScreenState.MAIN_APP
                    },
                    onNavigateToRegister = {
                        screenState = ScreenState.REGISTER
                    },
                    onNavigateToForgotPassword = {
                        screenState = ScreenState.FORGOT_PASSWORD
                    }
                )
            }
            ScreenState.REGISTER -> {
                RegisterScreen(
                    repository = repository,
                    onRegisterSuccess = {
                        screenState = ScreenState.MAIN_APP
                    },
                    onBack = {
                        screenState = ScreenState.LOGIN
                    }
                )
            }
            ScreenState.FORGOT_PASSWORD -> {
                ForgotPasswordScreen(
                    repository = repository,
                    onBack = {
                        screenState = ScreenState.LOGIN
                    }
                )
            }
            ScreenState.MAIN_APP -> {
                val user = currentUser
                if (user == null) {
                    LaunchedEffect(Unit) {
                        screenState = ScreenState.LOGIN
                    }
                } else {
                    // RBAC check and account status verification
                    when (user.role) {
                        UserRole.WARGA -> {
                            if (user.status == AccountStatus.DISETUJUI) {
                                CitizenMainScreen(
                                    repository = repository,
                                    currentUser = user,
                                    onLogout = {
                                        repository.logout()
                                        screenState = ScreenState.LOGIN
                                    }
                                )
                            } else {
                                AccountStatusScreen(
                                    user = user,
                                    onLogout = {
                                        repository.logout()
                                        screenState = ScreenState.LOGIN
                                    },
                                    onRefresh = {
                                        // Refresh is automatic via StateFlow
                                    }
                                )
                            }
                        }
                        UserRole.SECURITY -> {
                            SecurityMainScreen(
                                repository = repository,
                                currentUser = user,
                                onLogout = {
                                    repository.logout()
                                    screenState = ScreenState.LOGIN
                                }
                            )
                        }
                        UserRole.ADMIN -> {
                            AdminMainScreen(
                                repository = repository,
                                currentUser = user,
                                onLogout = {
                                    repository.logout()
                                    screenState = ScreenState.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
