# Design Tokens Gaya A — Jaga Kampung

## Warna (diukur langsung dari kanvas "Mockup Jaga Kampung" Gaya A, 11 Nov 2026)
| Token | Heks (terukur) | Pakai untuk |
|---|---|---|
| teal_primary | #10766B | tombol utama, FAB, header |
| teal_dark | #0E766D | tekan/teks di atas teal_light |
| teal_soft | #48938E | ikon sekunder |
| teal_light | #D9EAE8 | latar chip, highlight |
| accent | #FBB315 | aksen/label menunggu |
| background | #F4F7FC | latar layar app |
| surface | #FFFFFF | kartu |
| text_primary | #444853 | teks utama |
| text_secondary | #4E525D | teks sekunder |
| divider | #E8EDF3 | garis pemisah |
| status_dilaporkan | #9E9E9E | badge abu |
| status_ditinjau | #F2B233 | badge kuning |
| status_diproses | #3B82F6 | badge biru |
| status_selesai | #2E9E5B | badge hijau |
| status_pending | #FBB315 | badge akun menunggu |
| status_approved | #2E9E5B | badge akun disetujui |
| status_rejected | #E5484D | badge akun ditolak |

Catatan: teal (#10766B/#0E766D), teks (#444853/#4E525D), latar (#F4F7FC), abu (#E8EDF3),
dan aksen kuning (#FBB315) adalah nilai faktis hasil ekstraksi piksel kanvas Gaya A.
Nilai badge status lain (abu/biru/hijau/merah) memakai konvensi semantik yang aman; kalau nanti
Claude bisa dibuka lagi (limit reset 16:20), minta cocokkan kembali nilai badge dari kanvas.

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
Kanvas "Mockup Jaga Kampung" (Gaya A). Nilai teal/teks/latar diukur dari piksel kanvas 11 Nov 2026.
Sisa verifikasi (opsional, saat limit Claude reset 16:20): cocokkan heks badge
abu/biru/hijau/merah dari kanvas -> perbarui colors.xml bila ada beda.
