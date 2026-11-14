package com.jagakampung.demo.admin;

import android.app.Activity;
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

    // Satu data laporan (field sama dengan di Firebase)
    static class Laporan {
        String id, judul, kategori, status;

        Laporan(String id, String judul, String kategori, String status) {
            this.id = id;
            this.judul = judul;
            this.kategori = kategori;
            this.status = status;
        }
    }

    private final List<Laporan> daftar = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_laporan);

        // DATA CONTOH. Nanti bagian ini diganti dengan data dari Firebase.
        daftar.add(new Laporan("1", "Lampu jalan mati", "Infrastruktur", "dilaporkan"));
        daftar.add(new Laporan("2", "Sampah menumpuk di gang 3", "Kebersihan", "ditinjau"));
        daftar.add(new Laporan("3", "Selokan tersumbat", "Infrastruktur", "diproses"));
        daftar.add(new Laporan("4", "Pos ronda rusak", "Keamanan", "selesai"));

        ListView list = findViewById(R.id.listLaporan);
        list.setAdapter(new LaporanAdapter());
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
