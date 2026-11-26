package com.jagakampung.app.warga;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jagakampung.app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailLaporan extends AppCompatActivity {

    private TextView tvJudul, tvStatus, tvInfo, tvDeskripsi, tvLokasi, tvFotoKosong;
    private TextView tvTanggapanKosong;
    private LinearLayout boxTanggapan;
    private ImageView ivFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warga_detail_laporan);

        tvJudul = findViewById(R.id.tvJudul);
        tvStatus = findViewById(R.id.tvStatus);
        tvInfo = findViewById(R.id.tvInfo);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvLokasi = findViewById(R.id.tvLokasi);
        tvFotoKosong = findViewById(R.id.tvFotoKosong);
        tvTanggapanKosong = findViewById(R.id.tvTanggapanKosong);
        boxTanggapan = findViewById(R.id.boxTanggapan);
        ivFoto = findViewById(R.id.ivFoto);
        Button btnKembali = findViewById(R.id.btnKembali);
        btnKembali.setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("laporanId");
        if (id == null) {
            Toast.makeText(this, "Laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        // 1. Data teks laporan
        root.child("laporan").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Laporan l = snapshot.getValue(Laporan.class);
                if (l == null) {
                    Toast.makeText(DetailLaporan.this, "Data kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                tvJudul.setText(l.judul);
                LaporanUtil.terapkanStatus(tvStatus, l.status);
                tvInfo.setText((l.kategori == null ? "-" : l.kategori)
                        + " • " + LaporanUtil.formatTanggal(l.dibuatPada));
                tvDeskripsi.setText(l.deskripsi);

                StringBuilder lokasi = new StringBuilder();
                if (l.alamatTeks != null && !l.alamatTeks.isEmpty()) lokasi.append(l.alamatTeks);
                if (l.lat != null && l.lng != null) {
                    if (lokasi.length() > 0) lokasi.append("\n");
                    lokasi.append(String.format("Koordinat: %.5f, %.5f", l.lat, l.lng));
                }
                tvLokasi.setText(lokasi.length() == 0 ? "Lokasi tidak dicantumkan" : lokasi.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailLaporan.this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 2. Foto (Base64 -> gambar)
        root.child("laporanFoto").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64 = snapshot.getValue(String.class);
                if (base64 == null || base64.isEmpty()) {
                    tvFotoKosong.setVisibility(View.VISIBLE);
                    return;
                }
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ivFoto.setImageBitmap(bmp);
                    ivFoto.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    tvFotoKosong.setText("Foto gagal ditampilkan");
                    tvFotoKosong.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvFotoKosong.setText("Foto tidak bisa dimuat");
                tvFotoKosong.setVisibility(View.VISIBLE);
            }
        });

        // 3. Tanggapan admin (rules: warga approved boleh membaca tanggapan)
        root.child("tanggapan").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tampilkanTanggapan(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvTanggapanKosong.setText("Tanggapan tidak bisa dimuat: " + error.getMessage());
                        tvTanggapanKosong.setVisibility(View.VISIBLE);
                    }
                });
    }

    /** Satu tanggapan admin pada satu laporan. */
    private static final class Item {
        String teks;
        String statusBaru;
        long waktu;
    }

    private void tampilkanTanggapan(DataSnapshot snapshot) {
        List<Item> daftar = new ArrayList<>();
        for (DataSnapshot anak : snapshot.getChildren()) {
            Item it = new Item();
            it.teks = anak.child("teks").getValue(String.class);
            it.statusBaru = anak.child("statusBaru").getValue(String.class);
            Object w = anak.child("dibuatPada").getValue();
            it.waktu = (w instanceof Number) ? ((Number) w).longValue() : 0L;
            if (it.teks != null && !it.teks.isEmpty()) daftar.add(it);
        }

        boxTanggapan.removeAllViews();

        if (daftar.isEmpty()) {
            tvTanggapanKosong.setVisibility(View.VISIBLE);
            return;
        }
        tvTanggapanKosong.setVisibility(View.GONE);

        // Terlama di atas, terbaru di bawah (urutan percakapan)
        Collections.sort(daftar, (a, b) -> Long.compare(a.waktu, b.waktu));

        for (Item it : daftar) {
            TextView kartu = new TextView(this);
            String waktu = LaporanUtil.formatTanggal(it.waktu);
            String kepala = it.statusBaru == null || it.statusBaru.isEmpty()
                    ? waktu
                    : labelStatus(it.statusBaru) + " • " + waktu;
            kartu.setText(it.teks + "\n\n" + kepala);
            kartu.setTextSize(14);
            kartu.setTextColor(getResources().getColor(R.color.lp_text, getTheme()));
            kartu.setBackgroundResource(R.drawable.lp_bg_card);
            int p = dp(14);
            kartu.setPadding(p, p, p, p);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(10);
            kartu.setLayoutParams(lp);

            boxTanggapan.addView(kartu);
        }
    }

    private String labelStatus(String status) {
        String s = status.toLowerCase(java.util.Locale.ROOT);
        return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
    }

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }
}