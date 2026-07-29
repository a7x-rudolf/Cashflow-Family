package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.Event
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventTransaction
import com.app.cashflowfamily.data.model.EventTransfer
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "EventRepository"
        private const val COL_EVENTS = "events"
        private const val COL_CATEGORIES = "categories"
        private const val COL_TRANSACTIONS = "transactions"
        private const val COL_TRANSFERS = "transfers"
    }

    /**
     * Helper: hitung tier threshold berdasarkan percentage saat ini.
     * 100 = over budget, 80 = warning, 0 = normal.
     */
    private fun computeTier(spent: Double, effectiveBudget: Double): Int {
        if (effectiveBudget <= 0) return 0
        val pct = (spent / effectiveBudget * 100)
        return when {
            pct >= 100.0 -> 100
            pct >= 80.0 -> 80
            else -> 0
        }
    }

    /**
     * Helper: safe subtract dengan log warning jika hasil < 0.
     * Fix bug 1.8 — tidak diam-diam coerce.
     */
    private fun safeSubtract(current: Double, delta: Double, context: String): Double {
        val result = current - delta
        if (result < 0.0) {
            Log.w(TAG, "safeSubtract underflow in $context: current=$current, delta=$delta, result=$result → floored to 0")
            return 0.0
        }
        return result
    }

    // ============================================================
    // EVENTS
    // ============================================================

    suspend fun getEvents(familyId: String): Result<List<Event>> {
        return try {
            val snapshot = firestore.collection(COL_EVENTS)
                .whereEqualTo("familyId", familyId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(eventId = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "getEvents error", e)
            Result.failure(e)
        }
    }

    suspend fun getEventById(eventId: String): Result<Event> {
        return try {
            val doc = firestore.collection(COL_EVENTS)
                .document(eventId)
                .get()
                .await()

            val event = doc.toObject(Event::class.java)?.copy(eventId = doc.id)
                ?: return Result.failure(Exception("Event tidak ditemukan"))

            Result.success(event)
        } catch (e: Exception) {
            Log.e(TAG, "getEventById error", e)
            Result.failure(e)
        }
    }

    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val ref = firestore.collection(COL_EVENTS).document()
            val newEvent = event.copy(
                eventId = ref.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            ref.set(newEvent).await()
            Result.success(newEvent)
        } catch (e: Exception) {
            Log.e(TAG, "createEvent error", e)
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            val existing = firestore.collection(COL_EVENTS)
                .document(event.eventId).get().await()
                .toObject(Event::class.java)
                ?: return Result.failure(Exception("Event tidak ditemukan"))

            val updated = event.copy(
                createdAt = existing.createdAt,
                createdBy = existing.createdBy,
                familyId = existing.familyId,
                spentAmount = existing.spentAmount,
                lastNotifiedPercentage = existing.lastNotifiedPercentage, // preserve
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(COL_EVENTS)
                .document(event.eventId)
                .set(updated)
                .await()

            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "updateEvent error", e)
            Result.failure(e)
        }
    }

    suspend fun updateEventStatus(eventId: String, status: String): Result<Unit> {
        return try {
            firestore.collection(COL_EVENTS)
                .document(eventId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateEventStatus error", e)
            Result.failure(e)
        }
    }

    /**
     * Update tier notifikasi terakhir untuk event.
     * Dipanggil oleh EventThresholdNotifier setelah kirim notif.
     */
    suspend fun updateEventNotifiedPercentage(eventId: String, tier: Int): Result<Unit> {
        return try {
            firestore.collection(COL_EVENTS)
                .document(eventId)
                .update("lastNotifiedPercentage", tier)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateEventNotifiedPercentage error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val categoriesSnap = firestore.collection(COL_EVENTS)
                .document(eventId).collection(COL_CATEGORIES).get().await()
            val transactionsSnap = firestore.collection(COL_EVENTS)
                .document(eventId).collection(COL_TRANSACTIONS).get().await()
            val transfersSnap = firestore.collection(COL_EVENTS)
                .document(eventId).collection(COL_TRANSFERS).get().await()

            val allDocs = mutableListOf<com.google.firebase.firestore.DocumentReference>()
            categoriesSnap.documents.forEach { allDocs.add(it.reference) }
            transactionsSnap.documents.forEach { allDocs.add(it.reference) }
            transfersSnap.documents.forEach { allDocs.add(it.reference) }
            allDocs.add(firestore.collection(COL_EVENTS).document(eventId))

            allDocs.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { batch.delete(it) }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteEvent error", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // CATEGORIES
    // ============================================================

    suspend fun getCategories(eventId: String): Result<List<EventCategory>> {
        return try {
            val snapshot = firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_CATEGORIES)
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EventCategory::class.java)?.copy(categoryId = doc.id)
            }
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "getCategories error", e)
            Result.failure(e)
        }
    }

    suspend fun getCategoryById(eventId: String, categoryId: String): Result<EventCategory> {
        return try {
            val doc = firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_CATEGORIES)
                .document(categoryId)
                .get()
                .await()

            val category = doc.toObject(EventCategory::class.java)?.copy(categoryId = doc.id)
                ?: return Result.failure(Exception("Kategori tidak ditemukan"))

            Result.success(category)
        } catch (e: Exception) {
            Log.e(TAG, "getCategoryById error", e)
            Result.failure(e)
        }
    }

    suspend fun addCategory(category: EventCategory): Result<EventCategory> {
        return try {
            val ref = firestore.collection(COL_EVENTS)
                .document(category.eventId)
                .collection(COL_CATEGORIES)
                .document()

            val newCategory = category.copy(
                categoryId = ref.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            ref.set(newCategory).await()
            Result.success(newCategory)
        } catch (e: Exception) {
            Log.e(TAG, "addCategory error", e)
            Result.failure(e)
        }
    }

    suspend fun addCategoriesBatch(
        eventId: String,
        categories: List<EventCategory>
    ): Result<Unit> {
        return try {
            categories.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { category ->
                    val ref = firestore.collection(COL_EVENTS)
                        .document(eventId)
                        .collection(COL_CATEGORIES)
                        .document()

                    val newCat = category.copy(
                        categoryId = ref.id,
                        eventId = eventId,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    batch.set(ref, newCat)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "addCategoriesBatch error", e)
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: EventCategory): Result<EventCategory> {
        return try {
            val existing = firestore.collection(COL_EVENTS)
                .document(category.eventId)
                .collection(COL_CATEGORIES)
                .document(category.categoryId)
                .get().await()
                .toObject(EventCategory::class.java)
                ?: return Result.failure(Exception("Kategori tidak ditemukan"))

            val updated = category.copy(
                createdAt = existing.createdAt,
                spentAmount = existing.spentAmount,
                transferredIn = existing.transferredIn,
                transferredOut = existing.transferredOut,
                lastNotifiedPercentage = existing.lastNotifiedPercentage, // preserve
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(COL_EVENTS)
                .document(category.eventId)
                .collection(COL_CATEGORIES)
                .document(category.categoryId)
                .set(updated)
                .await()
            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "updateCategory error", e)
            Result.failure(e)
        }
    }

    /**
     * Update tier notifikasi terakhir untuk kategori.
     * Dipanggil oleh EventThresholdNotifier setelah kirim notif.
     */
    suspend fun updateCategoryNotifiedPercentage(
        eventId: String,
        categoryId: String,
        tier: Int
    ): Result<Unit> {
        return try {
            firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_CATEGORIES)
                .document(categoryId)
                .update("lastNotifiedPercentage", tier)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateCategoryNotifiedPercentage error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(eventId: String, categoryId: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection(COL_EVENTS).document(eventId)
            val catRef = eventRef.collection(COL_CATEGORIES).document(categoryId)

            val txSnap = eventRef.collection(COL_TRANSACTIONS)
                .whereEqualTo("categoryId", categoryId).get().await()

            val transfersInSnap = eventRef.collection(COL_TRANSFERS)
                .whereEqualTo("toCategoryId", categoryId).get().await()
            val transfersOutSnap = eventRef.collection(COL_TRANSFERS)
                .whereEqualTo("fromCategoryId", categoryId).get().await()

            val totalSpentToRemove = txSnap.documents.sumOf {
                it.getDouble("amount") ?: 0.0
            }

            val batch = firestore.batch()

            transfersInSnap.documents.forEach { transferDoc ->
                val fromCatId = transferDoc.getString("fromCategoryId") ?: return@forEach
                val amount = transferDoc.getDouble("amount") ?: 0.0
                val fromCatRef = eventRef.collection(COL_CATEGORIES).document(fromCatId)
                batch.update(fromCatRef, mapOf(
                    "transferredOut" to FieldValue.increment(-amount),
                    "updatedAt" to System.currentTimeMillis()
                ))
                batch.delete(transferDoc.reference)
            }

            transfersOutSnap.documents.forEach { transferDoc ->
                val toCatId = transferDoc.getString("toCategoryId") ?: return@forEach
                val amount = transferDoc.getDouble("amount") ?: 0.0
                val toCatRef = eventRef.collection(COL_CATEGORIES).document(toCatId)
                batch.update(toCatRef, mapOf(
                    "transferredIn" to FieldValue.increment(-amount),
                    "updatedAt" to System.currentTimeMillis()
                ))
                batch.delete(transferDoc.reference)
            }

            txSnap.documents.forEach { batch.delete(it.reference) }
            batch.delete(catRef)

            if (totalSpentToRemove > 0) {
                batch.update(eventRef, mapOf(
                    "spentAmount" to FieldValue.increment(-totalSpentToRemove),
                    "updatedAt" to System.currentTimeMillis()
                ))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory error", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // TRANSACTIONS
    // ============================================================

    suspend fun getTransactions(
        eventId: String,
        categoryId: String = "",
        limit: Long = 0L
    ): Result<List<EventTransaction>> {
        return try {
            val baseQuery = firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_TRANSACTIONS)

            var query: Query = if (categoryId.isNotBlank()) {
                baseQuery
                    .whereEqualTo("categoryId", categoryId)
                    .orderBy("transactionDate", Query.Direction.DESCENDING)
            } else {
                baseQuery.orderBy("transactionDate", Query.Direction.DESCENDING)
            }

            if (limit > 0L) query = query.limit(limit)

            val snapshot = query.get().await()
            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EventTransaction::class.java)?.copy(transactionId = doc.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Log.e(TAG, "getTransactions error", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactionById(
        eventId: String,
        transactionId: String
    ): Result<EventTransaction> {
        return try {
            val doc = firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_TRANSACTIONS)
                .document(transactionId)
                .get()
                .await()

            val tx = doc.toObject(EventTransaction::class.java)?.copy(transactionId = doc.id)
                ?: return Result.failure(Exception("Transaksi tidak ditemukan"))

            Result.success(tx)
        } catch (e: Exception) {
            Log.e(TAG, "getTransactionById error", e)
            Result.failure(e)
        }
    }

    /**
     * Tambah transaksi + update spent atomik.
     * Return: Pair<newTransaction, thresholdReset>
     *   thresholdReset = true kalau ada threshold yang perlu dicek ulang
     *                    (dipakai notifier untuk trigger check)
     */
    suspend fun addTransaction(transaction: EventTransaction): Result<EventTransaction> {
        return try {
            val txRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_TRANSACTIONS)
                .document()

            val catRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_CATEGORIES)
                .document(transaction.categoryId)

            val eventRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)

            val newTx = transaction.copy(
                transactionId = txRef.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            firestore.runTransaction { tx ->
                val catSnap = tx.get(catRef)
                val eventSnap = tx.get(eventRef)

                if (!catSnap.exists()) throw Exception("Kategori tidak ditemukan")
                if (!eventSnap.exists()) throw Exception("Event tidak ditemukan")

                val currentCatSpent = catSnap.getDouble("spentAmount") ?: 0.0
                val currentEventSpent = eventSnap.getDouble("spentAmount") ?: 0.0

                tx.set(txRef, newTx)
                tx.update(catRef, mapOf(
                    "spentAmount" to (currentCatSpent + transaction.amount),
                    "updatedAt" to System.currentTimeMillis()
                ))
                tx.update(eventRef, mapOf(
                    "spentAmount" to (currentEventSpent + transaction.amount),
                    "updatedAt" to System.currentTimeMillis()
                ))
                null
            }.await()

            Result.success(newTx)
        } catch (e: Exception) {
            Log.e(TAG, "addTransaction error", e)
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(
        oldAmount: Double,
        transaction: EventTransaction
    ): Result<EventTransaction> {
        return try {
            val txRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_TRANSACTIONS)
                .document(transaction.transactionId)

            val newCatRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_CATEGORIES)
                .document(transaction.categoryId)

            val eventRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)

            val updated = transaction.copy(updatedAt = System.currentTimeMillis())

            // Track resetted categories & event untuk dipakai notifier check ulang
            firestore.runTransaction { tx ->
                val existingTxSnap = tx.get(txRef)
                if (!existingTxSnap.exists()) throw Exception("Transaksi tidak ditemukan")

                val oldCategoryId = existingTxSnap.getString("categoryId") ?: ""
                val diff = transaction.amount - oldAmount

                if (oldCategoryId.isNotBlank() && oldCategoryId != transaction.categoryId) {
                    val oldCatRef = firestore.collection(COL_EVENTS)
                        .document(transaction.eventId)
                        .collection(COL_CATEGORIES)
                        .document(oldCategoryId)

                    val oldCatSnap = tx.get(oldCatRef)
                    val newCatSnap = tx.get(newCatRef)

                    if (!newCatSnap.exists()) throw Exception("Kategori baru tidak ditemukan")

                    val oldCatSpent = oldCatSnap.getDouble("spentAmount") ?: 0.0
                    val newCatSpent = newCatSnap.getDouble("spentAmount") ?: 0.0

                    val newOldCatSpent = safeSubtract(oldCatSpent, oldAmount, "updateTx.oldCat")

                    // Cek apakah kategori lama perlu reset lastNotifiedPercentage
                    val oldCatAllocated = oldCatSnap.getDouble("allocatedBudget") ?: 0.0
                    val oldCatTransferIn = oldCatSnap.getDouble("transferredIn") ?: 0.0
                    val oldCatTransferOut = oldCatSnap.getDouble("transferredOut") ?: 0.0
                    val oldCatEffective = oldCatAllocated + oldCatTransferIn - oldCatTransferOut
                    val oldCatLastTier = (oldCatSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                    val oldCatNewTier = computeTier(newOldCatSpent, oldCatEffective)

                    val oldCatUpdate = mutableMapOf<String, Any>(
                        "spentAmount" to newOldCatSpent,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    // Fix konfirmasi 4: reset tier kalau spent turun di bawah threshold
                    if (oldCatNewTier < oldCatLastTier) {
                        oldCatUpdate["lastNotifiedPercentage"] = oldCatNewTier
                    }
                    tx.update(oldCatRef, oldCatUpdate)

                    tx.update(newCatRef, mapOf(
                        "spentAmount" to (newCatSpent + transaction.amount),
                        "updatedAt" to System.currentTimeMillis()
                    ))
                } else if (diff != 0.0) {
                    val newCatSnap = tx.get(newCatRef)
                    val newCatSpent = newCatSnap.getDouble("spentAmount") ?: 0.0
                    val newSpent = if (diff < 0) safeSubtract(newCatSpent, -diff, "updateTx.sameCat")
                    else newCatSpent + diff

                    val catAllocated = newCatSnap.getDouble("allocatedBudget") ?: 0.0
                    val catTransferIn = newCatSnap.getDouble("transferredIn") ?: 0.0
                    val catTransferOut = newCatSnap.getDouble("transferredOut") ?: 0.0
                    val catEffective = catAllocated + catTransferIn - catTransferOut
                    val catLastTier = (newCatSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                    val catNewTier = computeTier(newSpent, catEffective)

                    val catUpdate = mutableMapOf<String, Any>(
                        "spentAmount" to newSpent,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    if (catNewTier < catLastTier) {
                        catUpdate["lastNotifiedPercentage"] = catNewTier
                    }
                    tx.update(newCatRef, catUpdate)
                }

                if (diff != 0.0) {
                    val eventSnap = tx.get(eventRef)
                    val currentEventSpent = eventSnap.getDouble("spentAmount") ?: 0.0
                    val newEventSpent = if (diff < 0) safeSubtract(currentEventSpent, -diff, "updateTx.event")
                    else currentEventSpent + diff

                    val eventBudget = eventSnap.getDouble("totalBudget") ?: 0.0
                    val eventLastTier = (eventSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                    val eventNewTier = computeTier(newEventSpent, eventBudget)

                    val eventUpdate = mutableMapOf<String, Any>(
                        "spentAmount" to newEventSpent,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    if (eventNewTier < eventLastTier) {
                        eventUpdate["lastNotifiedPercentage"] = eventNewTier
                    }
                    tx.update(eventRef, eventUpdate)
                }

                tx.set(txRef, updated)
                null
            }.await()

            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "updateTransaction error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transaction: EventTransaction): Result<Unit> {
        return try {
            val txRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_TRANSACTIONS)
                .document(transaction.transactionId)

            val catRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)
                .collection(COL_CATEGORIES)
                .document(transaction.categoryId)

            val eventRef = firestore.collection(COL_EVENTS)
                .document(transaction.eventId)

            firestore.runTransaction { tx ->
                val txSnap = tx.get(txRef)
                if (!txSnap.exists()) return@runTransaction null

                val catSnap = tx.get(catRef)
                val eventSnap = tx.get(eventRef)

                val currentCatSpent = catSnap.getDouble("spentAmount") ?: 0.0
                val currentEventSpent = eventSnap.getDouble("spentAmount") ?: 0.0

                tx.delete(txRef)

                if (catSnap.exists()) {
                    val newCatSpent = safeSubtract(currentCatSpent, transaction.amount, "deleteTx.cat")

                    val catAllocated = catSnap.getDouble("allocatedBudget") ?: 0.0
                    val catTransferIn = catSnap.getDouble("transferredIn") ?: 0.0
                    val catTransferOut = catSnap.getDouble("transferredOut") ?: 0.0
                    val catEffective = catAllocated + catTransferIn - catTransferOut
                    val catLastTier = (catSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                    val catNewTier = computeTier(newCatSpent, catEffective)

                    val catUpdate = mutableMapOf<String, Any>(
                        "spentAmount" to newCatSpent,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    // Fix konfirmasi 4: reset tier kalau spent turun di bawah threshold
                    if (catNewTier < catLastTier) {
                        catUpdate["lastNotifiedPercentage"] = catNewTier
                    }
                    tx.update(catRef, catUpdate)
                }

                val newEventSpent = safeSubtract(currentEventSpent, transaction.amount, "deleteTx.event")
                val eventBudget = eventSnap.getDouble("totalBudget") ?: 0.0
                val eventLastTier = (eventSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                val eventNewTier = computeTier(newEventSpent, eventBudget)

                val eventUpdate = mutableMapOf<String, Any>(
                    "spentAmount" to newEventSpent,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (eventNewTier < eventLastTier) {
                    eventUpdate["lastNotifiedPercentage"] = eventNewTier
                }
                tx.update(eventRef, eventUpdate)

                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteTransaction error", e)
            Result.failure(e)
        }
    }

    // ============================================================
    // TRANSFERS
    // ============================================================

    suspend fun transferBudget(transfer: EventTransfer): Result<EventTransfer> {
        return try {
            if (transfer.amount <= 0) {
                return Result.failure(Exception("Jumlah transfer harus lebih dari 0"))
            }
            if (transfer.fromCategoryId == transfer.toCategoryId) {
                return Result.failure(Exception("Kategori asal & tujuan tidak boleh sama"))
            }

            val transferRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_TRANSFERS)
                .document()

            val fromCatRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_CATEGORIES)
                .document(transfer.fromCategoryId)

            val toCatRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_CATEGORIES)
                .document(transfer.toCategoryId)

            val newTransfer = transfer.copy(
                transferId = transferRef.id,
                createdAt = System.currentTimeMillis()
            )

            firestore.runTransaction { tx ->
                val fromSnap = tx.get(fromCatRef)
                val toSnap = tx.get(toCatRef)

                if (!fromSnap.exists()) throw Exception("Kategori asal tidak ditemukan")
                if (!toSnap.exists()) throw Exception("Kategori tujuan tidak ditemukan")

                val fromAllocated = fromSnap.getDouble("allocatedBudget") ?: 0.0
                val fromSpent = fromSnap.getDouble("spentAmount") ?: 0.0
                val fromIn = fromSnap.getDouble("transferredIn") ?: 0.0
                val fromOut = fromSnap.getDouble("transferredOut") ?: 0.0

                val effectiveBudget = fromAllocated + fromIn - fromOut
                val availableForTransfer = effectiveBudget - fromSpent

                if (transfer.amount > availableForTransfer) {
                    throw Exception(
                        "Dana tidak cukup. Tersedia: ${availableForTransfer.toLong()}"
                    )
                }

                tx.set(transferRef, newTransfer)

                // Update source (effective budget turun → tier bisa naik untuk kategori target,
                //                dan tier source juga bisa berubah)
                val fromNewOut = fromOut + transfer.amount
                val fromNewEffective = fromAllocated + fromIn - fromNewOut
                val fromLastTier = (fromSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                val fromNewTier = computeTier(fromSpent, fromNewEffective)
                val fromUpdate = mutableMapOf<String, Any>(
                    "transferredOut" to fromNewOut,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (fromNewTier < fromLastTier) {
                    fromUpdate["lastNotifiedPercentage"] = fromNewTier
                }
                tx.update(fromCatRef, fromUpdate)

                // Update target (effective budget naik → tier bisa turun)
                val toIn = toSnap.getDouble("transferredIn") ?: 0.0
                val toAllocated = toSnap.getDouble("allocatedBudget") ?: 0.0
                val toOut = toSnap.getDouble("transferredOut") ?: 0.0
                val toSpent = toSnap.getDouble("spentAmount") ?: 0.0
                val toNewIn = toIn + transfer.amount
                val toNewEffective = toAllocated + toNewIn - toOut
                val toLastTier = (toSnap.getLong("lastNotifiedPercentage") ?: 0L).toInt()
                val toNewTier = computeTier(toSpent, toNewEffective)
                val toUpdate = mutableMapOf<String, Any>(
                    "transferredIn" to toNewIn,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (toNewTier < toLastTier) {
                    toUpdate["lastNotifiedPercentage"] = toNewTier
                }
                tx.update(toCatRef, toUpdate)
                null
            }.await()

            Result.success(newTransfer)
        } catch (e: Exception) {
            Log.e(TAG, "transferBudget error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTransfer(transfer: EventTransfer): Result<Unit> {
        return try {
            val transferRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_TRANSFERS)
                .document(transfer.transferId)

            val fromCatRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_CATEGORIES)
                .document(transfer.fromCategoryId)

            val toCatRef = firestore.collection(COL_EVENTS)
                .document(transfer.eventId)
                .collection(COL_CATEGORIES)
                .document(transfer.toCategoryId)

            firestore.runTransaction { tx ->
                val transferSnap = tx.get(transferRef)
                if (!transferSnap.exists()) throw Exception("Transfer tidak ditemukan")

                val toSnap = tx.get(toCatRef)
                if (!toSnap.exists()) throw Exception("Kategori tujuan tidak ditemukan")

                val toAllocated = toSnap.getDouble("allocatedBudget") ?: 0.0
                val toSpent = toSnap.getDouble("spentAmount") ?: 0.0
                val toIn = toSnap.getDouble("transferredIn") ?: 0.0
                val toOut = toSnap.getDouble("transferredOut") ?: 0.0

                val effectiveBudgetAfter = toAllocated + (toIn - transfer.amount) - toOut

                if (toSpent > effectiveBudgetAfter) {
                    val excess = toSpent - effectiveBudgetAfter
                    throw Exception(
                        "Tidak bisa rollback: ${transfer.toCategoryName} sudah menggunakan " +
                                "dana transfer ini. Kelebihan: ${excess.toLong()}"
                    )
                }

                val fromSnap = tx.get(fromCatRef)
                tx.delete(transferRef)

                if (fromSnap.exists()) {
                    val fromOut = fromSnap.getDouble("transferredOut") ?: 0.0
                    tx.update(fromCatRef, mapOf(
                        "transferredOut" to safeSubtract(fromOut, transfer.amount, "deleteTransfer.from"),
                        "updatedAt" to System.currentTimeMillis()
                    ))
                }
                tx.update(toCatRef, mapOf(
                    "transferredIn" to safeSubtract(toIn, transfer.amount, "deleteTransfer.to"),
                    "updatedAt" to System.currentTimeMillis()
                ))
                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteTransfer error", e)
            Result.failure(e)
        }
    }

    suspend fun getTransfers(eventId: String): Result<List<EventTransfer>> {
        return try {
            val snapshot = firestore.collection(COL_EVENTS)
                .document(eventId)
                .collection(COL_TRANSFERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val transfers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EventTransfer::class.java)?.copy(transferId = doc.id)
            }
            Result.success(transfers)
        } catch (e: Exception) {
            Log.e(TAG, "getTransfers error", e)
            Result.failure(e)
        }
    }
}