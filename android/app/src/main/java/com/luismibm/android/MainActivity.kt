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
import com.luismibm.android.ui.LoginScreen
import com.luismibm.android.ui.HomeScreen
import com.luismibm.android.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var isLoggedIn by remember { mutableStateOf(false) }
                    var token by remember { mutableStateOf("") }
                    
                    if (isLoggedIn) {
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = { accessToken ->
                                token = accessToken
                                isLoggedIn = true
                            },
                            onLoginError = { message ->
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }

                }
            }
        }
    }
}
