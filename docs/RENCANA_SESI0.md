# Rencana Kerja Sesi 0 — Jaga Kampung (hari pertama kerja bertiga)

## Rizal (akun & akses, Rules)
1. Bikin project Firebase baru: nama `jaga-kampung` (Spark/free, tanpa kartu kredit).
2. Tambah Android app dengan package **final** `com.kelompokX.jagakampung` (X = nomor kelompok kalian —
   TENTUKAN dulu sebelum langkah ini, karena package susah diubah setelah koding).
3. Unduh `google-services.json` -> taruh di folder `app/` (file ini TIDAK boleh di-commit; sudah di-gitignore).
4. Aktifkan Authentication > Email/Password + buat Realtime Database (mode locked, region singapura/seasia terdekat).
5. Publish `firebase/database.rules.json` dari repo ini ke Realtime Database > Rules (paste + Publish).
6. Invite 2 teman: github.com/aremakonveksi/jagakampung > Settings > Collaborators > Add people.
7. Bikin admin pertama: daftar dari app, lalu di Firebase Console > Realtime Database edit node
   `users/{uid}` kamu sendiri: peran=admin, status=approved. (Hanya ini yang boleh edit manual.)
8. Definisi selesai: app bisa login/register + status pending muncul; Rules ter-publish; 2 teman bisa clone+push.

## Teman A (laporan warga)
1. Clone repo (GitHub Desktop), bikin branch `fitur-<namamu>`.
2. Bangun skeleton 4 layar warga sesuai Gaya A: Beranda warga (feed + FAB), Form laporan
   (judul, deskripsi, kategori, kamera, alamat), Detail laporan, "Laporan saya".
3. Tahan dulu: JANGAN sentuh Rules, JANGAN sentuh modul admin, JANGAN ubah kontrak data.
4. Simpan foto: kompres JPEG sisi panjang 1024px kualitas ~60, kirim Base64 ke `laporanFoto/{id}`.
5. Definisi selesai: form bisa ambil foto + submit laporan dan data muncul di RTDB (status "dilaporkan").

## Teman B (admin)
1. Clone repo, branch `fitur-<namamu>`.
2. Bangun skeleton 3 layar admin sesuai Gaya A: Antrean laporan masuk (+filter status),
   ubah status + tanggapan, verifikasi akun (pending -> approved/rejected).
3. Tahan dulu: JANGAN sentuh modul warga, JANGAN sentuh Rules.
4. Definisi selesai: dari layar admin bisa ubah status laporan + tulis tanggapan; bisa ACC/tolak akun baru.

## Urutan merge ke main
1. Rizal merge pertama (fondasi: auth + UsersRepository + Rules).
2. Teman A, lalu Teman B rebase di atas hasilnya (pakai GitHub Desktop: Branch > Update from main).
3. Semua merge lewat Pull Request + 1 reviewer (gerbang "bisa menjelaskan?").

## Ganti package nanti (kalau X baru diketahui setelah project dibuat)
1. Android Studio > panel Project (mode Android) > klik kanan package > Refactor > Rename > Rename package.
2. Ulangi untuk tiap segmen (com / kelompokX / jagakampung) sampai semua terganti.
3. Update `applicationId` di `app/build.gradle` + `package="..."` di AndroidManifest.
4. HAPUS + tambah ulang app di Firebase dengan package baru -> unduh google-services.json baru.
5. Clean/Rebuild. Jalankan. Kalau gagal compile: periksa import R dan AndroidManifest.

## Design tokens Gaya A
File: desain/GayaA/colors.xml (basis kerja; heks final dikoreksi dari kanvas Claude saat limit reset).
