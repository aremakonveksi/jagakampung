package com.jagakampung.app.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jagakampung.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailLaporanActivity extends Activity {

    // Status yang boleh dipilih admin (sesuai aturan database untuk tanggapan)
    private static final String[] KODE_STATUS = {"ditinjau", "diproses", "selesai", "ditolak"};

    private String idLaporan;
    private String statusPilihan = null;
    private final List<TextView> chipStatus = new ArrayList<>();
    private Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_laporan);

        Intent i = getIntent();
        idLaporan = i.getStringExtra("id");
        String status = i.getStringExtra("status");
        if (status == null || status.isEmpty()) status = "dilaporkan";

        ((TextView) findViewById(R.id.tvJudul)).setText(i.getStringExtra("judul"));
        ((TextView) findViewById(R.id.tvDeskripsi)).setText(i.getStringExtra("deskripsi"));
        ((TextView) findViewById(R.id.tvKategori)).setText(i.getStringExtra("kategori"));
        ((TextView) findViewById(R.id.tvAlamat)).setText(i.getStringExtra("alamat"));
        ((TextView) findViewById(R.id.tvPelapor)).setText(i.getStringExtra("pelapor"));

        // Label status berwarna
        TextView tvStatus = findViewById(R.id.tvStatus);
        tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        int warna = warnaStatus(status);
        tvStatus.setTextColor(warna);
        GradientDrawable chip = new GradientDrawable();
        chip.setCornerRadius(dp(8));
        chip.setColor(Color.argb(38, Color.red(warna), Color.green(warna), Color.blue(warna)));
        tvStatus.setBackground(chip);

        // Foto dimuat dari Firebase (laporanFoto/{id})
        muatFoto();

        // Pilihan status baru
        for (String k : KODE_STATUS) {
            if (k.equals(status)) statusPilihan = k;
        }
        buatPilihanStatus();

        // Tombol simpan
        btnSimpan = findViewById(R.id.btnSimpan);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(Color.parseColor("#10766B"));
        btnSimpan.setBackground(bg);
        btnSimpan.setOnClickListener(v -> simpan());

        findViewById(R.id.tvKembali).setOnClickListener(v -> finish());
    }

    // ===== UBAH STATUS =====

    private void buatPilihanStatus() {
        LinearLayout bar = findViewById(R.id.pilihanStatusBar);
        for (String kode : KODE_STATUS) {
            TextView chip = new TextView(this);
            chip.setTag(kode);
            chip.setText(kode.substring(0, 1).toUpperCase() + kode.substring(1));
            chip.setTextSize(14);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                statusPilihan = (String) v.getTag();
                gayaPilihanStatus();
            });
            bar.addView(chip);
            chipStatus.add(chip);
        }
        gayaPilihanStatus();
    }

    private void gayaPilihanStatus() {
        for (TextView chip : chipStatus) {
            String kode = (String) chip.getTag();
            int warna = warnaStatus(kode);
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(20));
            if (kode.equals(statusPilihan)) {
                g.setColor(warna);
                chip.setTextColor(Color.WHITE);
            } else {
                g.setColor(Color.WHITE);
                g.setStroke(dp(1), warna);
                chip.setTextColor(warna);
            }
            chip.setBackground(g);
        }
    }

    // ===== SIMPAN KE FIREBASE =====

    private void simpan() {
        EditText etTanggapan = findViewById(R.id.etTanggapan);
        String teks = etTanggapan.getText().toString().trim();

        if (statusPilihan == null) {
            Toast.makeText(this, "Pilih status baru dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (teks.isEmpty()) {
            etTanggapan.setError("Tanggapan wajib diisi");
            return;
        }
        FirebaseUser admin = FirebaseAuth.getInstance().getCurrentUser();
        if (admin == null) {
            Toast.makeText(this, "Sesi login habis, masuk lagi", Toast.LENGTH_LONG).show();
            return;
        }

        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        String idTanggapan = root.child("tanggapan").child(idLaporan).push().getKey();
        long sekarang = System.currentTimeMillis();

        // Satu tanggapan baru (field sama dengan aturan database)
        Map<String, Object> tanggapan = new HashMap<>();
        tanggapan.put("uidAdmin", admin.getUid());
        tanggapan.put("teks", teks);
        tanggapan.put("statusBaru", statusPilihan);
        tanggapan.put("dibuatPada", sekarang);

        // Tiga penulisan dalam satu perintah: berhasil semua atau gagal semua
        Map<String, Object> ubah = new HashMap<>();
        ubah.put("laporan/" + idLaporan + "/status", statusPilihan);
        ubah.put("laporan/" + idLaporan + "/diperbaruiPada", sekarang);
        ubah.put("tanggapan/" + idLaporan + "/" + idTanggapan, tanggapan);

        root.updateChildren(ubah).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show();
                finish();   // daftar laporan otomatis ikut diperbarui
            } else {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan Perubahan");
                Exception e = task.getException();
                Toast.makeText(this, "Gagal: " + (e == null ? "tidak diketahui" : e.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== FOTO (Base64 dari Firebase) =====

    private void muatFoto() {
        final ImageView ivFoto = findViewById(R.id.ivFoto);
        final TextView tvKosong = findViewById(R.id.tvFotoKosong);
        tvKosong.setText("Memuat foto...");
        tvKosong.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("laporanFoto").child(idLaporan)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) return;
                        String base64 = snapshot.getValue(String.class);
                        if (base64 == null || base64.isEmpty()) {
                            tvKosong.setText("Tidak ada foto");
                            return;
                        }
                        try {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bmp == null) throw new IllegalArgumentException("bukan gambar");
                            ivFoto.setImageBitmap(bmp);
                            ivFoto.setVisibility(View.VISIBLE);
                            tvKosong.setVisibility(View.GONE);
                        } catch (Exception e) {
                            tvKosong.setText("Foto gagal dimuat");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (isFinishing() || isDestroyed()) return;
                        tvKosong.setText("Foto gagal dimuat: " + error.getMessage());
                    }
                });
    }

    // ===== BANTUAN =====

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }

    private int warnaStatus(String status) {
        switch (status) {
            case "ditinjau":  return Color.parseColor("#F2B233");
            case "diproses":  return Color.parseColor("#3B82F6");
            case "selesai":   return Color.parseColor("#2E9E5B");
            case "ditolak":   return Color.parseColor("#E5484D");
            default:          return Color.parseColor("#9E9E9E");
        }
    }
}
