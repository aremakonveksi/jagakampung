package com.jagakampung.app.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jagakampung.app.BuildConfig;
import com.jagakampung.app.CekNotifWorker;
import com.jagakampung.app.LoginActivity;
import com.jagakampung.app.NotifUtil;
import com.jagakampung.app.NotifWatcher;
import com.jagakampung.app.R;
import com.jagakampung.app.UpdateDialog;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Dashboard admin. Semua angka dihitung langsung dari Firebase:
 * - kartu utama   : jumlah laporan berstatus "dilaporkan"
 * - baris kategori: jumlah laporan per kategori
 * - warga         : users dengan peran "warga"
 * - menunggu      : users dengan status "pending"
 *
 * Catatan: kategori disimpan huruf kecil oleh form warga (mis. "infrastruktur")
 * tetapi data lama bisa berkapital. Perbandingan dilakukan case-insensitive.
 */
public class DashboardActivity extends Activity {

    private static final int TEAL = Color.parseColor("#10766B");

    /** Kategori yang ditampilkan di dashboard (label tampilan). */
    private static final String[] KATEGORI = {"Infrastruktur", "Kebersihan", "Keamanan"};

    /** Jumlah per kategori, sudah digabung case-insensitive. */
    private final Map<String, Integer> jumlahKategori = new LinkedHashMap<>();

    private DatabaseReference root;
    private ValueEventListener pendengarLaporan;
    private ValueEventListener pendengarUsers;
    private NotifWatcher.Pasangan notifAdmin;

    private LinearLayout boxKategori;
    private TextView tvBelum, tvTotalLaporan, tvWarga, tvMenunggu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        for (String k : KATEGORI) jumlahKategori.put(k.toLowerCase(Locale.ROOT), 0);

        tvBelum = findViewById(R.id.tvBelum);
        tvTotalLaporan = findViewById(R.id.tvTotalLaporan);
        tvWarga = findViewById(R.id.tvWarga);
        tvMenunggu = findViewById(R.id.tvMenunggu);
        boxKategori = findViewById(R.id.boxKategori);

        // Kartu utama berwarna teal
        GradientDrawable bgBelum = new GradientDrawable();
        bgBelum.setCornerRadius(dp(16));
        bgBelum.setColor(TEAL);
        findViewById(R.id.cardBelum).setBackground(bgBelum);

        // Tombol Antrean Laporan: hijau penuh
        Button btnLaporan = findViewById(R.id.btnLaporan);
        GradientDrawable bgLaporan = new GradientDrawable();
        bgLaporan.setCornerRadius(dp(14));
        bgLaporan.setColor(TEAL);
        btnLaporan.setBackground(bgLaporan);
        btnLaporan.setOnClickListener(v ->
                startActivity(new Intent(this, DaftarLaporanActivity.class)));

        // Tombol Verifikasi Akun: garis hijau
        Button btnVerifikasi = findViewById(R.id.btnVerifikasi);
        GradientDrawable bgVerifikasi = new GradientDrawable();
        bgVerifikasi.setCornerRadius(dp(14));
        bgVerifikasi.setColor(Color.WHITE);
        bgVerifikasi.setStroke(dp(1), TEAL);
        btnVerifikasi.setBackground(bgVerifikasi);
        btnVerifikasi.setTextColor(TEAL);
        btnVerifikasi.setOnClickListener(v ->
                startActivity(new Intent(this, VerifikasiAkunActivity.class)));

        // Tombol Keluar: putuskan login, lalu kembali ke layar Masuk
        Button btnKeluar = findViewById(R.id.btnKeluar);
        btnKeluar.setBackgroundColor(Color.TRANSPARENT);
        btnKeluar.setTextColor(Color.parseColor("#E5484D"));
        btnKeluar.setOnClickListener(v -> {
            CekNotifWorker.hentikan(this);
            FirebaseAuth.getInstance().signOut();
            Intent masuk = new Intent(this, LoginActivity.class);
            masuk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(masuk);
            finish();
        });

        root = FirebaseDatabase.getInstance().getReference();
        buatBarisKategori();
        pasangPendengar();

        // Versi + tombol update manual
        TextView tvVersi = findViewById(R.id.tvVersi);
        tvVersi.setText(getString(R.string.versi_terpasang, BuildConfig.VERSION_NAME));
        findViewById(R.id.btnUpdate).setOnClickListener(v -> UpdateDialog.cekManual(this));

        NotifUtil.siapkanKanal(this);
        NotifUtil.mintaIzin(this);

        // Notifikasi lokal: ada laporan baru masuk
        notifAdmin = NotifWatcher.pasangAdmin(this);

        // Pemeriksaan latar berkala
        CekNotifWorker.jadwalkan(this);

