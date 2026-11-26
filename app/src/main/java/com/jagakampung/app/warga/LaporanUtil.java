package com.jagakampung.app.warga;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jagakampung.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Fungsi bantu: warna badge status, label kategori & format tanggal. */
public class LaporanUtil {

    public static void terapkanStatus(TextView badge, String status) {
        Context c = badge.getContext();
        String s = status == null ? "dilaporkan" : status.toLowerCase(Locale.ROOT);
        int fg;
        int bg;
        String label;
        switch (s) {
            case "ditinjau":
                fg = R.color.lp_st_ditinjau_fg; bg = R.color.lp_st_ditinjau_bg; label = "Ditinjau"; break;
            case "diproses":
                fg = R.color.lp_st_diproses_fg; bg = R.color.lp_st_diproses_bg; label = "Diproses"; break;
            case "selesai":
                fg = R.color.lp_st_selesai_fg; bg = R.color.lp_st_selesai_bg; label = "Selesai"; break;
            case "ditolak":
                fg = R.color.lp_st_ditolak_fg; bg = R.color.lp_st_ditolak_bg; label = "Ditolak"; break;
            default:
                fg = R.color.lp_st_dilaporkan_fg; bg = R.color.lp_st_dilaporkan_bg; label = "Dilaporkan"; break;
        }
        badge.setText(label);
        badge.setTextColor(ContextCompat.getColor(c, fg));
        badge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, bg)));
    }

    /** Kategori di database huruf kecil ("sosial") -> tampil "Sosial". */
    public static String labelKategori(String kategori) {
        if (kategori == null || kategori.isEmpty()) return "-";
        return kategori.substring(0, 1).toUpperCase(Locale.ROOT)
                + kategori.substring(1).toLowerCase(Locale.ROOT);
    }

    public static String formatTanggal(Long waktu) {
        if (waktu == null) return "-";
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
        return f.format(new Date(waktu));
    }
}