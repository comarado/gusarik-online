package com.gusarik.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gusarik.core.domain.model.UserProfile
import com.gusarik.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Check if already logged in
        if (authRepository.isAuthenticated()) {
            _uiState.value = LoginUiState(isLoggedIn = true)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authRepository.signInWithGoogle(idToken)
                .onSuccess { profile ->
                    _uiState.value = LoginUiState(isLoggedIn = true, userProfile = profile)
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState(error = e.message ?: "Ошибка входа")
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authRepository.signInWithEmail(email, password)
                .onSuccess { profile ->
                    _uiState.value = LoginUiState(isLoggedIn = true, userProfile = profile)
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState(error = e.message ?: "Ошибка входа")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
