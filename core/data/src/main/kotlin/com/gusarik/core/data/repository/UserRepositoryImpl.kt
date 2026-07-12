package com.gusarik.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gusarik.core.domain.model.UserProfile
import com.gusarik.core.domain.model.UserStats
import com.gusarik.core.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("Профиль не найден"))
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(profile.id).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStats(userId: String, stats: UserStats): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("stats", stats).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> {
        return try {
            val ref = storage.reference.child("avatars/$userId.jpg")
            ref.putBytes(imageBytes).await()
            val url = ref.downloadUrl.await().toString()
            firestore.collection("users").document(userId)
                .update("avatarUrl", url).await()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
