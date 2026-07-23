package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Cek user yang sedang login
    fun getCurrentUser() = firebaseAuth.currentUser

    // Register - Daftar akun baru
    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // Buat akun di Firebase Auth
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("User ID tidak ditemukan"))

            // Buat data user di Firestore
            val user = User(
                userId = userId,
                name = name,
                email = email,
                familyId = "",
                role = "member"
            )

            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            syncFcmToken(userId)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("User ID tidak ditemukan"))

            // Ambil data user dari Firestore
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Data user tidak ditemukan"))

            syncFcmToken(userId)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Simpan/update token FCM device saat ini ke dokumen user di Firestore.
    // Dipanggil setiap kali user berhasil login/register, dan juga dari
    // FCMService.onNewToken() saat Firebase merotasi token secara otomatis.
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            if (userId.isBlank() || token.isBlank()) return Result.success(Unit)
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Gagal update fcmToken", e)
            Result.failure(e)
        }
    }

    // Ambil token FCM device saat ini lalu simpan ke Firestore.
    // Kegagalan di sini tidak boleh menggagalkan proses login/register,
    // jadi errornya cukup di-log saja.
    private suspend fun syncFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            updateFcmToken(userId, token)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Gagal ambil/sync FCM token", e)
        }
    }

    // Login / Register dengan Google (via Credential Manager idToken)
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("User ID tidak ditemukan"))

            val userId = firebaseUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            val existingDoc = userDocRef.get().await()

            val user = if (existingDoc.exists()) {
                // User sudah pernah daftar sebelumnya
                existingDoc.toObject(User::class.java)
                    ?: return Result.failure(Exception("Data user tidak ditemukan"))
            } else {
                // User baru login via Google, buat data user di Firestore
                val newUser = User(
                    userId = userId,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    familyId = "",
                    role = "member"
                )
                userDocRef.set(newUser).await()
                newUser
            }

            syncFcmToken(userId)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kirim email reset password
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ambil data user dari Firestore berdasarkan userId
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = doc.toObject(User::class.java)
                ?: return Result.failure(Exception("User tidak ditemukan"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update nama user
    suspend fun updateUserName(userId: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("name", newName)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ganti password (butuh re-authenticate dulu untuk security)
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User tidak login"))

            val email = user.email
                ?: return Result.failure(Exception("Email tidak ditemukan"))

            // Re-authenticate dengan password lama
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)

            user.reauthenticate(credential).await()

            // Update password baru
            user.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout
    fun logout() {
        firebaseAuth.signOut()
    }
}