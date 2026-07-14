package com.gusarik.engine.scoring

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringSystem @Inject constructor() {

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _comboMultiplier = MutableStateFlow(1)
    val comboMultiplier: StateFlow<Int> = _comboMultiplier.asStateFlow()

    /**
     * Начисляет базовые очки за успешное действие с учетом текущего комбо.
     * @param basePoints базовое количество очков за действие.
     */
    fun addPoints(basePoints: Int) {
        val pointsToAdd = basePoints * _comboMultiplier.value
        _currentScore.value += pointsToAdd
        
        if (_currentScore.value > _highScore.value) {
            _highScore.value = _currentScore.value
        }
    }

    /**
     * Увеличивает множитель комбо (например, при серии быстрых/успешных ходов).
     */
    fun incrementCombo() {
        _comboMultiplier.value += 1
    }

    /**
     * Сбрасывает множитель комбо к единице (например, при ошибке или пропуске хода).
     */
    fun resetCombo() {
        _comboMultiplier.value = 1
    }

    /**
     * Сбрасывает текущую игру и комбо (например, при старте новой игры).
     */
    fun resetGame() {
        _currentScore.value = 0
        _comboMultiplier.value = 1
    }

    /**
     * Принудительно устанавливает новый рекорд (например, при загрузке из локального сохранения).
     */
    fun loadHighScore(savedHighScore: Int) {
        if (savedHighScore > _highScore.value) {
            _highScore.value = savedHighScore
        }
    }
}
