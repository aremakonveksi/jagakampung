package com.jagakampung.app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Pemeriksa latar belakang: mendeteksi laporan baru (admin) atau perubahan status
 * laporan warga, lalu memunculkan notifikasi walau aplikasi sedang tidak dibuka.
 *
 * Batasan jujur: WorkManager menjalankan tugas secara berkala (minimal 15 menit,
 * ditentukan sistem) dan TIDAK berjalan kalau aplikasi di-force stop oleh user.
 * Untuk notifikasi instan 24 jam, tetap perlu FCM + server.
 */
public class CekNotifWorker extends Worker {

    private static final String NAMA_KERJA = "cek_notif_berkala";
    private static final String PREF = "jaga_notif";
    private static final String KUNCI_WAKTU_ADMIN = "waktu_laporan_admin";
    private static final String KUNCI_STATUS_WARGA = "status_laporan_warga";

    public CekNotifWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();   // belum login: tidak ada yang dicek

        if (!NotifUtil.bolehTampil(getApplicationContext())) return Result.success();

        // Berurutan, bukan paralel: hasil baca laporan dipakai oleh kedua jalur.
        String peran = bacaTunggu(FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("peran"));

        DataSnapshot laporan = bacaSnapshotTunggu(
                FirebaseDatabase.getInstance().getReference("laporan"));
        if (laporan == null) return Result.retry();

        if ("admin".equalsIgnoreCase(peran)) prosesAdminBaru(laporan);
        else prosesStatusWarga(user.getUid(), laporan);

        return Result.success();
    }

    /** Baca satu nilai String dan tunggu sampai selesai (maks 20 detik). */
    private String bacaTunggu(com.google.firebase.database.Query query) {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] hasil = {null};
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hasil[0] = snapshot.getValue(String.class);
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                latch.countDown();
            }
        });
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return hasil[0];
    }

    /** Baca seluruh node laporan dan tunggu sampai selesai (maks 25 detik). */
    private DataSnapshot bacaSnapshotTunggu(com.google.firebase.database.Query query) {
        final CountDownLatch latch = new CountDownLatch(1);
        final DataSnapshot[] hasil = {null};
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hasil[0] = snapshot;
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                latch.countDown();
            }
        });
        try {
            latch.await(25, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return hasil[0];
    }

    // ============================================================

    private void prosesAdminBaru(DataSnapshot snapshot) {
        SharedPreferences p = pref();
        long batas = p.getLong(KUNCI_WAKTU_ADMIN, -1L);
        long maks = 0;
        int jumlahBaru = 0;
        String judulPertama = null;

        for (DataSnapshot anak : snapshot.getChildren()) {
            long waktu = angka(anak.child("dibuatPada").getValue());
            if (waktu > maks) maks = waktu;
            if (batas >= 0 && waktu > batas) {
                jumlahBaru++;
                if (judulPertama == null) {
                    String j = anak.child("judul").getValue(String.class);
                    judulPertama = (j == null || j.isEmpty()) ? "(tanpa judul)" : j;
                }
            }
        }

        if (batas < 0) {
            p.edit().putLong(KUNCI_WAKTU_ADMIN, maks).apply();
            return;
        }
        if (jumlahBaru > 0 && judulPertama != null) {
            String isi = jumlahBaru == 1
                    ? judulPertama
                    : jumlahBaru + " laporan baru: " + judulPertama + ", dll.";
            NotifUtil.tampilkan(getApplicationContext(), 2001,
                    getApplicationContext().getString(R.string.notif_laporan_baru), isi);
        }
        if (maks > batas) p.edit().putLong(KUNCI_WAKTU_ADMIN, maks).apply();
    }

    private void prosesStatusWarga(String uid, DataSnapshot snapshot) {
        if (snapshot == null) return;

        SharedPreferences p = pref();
        String kunci = KUNCI_STATUS_WARGA + "_" + uid;
        Map<String, String> lama = bacaPeta(p.getString(kunci, ""));
        Map<String, String> baru = new HashMap<>();
        boolean pertamaKali = lama.isEmpty();

        for (DataSnapshot anak : snapshot.getChildren()) {
            String pelapor = anak.child("uidPelapor").getValue(String.class);
            if (!uid.equals(pelapor)) continue;

            String id = anak.getKey();
            if (id == null) continue;
            String status = anak.child("status").getValue(String.class);
            if (status == null) status = "dilaporkan";
            baru.put(id, status);

            if (pertamaKali) continue;
            String statusLama = lama.get(id);
            if (statusLama != null && !statusLama.equalsIgnoreCase(status)) {
                String judul = anak.child("judul").getValue(String.class);
                if (judul == null || judul.isEmpty()) judul = "(tanpa judul)";
                NotifUtil.tampilkan(getApplicationContext(), id.hashCode(),
                        getApplicationContext().getString(R.string.notif_status_berubah),
                        judul + " → " + labelStatus(status));
            }
        }
        simpanPeta(p, kunci, baru);
    }

    // ============================================================

    private SharedPreferences pref() {
        return getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String labelStatus(String status) {
        String s = status.toLowerCase(java.util.Locale.ROOT);
        return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
    }

    private static long angka(Object v) {
        return (v instanceof Number) ? ((Number) v).longValue() : 0L;
    }

    private static Map<String, String> bacaPeta(String isi) {
        Map<String, String> peta = new HashMap<>();
        if (isi == null || isi.isEmpty()) return peta;
        for (String baris : isi.split("\n")) {
            int pisah = baris.indexOf('=');
            if (pisah <= 0) continue;
            peta.put(baris.substring(0, pisah), baris.substring(pisah + 1));
        }
        return peta;
    }

    private static void simpanPeta(SharedPreferences p, String kunci, Map<String, String> peta) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : peta.entrySet()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        p.edit().putString(kunci, sb.toString()).apply();
    }

    // ============================================================

    /** Daftarkan pemeriksaan berkala (idempoten: aman dipanggil berulang). */
    public static void jadwalkan(Context ctx) {
        Constraints syarat = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest kerja = new PeriodicWorkRequest.Builder(
                CekNotifWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(syarat)
                .build();

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                NAMA_KERJA, ExistingPeriodicWorkPolicy.KEEP, kerja);
    }

    /** Hentikan pemeriksaan latar (dipanggil saat keluar akun). */
    public static void hentikan(Context ctx) {
        WorkManager.getInstance(ctx).cancelUniqueWork(NAMA_KERJA);
    }
}
