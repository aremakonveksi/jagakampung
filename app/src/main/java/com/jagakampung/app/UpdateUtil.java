package com.jagakampung.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Pemeriksa versi + pengunduh APK update.
 *
 * Sumber: version.json di repo publik kanal update.
 * Format yang diharapkan:
 *   { "versionCode": 3, "versionName": "1.2", "catatan": "...", "apkUrl": "https://..." }
 *
 * Semua jaringan berjalan di thread latar; hasil dikembalikan lewat Handler main.
 */
public final class UpdateUtil {

    /**
     * URL version.json. Kanal update memakai repo kode ini sendiri (publik),
     * jadi version.json + app-release.apk berada di root branch main.
     */
    public static final String URL_VERSION =
            "https://raw.githubusercontent.com/aremakonveksi/jagakampung/main/version.json";

    private static final int TIMEOUT_KONEK = 20_000;
    private static final int TIMEOUT_BACA = 60_000;

    private UpdateUtil() {}

    /** Hasil pemeriksaan versi. */
    public static final class Info {
        public int versionCode;
        public String versionName = "";
        public String catatan = "";
        public String apkUrl = "";
    }

    /** Callback ke UI thread. */
    public interface CekCallback {
        /** Ada versi lebih baru. */
        void onAdaUpdate(Info info);

        /** Sudah versi terbaru. */
        void onTerbaru(String versiTerpasang);

        /** Gagal memeriksa; pesan sudah berisi URL + kode HTTP bila ada. */
        void onGagal(String pesan);
    }

    /** Callback unduh. */
    public interface UnduhCallback {
        void onProgres(int persen);
        void onSelesai(File apk);
        void onGagal(String pesan);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // ============================================================
    // 1. Cek versi
    // ============================================================

    public static void cekVersi(final CekCallback cb) {
        new Thread(() -> {
            HttpURLConnection konek = null;
            try {
                URL url = new URL(URL_VERSION);
                konek = (HttpURLConnection) url.openConnection();
                konek.setConnectTimeout(TIMEOUT_KONEK);
                konek.setReadTimeout(TIMEOUT_BACA);
                konek.setInstanceFollowRedirects(true);
                konek.setRequestProperty("User-Agent", "JagaKampung-Android");
                konek.setRequestProperty("Cache-Control", "no-cache");

                int kode = konek.getResponseCode();
                if (kode != HttpURLConnection.HTTP_OK) {
                    gagal(cb, "Server versi menjawab HTTP " + kode + "\n" + URL_VERSION);
                    return;
                }

                String isi = bacaSemua(konek.getInputStream());
                JSONObject json = new JSONObject(isi);

                Info info = new Info();
                info.versionCode = json.optInt("versionCode", 0);
                info.versionName = json.optString("versionName", "");
                info.catatan = json.optString("catatan", "");
                info.apkUrl = json.optString("apkUrl", "");

                if (info.versionCode <= 0 || info.apkUrl.isEmpty()) {
                    gagal(cb, "version.json tidak lengkap (versionCode/apkUrl kosong)\n" + URL_VERSION);
                    return;
                }

                final int terpasang = BuildConfig.VERSION_CODE;
                MAIN.post(() -> {
                    if (info.versionCode > terpasang) cb.onAdaUpdate(info);
                    else cb.onTerbaru(BuildConfig.VERSION_NAME);
                });
            } catch (Exception e) {
                gagal(cb, "Gagal memeriksa versi: " + e.getClass().getSimpleName()
                        + (e.getMessage() == null ? "" : " — " + e.getMessage())
                        + "\n" + URL_VERSION);
            } finally {
                if (konek != null) konek.disconnect();
            }
        }, "cek-versi").start();
    }

    private static void gagal(CekCallback cb, String pesan) {
        MAIN.post(() -> cb.onGagal(pesan));
    }

    // ============================================================
    // 2. Unduh APK
    // ============================================================