        // Cek update senyap sekali saat dashboard dibuka
        UpdateDialog.cekSenyap(this);
    }

    // ============================================================
    // Baca Firebase
    // ============================================================

    private void pasangPendengar() {
        pendengarLaporan = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int belumDitangani = 0;
                int total = 0;
                for (String k : jumlahKategori.keySet()) jumlahKategori.put(k, 0);

                for (DataSnapshot anak : snapshot.getChildren()) {
                    total++;
                    String status = teks(anak, "status", "dilaporkan");
                    if ("dilaporkan".equalsIgnoreCase(status)) belumDitangani++;

                    String kat = teks(anak, "kategori", "").toLowerCase(Locale.ROOT);
                    if (jumlahKategori.containsKey(kat)) {
                        jumlahKategori.put(kat, jumlahKategori.get(kat) + 1);
                    }
                }

                tvBelum.setText(String.valueOf(belumDitangani));
                tvTotalLaporan.setText("dari " + total + " laporan masuk");
                perbaruiBarisKategori();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this,
                        "Gagal memuat laporan: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        root.child("laporan").addValueEventListener(pendengarLaporan);

        pendengarUsers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int warga = 0;
                int pending = 0;
                for (DataSnapshot anak : snapshot.getChildren()) {
                    if ("warga".equalsIgnoreCase(teks(anak, "peran", ""))) warga++;
                    if ("pending".equalsIgnoreCase(teks(anak, "status", ""))) pending++;
                }
                tvWarga.setText(String.valueOf(warga));
                tvMenunggu.setText(String.valueOf(pending));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this,
                        "Gagal memuat warga: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        root.child("users").addValueEventListener(pendengarUsers);
    }

    private String teks(DataSnapshot anak, String kunci, String cadangan) {
        Object v = anak.child(kunci).getValue();
        return v == null ? cadangan : String.valueOf(v);
    }

    @Override
    protected void onDestroy() {
        if (notifAdmin != null) notifAdmin.lepas();
        if (root != null) {
            if (pendengarLaporan != null) root.child("laporan").removeEventListener(pendengarLaporan);
            if (pendengarUsers != null) root.child("users").removeEventListener(pendengarUsers);
        }
        super.onDestroy();
    }

    // ============================================================
    // Baris kategori
    // ============================================================

    private void buatBarisKategori() {
        boxKategori.removeAllViews();
        for (String label : KATEGORI) {
            LinearLayout baris = new LinearLayout(this);
            baris.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lpBaris = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (baris.getChildCount() > 0) lpBaris.topMargin = dp(14);
            baris.setLayoutParams(lpBaris);

            LinearLayout atas = new LinearLayout(this);
            atas.setOrientation(LinearLayout.HORIZONTAL);

            TextView nama = new TextView(this);
            nama.setText(label);
            nama.setTextSize(15);
            nama.setTextColor(Color.parseColor("#444853"));
            nama.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView angka = new TextView(this);
            angka.setText("0");
            angka.setTextSize(15);
            angka.setTextColor(TEAL);
            angka.setTypeface(null, android.graphics.Typeface.BOLD);

            atas.addView(nama);
            atas.addView(angka);

            LinearLayout jalur = new LinearLayout(this);
            LinearLayout.LayoutParams lpJalur = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
            lpJalur.topMargin = dp(6);
            jalur.setLayoutParams(lpJalur);
            GradientDrawable bgJalur = new GradientDrawable();
            bgJalur.setCornerRadius(dp(4));
            bgJalur.setColor(Color.parseColor("#D9EAE8"));
            jalur.setBackground(bgJalur);

            View isi = new View(this);
            isi.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            GradientDrawable bgIsi = new GradientDrawable();
            bgIsi.setCornerRadius(dp(4));
            bgIsi.setColor(TEAL);
            isi.setBackground(bgIsi);
            jalur.addView(isi);

            baris.addView(atas);
            baris.addView(jalur);
            boxKategori.addView(baris);

            // simpan referensi supaya bisa diperbarui
            baris.setTag(new View[]{angka, isi});
        }
        perbaruiBarisKategori();
    }

    private void perbaruiBarisKategori() {
        if (boxKategori == null) return;

        int maks = 1;
        for (int v : jumlahKategori.values()) maks = Math.max(maks, v);

        for (int n = 0; n < boxKategori.getChildCount(); n++) {
            LinearLayout baris = (LinearLayout) boxKategori.getChildAt(n);
            Object tag = baris.getTag();
            if (!(tag instanceof View[])) continue;

            View[] ref = (View[]) tag;
            TextView angka = (TextView) ref[0];
            View isi = ref[1];

            String kunci = KATEGORI[n].toLowerCase(Locale.ROOT);
            int jumlah = jumlahKategori.containsKey(kunci) ? jumlahKategori.get(kunci) : 0;

            angka.setText(String.valueOf(jumlah));

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) isi.getLayoutParams();
            lp.weight = Math.max(jumlah, 0.0001f);
            isi.setLayoutParams(lp);

            View jalur = (View) isi.getParent();
            if (jalur instanceof LinearLayout) {
                ((LinearLayout) jalur).setWeightSum(maks + 0.0001f);
            }
        }
    }

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }
}
