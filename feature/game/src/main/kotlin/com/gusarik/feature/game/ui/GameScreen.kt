package com.gusarik.feature.game.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gusarik.core.domain.model.*
import com.gusarik.core.ui.theme.*

@Composable
fun GameScreen(
    roomCode: String,
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomCode) {
        viewModel.initializeGame(roomCode)
    }

    val gameState = uiState.gameState

    Box(modifier = Modifier.fillMaxSize()) {
        if (gameState == null || uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar: opponent's hand
                OpponentHandView(
                    cardCount = gameState.handOf(PlayerSeat.NORTH).size,
                    modifier = Modifier.weight(0.1f)
                )

                // Middle area: game board
                Row(modifier = Modifier.weight(0.6f)) {
                    // NPC hand (right side)
                    NpcHandView(
                        cardCount = gameState.handOf(PlayerSeat.EAST).size,
                        modifier = Modifier.weight(0.15f)
                    )

                    // Center: trick area + info
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Game info
                        GameInfoBar(gameState)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Trick area
                        TrickAreaView(
                            currentTrick = gameState.currentTrick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Trick counts
                        TrickCountsView(gameState.trickCounts)
                    }

                    // Spacer for symmetry
                    Spacer(modifier = Modifier.weight(0.15f))
                }

                // Bottom: player's hand + controls
                Column(modifier = Modifier.weight(0.3f)) {
                    // Phase-specific controls
                    when (gameState.phase) {
                        GamePhase.BIDDING -> BiddingPanel(
                            currentBidder = gameState.currentBidder,
                            mySeat = uiState.mySeat,
                            bids = gameState.bids,
                            onBid = { viewModel.placeBid(it) }
                        )
                        GamePhase.WON_BID -> ContractSelectionPanel(
                            winningBid = gameState.bids.lastOrNull { !it.isPass },
                            mySeat = uiState.mySeat,
                            currentBidder = gameState.currentBidder,
                            onChooseContract = { viewModel.chooseContract(it) }
                        )
                        GamePhase.TALON -> TalonPanel(
                            showTalon = uiState.showTalon,
                            talonCards = gameState.talon,
                            selectedDiscards = uiState.selectedDiscards,
                            onShowTalon = { viewModel.showTalon() },
                            onDiscard = { viewModel.discardTalon() }
                        )
                        else -> {}
                    }

                    // Player's hand
                    PlayerHandView(
                        cards = gameState.handOf(uiState.mySeat),
                        selectedCard = uiState.selectedCard,
                        selectedDiscards = uiState.selectedDiscards,
                        isMyTurn = gameState.currentPlayer == uiState.mySeat,
                        onCardClick = { viewModel.selectCard(it) }
                    )
                }
            }

            // Chat button
            IconButton(
                onClick = { viewModel.toggleChat() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = "Чат")
            }

            // Chat panel
            AnimatedVisibility(
                visible = uiState.showChat,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                ChatPanel(
                    messages = uiState.chatMessages,
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onClose = { viewModel.toggleChat() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun GameInfoBar(gameState: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Contract
        gameState.contract?.let {
            Text(
                text = "Контракт: ${it.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Trump
        gameState.trumpSuit?.let {
            Text(
                text = "Козырь: ${it.symbol}",
                style = MaterialTheme.typography.titleMedium,
                color = if (it == Suit.HEARTS || it == Suit.DIAMONDS) CardRed else CardBlack
            )
        }

        // Scores
        Text(
            text = "Счёт: ${gameState.scores[PlayerSeat.SOUTH] ?: 0}",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun TrickCountsView(trickCounts: Map<PlayerSeat, Int>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("Вы: ${trickCounts[PlayerSeat.SOUTH] ?: 0}")
        Text("Соперник: ${trickCounts[PlayerSeat.NORTH] ?: 0}")
        Text("NPC: ${trickCounts[PlayerSeat.EAST] ?: 0}")
    }
}

@Composable
private fun OpponentHandView(cardCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(cardCount) {
            Box(
                modifier = Modifier
                    .size(30.dp, 45.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CardBack)
            )
        }
    }
}

@Composable
private fun NpcHandView(cardCount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        repeat(cardCount) {
            Box(
                modifier = Modifier
                    .size(25.dp, 38.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CardBack)
            )
        }
    }
}

@Composable
private fun TrickAreaView(currentTrick: PartialTrick, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TableGreen.copy(alpha = 0.3f))
            .border(2.dp, TableGreen, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            currentTrick.cards.forEach { trickCard ->
                PlayingCardView(
                    card = trickCard.card,
                    isFaceUp = true,
                    size = CardSize.MEDIUM
                )
            }
        }
    }
}

@Composable
private fun PlayerHandView(
    cards: List<Card>,
    selectedCard: Card?,
    selectedDiscards: Set<Card>,
    isMyTurn: Boolean,
    onCardClick: (Card) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(cards) { card ->
            val isSelected = card == selectedCard || card in selectedDiscards
            PlayingCardView(
                card = card,
                isFaceUp = true,
                size = CardSize.LARGE,
                isSelected = isSelected,
                isPlayable = isMyTurn,
                onClick = { onCardClick(card) }
            )
        }
    }
}

@Composable
private fun BiddingPanel(
    currentBidder: PlayerSeat?,
    mySeat: PlayerSeat,
    bids: List<Bid>,
    onBid: (Bid) -> Unit
) {
    val isMyTurn = currentBidder == mySeat

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isMyTurn) "Ваш ход в торговле" else "Ожидание...",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bid history
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            bids.takeLast(4).forEach { bid ->
                Text(
                    text = bid.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isMyTurn) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pass button
                OutlinedButton(onClick = { onBid(Bid.pass(mySeat)) }) {
                    Text("Пас")
                }

                // Quick bid buttons
                val ladder = BidLadder.fullLadder()
                val highestBid = bids.filter { !it.isPass }.maxByOrNull { it.numericValue }
                val nextBids = if (highestBid != null) {
                    BidLadder.bidsAbove(highestBid).take(3)
                } else {
                    ladder.take(3)
                }

                nextBids.forEach { bid ->
                    Button(
                        onClick = { onBid(Bid.suitBid(mySeat, bid.level!!, bid.suit!!)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(bid.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractSelectionPanel(
    winningBid: Bid?,
    mySeat: PlayerSeat,
    currentBidder: PlayerSeat?,
    onChooseContract: (Contract) -> Unit
) {
    val isMyChoice = currentBidder == mySeat

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isMyChoice) {
            Text("Выберите контракт:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Miser button
                OutlinedButton(onClick = { onChooseContract(Contract.miser(mySeat)) }) {
                    Text("Мизер")
                }

                // Suit buttons
                Suit.entries.filter { it != Suit.entries.first() || winningBid?.type == BidType.SUIT_BID }.forEach { suit ->
                    val level = winningBid?.level ?: 6
                    Button(
                        onClick = { onChooseContract(Contract(mySeat, level, suit)) }
                    ) {
                        Text("$level${suit.symbol}")
                    }
                }
            }
        } else {
            Text("Ожидание выбора контракта...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TalonPanel(
    showTalon: Boolean,
    talonCards: List<Card>,
    selectedDiscards: Set<Card>,
    onShowTalon: () -> Unit,
    onDiscard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showTalon) {
            Button(onClick = onShowTalon) {
                Text("Открыть прикуп")
            }
        } else {
            Text("Прикуп:", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                talonCards.forEach { card ->
                    PlayingCardView(card = card, isFaceUp = true, size = CardSize.MEDIUM)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Выберите 2 карты для сноса", style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = onDiscard,
                enabled = selectedDiscards.size == 2
            ) {
                Text("Сбросить (${selectedDiscards.size}/2)")
            }
        }
    }
}

@Composable
private fun ChatPanel(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Чат", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages
        Column(modifier = Modifier.weight(1f)) {
            messages.forEach { msg ->
                Text(
                    text = "${msg.senderNickname}: ${msg.text}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                }
            ) {
                Text("→")
            }
        }
    }
}

// === Card View Components ===

enum class CardSize { SMALL, MEDIUM, LARGE }

@Composable
fun PlayingCardView(
    card: Card,
    isFaceUp: Boolean,
    size: CardSize = CardSize.MEDIUM,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val (width, height) = when (size) {
        CardSize.SMALL -> Pair(35.dp, 50.dp)
        CardSize.MEDIUM -> Pair(50.dp, 70.dp)
        CardSize.LARGE -> Pair(60.dp, 85.dp)
    }

    val cardColor = if (isFaceUp) {
        if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) CardRed else CardBlack
    } else {
        CardBack
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPlayable -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(width, height)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFaceUp) Color.White else CardBack)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .then(if (onClick != null && isPlayable) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.symbol,
                    fontSize = when (size) {
                        CardSize.SMALL -> 10.sp
                        CardSize.MEDIUM -> 14.sp
                        CardSize.LARGE -> 18.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = when (size) {
                        CardSize.SMALL -> 12.sp
                        CardSize.MEDIUM -> 16.sp
                        CardSize.LARGE -> 20.sp
                    },
                    color = cardColor
                )
            }
        }
    }
}
