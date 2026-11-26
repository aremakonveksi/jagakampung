package com.jagakampung.app;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/** Kanal notifikasi + izin + cara menampilkan notifikasi lokal. */
public final class NotifUtil {

    public static final String KANAL_INFO = "jaga_kampung_info";
    public static final int KODE_MINTA_IZIN = 4301;

    private NotifUtil() {}

    /** Buat NotificationChannel (wajib Android 8+). Aman dipanggil berulang. */
    public static void siapkanKanal(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel kanal = new NotificationChannel(
                KANAL_INFO,
                ctx.getString(R.string.kanal_notif_nama),
                NotificationManager.IMPORTANCE_DEFAULT);
        kanal.setDescription(ctx.getString(R.string.kanal_notif_deskripsi));
        kanal.setShowBadge(true);
        nm.createNotificationChannel(kanal);
    }

    /** Minta izin POST_NOTIFICATIONS (Android 13+). Kalau sudah ada, tidak melakukan apa pun. */
    public static void mintaIzin(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        int status = ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS);
        if (status == PackageManager.PERMISSION_GRANTED) return;
        activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, KODE_MINTA_IZIN);
    }

    /** Tampilkan notifikasi lokal. Kalau izin belum ada, diam saja (tidak crash). */
    public static void tampilkan(Context ctx, int id, String judul, String isi) {
        if (!bolehTampil(ctx)) return;

        Intent buka = new Intent(ctx, LoginActivity.class);
        buka.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, id, buka,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, KANAL_INFO)
                .setSmallIcon(R.drawable.ic_notif_jaga)
                .setContentTitle(judul)
                .setContentText(isi)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(isi))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        try {
            NotificationManagerCompat.from(ctx).notify(id, b.build());
        } catch (SecurityException abaikan) {
            // izin dicabut di tengah jalan — tidak perlu mengganggu user
        }
    }

    /** Apakah notifikasi benar-benar boleh tampil sekarang (dipakai juga oleh Worker). */
    public static boolean bolehTampil(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled();
    }
}
