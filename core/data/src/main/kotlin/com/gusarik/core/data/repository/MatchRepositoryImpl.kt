package com.gusarik.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gusarik.core.domain.model.MatchResult
import com.gusarik.core.domain.repository.MatchRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MatchRepository {

    override suspend fun saveMatch(match: MatchResult): Result<Unit> {
        return try {
            firestore.collection("matches").document(match.gameId).set(match).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMatchHistory(userId: String): Result<List<MatchResult>> {
        return try {
            val snapshot = firestore.collection("matches")
                .whereEqualTo("player1Id", userId)
                .get().await()

            val snapshot2 = firestore.collection("matches")
                .whereEqualTo("player2Id", userId)
                .get().await()

            val matches = (snapshot.documents + snapshot2.documents)
                .mapNotNull { it.toObject(MatchResult::class.java) }
                .distinctBy { it.gameId }
                .sortedByDescending { it.finishedAt }

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMatchHistory(userId: String): Flow<List<MatchResult>> = callbackFlow {
        val listener = firestore.collection("matches")
            .orderBy("finishedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val matches = snapshot?.documents?.mapNotNull {
                    it.toObject(MatchResult::class.java)
                }?.filter {
                    it.player1Id == userId || it.player2Id == userId
                } ?: emptyList()
                trySend(matches)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getMatch(matchId: String): Result<MatchResult> {
        return try {
            val doc = firestore.collection("matches").document(matchId).get().await()
            val match = doc.toObject(MatchResult::class.java)
                ?: return Result.failure(Exception("Матч не найден"))
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
