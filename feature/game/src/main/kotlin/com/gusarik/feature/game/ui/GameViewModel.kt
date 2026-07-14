package com.gusarik.feature.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gusarik.core.domain.model.*
import com.gusarik.core.domain.repository.AuthRepository
import com.gusarik.core.domain.repository.GameRepository
import com.gusarik.engine.game.GameEngine
import com.gusarik.engine.scoring.ScoringFactory
import com.gusarik.engine.scoring.ScoringSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gusarik.engine.scoring.ScoreType
import com.gusarik.engine.scoring.ScoreType.BULLET

data class GameUiState(
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mySeat: PlayerSeat = PlayerSeat.SOUTH,
    val selectedCard: Card? = null,
    val talonCards: List<Card> = emptyList(),
    val showTalon: Boolean = false,
    val selectedDiscards: Set<Card> = emptySet(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val showChat: Boolean = false,
    val showResult: Boolean = false,
    val roundResults: List<String> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val gameEngine = GameEngine(ScoringSystem(ScoreType.BULLET))
    private var roomCode: String = ""

    fun initializeGame(roomCode: String) {
        this.roomCode = roomCode
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Observe game state
            gameRepository.observeGameState(roomCode).collect { state ->
                if (state != null) {
                    _uiState.value = _uiState.value.copy(
                        gameState = state,
                        isLoading = false
                    )
                }
            }
        }

        // Observe chat
        viewModelScope.launch {
            gameRepository.observeChat(roomCode).collect { messages ->
                _uiState.value = _uiState.value.copy(chatMessages = messages)
            }
        }
    }

    fun selectCard(card: Card) {
        val state = _uiState.value
        val gameState = state.gameState ?: return

        if (gameState.phase == GamePhase.PLAYING && gameState.currentPlayer == state.mySeat) {
            // Play the card
            playCard(card)
        } else if (gameState.phase == GamePhase.TALON) {
            // Toggle discard selection
            val selected = state.selectedDiscards.toMutableSet()
            if (card in selected) {
                selected.remove(card)
            } else if (selected.size < 2) {
                selected.add(card)
            }
            _uiState.value = state.copy(selectedDiscards = selected)
        }
    }

    private fun playCard(card: Card) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val action = GameAction.PlayCard(userId, card)
            gameRepository.submitAction(roomCode, action)
        }
    }

    fun placeBid(bid: Bid) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val action = GameAction.PlaceBid(userId, bid)
            gameRepository.submitAction(roomCode, action)
        }
    }

    fun chooseContract(contract: Contract) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val action = GameAction.ChooseContract(userId, contract)
            gameRepository.submitAction(roomCode, action)
        }
    }

    fun discardTalon() {
        val userId = authRepository.getCurrentUserId() ?: return
        val cards = _uiState.value.selectedDiscards.toList()
        if (cards.size != 2) return

        viewModelScope.launch {
            val action = GameAction.DiscardTalon(userId, cards)
            gameRepository.submitAction(roomCode, action)
            _uiState.value = _uiState.value.copy(
                selectedDiscards = emptySet(),
                showTalon = false
            )
        }
    }

    fun sendChatMessage(message: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        val nickname = "Игрок" // Would get from profile
        viewModelScope.launch {
            val chatMsg = ChatMessage(
                id = System.currentTimeMillis().toString(),
                senderId = userId,
                senderNickname = nickname,
                text = message
            )
            gameRepository.sendChatMessage(roomCode, chatMsg)
        }
    }

    fun toggleChat() {
        _uiState.value = _uiState.value.copy(showChat = !_uiState.value.showChat)
    }

    fun showTalon() {
        _uiState.value = _uiState.value.copy(showTalon = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun leaveGame() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            gameRepository.leaveRoom(roomCode, userId)
        }
    }
}
