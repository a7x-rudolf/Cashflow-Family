import { setGlobalOptions } from "firebase-functions/v2";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

admin.initializeApp();

// Sesuaikan region kalau Firestore-mu bukan di asia-southeast2 (Jakarta).
// Cek di Firebase Console -> Firestore -> lokasi database.
setGlobalOptions({ region: "asia-southeast2" });

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
export const sendPushOnNotificationCreate = onDocumentCreated(
  "notifications/{notificationId}",
  async (event) => {
    const snap = event.data;
    if (!snap) {
      logger.warn("Tidak ada data pada event, skip");
      return;
    }

    const notif = snap.data();
    const recipientId: string | undefined = notif.userId;
    const notificationId = event.params.notificationId;

    if (!recipientId) {
      logger.warn(`Notification ${notificationId} tanpa userId, skip`);
      return;
    }

    // Untuk family_activity: jangan push ke device pembuat transaksi itu
    // sendiri -- dia sudah tahu, ini notif untuk anggota LAIN.
    const actorId: string | undefined = notif.data?.userId;
    if (notif.type === "family_activity" && actorId && actorId === recipientId) {
      logger.debug(`Skip push ke actor sendiri (${recipientId})`);
      return;
    }

    const userSnap = await db.collection("users").doc(recipientId).get();
    const token: string | undefined = userSnap.data()?.fcmToken;

    if (!token) {
      logger.debug(`User ${recipientId} tidak punya fcmToken, skip push`);
      return;
    }

    const message: admin.messaging.Message = {
      token,
      data: {
        title: notif.title ?? "Cashflow Family",
        message: notif.message ?? "Ada aktivitas baru di keluarga Anda",
        type: notif.type ?? "info",
        notificationId,
      },
      android: {
        priority: "high",
      },
    };

    try {
      await messaging.send(message);
      logger.info(`Push terkirim ke user ${recipientId} (notif ${notificationId})`);
    } catch (err: any) {
      logger.error(`Gagal kirim push ke user ${recipientId}`, err);

      // Token sudah tidak valid (uninstall / logout lama) -> bersihkan
      // supaya function berikutnya tidak coba-coba kirim ke token mati.
      const code = err?.code as string | undefined;
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        await userSnap.ref.update({ fcmToken: admin.firestore.FieldValue.delete() });
        logger.info(`fcmToken tidak valid dihapus untuk user ${recipientId}`);
      }
    }
  }
);
