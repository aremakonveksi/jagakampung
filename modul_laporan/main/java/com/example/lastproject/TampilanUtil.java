package com.example.lastproject;

import android.app.Activity;
import android.graphics.Color;
import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** LAYAR UJI: membuat status bar & navigation bar transparan (jangan diupload). */
public class TampilanUtil {

    public static void layarPenuh(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat kontrol =
                WindowCompat.getInsetsController(window, window.getDecorView());
        kontrol.setAppearanceLightStatusBars(false);      // ikon status bar putih
        kontrol.setAppearanceLightNavigationBars(false);  // ikon tombol navigasi putih
    }
}