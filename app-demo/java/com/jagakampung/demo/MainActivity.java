package com.jagakampung.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

public class MainActivity extends Activity {
    static final int TEAL = 0xFF10766B, TEAL_DARK = 0xFF0E766D, TEAL_SOFT = 0xFF48938E,
            TEAL_LIGHT = 0xFFD9EAE8, ACCENT = 0xFFFBB315, BG = 0xFFF4F7FC, WHITE = 0xFFFFFFFF,
            TXT = 0xFF444853, TXT2 = 0xFF4E525D, DIV = 0xFFE8EDF3;

    LinearLayout root;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        w.setStatusBarColor(TEAL_DARK);
        w.setNavigationBarColor(WHITE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        setContentView(root);
        tampilkanSplash();
    }

    void tampilkanSplash() {
        root.removeAllViews();
        root.setGravity(Gravity.CENTER);
        TextView judul = tv("Jaga Kampung", 30, true, TEAL);
        judul.setGravity(Gravity.CENTER);
        TextView tag = tv("Suarakan lingkunganmu", 15, false, TXT2);
        tag.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(-2, -2);
        p1.setMargins(0, dp(24), 0, dp(6));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(-2, -2);

        LinearLayout barisLogo = new LinearLayout(this);
        barisLogo.setGravity(Gravity.CENTER);
        View ikon = new View(this) {
            public void draw(android.graphics.Canvas c) {
                android.graphics.Paint pa = new android.graphics.Paint();
                pa.setAntiAlias(true); pa.setColor(TEAL);
                float cx = getWidth()/2f, cy = getHeight()/2f, r = getWidth()/2f - 2;
                c.drawCircle(cx, cy, r, pa);
                pa.setColor(WHITE); c.drawCircle(cx, cy, r*0.72f, pa);
                pa.setColor(ACCENT);
                android.graphics.Path st = new android.graphics.Path();
                st.moveTo(cx, cy - r*0.38f);
                st.lineTo(cx + r*0.11f, cy - r*0.12f);
                st.lineTo(cx + r*0.36f, cy - r*0.1f);
                st.lineTo(cx + r*0.16f, cy + r*0.05f);
                st.lineTo(cx + r*0.22f, cy + r*0.32f);
                st.lineTo(cx, cy + r*0.17f);
                st.lineTo(cx - r*0.22f, cy + r*0.32f);
                st.lineTo(cx - r*0.16f, cy + r*0.05f);
                st.lineTo(cx - r*0.36f, cy - r*0.1f);
                st.lineTo(cx - r*0.11f, cy - r*0.12f);
                st.close();
                c.drawPath(st, pa);
            }
        };
        ikon.setBackground(null);
        barisLogo.addView(ikon, dp(96), dp(96));
        root.addView(barisLogo, p1);
        root.addView(judul, p1);
        root.addView(tag, p2);

        TextView lanjut = tombolUtama("Lanjut ke Masuk");
        lanjut.setOnClickListener(v -> tampilkanLogin());
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(-1, -2);
        p3.setMargins(0, dp(36), 0, 0);
        root.addView(lanjut, p3);
    }

    void tampilkanLogin() {
        root.removeAllViews();
        root.setGravity(Gravity.START);

        TextView head = tv("Masuk", 26, true, TEAL);
        TextView sub = tv("Masuk untuk melapor lingkungan RT-mu", 14, false, TXT2);
        LinearLayout.LayoutParams ph = new LinearLayout.LayoutParams(-2, -2);
        ph.setMargins(0, dp(20), 0, dp(4));

        final EditText eEmail = input("Email");
        final EditText eSandi = input("Kata sandi");
        Button masuk = tombolUtama("Masuk");
        TextView daftar = tv("Belum punya akun? Daftar sekarang", 13, false, TEAL_SOFT);
        daftar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(-1, -2);
        pm.setMargins(0, dp(18), 0, 0);
        LinearLayout.LayoutParams pd = new LinearLayout.LayoutParams(-1, -2);
        pd.setMargins(0, dp(20), 0, 0);

        masuk.setOnClickListener(v -> {
            String mail = eEmail.getText().toString().trim();
            if (mail.length() == 0) { eEmail.setError("Email wajib diisi"); return; }
            tampilkanHome(mail);
        });
        daftar.setOnClickListener(v -> {
            daftar.setText("Pendaftaran dibuka di build berikutnya (perlu Firebase Auth)");
        });

        root.addView(head, ph);
        root.addView(sub, ph);
        root.addView(eEmail, pm);
        root.addView(eSandi, pm);
        root.addView(masuk, pm);
        root.addView(daftar, pd);
    }

