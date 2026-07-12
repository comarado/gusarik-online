# 🃏 Гусарик Online

Полноценная Android-приложение для карточной игры "Гусарик" (разновидность преферанса на двоих).

## 🎯 Что это

- **Два реальных игрока** играют через интернет
- **Виртуальный третий игрок** (NPC) с детерминированным алгоритмом
- **Firebase** как backend (Auth + Firestore)
- **Material 3** дизайн с тёмной/светлой темой
- **Чистая архитектура** (Clean Architecture + MVVM)

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  Jetpack Compose + Material 3 + Navigation   │
├─────────────────────────────────────────────┤
│                Domain Layer                  │
│  Use Cases + Models + Repository Interfaces  │
├─────────────────────────────────────────────┤
│                 Data Layer                   │
│  Repository Impls + Firebase + Room          │
├─────────────────────────────────────────────┤
│               Game Engine                    │
│  Game Logic + Scoring + AI + Validation      │
└─────────────────────────────────────────────┘
```

## 📁 Структура проекта

```
gusarik-online/
├── app/                          # Application entry point
│   ├── src/main/kotlin/          # Application, Activity, DI, Navigation
│   └── build.gradle.kts
├── core/
│   ├── ui/                       # Theme, Design system, Common composables
│   ├── domain/                   # Models, Repository interfaces
│   └── data/                     # Firebase implementations
├── engine/
│   ├── game/                     # Core game logic (Deck, Bidding, Tricks)
│   ├── scoring/                  # Scoring strategies (Bullet, Mountain, etc.)
│   ├── ai/                       # Virtual player algorithm
│   └── validation/               # Move validation, anti-cheat
├── network/
│   ├── firebase/                 # Firebase services
│   └── realtime/                 # Real-time sync, reconnection
├── feature/
│   ├── auth/                     # Login, Registration
│   ├── menu/                     # Main menu
│   ├── lobby/                    # Room creation/joining
│   ├── game/                     # Game screen, board, cards
│   ├── history/                  # Match history
│   ├── stats/                    # Statistics
│   ├── settings/                 # App settings
│   └── chat/                     # In-game chat
└── firebase/                     # Security rules
```

## 🚀 Быстрый старт

### 1. Установка

Требования:
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17+
- Android SDK 34+
- Google Firebase аккаунт

### 2. Клонирование

```bash
git clone <repository-url>
cd gusarik-online
```

### 3. Настройка Firebase

1. Создайте проект в [Firebase Console](https://console.firebase.google.com/)
2. Включите **Authentication** → Google + Email/Password
3. Создайте **Firestore Database**
4. Скачайте `google-services.json` и поместите в `app/`
5. Примените правила безопасности из `firebase/firestore.rules`

### 4. Сборка

```bash
./gradlew assembleDebug
```

Или откройте проект в Android Studio и нажмите Run.

### 5. Запуск

- Установите APK на устройство/эмулятор
- Войдите через Google или Email
- Создайте комнату и отправьте код другу

## 🎮 Правила игры

### Карты
- Колода: 32 карты (7-Т, 4 масти)
- Старшинство: 7 < 8 < 9 < 10 < В < Д < К < Т

### Раздача
- Каждому игроку: 10 карт
- NPC: 10 карт
- Прикуп: 2 карты

### Торговля
- Участвуют только реальные игроки
- Лестница: 6♠ → 6♣ → 6♦ → 6♥ → 6БК → 7♠ → ... → 10БК → Мизер
- Пас всегда можно сказать

### Прикуп
- Победитель торговли видит 2 карты
- Добавляет к своей руке (12 карт)
- Сносит 2 карты (остаётся 10)

### Игра
- 10 взяток
- Обязан класть масть
- Козырь бьёт масть
- Победитель взятки ходит следующим

### NPC Алгоритм
1. Есть масть → младшая карта масти
2. Нет масти, есть козырь → младший козырь
3. Нет козыря → младшая карта

## 📊 Системы подсчёта

### Пуля (по умолчанию)
- Контракт сыгран: +10 × уровень
- Контракт не сыгран: -10 × уровень
- Мизер: ±100
- Распасы: -10 за взятку

### Гора
- Неисполненные контракты → в гору
- Исполненный контракт → забирает гору

### Висты (stub)
- Заготовка для будущей реализации

## 🔧 Модули

### engine/game
Ядро игры:
- `Deck` — колода 32 карты
- `Dealer` — раздача
- `BidManager` — торговля
- `TrickManager` — взятки
- `GameEngine` — оркестратор

### engine/scoring
Стратегии подсчёта:
- `ScoringStrategy` — интерфейс
- `BulletScoring` — пуля
- `MountainScoring` — гора
- `ScoringFactory` — фабрика

### engine/ai
Виртуальный игрок:
- `VirtualPlayer` — интерфейс
- `SimpleVirtualPlayer` — детерминированный алгоритм

### engine/validation
Валидация:
- `MoveValidator` — проверка ходов
- `ValidationResult` — результат проверки

## 🧪 Тесты

Запуск unit-тестов:

```bash
./gradlew :engine:game:test
```

Тесты покрывают:
- Раздачу карт
- Торговлю
- Определение победителя взятки
- Валидацию ходов
- Подсчёт очков
- NPC алгоритм

## 🔐 Безопасность

### Firestore Rules
- Пользователи видят только свои данные
- Комнаты доступны только участникам
- Ходы можно делать только от своего имени

### Валидация
- Все действия проверяются на сервере
- Нельзя сыграть чужую карту
- Нельзя ходить не в свою очередь
- Нельзя изменить счёт

## 📱 Поддержка

- Android 9+ (API 28+)
- Портретная ориентация
- Тёмная и светлая тема
- Русский язык интерфейса

## 🛠️ Технологии

| Технология | Версия | Назначение |
|-----------|--------|------------|
| Kotlin | 1.9.22 | Язык разработки |
| Jetpack Compose | 2024.02 | UI фреймворк |
| Material 3 | latest | Дизайн-система |
| Hilt | 2.50 | Dependency Injection |
| Firebase Auth | latest | Авторизация |
| Firestore | latest | База данных |
| Coroutines | 1.7.3 | Асинхронность |
| Navigation | 2.7.7 | Навигация |
| Room | 2.6.1 | Локальная БД |

## 📋 TODO

- [ ] Cloud Functions для серверной валидации
- [ ] Push уведомления
- [ ] Голосовой чат
- [ ] Рейтинговая система (ELO)
- [ ] Турниры
- [ ] Просмотр повторов
- [ ] Добавление друзей
- [ ] Игра против компьютера (полноценная)
- [ ] Системы подсчёта: Сочи, Ленинград, Ростов, Питер

## 📄 Лицензия

MIT License

---

**Приятной игры! 🃏**
