# Catatan Modul Admin — untuk Rizal

Modul admin (Rizqi) ada di folder `modul_admin/`. Ini ringkasan supaya mudah digabung ke aplikasi utama.

## Isi modul

| File | Fungsi |
|---|---|
| `java/DashboardActivity.java` | Pintu masuk admin: ringkasan angka + tombol ke layar lain |
| `java/DaftarLaporanActivity.java` | Antrean laporan + filter status |
| `java/DetailLaporanActivity.java` | Detail laporan, foto Base64, ubah status, tanggapan admin |
| `java/VerifikasiAkunActivity.java` | Daftar akun pending, tombol Setujui / Tolak (+ alasan) |
| `res/layout/*.xml` | Layout untuk semua layar di atas (`activity_dashboard`, `activity_daftar_laporan`, `activity_detail_laporan`, `activity_verifikasi_akun`, `item_laporan`, `item_akun`) |
| `res/drawable/bg_card_laporan.xml` | Gaya kartu (putih, sudut bulat, garis tipis) |

## Cara menggabungkan

1. Salin `java/*.java` ke `app/src/main/java/com/jagakampung/demo/admin/`
2. Salin `res/layout/*.xml` ke `app/src/main/res/layout/`
3. Salin `res/drawable/bg_card_laporan.xml` ke `app/src/main/res/drawable/`
4. Tambahkan di `AndroidManifest.xml`, di dalam `<application>`:

```xml
<activity android:name="com.jagakampung.demo.admin.DashboardActivity" android:exported="false" />
<activity android:name="com.jagakampung.demo.admin.DaftarLaporanActivity" android:exported="false" />
<activity android:name="com.jagakampung.demo.admin.DetailLaporanActivity" android:exported="false" />
<activity android:name="com.jagakampung.demo.admin.VerifikasiAkunActivity" android:exported="false" />
```

5. Dari layar beranda/login, kalau `users/{uid}/peran == "admin"`, buka dashboard:

```java
startActivity(new Intent(this, com.jagakampung.demo.admin.DashboardActivity.class));
```

## Yang perlu dicatat

- Package: `com.jagakampung.demo.admin`. Memakai `com.jagakampung.demo.R`.
- Memakai `android.app.Activity` biasa, tanpa tambahan library.
- Warna mengikuti `desain/GayaA/colors.xml` (ditulis langsung sebagai kode hex).
- **Semua data masih contoh** (ditulis langsung di kode, ada komentar "DATA CONTOH"). Belum tersambung Firebase.
- Angka di Dashboard belum ikut berubah saat status laporan diubah, karena datanya contoh dan belum dibagi antar layar. Akan sinkron setelah semuanya membaca Firebase.

## Yang perlu dari Rizal

1. Proyek sudah disambungkan ke Firebase (`google-services.json` + dependency Realtime Database)? `app-demo` di repo belum punya `build.gradle`.
2. Akun admin pertama dibuat manual di Firebase Console.
3. Status laporan **"ditolak"** belum diizinkan di `database.rules.json` (untuk `laporan/status` maupun `tanggapan/statusBaru`). Mau ditambah, atau cukup 4 status?
4. Aturan `laporan` memakai `.create`, `.update`, `.delete`. Setahu Rizqi, Realtime Database hanya mengenal `.read`, `.write`, `.validate`. Mohon dites apakah admin benar-benar bisa mengubah status laporan.
