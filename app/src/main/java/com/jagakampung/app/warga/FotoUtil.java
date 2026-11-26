package com.jagakampung.app.warga;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/** Mengecilkan foto lalu mengubahnya menjadi teks Base64 (sesuai kontrak: laporanFoto = Base64). */
public class FotoUtil {

    private static final int SISI_MAKS = 800;   // piksel
    private static final int KUALITAS = 60;     // JPEG 0-100

    public static String keBase64(File file) throws IOException {
        String path = file.getAbsolutePath();

        // 1. Baca ukuran asli dulu
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);

        // 2. Muat versi yang sudah diperkecil supaya tidak boros memori
        int sample = 1;
        while (Math.max(o.outWidth, o.outHeight) / sample > SISI_MAKS * 2) sample *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sample;
        Bitmap bmp = BitmapFactory.decodeFile(path, o2);
        if (bmp == null) throw new IOException("Foto gagal dibaca");

        // 3. Perbaiki rotasi (foto kamera sering miring)
        int derajat = 0;
        ExifInterface exif = new ExifInterface(path);
        int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        if (ori == ExifInterface.ORIENTATION_ROTATE_90) derajat = 90;
        else if (ori == ExifInterface.ORIENTATION_ROTATE_180) derajat = 180;
        else if (ori == ExifInterface.ORIENTATION_ROTATE_270) derajat = 270;
        if (derajat != 0) {
            Matrix m = new Matrix();
            m.postRotate(derajat);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }

        // 4. Kecilkan ke sisi maksimal 800 px
        float skala = (float) SISI_MAKS / Math.max(bmp.getWidth(), bmp.getHeight());
        if (skala < 1f) {
            bmp = Bitmap.createScaledBitmap(bmp,
                    Math.round(bmp.getWidth() * skala),
                    Math.round(bmp.getHeight() * skala), true);
        }

        // 5. Kompres JPEG -> Base64
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, KUALITAS, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }
}