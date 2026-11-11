# Design Tokens Gaya A — Jaga Kampung

## Warna (heks final menunggu verifikasi dari kanvas Claude saat limit reset 16:20)
| Token | Heks (basis kerja) | Pakai untuk |
|---|---|---|
| teal_primary | #2A9D8F | tombol utama, FAB, header |
| teal_dark | #1F7A70 | teks di atas teal_light, tekan |
| teal_light | #D8F3EF | latar chip, highlight |
| accent | #F4A261 | aksen kecil |
| background | #F5F7F6 | latar layar |
| surface | #FFFFFF | kartu |
| text_primary | #1C2B2A | teks utama |
| text_secondary | #5C6B69 | teks sekunder |
| status_dilaporkan | #9E9E9E | badge abu |
| status_ditinjau | #F2B233 | badge kuning |
| status_diproses | #3B82F6 | badge biru |
| status_selesai | #2E9E5B | badge hijau |
| status_pending | #F2B233 | badge akun menunggu |
| status_approved | #2E9E5B | badge akun disetujui |
| status_rejected | #E5484D | badge akun ditolak |

## Tipografi (Inter)
| Peran | Ukuran | Berat |
|---|---|---|
| Judul layar | 22sp | 700 |
| Subjudul/kartu | 16sp | 600 |
| Body | 14sp | 400 |
| Caption/badge | 12sp | 500 |

## Bentuk & jarak
| Token | Nilai |
|---|---|
| Radius kartu | 16dp |
| Radius tombol | 12dp |
| Radius badge | 999dp (pil) |
| Spacing | 4/8/16/24dp |
| FAB | 56dp, ikon SVG "+" putih |
| Bottom nav | 3 item: Beranda, Laporan Saya, Profil |

## Sumber kebenaran
Kanvas "Mockup Jaga Kampung" (Gaya A). Setelah limit reset (16:20), minta Claude mencocokkan
heks asli kanvas -> colors.xml diperbarui dan di-commit.
