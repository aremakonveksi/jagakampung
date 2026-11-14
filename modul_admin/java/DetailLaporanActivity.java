package com.jagakampung.demo.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.jagakampung.demo.R;

public class DetailLaporanActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_laporan);

        Intent i = getIntent();
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
        chip.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        chip.setColor(Color.argb(38, Color.red(warna), Color.green(warna), Color.blue(warna)));
        tvStatus.setBackground(chip);

        // Foto: teks Base64 diubah kembali menjadi gambar
        tampilkanFoto(i.getStringExtra("foto"));

        findViewById(R.id.tvKembali).setOnClickListener(v -> finish());
    }

    private void tampilkanFoto(String base64) {
        ImageView ivFoto = findViewById(R.id.ivFoto);
        TextView tvKosong = findViewById(R.id.tvFotoKosong);

        if (base64 == null || base64.isEmpty()) {
            tvKosong.setVisibility(android.view.View.VISIBLE);
            return;
        }
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp == null) throw new IllegalArgumentException("bukan gambar");
            ivFoto.setImageBitmap(bmp);
            ivFoto.setVisibility(android.view.View.VISIBLE);
        } catch (Exception e) {
            tvKosong.setText("Foto gagal dimuat");
            tvKosong.setVisibility(android.view.View.VISIBLE);
        }
    }

    private int warnaStatus(String status) {
        switch (status) {
            case "ditinjau":  return Color.parseColor("#F2B233");
            case "diproses":  return Color.parseColor("#3B82F6");
            case "selesai":   return Color.parseColor("#2E9E5B");
            default:          return Color.parseColor("#9E9E9E");
        }
    }
}
