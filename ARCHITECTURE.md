# Гусарик Online — Game Design Document & Software Architecture

## 1. Game Design Document (GDD)

### 1.1 Обзор

**Гусарик Online** — карточная игра, разновидность преферанса для двух реальных игроков.
Третий игрок — виртуальный (NPC), играющий по детерминированному алгоритму.

### 1.2 Карты

| Масть | Карты |
|-------|-------|
| ♠ Пики | 7, 8, 9, 10, В, Д, К, Т |
| ♣ Трефы | 7, 8, 9, 10, В, Д, К, Т |
| ♦ Бубны | 7, 8, 9, 10, В, Д, К, Т |
| ♥ Черви | 7, 8, 9, 10, В, Д, К, Т |

**Старшинство карт:** 7 < 8 < 9 < 10 < Валет < Дама < Король < Туз

**Старшинство мастей (для торговли):** ♠ < ♣ < ♦ < ♥ < БК

### 1.3 Раздача

Колода 32 карты раздаётся как в преферансе на троих:
- Игрок 1 (реальный): 10 карт
- Игрок 2 (реальный): 10 карт
- Игрок 3 (виртуальный): 10 карт
- Прикуп: 2 карты

### 1.4 Торговля

Участвуют только два реальных игрока.

**Лестница торговли:**
```
6♠ → 6♣ → 6♦ → 6♥ → 6БК → 7♠ → 7♣ → 7♦ → 7♥ → 7БК → ... → 10БК → Мизер
```

**Правила:**
- Первый ход в торговле определяется по старшинству карты (раздающий ходит первым или определяется жребием)
- Каждый игрок может повысить ставку или сказать "Пас"
- Игрок не может повысить свою же ставку
- Последний не спасовавший выигрывает торговлю
- Если оба спасовали → распасы

**Распасы:**
- Оба игрока играют "в минус"
- Каждый старается взять как можно меньше взяток
- Подсчёт очков по распасам

### 1.5 Прикуп

1. Победитель торговли видит 2 карты прикупа (открыто)
2. Добавляет их к своей руке (12 карт)
3. Сносит любые 2 карты (закрыто)
4. Возвращается к 10 картам

### 1.6 Контракт

Победитель торговли выбирает контракт:
- **Масти:** 6-10 с козырной мастью (♠, ♣, ♦, ♥)
- **БК (без козыря):** 6-10
- **Мизер:** взять 0 взяток

Контракт не может быть ниже выигранной ставки в торговле.

### 1.7 Виртуальный третий игрок (NPC)

**Алгоритм (детерминированный):**

```
При ходе:
  1. Определить ведущую масть (если не первый ход)
  2. Если есть карты ведущей масти:
     → Положить младшую карту этой масти
  3. Если нет карт ведущей масти:
     a. Если есть козыри → положить младший козырь
     b. Если нет козырей → положить младшую карту из всех
```

### 1.8 Правила взятки

1. Первый игрок кладёт любую карту
2. Второй обязан положить масть, если есть
3. Если масти нет — козырь, если есть
4. Если козыря нет — любую карту
5. NPC следует тем же правилам

**Определение победителя взятки:**
- Если есть козыри → старший козырь
- Если козырей нет → старшая карта ведущей масти
- Победитель начинает следующую взятку

### 1.9 Окончание сдачи

После 10 взяток:
- Подсчёт взяток каждого игрока
- Определение: сыграл/не сыграл контракт
- Начисление очков

### 1.10 Система очков

#### Пуля (по умолчанию)

| Исход | Очки |
|-------|------|
| Контракт сыгран | +10 × уровень (6=60, 7=70, ...) |
| Контракт не сыгран | -10 × уровень |
| Мизер сыгран | +100 |
| Мизер не сыгран | -100 |
| Распасы | -10 за каждую взятку |

#### Гора

Кумулятивная система: каждый несыгранный контракт добавляет очки к "горе".
Сыгранный контракт забирает очки из горы.

#### Висты

Классическая система вистов из преферанса.

### 1.11 Онлайн-комнаты

**Создание комнаты:**
1. Игрок нажимает "Создать комнату"
2. Генерируется 6-значный код
3. Код отправляется другу

