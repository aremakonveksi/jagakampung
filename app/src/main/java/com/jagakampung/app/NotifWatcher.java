package com.jagakampung.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pemicu notifikasi lokal dari perubahan Realtime Database.
 *
 * Catatan penting: listener hanya hidup selama proses aplikasi berjalan.
 * Kalau aplikasi di-force stop, tidak ada notifikasi sampai aplikasi dibuka lagi.
 * Pada pembukaan pertama, keadaan terakhir disimpan tanpa memunculkan notifikasi
 * (mencegah banjir notifikasi lama).
 */
public final class NotifWatcher {

    private static final String PREF = "jaga_notif";
    private static final String KUNCI_WAKTU_ADMIN = "waktu_laporan_admin";
    private static final String KUNCI_STATUS_WARGA = "status_laporan_warga";

    private NotifWatcher() {}

    private static SharedPreferences pref(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /** Pegangan listener supaya bisa dilepas rapi di onDestroy. */
    public static final class Pasangan {
        final DatabaseReference ref;
        final Query query;
        final ValueEventListener listener;

        Pasangan(DatabaseReference ref, Query query, ValueEventListener listener) {
            this.ref = ref;
            this.query = query;
            this.listener = listener;
        }

        public void lepas() {
            if (query != null) query.removeEventListener(listener);
            else if (ref != null) ref.removeEventListener(listener);
        }
    }

    // ============================================================
    // ADMIN — laporan baru masuk
    // ============================================================

    public static Pasangan pasangAdmin(final Activity activity) {
        final SharedPreferences p = pref(activity);
        DatabaseReference laporan = FirebaseDatabase.getInstance().getReference("laporan");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maks = 0;
                List<String> baru = new ArrayList<>();

                long batas = p.getLong(KUNCI_WAKTU_ADMIN, -1L);

                for (DataSnapshot anak : snapshot.getChildren()) {
                    long waktu = angka(anak.child("dibuatPada").getValue());
                    if (waktu > maks) maks = waktu;
                    if (batas >= 0 && waktu > batas) {
                        baru.add(teks(anak.child("judul").getValue(), "(tanpa judul)"));
                    }
                }

                if (batas < 0) {
                    // Kunjungan pertama: simpan patokan, jangan notifikasi.
                    p.edit().putLong(KUNCI_WAKTU_ADMIN, maks).apply();
                    return;
                }

                if (!baru.isEmpty()) {
                    String isi = baru.size() == 1
                            ? baru.get(0)
                            : baru.size() + " laporan baru: " + baru.get(0) + ", dll.";
                    NotifUtil.tampilkan(activity, 2001,
                            activity.getString(R.string.notif_laporan_baru), isi);
                }
                if (maks > batas) p.edit().putLong(KUNCI_WAKTU_ADMIN, maks).apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        laporan.addValueEventListener(listener);
        return new Pasangan(laporan, null, listener);
    }

    // ============================================================
    // WARGA — status laporannya berubah
    // ============================================================

    public static Pasangan pasangWarga(final Activity activity, FirebaseUser user) {
        if (user == null) return null;

        final SharedPreferences p = pref(activity);
        final String kunci = KUNCI_STATUS_WARGA + "_" + user.getUid();

        Query query = FirebaseDatabase.getInstance().getReference("laporan")
                .orderByChild("uidPelapor").equalTo(user.getUid());

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> lama = bacaPeta(p.getString(kunci, ""));
                Map<String, String> baru = new HashMap<>();
                boolean pertamaKali = lama.isEmpty();

                for (DataSnapshot anak : snapshot.getChildren()) {
                    String id = anak.getKey();
                    if (id == null) continue;
                    String status = teks(anak.child("status").getValue(), "dilaporkan");
                    String judul = teks(anak.child("judul").getValue(), "(tanpa judul)");
                    baru.put(id, status);

                    if (pertamaKali) continue;

                    String statusLama = lama.get(id);
                    if (statusLama != null && !statusLama.equalsIgnoreCase(status)) {
                        NotifUtil.tampilkan(activity, id.hashCode(),
                                activity.getString(R.string.notif_status_berubah),
                                judul + " → " + labelStatus(status));
                    }
                }

                simpanPeta(p, kunci, baru);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        query.addValueEventListener(listener);
        return new Pasangan(null, query, listener);
    }

    // ============================================================

    private static String labelStatus(String status) {
        if (status == null || status.isEmpty()) return "-";
        String s = status.toLowerCase(Locale.ROOT);
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    private static long angka(Object v) {
        return (v instanceof Number) ? ((Number) v).longValue() : 0L;
    }

    private static String teks(Object v, String cadangan) {
        if (v == null) return cadangan;
        String s = String.valueOf(v);
        return s.isEmpty() ? cadangan : s;
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
}
