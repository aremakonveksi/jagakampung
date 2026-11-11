# BRIEF TIM — SYAILENDRA (Modul Laporan Warga)

> Salin seluruh isi file ini ke sesi Claude-mu, lalu lanjutkan percakapan di sana.

## 1. Keadaan Repo GitHub Saat Ini

| Item | Nilai |
|---|---|
| URL repo | https://github.com/aremakonveksi/jagakampung |
| Visibilitas | PRIVATE (sudah diinvite Rizal, cek email untuk ACC) |
| Branch default | `main` |
| Total commit | 5 |
| Commit terakhir | `750bc58` — "Tambah app-demo: prototipe 3 layar (splash/login/beranda) sesuai Gaya A, sudah jalan di HP Rizal" |

### File yang sudah ada di `main`

```
.gitignore                       — pengaturan file yang tidak di-commit (build/, google-services.json, keystore)
NAMA_BARU_draft.md               — dokumen perubahan nama (LaporWarga → Jaga Kampung), arsip saja
README.md                        — aturan tim + struktur data Firebase + panduan Git untuk pemula
app-demo/                        — prototipe 3 layar (splash, login, beranda), sudah jalan di HP
  AndroidManifest.xml
  java/com/jagakampung/demo/MainActivity.java
  res/drawable/ic_logo.xml
  res/values/colors.xml
  res/values/strings.xml
  res/values/themes.xml
desain/GayaA/DESIGN_TOKENS.md    — spesifikasi warna + tipografi + bentuk Gaya A (terukur dari mockup)
desain/GayaA/colors.xml          — file resource warna resmi
desain/GayaA/themes.xml          — tema Android Material3 (teal)
docs/RENCANA_SESI0.md            — langkah hari pertama per orang (termasuk jatahmu)
firebase/database.rules.json     — aturan keamanan Firebase RTDB (43 aturan, sudah hardened)
```

## 2. Peranmu — Syailendra (Teman A)

**Fokus tunggal: SEMUA yang berkaitan dengan laporan warga.**

### Yang kamu kerjakan
1. **Form Laporan** — layar dengan input: judul, deskripsi, kategori (dropdown), kamera (ambil foto), alamat (GPS + teks). Tombol submit → simpan ke `laporan/{id}` di Firebase RTDB.
2. **Kamera** — integrasi `ImagePicker` atau `ActivityResultContracts.TakePicture`. Kompres JPEG: sisi panjang 1024px, kualitas ~60 (target 100–200KB). Kirim sebagai Base64 ke `laporanFoto/{id}`.
3. **Beranda Warga** — feed kartu laporan (judul + badge status berwarna + tanggal). Pull-to-refresh. FAB "+ Buat laporan" → buka Form Laporan.
4. **Laporan Saya** — tab/layar filter laporan berdasarkan `uidPelapor == currentUid`.
5. **Detail Laporan** — layar penuh: foto, badge status, riwayat tanggapan admin, peta kecil (lat/lng).

### Yang TIDAK boleh kamu sentuh
- `firebase/database.rules.json` — milik Rizal.
- Layar-layar admin (antrean, ubah status, verifikasi akun) — milik Rizqi.
- Struktur data Firebase (`users/`, `laporan/`, `tanggapan/`) — kontrak dibekukan.
- `google-services.json` — Rizal yang pegang.

## 3. Struktur Data yang Berlaku (dibekukan)

```
laporan/{laporanId}
  uidPelapor: "abc123"          // Firebase Auth UID kamu
  namaPelapor: "Syailendra"
  judul: "Lampu jalan mati"
  deskripsi: "Detail laporan..."
  kategori: "infrastruktur"     // pilihan: infrastruktur | keamanan | kebersihan | sosial | lainnya
  alamatTeks: "Gang 3 RT 01"
  lat: -7.28, lng: 112.79      // opsional
  status: "dilaporkan"          // awal; admin yang mengubah ke ditinjau/diproses/selesai
  dibuatPada: 1699564800000
  diperbaruiPada: 1699564800000

laporanFoto/{laporanId}
  "data:image/jpeg;base64,/9j/4AAQ..."
```

## 4. Desain — Warna & Gaya (Gaya A, teal modern bersih)

Semua warna sudah terukur dari mockup, pakai dari `desain/GayaA/colors.xml`:

| Token | Heks | Pakai untuk |
|---|---|---|
| teal_primary | `#10766B` | tombol utama, FAB, header |
| teal_dark | `#0E766D` | status bar, teks di atas teal_light |
| teal_light | `#D9EAE8` | latar chip, highlight |
| background | `#F4F7FC` | latar layar |
| surface | `#FFFFFF` | kartu |
| text_primary | `#444853` | teks utama |
| status_dilaporkan | `#9E9E9E` | badge abu |
| status_ditinjau | `#F2B233` | badge kuning |
| status_diproses | `#3B82F6` | badge biru |
| status_selesai | `#2E9E5B` | badge hijau |

Font: **Inter** (Google Fonts, sudah didownload via Android Studio). Lihat `DESIGN_TOKENS.md` untuk ukuran/bentuk lengkap. **Tanpa emoji** — ikon harus SVG/vector drawable.

## 5. Cara Mulai (GitHub Desktop)

1. Buka email dari GitHub (undangan collaborator dari Rizal) → klik **Accept**.
2. Buka **GitHub Desktop** → **File > Clone Repository** → tab **URL** → masukkan `https://github.com/aremakonveksi/jagakampung`.
3. Setelah clone selesai, buat branch: **Branch > New Branch** → nama `fitur-laporan-syailendra`.
4. Buka proyek di **Android Studio** (kalau `app/` belum ada, coba build dari `app-demo/` dulu atau tunggu Rizal selesai fondasi auth-nya).
5. Kerjakan fitur-mu, commit sering, push ke branch-mu.
6. Kalau selesai → buat **Pull Request** di GitHub → minta Rizal review.

## 6. Aturan Wajib

- **Jangan pernah push ke `main`.** Selalu kerja di branch-mu sendiri.
- **Kontrak data tidak boleh diubah** tanpa persetujuan bertiga.
- **Ikon pakai SVG/vector**, BUKAN emoji.
- **Commit kecil-kecil**, pesan commit dalam bahasa Indonesia yang jelas (contoh: `Tambah form laporan warga`).
- **Sebelum minta merge**, pastikan bisa menjelaskan semua perubahanmu secara lisan.

## 7. Prompt untuk Claude-mu

Setelah file ini terbuka di Claude-mu, tulis:

> "Aku Syailendra, mengerjakan modul laporan warga proyek Jaga Kampung. Brief di atas adalah konteks proyekku. Aku butuh: (1) kode layar Form Laporan dengan integrasi kamera + submit ke Firebase RTDB, (2) kode layar Beranda Warga dengan feed kartu, (3) kode layar Laporan Saya. Bangun bertahap, mulai dari Form Laporan. Tanya dulu kalau ada yang belum jelas."