**Подключение:**
1. Игрок вводит код
2. Проверка существования комнаты
3. Подключение к комнате
4. Начало игры

### 1.12 Чат

- Текстовые сообщения во время партии
- Быстрые фразы ("Хорошая игра!", "Думаю...")
- Хранение в Firestore (сессия)

### 1.13 Переподключение

- Firestore сохраняет полное состояние игры
- При потере соединения: авто-retry с exponential backoff
- При восстановлении: синхронизация состояния
- Таймаут ожидания: 60 секунд (ход), 120 секунд (отключение)

---

## 2. Software Architecture

### 2.1 Паттерны

| Паттерн | Применение |
|---------|------------|
| MVVM | UI → ViewModel → UseCase |
| Repository | Data access abstraction |
| StateFlow | Reactive state management |
| State Machine | Game state transitions |
| Strategy | Scoring systems |
| Observer | Real-time Firestore listeners |

### 2.2 Состояния игры (State Machine)

```
                    ┌──────────┐
                    │  LOBBY   │
                    └────┬─────┘
                         │ Оба игрока подключены
                         ▼
                    ┌──────────┐
              ┌────→│DEALING   │
              │     └────┬─────┘
              │          │ Карты розданы
              │          ▼
              │     ┌──────────┐
              │     │BIDDING   │ ← Торговля
              │     └────┬─────┘
              │          │
              │     ┌────┴────┐
              │     │         │
              │     ▼         ▼
              │ ┌────────┐ ┌─────────┐
              │ │PASSED  │ │ WON_BID │
              │ │(распасы)│ └────┬────┘
              │ └───┬────┘      │ Выбран контракт
              │     │           ▼
              │     │     ┌──────────┐
              │     │     │TALON     │ ← Прикуп + снос
              │     │     └────┬─────┘
              │     │          │
              │     ▼          ▼
              │ ┌────────────────────┐
              │ │    PLAYING         │ ← Разыгрышь
              │ │  (10 взяток)       │
              │ └────────┬───────────┘
              │          │
              │          ▼
              │     ┌──────────┐
              │     │SCORING   │ ← Подсчёт очков
              │     └────┬─────┘
              │          │
              │     ┌────┴────┐
              │     │         │
              │     ▼         ▼
              │ ┌────────┐ ┌─────────┐
              │ │FINISHED│ │NEXT_DEAL│
              │ └────────┘ └────┬────┘
              │                 │
              └─────────────────┘
```

### 2.3 Модели данных (Domain)

```kotlin
// === Карты ===
enum class Suit { SPADES, CLUBS, DIAMONDS, HEARTS, NO_TRUMP }
enum class Rank { SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
data class Card(val suit: Suit, val rank: Rank)

// === Игроки ===
enum class PlayerSeat { SOUTH, NORTH, EAST } // SOUTH=1, NORTH=2, EAST=NPC
data class Player(val id: String, val seat: PlayerSeat, val nickname: String)

// === Торговля ===
enum class BidType { PASS, SUIT_BID, NO_TRUMP, MISER }
data class Bid(val player: PlayerSeat, val type: BidType, val level: Int?, val suit: Suit?)

// === Контракт ===
data class Contract(
    val player: PlayerSeat,
    val level: Int,        // 6-10
    val suit: Suit?,       // null = БК
    val isMiser: Boolean
)

// === Взятка ===
data class TrickCard(val player: PlayerSeat, val card: Card)
data class Trick(
    val cards: List<TrickCard>,
    val winner: PlayerSeat?,
    val leadSuit: Suit?
)

// === Состояние игры ===
enum class GamePhase {
    LOBBY, DEALING, BIDDING, PASSED, WON_BID,
    TALON, PLAYING, SCORING, FINISHED, NEXT_DEAL
}

data class GameState(
    val phase: GamePhase,
    val gameId: String,
    val players: Map<PlayerSeat, Player>,
    val hands: Map<PlayerSeat, List<Card>>,     // Карты в руке
    val talon: List<Card>,                       // Прикуп (2 карты)
    val currentTrick: List<TrickCard>,           // Текущая взятка
    val completedTricks: List<Trick>,            // Завершённые взятки
    val trickCounts: Map<PlayerSeat, Int>,       // Взятки по игрокам
    val bids: List<Bid>,                         // История торговли
    val currentBidder: PlayerSeat?,              // Кто торгует сейчас
    val contract: Contract?,                     // Выбранный контракт
    val currentPlayer: PlayerSeat?,              // Чей ход
    val trumpSuit: Suit?,                        // Козырь
    val scores: Map<PlayerSeat, Int>,            // Очки
    val roundNumber: Int,                        // Номер сдачи
    val chatMessages: List<ChatMessage>,
    val turnDeadline: Long?,                     // Дедлайн хода (timestamp)
    val lastAction: GameAction?                  // Последнее действие
)

// === Действия ===
sealed class GameAction {
    data class PlaceBid(val bid: Bid) : GameAction()
    data class ChooseContract(val contract: Contract) : GameAction()
    data class DiscardTalon(val cards: List<Card>) : GameAction()
    data class PlayCard(val card: Card) : GameAction()
    data class SendChat(val message: String) : GameAction()
    object Reconnect : GameAction()
}
```

