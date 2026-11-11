# Jaga Kampung

Aplikasi Android pelaporan keluhan warga satu RT (proyek final Workshop Pemrograman Aplikasi Mobile).

## Anggota & Pembagian Irisan

| Irisan | Isi | Pemilik |
|---|---|---|
| Akun & akses | Daftar, login, status ACC, gerbang peran, Rules | Rizal |
| Laporan warga | Form lapor, kamera, simpan, "laporan saya", detail | Teman A |
| Sisi admin | Antrean laporan, verifikasi akun, ubah status, tanggapan | Teman B |

## Aturan Kerja Tim

1. Tidak ada yang mengetik langsung di `main` — semua lewat branch + Pull Request, direview anggota lain.
2. Ubah file bersama (`AndroidManifest.xml`, `strings.xml`, `build.gradle`) sedikit-sedikit, commit sering.
3. Sebelum merge, wajib bisa menjelaskan perubahanmu secara lisan (gerbang "bisa menjelaskan?").
4. Kontrak data Firebase (struktur di bawah) dibekukan — jangan ubah tanpa kesepakatan bertiga.

## Struktur Data Firebase

users/{uid}
  nama, email, rt, rw
  peran: "warga" | "admin"
  status: "pending" | "approved" | "rejected"
  dibuatPada, alasanTolak (opsional)

laporan/{laporanId}
  uidPelapor, namaPelapor, judul, deskripsi, kategori
  alamatTeks, lat, lng (opsional)
  status: "dilaporkan" | "ditinjau" | "diproses" | "selesai"
  dibuatPada, diperbaruiPada

laporanFoto/{laporanId}
  foto JPEG terkompresi sebagai teks Base64

tanggapan/{laporanId}/{tanggapanId}
  uidAdmin, teks, statusBaru, dibuatPada

## Cara Kerja Git (untuk Teman A & B — tinggal pakai)

1. Unduh GitHub Desktop: https://desktop.github.com
2. Repository > Clone Repository > tab URL > masukkan link repo yang Rizal kirim:
   https://github.com/aremakonveksi/jagakampung
3. Buat branch: Branch > New Branch > nama: `fitur-<namamu>` (mis. `fitur-andi`).
4. Kerjakan perubahan, lalu commit (tulis apa yang berubah).
5. Push origin, lalu buka Pull Request di github.com — tunggu review.

Tidak perlu setup akun sendiri. Kendala? Chat Rizal.
