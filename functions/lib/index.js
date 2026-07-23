"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.sendPushOnNotificationCreate = void 0;
const v2_1 = require("firebase-functions/v2");
const firestore_1 = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
admin.initializeApp();
// Sesuaikan region kalau Firestore-mu bukan di asia-southeast2 (Jakarta).
// Cek di Firebase Console -> Firestore -> lokasi database.
(0, v2_1.setGlobalOptions)({ region: "asia-southeast2" });
const db = admin.firestore();
const messaging = admin.messaging();
/**
 * Trigger tiap kali dokumen baru dibuat di collection `notifications`
 * (dibuat oleh NotificationRepository.addNotifications() di app Android,
 * misalnya lewat FamilyActivityListener saat ada transaksi baru).
 *
 * Function ini yang mengirim PUSH NOTIFICATION (FCM) sesungguhnya ke
 * device anggota keluarga lain -- termasuk saat app mereka sedang
 * tertutup/dibunuh, bukan cuma saat app-nya lagi jalan.
 */
exports.sendPushOnNotificationCreate = (0, firestore_1.onDocumentCreated)("notifications/{notificationId}", async (event) => {
    var _a, _b, _c, _d, _e;
    const snap = event.data;
    if (!snap) {
        logger.warn("Tidak ada data pada event, skip");
        return;
    }
    const notif = snap.data();
    const recipientId = notif.userId;
    const notificationId = event.params.notificationId;
    if (!recipientId) {
        logger.warn(`Notification ${notificationId} tanpa userId, skip`);
        return;
    }
    // Untuk family_activity: jangan push ke device pembuat transaksi itu
    // sendiri -- dia sudah tahu, ini notif untuk anggota LAIN.
    const actorId = (_a = notif.data) === null || _a === void 0 ? void 0 : _a.userId;
    if (notif.type === "family_activity" && actorId && actorId === recipientId) {
        logger.debug(`Skip push ke actor sendiri (${recipientId})`);
        return;
    }
    const userSnap = await db.collection("users").doc(recipientId).get();
    const token = (_b = userSnap.data()) === null || _b === void 0 ? void 0 : _b.fcmToken;
    if (!token) {
        logger.debug(`User ${recipientId} tidak punya fcmToken, skip push`);
        return;
    }
    const message = {
        token,
        data: {
            title: (_c = notif.title) !== null && _c !== void 0 ? _c : "Cashflow Family",
            message: (_d = notif.message) !== null && _d !== void 0 ? _d : "Ada aktivitas baru di keluarga Anda",
            type: (_e = notif.type) !== null && _e !== void 0 ? _e : "info",
            notificationId,
        },
        android: {
            priority: "high",
        },
    };
    try {
        await messaging.send(message);
        logger.info(`Push terkirim ke user ${recipientId} (notif ${notificationId})`);
    }
    catch (err) {
        logger.error(`Gagal kirim push ke user ${recipientId}`, err);
        // Token sudah tidak valid (uninstall / logout lama) -> bersihkan
        // supaya function berikutnya tidak coba-coba kirim ke token mati.
        const code = err === null || err === void 0 ? void 0 : err.code;
        if (code === "messaging/registration-token-not-registered" ||
            code === "messaging/invalid-registration-token") {
            await userSnap.ref.update({ fcmToken: admin.firestore.FieldValue.delete() });
            logger.info(`fcmToken tidak valid dihapus untuk user ${recipientId}`);
        }
    }
});
//# sourceMappingURL=index.js.map