### 2.4 Firebase Firestore Schema

```
firestore/
├── users/{userId}
│   ├── nickname: string
│   ├── avatarUrl: string
│   ├── email: string
│   ├── stats: {
│   │   ├── gamesPlayed: number
│   │   ├── wins: number
│   │   ├── losses: number
│   │   ├── totalTricks: number
│   │   ├── favoriteContract: string
│   │   ├── avgGameDuration: number
│   │ }
│   ├── createdAt: timestamp
│   └── lastLoginAt: timestamp
│
├── rooms/{roomCode}
│   ├── hostId: string (userId)
│   ├── guestId: string (userId) | null
│   ├── status: "waiting" | "playing" | "finished"
│   ├── createdAt: timestamp
│   ├── gameState: map (полное состояние)
│   ├── moves: subcollection
│   │   └── {moveId}: {
│   │       ├── playerId: string
│   │       ├── action: string
│   │       ├── data: map
│   │       ├── timestamp: timestamp
│   │       ├── sequenceNumber: number
│   │   }
│   ├── chat: subcollection
│   │   └── {messageId}: {
│   │       ├── senderId: string
│   │       ├── text: string
│   │       ├── timestamp: timestamp
│   │   }
│   └── history: subcollection
│       └── {roundId}: {
│           ├── roundNumber: number
│           ├── contract: map
│           ├── trickCounts: map
│           ├── scores: map
│           ├── timestamp: timestamp
│       }
│
├── matches/{matchId}              # Сохранённые партии
│   ├── player1Id: string
│   ├── player2Id: string
│   ├── startedAt: timestamp
│   ├── finishedAt: timestamp
│   ├── rounds: array of maps
│   ├── finalScores: map
│   └── winnerId: string
│
└── leaderboard/{period}           # Рейтинг (будущее)
    ├── entries: map
    └── updatedAt: timestamp
```

### 2.5 Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Room access: host and guest only
    match /rooms/{roomCode} {
      allow read: if request.auth != null &&
        (resource.data.hostId == request.auth.uid ||
         resource.data.guestId == request.auth.uid);
      allow create: if request.auth != null;
      allow update: if request.auth != null &&
        (resource.data.hostId == request.auth.uid ||
         resource.data.guestId == request.auth.uid);

      match /moves/{moveId} {
        allow read: if request.auth != null;
        allow write: if request.auth != null &&
          request.resource.data.playerId == request.auth.uid;
      }

      match /chat/{messageId} {
        allow read: if request.auth != null;
        allow write: if request.auth != null &&
          request.resource.data.senderId == request.auth.uid;
      }
    }

    // Matches: readable by participants
    match /matches/{matchId} {
      allow read: if request.auth != null &&
        (resource.data.player1Id == request.auth.uid ||
         resource.data.player2Id == request.auth.uid);
      allow create: if request.auth != null;
    }
  }
}
```

### 2.6 Полный список классов

#### Module: app
```
com.gusarik.app/
├── GusarikApplication.kt          // Application class, DI init
├── MainActivity.kt                // Single Activity
└── di/
    ├── AppModule.kt               // Hilt app-level bindings
    ├── FirebaseModule.kt          // Firebase instances
    └── RepositoryModule.kt        // Repository bindings
