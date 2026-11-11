# BRIEF TIM — RIZQI (Modul Admin)

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

## 2. Peranmu — Rizqi (Teman B)

**Fokus tunggal: SEMUA yang berkaitan dengan sisi admin.**

### Yang kamu kerjakan
1. **Beranda Admin (Antrean Laporan)** — layar daftar semua laporan warga dengan filter status (semua / dilaporkan / ditinjau / diproses / selesai). Tiap kartu bisa diklik → Detail.
2. **Detail Laporan (Admin)** — layar penuh: foto warga, info lengkap, tombol ubah status (dropdown: ditinjau → diproses → selesai), textarea untuk menulis **tanggapan** → simpan ke `tanggapan/{laporanId}`.
3. **Verifikasi Akun** — layar daftar akun warga dengan `status == "pending"`. Tombol **ACC** (ubah status → `approved`) atau **Tolak** (ubah → `rejected` + isi `alasanTolak`).

### Yang TIDAK boleh kamu sentuh
- `firebase/database.rules.json` — milik Rizal.
- Layar-layar warga (form laporan, beranda warga, kamera) — milik Syailendra.
- Struktur data Firebase (`users/`, `laporan/`, `laporanFoto/`) — kontrak dibekukan.
- `google-services.json` — Rizal yang pegang.

## 3. Struktur Data yang Berlaku (dibekukan)

```
users/{uid}
  nama, email, rt, rw
  peran: "warga" | "admin"
  status: "pending" | "approved" | "rejected"
  dibuatPada: 1699564800000
  alasanTolak: "..."          // opsional, kalau ditolak

laporan/{laporanId}
  uidPelapor: "abc123"
  namaPelapor: "Syailendra"
  judul, deskripsi, kategori, alamatTeks, lat, lng
  status: "dilaporkan"        // kamu yang ubah ke ditinjau/diproses/selesai
  dibuatPada, diperbaruiPada

laporanFoto/{laporanId}
  "data:image/jpeg;base64,..."

tanggapan/{laporanId}/{tanggapanId}
  uidAdmin: "def456"          // UID akun admin-mu
  teks: "Sudah diteruskan ke petugas..."
  statusBaru: "diproses"
  dibuatPada: 1699564800000
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
| status_ditinjau | `#F2B233` | badge kuning |
| status_diproses | `#3B82F6` | badge biru |
| status_selesai | `#2E9E5B` | badge hijau |
| status_approved | `#2E9E5B` | badge akun disetujui |
| status_rejected | `#E5484D` | badge akun ditolak |

Font: **Inter**. Lihat `DESIGN_TOKENS.md` untuk detail lengkap. **Tanpa emoji** — ikon pakai SVG/vector.

## 5. Cara Mulai (GitHub Desktop)

1. Buka email dari GitHub (undangan collaborator dari Rizal) → klik **Accept**.
2. Buka **GitHub Desktop** → **File > Clone Repository** → tab **URL** → masukkan `https://github.com/aremakonveksi/jagakampung`.
3. Setelah clone selesai, buat branch: **Branch > New Branch** → nama `fitur-admin-rizqi`.
4. Buka proyek di **Android Studio** (kalau `app/` belum ada, coba build dari `app-demo/` dulu atau tunggu Rizal selesai fondasi auth-nya).
5. Kerjakan fitur-mu, commit sering, push ke branch-mu.
6. Kalau selesai → buat **Pull Request** di GitHub → minta Rizal review.

## 6. Aturan Wajib

- **Jangan pernah push ke `main`.** Selalu kerja di branch-mu sendiri.
- **Kontrak data tidak boleh diubah** tanpa persetujuan bertiga.
- **Ikon pakai SVG/vector**, BUKAN emoji.
- **Commit kecil-kecil**, pesan commit dalam bahasa Indonesia yang jelas (contoh: `Tambah layar antrean laporan admin`).
- **Sebelum minta merge**, pastikan bisa menjelaskan semua perubahanmu secara lisan.

## 7. Urutan Merge ke Main

1. **Rizal** merge duluan (fondasi: Firebase Auth + UsersRepository + Rules ter-publish).
2. **Syailendra** merge kedua (modul laporan warga).
3. **Kamu (Rizqi)** merge terakhir (modul admin, di atas fondasi Rizal + modul Syailendra).

Pakai GitHub Desktop: **Branch > Update from main** setiap kali ada yang baru di `main` sebelum lanjut kerja.

## 8. Prompt untuk Claude-mu

Setelah file ini terbuka di Claude-mu, tulis:

> "Aku Rizqi, mengerjakan modul admin proyek Jaga Kampung. Brief di atas adalah konteks proyekku. Aku butuh: (1) kode layar Antrean Laporan admin dengan filter status, (2) kode layar Detail Laporan + ubah status + tulis tanggapan, (3) kode layar Verifikasi Akun (ACC/tolak akun baru). Bangun bertahap, mulai dari Antrean Laporan. Tanya dulu kalau ada yang belum jelas."
