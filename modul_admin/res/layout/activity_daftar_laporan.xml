package com.jagakampung.demo.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jagakampung.demo.R;

import java.util.ArrayList;
import java.util.List;

public class DaftarLaporanActivity extends Activity {

    // Foto contoh (gambar kecil dalam bentuk teks Base64)
    private static final String FOTO_CONTOH =
            "iVBORw0KGgoAAAANSUhEUgAAAKAAAABkCAIAAACO1KzYAAAA1klEQVR42u3RSw3CUBQA0SehsvBy95Q2fBywwTA2mslJRsGctd1H4ZYFgAVYgAVYgAVYgAGrA7yPwgEGLMACLMACLMACDFgh4McoHGDAAizAAizAAizAgBUCPkbhAAMWYAEWYAEWYAEGrBDwOQq3br+vwgEGLMACLMACLMACDFiABViABViABRiwAAuwLge8PUfhAAMWYAEWYAEWYAEGrBDwaxQOMGABFmABFmABFmDACgG/R+EAAxZgARZgARZgAQasEPBnFA4wYAEWYAEWYAEWYMDK9AfjKE7QIxskVQAAAABJRU5ErkJggg==";

    // Satu data laporan (field sama dengan di Firebase)
    static class Laporan {
        String id, judul, kategori, status, deskripsi, alamat, pelapor, fotoBase64;

        Laporan(String id, String judul, String kategori, String status,
                String deskripsi, String alamat, String pelapor, String fotoBase64) {
            this.id = id;
            this.judul = judul;
            this.kategori = kategori;
            this.status = status;
            this.deskripsi = deskripsi;
            this.alamat = alamat;
            this.pelapor = pelapor;
            this.fotoBase64 = fotoBase64;
        }
    }

    private final List<Laporan> daftar = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_laporan);

        // DATA CONTOH. Nanti bagian ini diganti dengan data dari Firebase.
        daftar.add(new Laporan("1", "Lampu jalan mati", "Infrastruktur", "dilaporkan",
                "Lampu jalan di depan rumah nomor 12 sudah mati tiga hari, jalan jadi gelap.",
                "Gang 3, RT 02 / RW 05", "Budi Santoso", FOTO_CONTOH));
        daftar.add(new Laporan("2", "Sampah menumpuk di gang 3", "Kebersihan", "ditinjau",
                "Sampah belum diangkut seminggu dan mulai berbau.",
                "Gang 3, RT 02 / RW 05", "Siti Aminah", ""));
        daftar.add(new Laporan("3", "Selokan tersumbat", "Infrastruktur", "diproses",
                "Selokan di samping pos ronda tersumbat, air meluap saat hujan.",
                "Dekat pos ronda, RT 03 / RW 05", "Agus Prasetyo", ""));
        daftar.add(new Laporan("4", "Pos ronda rusak", "Keamanan", "selesai",
                "Atap pos ronda bocor dan papan lantainya lapuk.",
                "Pos ronda RT 03", "Dewi Lestari", ""));

        ListView list = findViewById(R.id.listLaporan);
        list.setAdapter(new LaporanAdapter());

        // Ketuk kartu -> buka halaman detail
        list.setOnItemClickListener((parent, view, position, id) -> {
            Laporan l = daftar.get(position);
            Intent i = new Intent(DaftarLaporanActivity.this, DetailLaporanActivity.class);
            i.putExtra("id", l.id);
            i.putExtra("judul", l.judul);
            i.putExtra("kategori", l.kategori);
            i.putExtra("status", l.status);
            i.putExtra("deskripsi", l.deskripsi);
            i.putExtra("alamat", l.alamat);
            i.putExtra("pelapor", l.pelapor);
            i.putExtra("foto", l.fotoBase64);
            startActivity(i);
        });
    }

    // Warna status sesuai desain Rizal (colors.xml Gaya A)
    private int warnaStatus(String status) {
        switch (status) {
            case "ditinjau":  return Color.parseColor("#F2B233");
            case "diproses":  return Color.parseColor("#3B82F6");
            case "selesai":   return Color.parseColor("#2E9E5B");
            default:          return Color.parseColor("#9E9E9E"); // dilaporkan
        }
    }

    // "ditinjau" -> "Ditinjau"
    private String namaStatus(String status) {
        return status.substring(0, 1).toUpperCase() + status.substring(1);
    }

    // Adapter: mengisi satu baris di daftar dengan satu laporan
    private class LaporanAdapter extends BaseAdapter {
        @Override public int getCount() { return daftar.size(); }
        @Override public Object getItem(int i) { return daftar.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DaftarLaporanActivity.this)
                        .inflate(R.layout.item_laporan, parent, false);
            }
            Laporan l = daftar.get(position);
            ((TextView) convertView.findViewById(R.id.tvJudul)).setText(l.judul);
            ((TextView) convertView.findViewById(R.id.tvKategori)).setText(l.kategori);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            tvStatus.setText(namaStatus(l.status));
            int warna = warnaStatus(l.status);
            tvStatus.setTextColor(warna);
            // Latar label = warna status versi pudar (alpha ~15%)
            GradientDrawable chip = new GradientDrawable();
            chip.setCornerRadius(8 * getResources().getDisplayMetrics().density);
            chip.setColor(Color.argb(38, Color.red(warna), Color.green(warna), Color.blue(warna)));
            tvStatus.setBackground(chip);
            return convertView;
        }
    }
}
