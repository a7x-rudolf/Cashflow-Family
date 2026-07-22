package com.app.cashflowfamily.data.repository

import com.app.cashflowfamily.data.model.Family
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Generate kode family random (6 karakter huruf+angka)
    private fun generateFamilyCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // Buat family baru
    suspend fun createFamily(
        familyName: String,
        ownerId: String
    ): Result<Family> {
        return try {
            // Generate kode unik
            var familyCode: String
            var isCodeUnique = false

            do {
                familyCode = generateFamilyCode()
                // Cek apakah kode sudah digunakan
                val existing = firestore.collection("families")
                    .whereEqualTo("familyCode", familyCode)
                    .get()
                    .await()
                isCodeUnique = existing.isEmpty
            } while (!isCodeUnique)

            // Buat document baru
            val familyRef = firestore.collection("families").document()

            val family = Family(
                familyId = familyRef.id,
                familyName = familyName,
                familyCode = familyCode,
                ownerId = ownerId,
                members = listOf(ownerId),
                currency = "IDR"
            )

            familyRef.set(family).await()

            // Update user: set familyId dan role = admin
            firestore.collection("users")
                .document(ownerId)
                .update(
                    mapOf(
                        "familyId" to familyRef.id,
                        "role" to "admin"
                    )
                )
                .await()

            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Gabung family menggunakan kode
    suspend fun joinFamily(
        familyCode: String,
        userId: String
    ): Result<Family> {
        return try {
            // Cari family berdasarkan kode
            val querySnapshot = firestore.collection("families")
                .whereEqualTo("familyCode", familyCode.uppercase())
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Kode keluarga tidak ditemukan"))
            }

            val familyDoc = querySnapshot.documents.first()
            val family = familyDoc.toObject(Family::class.java)
                ?: return Result.failure(Exception("Data keluarga tidak valid"))

            // Cek apakah user sudah jadi member
            if (family.members.contains(userId)) {
                return Result.failure(Exception("Anda sudah menjadi anggota keluarga ini"))
            }

            // Tambah user ke members
            firestore.collection("families")
                .document(family.familyId)
                .update("members", FieldValue.arrayUnion(userId))
                .await()

            // Update user: set familyId dan role = member
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "familyId" to family.familyId,
                        "role" to "member"
                    )
                )
                .await()

            Result.success(family.copy(members = family.members + userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ambil data family berdasarkan ID
    suspend fun getFamilyById(familyId: String): Result<Family> {
        return try {
            val doc = firestore.collection("families")
                .document(familyId)
                .get()
                .await()

            val family = doc.toObject(Family::class.java)
                ?: return Result.failure(Exception("Family tidak ditemukan"))

            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ambil semua data anggota keluarga (return List<User>)
    suspend fun getFamilyMembers(memberIds: List<String>): Result<List<com.app.cashflowfamily.data.model.User>> {
        return try {
            if (memberIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val members = mutableListOf<com.app.cashflowfamily.data.model.User>()

            memberIds.forEach { userId ->
                val doc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                doc.toObject(com.app.cashflowfamily.data.model.User::class.java)?.let {
                    members.add(it)
                }
            }

            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Keluar dari keluarga (member biasa)
    suspend fun leaveFamily(familyId: String, userId: String): Result<Unit> {
        return try {
            // Hapus user dari members array di family
            firestore.collection("families")
                .document(familyId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()

            // Reset familyId di user document
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "familyId" to "",
                        "role" to "member"
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kick member (hanya admin)
    suspend fun kickMember(familyId: String, memberUserId: String): Result<Unit> {
        return try {
            // Hapus dari members array
            firestore.collection("families")
                .document(familyId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(memberUserId))
                .await()

            // Reset familyId member yang di-kick
            firestore.collection("users")
                .document(memberUserId)
                .update(
                    mapOf(
                        "familyId" to "",
                        "role" to "member"
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Promote member jadi admin
    suspend fun promoteToAdmin(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("role", "admin")
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}