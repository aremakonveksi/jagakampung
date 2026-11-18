package com.jagakampung.app.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jagakampung.app.R;

import java.util.ArrayList;
import java.util.List;

public class DaftarLaporanActivity extends Activity {

    private static final int REQ_DETAIL = 1;

    // Foto contoh (gambar kecil dalam bentuk teks Base64)
    private static final String FOTO_CONTOH =
            "iVBORw0KGgoAAAANSUhEUgAAAKAAAABkCAIAAACO1KzYAAAA1klEQVR42u3RSw3CUBQA0SehsvBy95Q2fBywwTA2mslJRsGctd1H4ZYFgAVYgAVYgAVYgAGrA7yPwgEGLMACLMACLMACDFgh4McoHGDAAizAAizAAizAgBUCPkbhAAMWYAEWYAEWYAEGrBDwOQq3br+vwgEGLMACLMACLMACDFiABViABViABRiwAAuwLge8PUfhAAMWYAEWYAEWYAEGrBDwaxQOMGABFmABFmABFmDACgG/R+EAAxZgARZgARZgAQasEPBnFA4wYAEWYAEWYAEWYMDK9AfjKE7QIxskVQAAAABJRU5ErkJggg==";

    // Satu data laporan (field sama dengan di Firebase)
    static class Laporan {
        String id, judul, kategori, status, deskripsi, alamat, pelapor, fotoBase64;
        String tanggapan = "";

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

    // Semua laporan, dan laporan yang tampil setelah difilter
    private final List<Laporan> semua = new ArrayList<>();
    private final List<Laporan> tampil = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private String filterAktif = "semua";
    private LaporanAdapter adapter;

    private static final String[] KODE_FILTER = {"semua", "dilaporkan", "ditinjau", "diproses", "selesai"};
    private static final String[] LABEL_FILTER = {"Semua", "Dilaporkan", "Ditinjau", "Diproses", "Selesai"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_laporan);

        // DATA CONTOH. Nanti bagian ini diganti dengan data dari Firebase.
        semua.add(new Laporan("1", "Lampu jalan mati", "Infrastruktur", "dilaporkan",
                "Lampu jalan di depan rumah nomor 12 sudah mati tiga hari, jalan jadi gelap.",
                "Gang 3, RT 02 / RW 05", "Budi Santoso", FOTO_CONTOH));
        semua.add(new Laporan("2", "Sampah menumpuk di gang 3", "Kebersihan", "ditinjau",
                "Sampah belum diangkut seminggu dan mulai berbau.",
                "Gang 3, RT 02 / RW 05", "Siti Aminah", ""));
        semua.add(new Laporan("3", "Selokan tersumbat", "Infrastruktur", "diproses",
                "Selokan di samping pos ronda tersumbat, air meluap saat hujan.",
                "Dekat pos ronda, RT 03 / RW 05", "Agus Prasetyo", ""));
        semua.add(new Laporan("4", "Pos ronda rusak", "Keamanan", "selesai",
                "Atap pos ronda bocor dan papan lantainya lapuk.",
                "Pos ronda RT 03", "Dewi Lestari", ""));
        semua.add(new Laporan("5", "Jalan berlubang depan balai RT", "Infrastruktur", "dilaporkan",
                "Lubang cukup dalam, berbahaya bagi pengendara motor saat malam.",
                "Depan balai RT 03", "Rudi Hartono", ""));

        ListView list = findViewById(R.id.listLaporan);
        adapter = new LaporanAdapter();
        list.setAdapter(adapter);

        buatFilter();
        terapkanFilter();

        // Ketuk kartu -> buka halaman detail
        list.setOnItemClickListener((parent, view, position, id) -> {
            Laporan l = tampil.get(position);
            Intent i = new Intent(DaftarLaporanActivity.this, DetailLaporanActivity.class);
            i.putExtra("id", l.id);
            i.putExtra("judul", l.judul);
            i.putExtra("kategori", l.kategori);
            i.putExtra("status", l.status);
            i.putExtra("deskripsi", l.deskripsi);
            i.putExtra("alamat", l.alamat);
            i.putExtra("pelapor", l.pelapor);
            i.putExtra("foto", l.fotoBase64);
            i.putExtra("tanggapan", l.tanggapan);
            startActivityForResult(i, REQ_DETAIL);
        });
    }

    // Menerima hasil dari halaman detail (status baru + tanggapan)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DETAIL && resultCode == RESULT_OK && data != null) {
            String id = data.getStringExtra("id");
            for (Laporan l : semua) {
                if (l.id.equals(id)) {
                    l.status = data.getStringExtra("status");
                    l.tanggapan = data.getStringExtra("tanggapan");
                }
            }
            terapkanFilter();
        }
    }

    // ===== FILTER STATUS =====

    private void buatFilter() {
        LinearLayout bar = findViewById(R.id.filterBar);
        for (int n = 0; n < KODE_FILTER.length; n++) {
            final String kode = KODE_FILTER[n];
            TextView chip = new TextView(this);
            chip.setTag(kode);
            chip.setTextSize(14);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                filterAktif = kode;
                terapkanFilter();
            });
            bar.addView(chip);
            chips.add(chip);
        }
    }

    private void terapkanFilter() {
        tampil.clear();
        for (Laporan l : semua) {
            if (filterAktif.equals("semua") || l.status.equals(filterAktif)) {
                tampil.add(l);
            }
        }
        adapter.notifyDataSetChanged();
        findViewById(R.id.tvKosong).setVisibility(tampil.isEmpty() ? View.VISIBLE : View.GONE);
        gayaChips();
    }

    private int hitung(String kode) {
        if (kode.equals("semua")) return semua.size();
        int jumlah = 0;
        for (Laporan l : semua) if (l.status.equals(kode)) jumlah++;
        return jumlah;
    }

    private void gayaChips() {
        for (int n = 0; n < chips.size(); n++) {
            TextView chip = chips.get(n);
            String kode = KODE_FILTER[n];
            boolean aktif = kode.equals(filterAktif);
            chip.setText(LABEL_FILTER[n] + " (" + hitung(kode) + ")");
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(20));
            if (aktif) {
                g.setColor(Color.parseColor("#10766B"));
                chip.setTextColor(Color.WHITE);
            } else {
                g.setColor(Color.WHITE);
                g.setStroke(dp(1), Color.parseColor("#E8EDF3"));
                chip.setTextColor(Color.parseColor("#4E525D"));
            }
            chip.setBackground(g);
        }
    }

    private int dp(int nilai) {
        return Math.round(nilai * getResources().getDisplayMetrics().density);
    }

    // ===== WARNA & NAMA STATUS =====

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
        @Override public int getCount() { return tampil.size(); }
        @Override public Object getItem(int i) { return tampil.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DaftarLaporanActivity.this)
                        .inflate(R.layout.item_laporan, parent, false);
            }
            Laporan l = tampil.get(position);
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