    public static void unduhApk(final Context ctx, final String apkUrl, final UnduhCallback cb) {
        final File tujuan = new File(new File(ctx.getCacheDir(), "update"), "jagakampung-baru.apk");
        new Thread(() -> {
            HttpURLConnection konek = null;
            InputStream masuk = null;
            FileOutputStream keluar = null;
            File sementara = new File(tujuan.getParentFile(), tujuan.getName() + ".part");
            try {
                File induk = tujuan.getParentFile();
                if (induk != null && !induk.exists() && !induk.mkdirs()) {
                    cbGagal(cb, "Tidak bisa membuat folder unduhan");
                    return;
                }

                URL url = new URL(apkUrl);
                konek = (HttpURLConnection) url.openConnection();
                konek.setConnectTimeout(TIMEOUT_KONEK);
                konek.setReadTimeout(TIMEOUT_BACA);
                konek.setInstanceFollowRedirects(true);
                konek.setRequestProperty("User-Agent", "JagaKampung-Android");

                int kode = konek.getResponseCode();
                if (kode != HttpURLConnection.HTTP_OK) {
                    cbGagal(cb, "Unduhan gagal: HTTP " + kode + "\n" + apkUrl);
                    return;
                }

                final long total = panjangKonten(konek);
                masuk = konek.getInputStream();
                keluar = new FileOutputStream(sementara);

                byte[] buffer = new byte[16 * 1024];
                long sudah = 0;
                int persenTerakhir = -1;
                int n;
                while ((n = masuk.read(buffer)) > 0) {
                    keluar.write(buffer, 0, n);
                    sudah += n;
                    if (total > 0) {
                        int persen = (int) (sudah * 100 / total);
                        if (persen != persenTerakhir) {
                            persenTerakhir = persen;
                            final int p = persen;
                            MAIN.post(() -> cb.onProgres(p));
                        }
                    }
                }
                keluar.flush();
                keluar.close();
                keluar = null;

                if (total > 0 && sementara.length() != total) {
                    cbGagal(cb, "Unduhan terputus (" + sementara.length() + " dari " + total + " byte)");
                    return;
                }
                if (tujuan.exists() && !tujuan.delete()) {
                    cbGagal(cb, "Tidak bisa mengganti file APK lama");
                    return;
                }
                if (!sementara.renameTo(tujuan)) {
                    cbGagal(cb, "Tidak bisa menyimpan APK");
                    return;
                }

                final File hasil = tujuan;
                MAIN.post(() -> cb.onSelesai(hasil));
            } catch (Exception e) {
                cbGagal(cb, "Unduhan gagal: " + e.getClass().getSimpleName()
                        + (e.getMessage() == null ? "" : " — " + e.getMessage()));
            } finally {
                tutup(keluar);
                tutup(masuk);
                if (konek != null) konek.disconnect();
                if (sementara.exists() && !tujuan.exists()) sementara.delete();
            }
        }, "unduh-apk").start();
    }

    private static void cbGagal(UnduhCallback cb, String pesan) {
        MAIN.post(() -> cb.onGagal(pesan));
    }

    private static void tutup(java.io.Closeable c) {
        if (c == null) return;
        try { c.close(); } catch (Exception abaikan) { }
    }

    /** getContentLengthLong() baru ada di API 24; minSdk proyek ini 23. */
    private static long panjangKonten(HttpURLConnection konek) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return konek.getContentLengthLong();
        }
        return konek.getContentLength();
    }

    // ============================================================
    // 3. Pasang APK
    // ============================================================

    /** Buka pemasang paket Android untuk APK yang sudah diunduh. */
    public static void pasang(Activity activity, File apk) {
        // Android 8+ membatasi pemasangan dari sumber tak dikenal per-aplikasi.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            Intent izin = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(izin);
            return;
        }

        Uri uri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", apk);

        Intent pasang = new Intent(Intent.ACTION_VIEW);
        pasang.setDataAndType(uri, "application/vnd.android.package-archive");
        pasang.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pasang.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(pasang);
    }

    // ============================================================

    private static String bacaSemua(InputStream masuk) throws Exception {
        java.io.ByteArrayOutputStream keluar = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8 * 1024];
        int n;
        try {
            while ((n = masuk.read(buffer)) > 0) keluar.write(buffer, 0, n);
        } finally {
            tutup(masuk);
        }
        return keluar.toString("UTF-8");
    }
}
