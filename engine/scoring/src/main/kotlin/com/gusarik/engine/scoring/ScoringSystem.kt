package com.gusarik.engine.scoring

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Добавляем типы очков, чтобы BULLET (Пуля) стал доступен компилятору
enum class ScoreType {
    BULLET,   // Пуля
    HILL,     // Гора
    WHIST     // Висты
}

@Singleton
class ScoringSystem @Inject constructor() {

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _comboMultiplier = MutableStateFlow(1)
    val comboMultiplier: StateFlow<Int> = _comboMultiplier.asStateFlow()

    fun addPoints(basePoints: Int) {
        val pointsToAdd = basePoints * _comboMultiplier.value
        _currentScore.value += pointsToAdd
        
        if (_currentScore.value > _highScore.value) {
            _highScore.value = _currentScore.value
        }
    }

    fun incrementCombo() {
        _comboMultiplier.value += 1
    }

    fun resetCombo() {
        _comboMultiplier.value = 1
    }

    fun resetGame() {
        _currentScore.value = 0
        _comboMultiplier.value = 1
    }

    fun loadHighScore(savedHighScore: Int) {
        if (savedHighScore > _highScore.value) {
            _highScore.value = savedHighScore
        }
    }
}
