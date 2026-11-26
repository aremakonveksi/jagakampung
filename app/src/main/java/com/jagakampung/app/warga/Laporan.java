package com.jagakampung.app.warga;

/**
 * Model data laporan. Nama field SAMA PERSIS dengan kontrak data Firebase di README.
 * Jangan ubah nama field tanpa kesepakatan tim.
 */
public class Laporan {
    public String id;            // key Firebase (bukan field di database)
    public String uidPelapor;
    public String namaPelapor;
    public String judul;
    public String deskripsi;
    public String kategori;
    public String alamatTeks;
    public Double lat;
    public Double lng;
    public String status;        // dilaporkan | ditinjau | diproses | selesai
    public Long dibuatPada;
    public Long diperbaruiPada;

    // Wajib ada: konstruktor kosong untuk Firebase
    public Laporan() { }
}