```

#### Module: core/ui
```
com.gusarik.core.ui/
├── theme/
│   ├── Color.kt
│   ├── Type.kt
│   ├── Theme.kt                   // Material 3 light/dark
│   └── Shapes.kt
├── components/
│   ├── CardView.kt                // Playing card composable
│   ├── PlayerHand.kt              // Hand of cards
│   ├── TrickArea.kt               // Center table area
│   ├── ScoreBoard.kt              // Score display
│   ├── ChatBubble.kt
│   ├── LoadingOverlay.kt
│   ├── TimerView.kt
│   └── AvatarView.kt
└── extensions/
    ├── Modifier.kt
    └── Context.kt
```

#### Module: core/domain
```
com.gusarik.core.domain/
├── model/
│   ├── Card.kt
│   ├── Player.kt
│   ├── GameState.kt
│   ├── Bid.kt
│   ├── Contract.kt
│   ├── Trick.kt
│   ├── GameAction.kt
│   ├── ChatMessage.kt
│   ├── UserProfile.kt
│   └── MatchResult.kt
├── repository/
│   ├── AuthRepository.kt          // Interface
│   ├── GameRepository.kt          // Interface
│   ├── UserRepository.kt          // Interface
│   └── MatchRepository.kt         // Interface
└── usecase/
    ├── auth/
    │   ├── SignInWithGoogleUseCase.kt
    │   ├── SignInWithEmailUseCase.kt
    │   ├── SignUpWithEmailUseCase.kt
    │   └── SignOutUseCase.kt
    ├── game/
    │   ├── CreateRoomUseCase.kt
    │   ├── JoinRoomUseCase.kt
    │   ├── MakeBidUseCase.kt
    │   ├── ChooseContractUseCase.kt
    │   ├── DiscardTalonUseCase.kt
    │   ├── PlayCardUseCase.kt
    │   ├── ObserveGameUseCase.kt
    │   └── ReconnectUseCase.kt
    ├── user/
    │   ├── GetProfileUseCase.kt
    │   ├── UpdateProfileUseCase.kt
    │   └── GetStatsUseCase.kt
    └── match/
        ├── GetMatchHistoryUseCase.kt
        └── SaveMatchUseCase.kt
```

#### Module: core/data
```
com.gusarik.core.data/
├── local/
│   ├── GusarikDatabase.kt         // Room DB
│   ├── dao/
│   │   ├── UserDao.kt
│   │   └── MatchDao.kt
│   └── entity/
│       ├── UserEntity.kt
│       └── MatchEntity.kt
├── remote/
│   ├── FirebaseAuthSource.kt
│   ├── FirestoreGameSource.kt
│   ├── FirestoreUserSource.kt
│   └── FirestoreMatchSource.kt
├── repository/
│   ├── AuthRepositoryImpl.kt
│   ├── GameRepositoryImpl.kt
│   ├── UserRepositoryImpl.kt
│   └── MatchRepositoryImpl.kt
└── mapper/
    ├── CardMapper.kt              // Domain ↔ Firestore
    ├── GameStateMapper.kt
    └── UserMapper.kt
```

#### Module: engine/game
```
com.gusarik.engine.game/
├── GameEngine.kt                  // Main game logic orchestrator
├── state/
│   ├── GameStateMachine.kt        // State transitions
│   ├── GameStateValidator.kt      // Validates transitions
│   └── GameEvent.kt               // Events for state machine
├── dealing/
│   ├── Deck.kt                    // 32-card deck
│   ├── Dealer.kt                  // Shuffling & dealing
│   └── DealResult.kt
├── bidding/
│   ├── BidManager.kt              // Bidding logic
│   ├── BidValidator.kt            // Validates bids
│   └── BidLadder.kt               // Bid ordering
├── playing/
│   ├── TrickManager.kt            // Trick resolution
│   ├── TrickValidator.kt          // Card play validation
│   ├── CardValidator.kt           // "Must follow suit" logic
│   └── TrickResolver.kt           // Winner determination
├── talon/
│   ├── TalonManager.kt            // Talon reveal & discard
│   └── TalonValidator.kt
└── contract/
    ├── ContractManager.kt         // Contract selection
    └── ContractValidator.kt
