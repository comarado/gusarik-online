package com.gusarik.engine.game

import com.gusarik.core.domain.model.*
import com.gusarik.engine.ai.SimpleVirtualPlayer
import com.gusarik.engine.ai.VirtualPlayer
import com.gusarik.engine.game.bidding.BidManager
import com.gusarik.engine.game.dealing.DealResult
import com.gusarik.engine.game.dealing.Deck
import com.gusarik.engine.game.playing.TrickManager
import com.gusarik.engine.game.playing.TrickResolver
import com.gusarik.engine.scoring.RoundResult
import com.gusarik.engine.scoring.ScoringFactory
import com.gusarik.engine.scoring.ScoringStrategy
import com.gusarik.engine.validation.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Main game engine that orchestrates all game logic.
 * This is the single source of truth for game state.
 */
class GameEngine(
    private val scoringStrategy: ScoringStrategy = ScoringFactory.create(ScoringSystem.BULLET),
    private val virtualPlayer: VirtualPlayer = SimpleVirtualPlayer()
) {
    private val deck = Deck()
    private val bidManager = BidManager()
    private val trickManager = TrickManager()

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var hands = mutableMapOf<PlayerSeat, List<Card>>()
    private var talon = listOf<Card>()
    private var npcHand = listOf<Card>()

    // === Game Lifecycle ===

    /**
     * Initialize a new game with two real players.
     */
    fun initializeGame(gameId: String, player1: Player, player2: Player) {
        val players = mapOf(
            PlayerSeat.SOUTH to player1,
            PlayerSeat.NORTH to player2,
            PlayerSeat.EAST to Player.npc()
        )

        _state.value = GameState(
            gameId = gameId,
            phase = GamePhase.LOBBY,
            players = players,
            hands = mapOf(
                PlayerSeat.SOUTH to emptyList(),
                PlayerSeat.NORTH to emptyList(),
                PlayerSeat.EAST to emptyList()
            )
        )
    }

    /**
     * Start a new deal. Shuffle and deal cards.
     */
    fun deal(seed: Long? = null): DealResult {
        deck.shuffle(seed)
        val result = deck.deal()

        hands[PlayerSeat.SOUTH] = result.hand1
        hands[PlayerSeat.NORTH] = result.hand2
        hands[PlayerSeat.EAST] = result.hand3
        npcHand = result.hand3
        talon = result.talon

        updateState {
            copy(
                phase = GamePhase.DEALING,
                hands = hands.toMap(),
                talon = talon,
                currentBidder = null,
                contract = null,
                currentTrick = PartialTrick(emptyList()),
                completedTricks = emptyList(),
                trickCounts = mapOf(
                    PlayerSeat.SOUTH to 0,
                    PlayerSeat.NORTH to 0,
                    PlayerSeat.EAST to 0
                )
            )
        }

        return result
    }

    /**
     * Start the bidding phase.
     */
    fun startBidding(firstBidder: PlayerSeat = PlayerSeat.SOUTH) {
        bidManager.startAuction(firstBidder)

        updateState {
            copy(
                phase = GamePhase.BIDDING,
                currentBidder = firstBidder,
                bids = emptyList()
            )
        }
    }

    // === Bidding ===

    /**
     * Player places a bid.
     */
    fun placeBid(playerSeat: PlayerSeat, bid: Bid): ValidationResult {
        val state = _state.value
        if (state.phase != GamePhase.BIDDING) {
            return ValidationResult.Invalid("Не фаза торговли")
        }
        if (state.currentBidder != playerSeat) {
            return ValidationResult.Invalid("Не ваш ход")
        }

        val result = bidManager.placeBid(bid)
        if (result is ValidationResult.Invalid) return result

        updateState {
            copy(
                bids = bidManager.getBids(),
                currentBidder = if (bidManager.isComplete()) null else bidManager.getCurrentBidder()
            )
        }

        // Check if NPC should bid
        if (!bidManager.isComplete() && bidManager.getCurrentBidder() == PlayerSeat.EAST) {
            processNpcBid()
        }

        // Handle auction completion
        if (bidManager.isComplete()) {
            handleAuctionComplete()
        }

        return ValidationResult.Valid
    }

    /**
     * Process NPC's bid (always passes).
     */
    private fun processNpcBid() {
        val npcBid = virtualPlayer.selectBid(bidManager.getBids(), npcHand)
        bidManager.placeBid(npcBid)

        updateState {
            copy(
                bids = bidManager.getBids(),
                currentBidder = if (bidManager.isComplete()) null else bidManager.getCurrentBidder()
            )
        }

        if (bidManager.isComplete()) {
            handleAuctionComplete()
        }
    }

    /**
     * Handle auction completion.
     */
    private fun handleAuctionComplete() {
        if (bidManager.isPassedHand()) {
            // Both passed — распасы
            updateState { copy(phase = GamePhase.PASSED) }
            startPassedPlay()
        } else {
            // Someone won — they choose a contract
            updateState {
                copy(
                    phase = GamePhase.WON_BID,
                    currentBidder = bidManager.getWinner()
                )
            }
        }
    }

    // === Contract & Talon ===

    /**
     * Winner of the auction chooses a contract.
     */
    fun chooseContract(playerSeat: PlayerSeat, contract: Contract): ValidationResult {
        val state = _state.value
        if (state.phase != GamePhase.WON_BID) {
            return ValidationResult.Invalid("Не фаза выбора контракта")
        }
        if (state.currentBidder != playerSeat) {
            return ValidationResult.Invalid("Не ваш выбор")
        }

        val winningBid = bidManager.getWinningBid()
            ?: return ValidationResult.Invalid("Нет выигранной ставки")

        val validation = com.gusarik.engine.validation.MoveValidator.validateContract(contract, winningBid)
        if (validation is ValidationResult.Invalid) return validation

        updateState {
            copy(
                phase = GamePhase.TALON,
                contract = contract,
                trumpSuit = contract.trumpSuit
            )
        }

        return ValidationResult.Valid
    }

    /**
     * Reveal the talon to the contract winner.
     */
    fun revealTalon(): List<Card> {
        return talon
    }

    /**
     * Winner discards 2 cards after seeing the talon.
     */
    fun discardTalon(playerSeat: PlayerSeat, cardsToDiscard: List<Card>): ValidationResult {
        val state = _state.value
        if (state.phase != GamePhase.TALON) {
            return ValidationResult.Invalid("Не фаза прикупа")
        }
        if (state.currentBidder != playerSeat) {
            return ValidationResult.Invalid("Не ваш выбор")
        }

        val handWithTalon = hands[playerSeat]!! + talon
        val validation = com.gusarik.engine.validation.MoveValidator.validateDiscard(cardsToDiscard, handWithTalon)
        if (validation is ValidationResult.Invalid) return validation

        // Remove discarded cards and talon cards, add remaining talon cards
        val newHand = (hands[playerSeat]!! + talon).filter { it !in cardsToDiscard }
            .sortedWith(compareBy({ it.suit.ordinal }, { it.rank.value }))

        hands[playerSeat] = newHand

        updateState {
            copy(
                phase = GamePhase.PLAYING,
                hands = hands.toMap(),
                currentPlayer = state.currentBidder // Declarer leads
            )
        }

        // Start play
        trickManager.startTrick(state.currentBidder!!)

        return ValidationResult.Valid
    }

    // === Play ===

    /**
     * Player plays a card.
     */
    fun playCard(playerSeat: PlayerSeat, card: Card): ValidationResult {
        val state = _state.value
        if (state.phase != GamePhase.PLAYING) {
            return ValidationResult.Invalid("Не фаза игры")
        }
        if (state.currentPlayer != playerSeat) {
            return ValidationResult.Invalid("Не ваш ход")
        }

        val hand = hands[playerSeat] ?: return ValidationResult.Invalid("Нет карт")
        val result = trickManager.playCard(card, playerSeat, hand, state.trumpSuit, state.contract)
        if (result is ValidationResult.Invalid) return result

        // Remove card from hand
        hands[playerSeat] = hand.filter { it != card }

        updateState {
            copy(
                hands = hands.toMap(),
                currentTrick = trickManager.getCurrentTrick(),
                lastAction = GameAction.PlayCard(playerSeat.toString(), card)
            )
        }

        // Check if trick is complete
        if (trickManager.isTrickComplete()) {
            processTrickCompletion()
        } else {
            // Move to next player
            advanceToNextPlayer()
        }

        return ValidationResult.Valid
    }

    /**
     * Process trick completion.
     */
    private fun processTrickCompletion() {
        val state = _state.value
        val trick = trickManager.resolveTrick(state.trumpSuit) ?: return

        updateState {
            copy(
                completedTricks = trickManager.getCompletedTricks(),
                trickCounts = trickManager.getTrickCounts(),
                currentTrick = PartialTrick(emptyList()),
                currentPlayer = trick.winner
            )
        }

        // If NPC won, play NPC's card
        if (trick.winner == PlayerSeat.EAST) {
            processNpcTurn()
        }

        // Check if round is complete
        if (trickManager.isRoundComplete()) {
            processRoundComplete()
        }
    }

    /**
     * Process NPC's turn.
     */
    private fun processNpcTurn() {
        val state = _state.value
        val npcCards = hands[PlayerSeat.EAST] ?: return
        if (npcCards.isEmpty()) return

        val card = virtualPlayer.selectCard(npcCards, trickManager.getCurrentTrick(), state.trumpSuit)

        // Play the NPC card
        trickManager.playCard(card, PlayerSeat.EAST, npcCards, state.trumpSuit, state.contract)
        hands[PlayerSeat.EAST] = npcCards.filter { it != card }

        updateState {
            copy(
                hands = hands.toMap(),
                currentTrick = trickManager.getCurrentTrick(),
                lastAction = GameAction.PlayCard(PlayerSeat.EAST.toString(), card)
            )
        }

        // Check if trick is complete after NPC play
        if (trickManager.isTrickComplete()) {
            processTrickCompletion()
        } else {
            advanceToNextPlayer()
        }
    }

    /**
     * Advance to the next player in turn order.
     */
    private fun advanceToNextPlayer() {
        val state = _state.value
        val nextPlayer = state.currentPlayer?.next() ?: return

        // Skip NPC — it plays automatically
        if (nextPlayer == PlayerSeat.EAST) {
            processNpcTurn()
            return
        }

        updateState { copy(currentPlayer = nextPlayer) }
    }

    /**
     * Process round completion — calculate scores.
     */
private fun processRoundComplete() {
        val state = _state.value
        val trickCounts = trickManager.getTrickCounts()
        
        // Извлекаем контракт в локальную переменную для безопасного смарт-каста
        val currentContract = state.contract

        val roundResults = if (state.isMisere) {
            // Проверяем локальную переменную на null
            if (currentContract != null) {
                scoringStrategy.scoreMisere(
                    currentContract.player,
                    trickCounts[currentContract.player] ?: 0,
                    state.scores
                )
            } else {
                emptyList() // Фолбек на случай, если контракта почему-то нет
            }
        } else if (state.isPassed) {
            scoringStrategy.scorePassed(trickCounts, state.scores)
        } else {
            // Здесь передаем уже проверенный на null currentContract
            scoringStrategy.scoreRound(currentContract, trickCounts, state.scores)
        }

        // Update scores
        val newScores = state.scores.toMutableMap()
        for (result in roundResults) {
            newScores[result.seat] = (newScores[result.seat] ?: 0) + result.points
        }

        val roundScore = RoundScore(
            roundNumber = state.roundNumber,
            contract = state.contract,
            trickCounts = trickCounts,
            roundPoints = roundResults.associate { it.seat to it.points },
            cumulativeScores = newScores.toMap()
        )

        updateState {
            copy(
                phase = GamePhase.SCORING,
                scores = newScores,
                roundScores = roundScores + roundScore
            )
        }
    }

    /**
     * Start распасы (passed hand) play.
     */
    private fun startPassedPlay() {
        updateState {
            copy(
                phase = GamePhase.PLAYING,
                contract = null,
                trumpSuit = null,
                currentBidder = null,
                currentPlayer = PlayerSeat.SOUTH
            )
        }

        trickManager.startTrick(PlayerSeat.SOUTH)
    }

    /**
     * Move to the next round.
     */
    fun nextRound() {
        val state = _state.value
        trickManager.reset()

        updateState {
            copy(
                phase = GamePhase.NEXT_DEAL,
                roundNumber = roundNumber + 1,
                dealer = dealer.next(),
                currentTrick = PartialTrick(emptyList()),
                completedTricks = emptyList(),
                trickCounts = mapOf(
                    PlayerSeat.SOUTH to 0,
                    PlayerSeat.NORTH to 0,
                    PlayerSeat.EAST to 0
                ),
                contract = null,
                trumpSuit = null,
                currentPlayer = null,
                currentBidder = null,
                bids = emptyList()
            )
        }
    }

    /**
     * Get the current game state.
     */
    fun getState(): GameState = _state.value

    /**
     * Set state from external source (reconnection).
     */
    fun setState(state: GameState) {
        _state.value = state
    }

    // === Helpers ===

    private fun updateState(transform: GameState.() -> GameState) {
        _state.value = _state.value.transform()
    }

    private fun createInitialState(): GameState = GameState(
        gameId = "",
        phase = GamePhase.LOBBY,
        players = emptyMap(),
        hands = emptyMap()
    )
}
