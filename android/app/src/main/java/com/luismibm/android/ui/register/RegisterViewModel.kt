package com.luismibm.android.ui.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.RegisterRequest
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun register(
        onRegisterSuccess: () -> Unit,
        onRegisterError: (String) -> Unit
    ) {
        if (_password.value != _confirmPassword.value) {
            onRegisterError("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                ApiClient.apiService.register(RegisterRequest(_email.value, _password.value))
                onRegisterSuccess()
            } catch (e: Exception) {
                onRegisterError("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}