```

#### Module: engine/scoring
```
com.gusarik.engine.scoring/
├── ScoringStrategy.kt             // Interface
├── models/
│   ├── RoundScore.kt
│   ├── GameScore.kt
│   └── PlayerScore.kt
├── strategies/
│   ├── BulletScoring.kt           // Пуля
│   ├── MountainScoring.kt         // Гора
│   ├── WhistScoring.kt            // Висты
│   ├── SochiScoring.kt            // Сочи (stub)
│   ├── LeningradScoring.kt        // Ленинград (stub)
│   ├── RostovScoring.kt           // Ростов (stub)
│   ├── PetersburgScoring.kt       // Питер (stub)
│   └── MisereScoring.kt           // Мизер
├── ScoringCalculator.kt           // Orchestrator
└── ScoringFactory.kt              // Factory for strategies
```

#### Module: engine/ai
```
com.gusarik.engine.ai/
├── VirtualPlayer.kt               // NPC interface
├── SimpleVirtualPlayer.kt         // Deterministic algorithm
├── CardSelector.kt                // Card selection logic
└── AiConstants.kt                 // AI parameters
```

#### Module: engine/validation
```
com.gusarik.engine.validation/
├── MoveValidator.kt               // Server-side validation
├── ActionValidator.kt             // Validates any GameAction
├── TurnValidator.kt               // Whose turn is it?
└── SecurityValidator.kt           // Anti-cheat checks
```

#### Module: feature/auth
```
com.gusarik.feature.auth/
├── ui/
│   ├── LoginScreen.kt
│   ├── LoginViewModel.kt
│   ├── RegisterScreen.kt
│   └── RegisterViewModel.kt
└── AuthNavGraph.kt
```

#### Module: feature/menu
```
com.gusarik.feature.menu/
├── ui/
│   ├── MainMenuScreen.kt
│   └── MainMenuViewModel.kt
└── MenuNavGraph.kt
```

#### Module: feature/lobby
```
com.gusarik.feature.lobby/
├── ui/
│   ├── CreateRoomScreen.kt
│   ├── JoinRoomScreen.kt
│   ├── LobbyScreen.kt
│   └── LobbyViewModel.kt
└── LobbyNavGraph.kt
```

#### Module: feature/game
```
com.gusarik.feature.game/
├── ui/
│   ├── GameScreen.kt              // Main game UI
│   ├── GameViewModel.kt
│   ├── board/
│   │   ├── BoardView.kt           // Full game board
│   │   ├── PlayerHandView.kt      // Bottom hand
│   │   ├── OpponentHandView.kt    // Top hand (face down)
│   │   ├── NpcHandView.kt         // Right side
│   │   ├── TrickAreaView.kt       // Center
│   │   ├── TrumpIndicator.kt
│   │   ├── TurnIndicator.kt
│   │   └── ContractDisplay.kt
│   ├── bidding/
│   │   ├── BiddingPanel.kt
│   │   ├── BidButton.kt
│   │   └── BidHistory.kt
│   ├── talon/
│   │   ├── TalonRevealView.kt
│   │   └── DiscardView.kt
│   ├── scoring/
│   │   ├── RoundResultScreen.kt
│   │   └── GameResultScreen.kt
│   └── animation/
│       ├── CardDealAnimation.kt
│       ├── CardPlayAnimation.kt
│       ├── TrickCollectAnimation.kt
│       └── AnimationConstants.kt
├── chat/
│   ├── ChatPanel.kt
│   └── ChatViewModel.kt
└── GameNavGraph.kt
```

#### Module: feature/history
```
com.gusarik.feature.history/
├── ui/
│   ├── HistoryScreen.kt
│   ├── HistoryDetailScreen.kt
│   └── HistoryViewModel.kt
└── HistoryNavGraph.kt
```

#### Module: feature/stats
```
com.gusarik.feature.stats/
├── ui/
│   ├── StatsScreen.kt
│   └── StatsViewModel.kt
└── StatsNavGraph.kt
```

#### Module: feature/settings
```
com.gusarik.feature.settings/
├── ui/
│   ├── SettingsScreen.kt
│   └── SettingsViewModel.kt
└── SettingsNavGraph.kt
```

#### Module: network/firebase
```
com.gusarik.network.firebase/
├── auth/
│   ├── FirebaseAuthService.kt
│   ├── GoogleSignInHelper.kt
│   └── EmailAuthService.kt
├── firestore/
│   ├── FirestoreService.kt
│   ├── FirestoreGameService.kt
│   ├── FirestoreUserService.kt
│   └── FirestoreMatchService.kt
├── functions/
│   ├── CloudFunctionService.kt   // Server-side validation
│   └── ValidateMoveFunction.kt
└── config/
    └── FirebaseConfig.kt
