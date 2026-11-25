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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jagakampung.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DaftarLaporanActivity extends Activity {

    // Satu laporan (field sama dengan node "laporan" di Firebase)
    static class Laporan {
        String id, judul, kategori, status, deskripsi, alamat, pelapor;
        long dibuatPada;
    }

    // Semua laporan, dan laporan yang tampil setelah difilter
    private final List<Laporan> semua = new ArrayList<>();
    private final List<Laporan> tampil = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private String filterAktif = "semua";
    private LaporanAdapter adapter;

    private DatabaseReference laporanRef;
    private ValueEventListener listener;
    private boolean sudahMemuat = false;
    private String pesanError = null;

    private static final String[] KODE_FILTER = {"semua", "dilaporkan", "ditinjau", "diproses", "selesai"};
    private static final String[] LABEL_FILTER = {"Semua", "Dilaporkan", "Ditinjau", "Diproses", "Selesai"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_laporan);

        laporanRef = FirebaseDatabase.getInstance().getReference("laporan");

        ListView list = findViewById(R.id.listLaporan);
        adapter = new LaporanAdapter();
        list.setAdapter(adapter);

        buatFilter();
        terapkanFilter();

        // Ketuk kartu -> buka halaman detail (foto dimuat di sana)
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
            startActivity(i);
        });
    }

    // ===== MEMBACA DATA DARI FIREBASE =====

    @Override
    protected void onStart() {
        super.onStart();
        // Dipanggil lagi setiap data "laporan" berubah, jadi status yang baru
        // diubah di halaman detail langsung terlihat di sini.
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                semua.clear();
                for (DataSnapshot anak : snapshot.getChildren()) {
                    Laporan l = new Laporan();
                    l.id = anak.getKey();
                    l.judul = teks(anak, "judul", "(tanpa judul)");
                    l.kategori = teks(anak, "kategori", "-");
                    l.status = teks(anak, "status", "dilaporkan");
                    l.deskripsi = teks(anak, "deskripsi", "-");
                    l.alamat = teks(anak, "alamatTeks", "-");
                    l.pelapor = teks(anak, "namaPelapor", "-");
                    Object waktu = anak.child("dibuatPada").getValue();
                    l.dibuatPada = (waktu instanceof Number) ? ((Number) waktu).longValue() : 0L;
                    semua.add(l);
                }
                // Terbaru di atas
                Collections.sort(semua, (a, b) -> Long.compare(b.dibuatPada, a.dibuatPada));
                sudahMemuat = true;
                pesanError = null;
                terapkanFilter();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                pesanError = error.getMessage();
                terapkanFilter();
            }
        };
        laporanRef.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) laporanRef.removeEventListener(listener);
    }

    private String teks(DataSnapshot induk, String kunci, String bawaan) {
        String nilai = induk.child(kunci).getValue(String.class);
        return (nilai == null || nilai.isEmpty()) ? bawaan : nilai;
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

        // Teks keadaan: memuat / gagal / kosong
        TextView tvKosong = findViewById(R.id.tvKosong);
        if (pesanError != null) {
            tvKosong.setText("Gagal memuat: " + pesanError);
            tvKosong.setVisibility(View.VISIBLE);
        } else if (!sudahMemuat) {
            tvKosong.setText("Memuat laporan...");
            tvKosong.setVisibility(View.VISIBLE);
        } else if (tampil.isEmpty()) {
            tvKosong.setText(semua.isEmpty()
                    ? "Belum ada laporan masuk"
                    : "Tidak ada laporan dengan status ini");
            tvKosong.setVisibility(View.VISIBLE);
        } else {
            tvKosong.setVisibility(View.GONE);
        }
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
            ((TextView) convertView.findViewById(R.id.tvKategori))
                    .setText(l.kategori + "  •  " + l.pelapor);
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
