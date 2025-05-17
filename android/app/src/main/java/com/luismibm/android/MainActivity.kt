package com.luismibm.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismibm.android.api.RetrofitClient
import com.luismibm.android.ui.LoginScreen
import com.luismibm.android.ui.HomeScreen
import com.luismibm.android.ui.RegisterScreen
import com.luismibm.android.ui.SpaceSelectionScreen
import com.luismibm.android.ui.TransactionsScreen
import com.luismibm.android.ui.BudgetByCategoryScreen
import com.luismibm.android.ui.ComparisonScreen
import com.luismibm.android.ui.SettingsScreen
import com.luismibm.android.ui.theme.AndroidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private enum class Screen {
        LOGIN, REGISTER, SPACE_SELECTION, HOME, TRANSACTIONS, BUDGET_BY_CATEGORY, COMPARISON, SETTINGS
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidTheme {
                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                var token by remember { mutableStateOf("") }
                var hasSpace by remember { mutableStateOf(false) }
                var spaceId by remember { mutableStateOf("") }
                var spaceName by remember { mutableStateOf("") }
                
                val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
                val scope = rememberCoroutineScope()

                fun getScreenTitle(screen: Screen): String {
                    return when (screen) {
                        Screen.HOME -> "Inicio"
                        Screen.TRANSACTIONS -> "Transacciones"
                        Screen.BUDGET_BY_CATEGORY -> "Presupuesto por Categoría"
                        Screen.COMPARISON -> "Comparador"
                        Screen.SETTINGS -> "Ajustes"
                        else -> "Trackify"
                    }
                }
                
                if (currentScreen == Screen.LOGIN || currentScreen == Screen.REGISTER || currentScreen == Screen.SPACE_SELECTION) {
                    androidx.compose.material3.Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        when (currentScreen) {
                            Screen.LOGIN -> LoginScreen(
                                onLoginSuccess = { accessToken ->
                                    token = accessToken
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            val user = RetrofitClient.authService.getCurrentUser("Bearer $accessToken")
                                            hasSpace = user.spaceId != null
                                            if (hasSpace && user.spaceId != null) {
                                                spaceId = user.spaceId
                                                try {
                                                    val spaces = RetrofitClient.authService.getSpaces("Bearer $accessToken")
                                                    val userSpace = spaces.find { it.id == spaceId }
                                                    spaceName = userSpace?.name ?: "Mi Espacio"
                                                } catch (e: Exception) { spaceName = "Mi Espacio" }
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
                                onLoginError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() },
                                onNavigateToRegister = { currentScreen = Screen.REGISTER }
                            )
                            Screen.REGISTER -> RegisterScreen(
                                onRegisterSuccess = {
                                    Toast.makeText(this@MainActivity, "Registro exitoso. Por favor inicia sesión.", Toast.LENGTH_LONG).show()
                                    currentScreen = Screen.LOGIN
                                },
                                onRegisterError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() },
                                onNavigateToLogin = { currentScreen = Screen.LOGIN }
                            )
                            Screen.SPACE_SELECTION -> SpaceSelectionScreen(
                                token = token,
                                onSpaceSelected = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            val user = RetrofitClient.authService.getCurrentUser("Bearer $token")
                                            if (user.spaceId != null) {
                                                spaceId = user.spaceId
                                                try {
                                                    val spaces = RetrofitClient.authService.getSpaces("Bearer $token")
                                                    val userSpace = spaces.find { it.id == spaceId }
                                                    spaceName = userSpace?.name ?: "Mi Espacio"
                                                } catch (e: Exception) { spaceName = "Mi Espacio" }
                                            }
                                            currentScreen = Screen.HOME
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error al obtener espacio: ${e.message}", Toast.LENGTH_LONG).show()
                                            currentScreen = Screen.HOME
                                        }
                                    }
                                },
                                onError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }
                            )
                            else -> { }
                        }
                    }
                } else {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = { Text(getScreenTitle(currentScreen), color = Color.White) },
                                backgroundColor = Color.Black,
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { scaffoldState.drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                                    }
                                }
                            )
                        },
                        drawerBackgroundColor = Color.Black,
                        drawerContentColor = Color.White,
                        drawerContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Trackify",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(32.dp))

                                DrawerItem(icon = Icons.Default.Home, text = "Inicio") {
                                    currentScreen = Screen.HOME
                                    scope.launch { scaffoldState.drawerState.close() }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                DrawerItem(icon = Icons.Filled.Face, text = "Transacciones") {
                                    if (token.isNotBlank() && spaceId.isNotBlank()) {
                                        currentScreen = Screen.TRANSACTIONS
                                    } else {
                                        Toast.makeText(this@MainActivity, "Error: Sesión inválida o espacio no seleccionado.", Toast.LENGTH_LONG).show()
                                        currentScreen = Screen.LOGIN
                                    }
                                    scope.launch { scaffoldState.drawerState.close() }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                DrawerItem(icon = Icons.Filled.Face, text = "Presupuesto por Categoría") {
                                    if (token.isNotBlank() && spaceId.isNotBlank()) {
                                        currentScreen = Screen.BUDGET_BY_CATEGORY
                                    } else {
                                        Toast.makeText(this@MainActivity, "Error: Sesión inválida o espacio no seleccionado.", Toast.LENGTH_LONG).show()
                                        currentScreen = Screen.LOGIN
                                    }
                                    scope.launch { scaffoldState.drawerState.close() }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                DrawerItem(icon = Icons.Filled.Face, text = "Comparador") {
                                    if (token.isNotBlank() && spaceId.isNotBlank()) {
                                        currentScreen = Screen.COMPARISON
                                    } else {
                                        Toast.makeText(this@MainActivity, "Error: Sesión inválida o espacio no seleccionado.", Toast.LENGTH_LONG).show()
                                        currentScreen = Screen.LOGIN
                                    }
                                    scope.launch { scaffoldState.drawerState.close() }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                DrawerItem(icon = Icons.Filled.Settings, text = "Ajustes") {
                                    currentScreen = Screen.SETTINGS
                                    scope.launch { scaffoldState.drawerState.close() }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(16.dp))

                                DrawerItem(icon = Icons.Default.Close, text = "Cerrar sesión") {
                                    scope.launch { scaffoldState.drawerState.close() }
                                    token = ""
                                    hasSpace = false
                                    spaceId = ""
                                    spaceName = ""
                                    currentScreen = Screen.LOGIN
                                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { innerPadding ->
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
                            Screen.TRANSACTIONS -> TransactionsScreen(
                                modifier = Modifier.padding(innerPadding),
                                token = token,
                                spaceId = spaceId,
                                onError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }
                            )
                            Screen.BUDGET_BY_CATEGORY -> BudgetByCategoryScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                            Screen.COMPARISON -> ComparisonScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                            else -> { }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = Color.White
        )
    }
}
