# Data Uji di Firebase (untuk Rizqi & Syailendra)

Dibuat oleh **Rizal**, project `jagakampung-p11`, database
`jagakampung-p11-default-rtdb` (asia-southeast1).

Tujuan: Rizqi bisa menyambungkan **Dashboard**, **Antrean Laporan**, dan
**Detail Laporan** ke Firebase tanpa menunggu form laporan warga dari
Syailendra.

---

## 1. Laporan contoh yang tersedia

Ada **4 laporan contoh** di node `laporan/{id}`, semuanya `status = "dilaporkan"`:

| ID | Judul | Kategori | Punya foto |
|---|---|---|---|
| `lpr_contoh_001` | Jalan berlubang di depan RT 02 | Infrastruktur | ya (PNG kecil) |
| `lpr_contoh_002` | Tumpukan sampah di pinggir sungai | Kebersihan | tidak |
| `lpr_contoh_003` | Lampu jalan mati di perempatan | Keamanan | tidak |
| `lpr_contoh_004` | Aspal amblas di dekat pos ronda | Infrastruktur | ya (PNG 240x160) |

Isi tiap laporan (field sesuai `database.rules.json`):

```json
{
  "uidPelapor": "LlipbzENRoS3JhfZgczBqDMHyVy1",
  "namaPelapor": "Warga Uji",
  "judul": "Aspal amblas di dekat pos ronda",
  "deskripsi": "Permukaan jalan turun sekitar 15 cm, air menggenang saat hujan...",
  "kategori": "Infrastruktur",
  "alamatTeks": "Jl. Flamboyan Rt 04 RW 05, dekat Pos Ronda",
  "lat": -7.9688,
  "lng": 112.6310,
  "status": "dilaporkan",
  "dibuatPada": 1795594854002
}
```

> Catatan: laporan `-P7GmlpxEwdHLeznD3Oj` ("lampu di blok B mati") **bukan**
> data uji — itu laporan asli yang dikirim warga `zen` lewat aplikasi.
> Jangan dihapus.

## 2. Foto

Foto disimpan di node **terpisah** `laporanFoto/{idLaporan}` sebagai string
Base64 (bukan di dalam `laporan`), sesuai `database.rules.json`.

Tersedia: `laporanFoto/lpr_contoh_001` dan `laporanFoto/lpr_contoh_004`.

Penting saat menyambungkan Detail Laporan:
- `laporanFoto` **tidak punya** `.read` di level root, hanya di level
  `laporanFoto/{id}`. Jadi baca per-ID, jangan baca seluruh node sekaligus
  (akan `Permission denied`).
- `.write` memakai `!data.exists()` — foto hanya bisa dibuat sekali,
  tidak bisa ditimpa. Kalau perlu ganti foto, hapus dulu node-nya.

## 3. Akun uji

| Peran | Email | Password | Keterangan |
|---|---|---|---|
| Admin | `admin@jagakampung.com` | (dikirim terpisah, jangan ditulis di dokumen) | UID `0XuwunnOtlRZBnia602S9fQxQOa2` |
| Warga | `warga.uji@jagakampung.com` | `UjiWarga123!` | UID `LlipbzENRoS3JhfZgczBqDMHyVy1`, pemilik 4 laporan contoh |

Akun warga uji ini yang dipakai untuk menulis laporan contoh, karena aturan
Firebase mewajibkan `laporan/{id}/uidPelapor === auth.uid` — admin **tidak
boleh** membuat laporan atas nama orang lain (desain yang benar untuk
mencegah pemalsuan laporan).

## 4. Status "ditolak" sudah diizinkan

Aturan live sudah dipublish dan **sudah diuji** (HTTP 200):

- `laporan/{id}/status` menerima `dilaporkan | ditinjau | diproses | selesai | ditolak`
- `tanggapan/{idLaporan}/{idTanggapan}/statusBaru` menerima `ditinjau | diproses | selesai | ditolak`

Uji yang sudah dilakukan (lewat REST API, bukan tampilan):
- admin menulis `laporan/lpr_contoh_004/status = "ditolak"` → **OK 200**
- admin menulis objek `tanggapan/lpr_contoh_004/tgh_uji` dengan
  `statusBaru = "ditolak"` → **OK 200**, lalu dibaca balik dan isinya benar
- data uji tanggapan sudah dihapus kembali agar database bersih

Catatan teknis: `tanggapan` memakai `.validate: newData.hasChildren([...])`,
jadi harus ditulis sebagai **satu objek lengkap** (`uidAdmin`, `teks`,
`statusBaru`, `dibuatPada`). Menulis per-field akan `Permission denied`.

## 5. Yang belum ada (menunggu Syailendra)

Modul laporan warga (form input + kamera + GPS) **belum ada** di repo.
Begitu modul itu masuk, laporan baru akan otomatis muncul di Antrean Laporan
karena sudah membaca node `laporan` yang sama.
