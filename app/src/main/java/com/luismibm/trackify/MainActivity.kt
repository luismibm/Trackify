package com.luismibm.trackify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luismibm.trackify.auth.RegisterScreen
import com.luismibm.trackify.ui.theme.TrackifyTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackifyTheme {

                var isAuthenticated by remember { mutableStateOf(false) }
                val navController = rememberNavController()

                var userEmail by remember { mutableStateOf("") }

                LaunchedEffect (isAuthenticated) {
                    if (isAuthenticated) {
                        navController.navigate("home")
                    }
                }

                NavHost(navController = navController, startDestination = if (isAuthenticated) "home" else "register") {
                    composable("register") {
                        RegisterScreen(
                            onSignUpSuccess = { email ->
                                userEmail = email
                                isAuthenticated = true
                            }
                        )
                    }
                    composable("home") {
                        MainScreen(navController = navController, email = userEmail)
                    }
                }

                /*
                if (isAuthenticated) {
                    MainScreen(navController = navController)
                } else {
                    RegisterScreen(
                        onSignUpSuccess = {
                            isAuthenticated = true
                        }
                    )
                }
                 */

            }
        }
    }
}

@Composable @OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(navController: NavHostController, email: String) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = "Home"
                    )
                }
            )
        }
    ) { innerPadding ->
        Row (
            modifier = Modifier.fillMaxWidth()
        ) {
            Column (
                modifier = Modifier.fillMaxHeight()
            ) {
                Greeting(
                    name = email,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}