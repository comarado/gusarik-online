package com.gusarik.engine.scoring

import com.gusarik.core.domain.model.ScoringSystem
import com.gusarik.engine.scoring.strategies.BulletScoring
import com.gusarik.engine.scoring.strategies.MountainScoring
import com.gusarik.engine.scoring.strategies.WhistScoring

/**
 * Factory for creating scoring strategy instances.
 */
object ScoringFactory {

    fun create(system: ScoringSystem): ScoringStrategy = when (system) {
        ScoringSystem.BULLET -> BulletScoring()
        ScoringSystem.MOUNTAIN -> MountainScoring()
        ScoringSystem.WHIST -> WhistScoring()
        ScoringSystem.SOCHI -> BulletScoring() // TODO: Implement
        ScoringSystem.LENINGRAD -> BulletScoring() // TODO: Implement
        ScoringSystem.ROSTOV -> BulletScoring() // TODO: Implement
        ScoringSystem.PETERSBURG -> BulletScoring() // TODO: Implement
    }

    fun availableSystems(): List<ScoringSystem> = listOf(
        ScoringSystem.BULLET,
        ScoringSystem.MOUNTAIN,
        ScoringSystem.WHIST
    )
}