```

#### Module: network/realtime
```
com.gusarik.network.realtime/
├── GameSyncManager.kt             // Real-time game sync
├── ConnectionStateMonitor.kt      // Network state
├── ReconnectionManager.kt         // Auto-reconnect
├── OfflineQueue.kt                // Queue actions when offline
└── SyncState.kt                   // Sync status sealed class
```

### 2.7 Зависимости (build.gradle)

```toml
[versions]
kotlin = "1.9.22"
compose-bom = "2024.02.00"
hilt = "2.50"
firebase-bom = "32.7.2"
coroutines = "1.7.3"
navigation = "2.7.7"
room = "2.6.1"
lifecycle = "2.7.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui = { group = "androidx.compose.ui", name: "ui" }
compose-navigation = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-functions = { group = "com.google.firebase", name = "firebase-functions-ktx" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.1.0" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Lifecycle
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Google Sign-In
google-signin = { group = "com.google.android.gms", name = "play-services-auth", version = "20.7.0" }

# Testing
junit = { group = "junit", name = "junit", version = "4.13.2" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.9" }
turbine = { group = "app.cash.turbine", name = "turbine", version = "1.0.0" }
```

### 2.8 Навигация

```
NavHost
├── auth/
│   ├── login
│   └── register
├── menu/
│   └── main_menu
├── lobby/
│   ├── create_room
│   ├── join_room
│   └── waiting/{roomCode}
├── game/
│   └── game/{roomCode}
│       ├── board
│       ├── bidding
│       ├── talon
│       ├── result
│       └── chat
├── history/
│   ├── list
│   └── detail/{matchId}
├── stats/
│   └── statistics
└── settings/
    └── settings
```

---

## 3. Диаграмма потоков данных

```
┌─────────┐     Action      ┌───────────┐    Validate    ┌──────────┐
│  UI     │ ───────────────→│ ViewModel │ ─────────────→│ UseCase  │
│(Compose)│                  │           │                │          │
└────┬────┘                  └─────┬─────┘                └────┬─────┘
     │                             │                           │
     │ StateFlow                   │ StateFlow                 │
     │◄────────────────────────────┘                           │
     │                                                         │
     │                                                         ▼
     │                                                   ┌──────────┐
     │                                                   │Repository│
     │                                                   └────┬─────┘
     │                                                        │
     │                              ┌─────────────────────────┤
     │                              │                         │
     │                              ▼                         ▼
     │                        ┌──────────┐            ┌──────────────┐
     │                        │  Engine  │            │   Firebase   │
     │                        │  (local) │            │  (Firestore) │
     │                        └──────────┘            └──────┬───────┘
     │                                                       │
     │                              Real-time Listener       │
     │◄──────────────────────────────────────────────────────┘
     │
     ▼
  Render UI
```

---

## 4. Очередь реализации

### Phase 1: Core Engine (без UI)
1. Модели данных (domain)
2. Deck + Dealer
3. BidManager + BidLadder
4. TrickManager + CardValidator
5. VirtualPlayer (NPC)
6. ScoringStrategy (Bullet, Mountain)
7. GameStateMachine
8. Unit-тесты

### Phase 2: Firebase Backend
1. Firebase Auth (Email + Google)
2. Firestore schema
3. Security rules
4. Repository implementations
5. GameSyncManager
6. ReconnectionManager

### Phase 3: UI
1. Theme + Design system
2. Auth screens
3. Main menu
4. Lobby (create/join room)
5. Game screen (board, hands, bidding, talon)
6. Chat
7. Animations
8. History + Stats screens
9. Settings

### Phase 4: Integration & Polish
1. End-to-end testing
2. Edge cases (disconnect, timeout)
3. Performance optimization
4. Play Store preparation
