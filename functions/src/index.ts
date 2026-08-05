import { setGlobalOptions } from "firebase-functions/v2";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";
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

/**
 * SECURITY FIX (v1.1.1): kick & promote/demote member dipindah ke sini
 * (server-side, Admin SDK) karena Firestore rules client-side untuk
 * families/update TIDAK BISA membedakan "member biasa" vs "admin" dengan
 * aman -- lihat catatan di Firestore-rules.txt. Semua pengecekan role &
 * penulisan ke families.members + users/{id} dilakukan di sini dalam
 * SATU transaction, supaya tidak ada state yang nyangkut kalau salah
 * satu write gagal.
 */

interface KickMemberRequest {
  familyId: string;
  targetUserId: string;
}

export const kickFamilyMember = onCall<KickMemberRequest>(async (request) => {
  const callerId = request.auth?.uid;
  if (!callerId) {
    throw new HttpsError("unauthenticated", "Kamu harus login.");
  }

  const { familyId, targetUserId } = request.data;
  if (!familyId || !targetUserId) {
    throw new HttpsError("invalid-argument", "familyId dan targetUserId wajib diisi.");
  }
  if (targetUserId === callerId) {
    throw new HttpsError(
      "failed-precondition",
      "Tidak bisa kick diri sendiri, gunakan fitur 'Keluar Keluarga'."
    );
  }

  const familyRef = db.collection("families").doc(familyId);
  const callerRef = db.collection("users").doc(callerId);
  const targetRef = db.collection("users").doc(targetUserId);

  await db.runTransaction(async (tx) => {
    const [familySnap, callerSnap, targetSnap] = await Promise.all([
      tx.get(familyRef),
      tx.get(callerRef),
      tx.get(targetRef),
    ]);

    if (!familySnap.exists) {
      throw new HttpsError("not-found", "Family tidak ditemukan.");
    }
    if (!callerSnap.exists) {
      throw new HttpsError("not-found", "User pemanggil tidak ditemukan.");
    }

    const callerData = callerSnap.data()!;
    const familyData = familySnap.data()!;

    // Pemanggil harus admin DAN anggota family yang sama.
    if (callerData.role !== "admin" || callerData.familyId !== familyId) {
      throw new HttpsError(
        "permission-denied",
        "Hanya admin keluarga ini yang boleh mengeluarkan anggota."
      );
    }

    const members: string[] = familyData.members ?? [];
    if (!members.includes(targetUserId)) {
      throw new HttpsError("not-found", "User tersebut bukan anggota keluarga ini.");
    }

    // Update family.members
    tx.update(familyRef, {
      members: admin.firestore.FieldValue.arrayRemove(targetUserId),
    });

    // Reset familyId & role di dokumen user yang di-kick (kalau ada)
    if (targetSnap.exists) {
      tx.update(targetRef, { familyId: "", role: "member" });
    }
  });

  logger.info(`Admin ${callerId} kick user ${targetUserId} dari family ${familyId}`);
  return { success: true };
});

interface PromoteRequest {
  familyId: string;
  targetUserId: string;
  newRole: "admin" | "member";
}

export const setFamilyMemberRole = onCall<PromoteRequest>(async (request) => {
  const callerId = request.auth?.uid;
  if (!callerId) {
    throw new HttpsError("unauthenticated", "Kamu harus login.");
  }

  const { familyId, targetUserId, newRole } = request.data;
  if (!familyId || !targetUserId || (newRole !== "admin" && newRole !== "member")) {
    throw new HttpsError("invalid-argument", "familyId, targetUserId, dan newRole wajib valid.");
  }
  if (targetUserId === callerId) {
    throw new HttpsError("failed-precondition", "Tidak bisa mengubah role diri sendiri.");
  }

  const callerRef = db.collection("users").doc(callerId);
  const targetRef = db.collection("users").doc(targetUserId);

  await db.runTransaction(async (tx) => {
    const [callerSnap, targetSnap] = await Promise.all([tx.get(callerRef), tx.get(targetRef)]);

    if (!callerSnap.exists || !targetSnap.exists) {
      throw new HttpsError("not-found", "User tidak ditemukan.");
    }

    const callerData = callerSnap.data()!;
    const targetData = targetSnap.data()!;

    if (callerData.role !== "admin" || callerData.familyId !== familyId) {
      throw new HttpsError(
        "permission-denied",
        "Hanya admin keluarga ini yang boleh mengubah role anggota."
      );
    }
    if (targetData.familyId !== familyId) {
      throw new HttpsError("failed-precondition", "User tersebut bukan anggota keluarga ini.");
    }

    tx.update(targetRef, { role: newRole });
  });

  logger.info(`Admin ${callerId} set role ${targetUserId} -> ${newRole} (family ${familyId})`);
  return { success: true };
});
