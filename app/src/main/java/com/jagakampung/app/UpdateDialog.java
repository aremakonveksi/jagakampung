package com.jagakampung.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import java.io.File;

/** Dialog alur update: cek -> tawarkan -> unduh -> pasang. */
public final class UpdateDialog {

    private UpdateDialog() {}

    /** Cek versi lalu tampilkan dialog hanya kalau ada versi lebih baru. */
    public static void cekSenyap(final Activity activity) {
        UpdateUtil.cekVersi(new UpdateUtil.CekCallback() {
            @Override
            public void onAdaUpdate(UpdateUtil.Info info) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                tawarkan(activity, info);
            }

            @Override
            public void onTerbaru(String versiTerpasang) {
                // senyap: tidak mengganggu saat aplikasi dibuka
            }

            @Override
            public void onGagal(String pesan) {
                // senyap: gagal cek otomatis tidak perlu mengganggu
            }
        });
    }

    /** Cek manual — selalu memberi umpan balik di semua cabang. */
    public static void cekManual(final Activity activity) {
        Toast.makeText(activity, "Memeriksa update…", Toast.LENGTH_SHORT).show();
        UpdateUtil.cekVersi(new UpdateUtil.CekCallback() {
            @Override
            public void onAdaUpdate(UpdateUtil.Info info) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                tawarkan(activity, info);
            }

            @Override
            public void onTerbaru(String versiTerpasang) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                info(activity, activity.getString(R.string.tombol_cek_update),
                        activity.getString(R.string.update_terbaru, versiTerpasang));
            }

            @Override
            public void onGagal(String pesan) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                info(activity, activity.getString(R.string.tombol_cek_update),
                        activity.getString(R.string.update_gagal, pesan));
            }
        });
    }

    private static void tawarkan(final Activity activity, final UpdateUtil.Info info) {
        String versiBaru = info.versionName.isEmpty()
                ? "v" + info.versionCode : "v" + info.versionName;
        String catatan = info.catatan.isEmpty() ? "Perbaikan dan peningkatan." : info.catatan;

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.judul_update_tersedia))
                .setMessage(activity.getString(R.string.pesan_update_tersedia, versiBaru, catatan))
                .setPositiveButton(activity.getString(R.string.tombol_unduh),
                        (d, w) -> unduh(activity, info))
                .setNegativeButton(activity.getString(R.string.tombol_nanti), null)
                .show();
    }

    private static void unduh(final Activity activity, final UpdateUtil.Info info) {
        final AlertDialog progres = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.judul_update_tersedia))
                .setMessage(activity.getString(R.string.update_sedang_unduh, 0))
                .setCancelable(false)
                .show();

        UpdateUtil.unduhApk(activity, info.apkUrl, new UpdateUtil.UnduhCallback() {
            @Override
            public void onProgres(int persen) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                progres.setMessage(activity.getString(R.string.update_sedang_unduh, persen));
            }

            @Override
            public void onSelesai(final File apk) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                tutup(progres);
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.judul_update_tersedia))
                        .setMessage(activity.getString(R.string.update_siap_pasang))
                        .setPositiveButton(activity.getString(R.string.tombol_pasang),
                                (d, w) -> UpdateUtil.pasang(activity, apk))
                        .setNegativeButton(activity.getString(R.string.tombol_nanti), null)
                        .show();
            }

            @Override
            public void onGagal(String pesan) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                tutup(progres);
                info(activity, activity.getString(R.string.tombol_cek_update),
                        activity.getString(R.string.update_gagal, pesan));
            }
        });
    }

    private static void info(Activity activity, String judul, String pesan) {
        new AlertDialog.Builder(activity)
                .setTitle(judul)
                .setMessage(pesan)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private static void tutup(AlertDialog dlg) {
        try { dlg.dismiss(); } catch (Exception abaikan) { }
    }
}
