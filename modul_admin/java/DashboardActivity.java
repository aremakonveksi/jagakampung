package com.jagakampung.demo.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jagakampung.demo.R;

public class DashboardActivity extends Activity {

    private static final int TEAL = Color.parseColor("#10766B");

    // ===== DATA CONTOH =====
    // Nanti diganti dengan hitungan dari Firebase:
    //  - laporan per kategori : hitung node "laporan" per field "kategori"
    //  - belum ditangani      : laporan dengan status "dilaporkan"
    //  - warga                : node "users" dengan status "approved"
    //  - menunggu verifikasi  : node "users" dengan status "pending"
    private static final String[] KATEGORI = {"Infrastruktur", "Kebersihan", "Keamanan"};
    private static final int[] JUMLAH_KATEGORI = {3, 1, 1};
    private static final int BELUM_DITANGANI = 2;
    private static final int JUMLAH_WARGA = 8;
    private static final int MENUNGGU_VERIFIKASI = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        int totalLaporan = 0;
        for (int j : JUMLAH_KATEGORI) totalLaporan += j;

        ((TextView) findViewById(R.id.tvBelum)).setText(String.valueOf(BELUM_DITANGANI));
        ((TextView) findViewById(R.id.tvTotalLaporan))
                .setText("dari " + totalLaporan + " laporan masuk");
        ((TextView) findViewById(R.id.tvWarga)).setText(String.valueOf(JUMLAH_WARGA));
        ((TextView) findViewById(R.id.tvMenunggu)).setText(String.valueOf(MENUNGGU_VERIFIKASI));

        // Kartu utama berwarna teal
        GradientDrawable bgBelum = new GradientDrawable();
        bgBelum.setCornerRadius(dp(16));
        bgBelum.setColor(TEAL);
        findViewById(R.id.cardBelum).setBackground(bgBelum);

        buatBarisKategori();

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
    }

    // Satu baris per kategori: nama + angka + batang proporsional
    private void buatBarisKategori() {
        LinearLayout box = findViewById(R.id.boxKategori);

        int maks = 1;
        for (int j : JUMLAH_KATEGORI) maks = Math.max(maks, j);

        for (int n = 0; n < KATEGORI.length; n++) {
            LinearLayout baris = new LinearLayout(this);
            baris.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lpBaris = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (n > 0) lpBaris.topMargin = dp(14);
            baris.setLayoutParams(lpBaris);

            // Baris atas: nama (kiri) dan angka (kanan)
            LinearLayout atas = new LinearLayout(this);
            atas.setOrientation(LinearLayout.HORIZONTAL);

            TextView nama = new TextView(this);
            nama.setText(KATEGORI[n]);
            nama.setTextSize(15);
            nama.setTextColor(Color.parseColor("#444853"));
            nama.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView angka = new TextView(this);
            angka.setText(String.valueOf(JUMLAH_KATEGORI[n]));
            angka.setTextSize(15);
            angka.setTextColor(TEAL);
            angka.setTypeface(null, android.graphics.Typeface.BOLD);

            atas.addView(nama);
            atas.addView(angka);

            // Batang: jalur abu-hijau muda, isi hijau sepanjang proporsi
            LinearLayout jalur = new LinearLayout(this);
            jalur.setWeightSum(maks);
            LinearLayout.LayoutParams lpJalur = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
            lpJalur.topMargin = dp(6);
            jalur.setLayoutParams(lpJalur);
            GradientDrawable bgJalur = new GradientDrawable();
            bgJalur.setCornerRadius(dp(4));
            bgJalur.setColor(Color.parseColor("#D9EAE8"));
            jalur.setBackground(bgJalur);

            android.view.View isi = new android.view.View(this);
            isi.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, JUMLAH_KATEGORI[n]));
            GradientDrawable bgIsi = new GradientDrawable();
            bgIsi.setCornerRadius(dp(4));
            bgIsi.setColor(TEAL);
            isi.setBackground(bgIsi);
            jalur.addView(isi);

            baris.addView(atas);
            baris.addView(jalur);
            box.addView(baris);
        }
    }

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }
}