    void tampilkanHome(String mail) {
        root.removeAllViews();
        root.setGravity(Gravity.START);

        TextView h = tv("Selamat datang", 24, true, TEAL);
        TextView s = tv("Berikut laporan lingkungan RT-mu", 14, false, TXT2);
        LinearLayout.LayoutParams ph = new LinearLayout.LayoutParams(-2, -2);
        ph.setMargins(0, dp(18), 0, dp(2));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(-1, -2);
        pl.setMargins(0, dp(12), 0, 0);
        list.addView(kartuLaporan("Lampu jalan mati di Gang 3", "Ditinjau", 0xFFF2B233), pl);
        list.addView(kartuLaporan("Saluran drainase tersumbat", "Diproses", 0xFF3B82F6), pl);
        list.addView(kartuLaporan("Sampah menumpuk di taman", "Selesai", 0xFF2E9E5B), pl);
        list.addView(kartuLaporan("Motor parkir menghalangi jalan", "Dilaporkan", 0xFF9E9E9E), pl);

        TextView loginInfo = tv("Login sebagai: " + mail, 12, false, TXT2);
        LinearLayout.LayoutParams pi = new LinearLayout.LayoutParams(-2, -2);
        pi.setMargins(0, dp(16), 0, 0);

        root.addView(h, ph);
        root.addView(s, ph);
        root.addView(list, pl);
        root.addView(loginInfo, pi);

        Button fab = tombolUtama("+  Buat laporan");
        LinearLayout.LayoutParams pf = new LinearLayout.LayoutParams(-1, -2);
        pf.setMargins(0, dp(20), 0, 0);
        fab.setOnClickListener(v -> {
            h.setText("Form laporan (dibangun di build berikutnya)");
        });
        root.addView(fab, pf);
    }

    LinearLayout kartuLaporan(String judul, String status, int warna) {
        LinearLayout kartu = new LinearLayout(this);
        kartu.setOrientation(LinearLayout.VERTICAL);
        kartu.setBackgroundColor(WHITE);
        kartu.setPadding(dp(16), dp(14), dp(16), dp(14));
        kartu.setElevation(dp(2));
        kartu.setBackground(kartuBulat());

        TextView t = tv(judul, 15, true, TXT);
        TextView chip = tv(status, 12, true, warna);
        chip.setBackgroundColor(0x1A000000 | (warna & 0xFFFFFF));
        chip.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams pc = new LinearLayout.LayoutParams(-2, -2);
        pc.setMargins(0, dp(8), 0, 0);
        kartu.addView(t);
        kartu.addView(chip, pc);
        return kartu;
    }

    android.graphics.drawable.GradientDrawable kartuBulat() {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(WHITE);
        g.setCornerRadius(dp(16));
        return g;
    }

    TextView tv(String teks, float ukuran, boolean tebal, int warna) {
        TextView t = new TextView(this);
        t.setText(teks);
        t.setTextSize(ukuran);
        t.setTypeface(null, tebal ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        t.setTextColor(warna);
        return t;
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(TXT);
        e.setHintTextColor(0xFF9AA0A6);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.setBackgroundColor(WHITE);
        e.getBackground().setTint(DIV);
        return e;
    }

    Button tombolUtama(String teks) {
        Button b = new Button(this);
        b.setText(teks);
        b.setTextColor(WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackgroundColor(TEAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(TEAL);
        bg.setCornerRadius(dp(12));
        b.setBackground(bg);
        b.setElevation(dp(2));
        b.setPadding(0, dp(14), 0, dp(14));
        return b;
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
