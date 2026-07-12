package com.gusarik.feature.lobby.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gusarik.core.domain.model.Room
import com.gusarik.core.domain.repository.AuthRepository
import com.gusarik.core.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LobbyUiState(
    val isLoading: Boolean = false,
    val roomCode: String? = null,
    val joinCode: String = "",
    val room: Room? = null,
    val error: String? = null,
    val isWaiting: Boolean = false
)

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    fun createRoom() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.value = LobbyUiState(isLoading = true)

            gameRepository.createRoom(userId)
                .onSuccess { code ->
                    _uiState.value = LobbyUiState(roomCode = code, isWaiting = true)
                    // Start observing room
                    observeRoom(code)
                }
                .onFailure { e ->
                    _uiState.value = LobbyUiState(error = e.message)
                }
        }
    }

    fun joinRoom() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val code = _uiState.value.joinCode.uppercase()

            if (code.length != 6) {
                _uiState.value = _uiState.value.copy(error = "Код должен содержать 6 символов")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            gameRepository.joinRoom(code, userId)
                .onSuccess { room ->
                    _uiState.value = _uiState.value.copy(
                        room = room,
                        roomCode = code,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
        }
    }

    fun updateJoinCode(code: String) {
        _uiState.value = _uiState.value.copy(joinCode = code.uppercase())
    }

    private fun observeRoom(roomCode: String) {
        viewModelScope.launch {
            gameRepository.observeRoom(roomCode).collect { room ->
                if (room != null) {
                    _uiState.value = _uiState.value.copy(
                        room = room,
                        isWaiting = room.guestId == null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
