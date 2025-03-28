package com.luismibm.trackify

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.luismibm.trackify.ui.theme.TrackifyTheme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackifyTheme {
                // MainScreen()
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {

    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }

    val context= LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(color = Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create An Account",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Text(
                text = "Enter your data to create an account",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Mail",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                TextField(
                    value = emailValue,
                    onValueChange = { newValue ->
                        emailValue = newValue
                    },
                    placeholder = {
                        Text(
                            text = "example@mail.com",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Password",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                TextField(
                    value = passwordValue,
                    onValueChange = { newValue ->
                        passwordValue = newValue
                    },
                    placeholder = {
                        Text(
                            text = "Enter your password",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Button(
                onClick = {
                    authManager.signUp(emailValue, passwordValue)
                        .onEach { result ->
                            if (result is AuthResponse.Success) {
                                Log.d("AUTH", "Mail Success")
                            } else {
                                Log.e("AUTH", "Email Fail")
                            }
                        }
                        .launchIn(coroutineScope)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign Up",
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            TextButton(
                onClick = {}
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            append("Already have an account? ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) {
                            append("Log In")
                        }
                    }
                )
            }

        }



    }



}

sealed interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String?): AuthResponse
}

class AuthManager(
    private val context: Context
) {

    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
    }

    fun signUp(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signIn(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }

    }

}

@Composable @OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
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
                    name = "Trackify",
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