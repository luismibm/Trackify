package com.luismibm.android.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.AuthRequest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login(
        onLoginSuccess: (String) -> Unit,
        onLoginError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.login(AuthRequest(_email.value, _password.value))
                onLoginSuccess(response.accessToken)
            } catch (e: Exception) {
                onLoginError("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

}