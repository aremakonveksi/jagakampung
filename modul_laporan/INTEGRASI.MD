# Panduan integrasi modul_laporan

Modul ini adalah kode sumber (belum project Gradle). Isi folder `main/` disalin ke `app/src/main` project final.

## 1. Package
Kode memakai `import com.example.lastproject.R;` di 5 file:
LaporanUtil, LaporanForm, LaporanAdapter, LaporanSaya, DetailLaporan.
Ganti baris itu dengan `<package-final>.R` setelah tim menyepakati package.

## 2. Dependency
Project-level build.gradle.kts, di plugins:
id("com.google.gms.google-services") version "4.4.2" apply false

Module app, di plugins:
id("com.google.gms.google-services")

Module app, di dependencies:
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-database")
implementation("com.google.firebase:firebase-auth")
implementation("com.google.android.gms:play-services-location:21.3.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")

## 3. AndroidManifest.xml
Di dalam <manifest>:
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

Di dalam <application>:
<activity android:name="modul_laporan.LaporanForm" android:exported="false" />
<activity android:name="modul_laporan.LaporanSaya" android:exported="false" />
<activity android:name="modul_laporan.DetailLaporan" android:exported="false" />
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/lp_file_paths" />
</provider>

Kamera memakai aplikasi kamera bawaan lewat FileProvider, jadi izin CAMERA tidak perlu.
Izin lokasi diminta saat runtime lewat tombol "Ambil lokasi GPS".

## 4. Nama resource (anti-bentrok dengan modul admin)
- layout: activity_warga_form_laporan, activity_warga_laporan_saya,
  activity_warga_detail_laporan, item_warga_laporan
- drawable dan values: awalan lp_
- xml: lp_file_paths

## 5. Kontrak data
- Menulis laporan/{id} dan laporanFoto/{id} (Base64) dalam satu update.
- kategori huruf kecil: infrastruktur | keamanan | kebersihan | sosial | lainnya
- status awal: "dilaporkan"
- "Laporan Saya" memakai orderByChild("uidPelapor"), jadi Rules perlu
  ".indexOn": ["uidPelapor"] di node laporan.

## 6. Pintu masuk
- LaporanForm: buat laporan baru
- LaporanSaya: riwayat laporan milik user yang login
- DetailLaporan: butuh extra "laporanId"