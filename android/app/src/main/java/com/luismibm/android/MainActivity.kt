package com.luismibm.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.luismibm.android.api.RetrofitClient
import com.luismibm.android.ui.LoginScreen
import com.luismibm.android.ui.HomeScreen
import com.luismibm.android.ui.RegisterScreen
import com.luismibm.android.ui.SpaceSelectionScreen
import com.luismibm.android.ui.TransactionsScreen
import com.luismibm.android.ui.theme.AndroidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private enum class Screen {
        LOGIN, REGISTER, SPACE_SELECTION, HOME, TRANSACTIONS
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                    var token by remember { mutableStateOf("") }
                    var hasSpace by remember { mutableStateOf(false) }
                    var spaceId by remember { mutableStateOf("") }
                    var spaceName by remember { mutableStateOf("") }
                    
                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { accessToken ->
                                    token = accessToken
                                    // Verificar si el usuario tiene un espacio
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            // Obtener información del usuario actual
                                            val user = RetrofitClient.authService.getCurrentUser("Bearer $accessToken")
                                            
                                            // Verificar si el usuario tiene un espacio asignado
                                            hasSpace = user.spaceId != null
                                            
                                            if (hasSpace && user.spaceId != null) {
                                                // Si tiene espacio, conseguir el nombre del espacio
                                                spaceId = user.spaceId
                                                
                                                try {
                                                    val spaces = RetrofitClient.authService.getSpaces("Bearer $accessToken")
                                                    val userSpace = spaces.find { it.id == spaceId }
                                                    if (userSpace != null) {
                                                        spaceName = userSpace.name
                                                    }
                                                } catch (e: Exception) {
                                                    // No mostramos error si falla obtener el nombre del espacio
                                                    spaceName = "Mi Espacio"
                                                }
                                                
                                                currentScreen = Screen.HOME
                                            } else {
                                                currentScreen = Screen.SPACE_SELECTION
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error al verificar espacio: ${e.message}", Toast.LENGTH_LONG).show()
                                            currentScreen = Screen.SPACE_SELECTION
                                        }
                                    }
                                },
                                onLoginError = { message ->
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                },
                                onNavigateToRegister = {
                                    currentScreen = Screen.REGISTER
                                }
                            )
                        }
                        Screen.REGISTER -> {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    Toast.makeText(this@MainActivity, "Registro exitoso. Por favor inicia sesión.", Toast.LENGTH_LONG).show()
                                    currentScreen = Screen.LOGIN
                                },
                                onRegisterError = { message ->
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                },
                                onNavigateToLogin = {
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }
                        Screen.SPACE_SELECTION -> {
                            SpaceSelectionScreen(
                                token = token,
                                onSpaceSelected = {
                                    // Actualizar información del usuario después de seleccionar un espacio
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            val user = RetrofitClient.authService.getCurrentUser("Bearer $token")
                                            if (user.spaceId != null) {
                                                spaceId = user.spaceId
                                                
                                                // Intentar obtener el nombre del espacio
                                                try {
                                                    val spaces = RetrofitClient.authService.getSpaces("Bearer $token")
                                                    val userSpace = spaces.find { it.id == spaceId }
                                                    if (userSpace != null) {
                                                        spaceName = userSpace.name
                                                    }
                                                } catch (e: Exception) {
                                                    // Si falla, usamos un nombre genérico
                                                    spaceName = "Mi Espacio"
                                                }
                                            }
                                            currentScreen = Screen.HOME
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error al obtener información actualizada: ${e.message}", Toast.LENGTH_LONG).show()
                                            currentScreen = Screen.HOME
                                        }
                                    }
                                },
                                onError = { message ->
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                        Screen.HOME -> {
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLogout = {
                                    // Cerrar sesión
                                    token = ""
                                    hasSpace = false
                                    currentScreen = Screen.LOGIN
                                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateToTransactions = {
                                    currentScreen = Screen.TRANSACTIONS
                                }
                            )
                        }
                        Screen.TRANSACTIONS -> {
                            TransactionsScreen(
                                token = token,
                                spaceId = spaceId,
                                spaceName = spaceName,
                                onNavigateBack = {
                                    currentScreen = Screen.HOME
                                },
                                onError = { message ->
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
