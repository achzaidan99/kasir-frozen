package com.kasir.model;

/**
 * Abstract Class Produk
 * Menerapkan prinsip:
 * 1. ENKAPSULASI: Atribut dideklarasikan secara private / protected dengan getter dan setter tervalidasi.
 * 2. ABSTRAKSI: Memiliki abstract method yang harus diimplementasikan oleh kelas turunan (child class).
 */
public abstract class Produk implements Diskonable {
    
    // Atribut (Encapsulation: private agar tidak dapat diakses langsung dari luar)
    private String kode;
    private String nama;
    private double harga;
    private int stok;

    // Constructor
    public Produk(String kode, String nama, double harga, int stok) {
        setKode(kode);
        setNama(nama);
        setHarga(harga);
        setStok(stok);
    }

    // ==========================================
    // ABSTRAKSI (Abstract Methods)
    // ==========================================
    
    /**
     * Mengembalikan nama kategori produk (misal: "Makanan", "Minuman").
     */
    public abstract String getKategori();

    /**
     * Menghitung besaran pajak per satuan produk.
     */
    public abstract double hitungPajak();

    /**
     * Informasi keterangan khusus spesifik per jenis produk.
     */
    public abstract String getKeteranganKhusus();

    // ==========================================
    // POLIMORFISME & LOGIKA BISNIS
    // ==========================================

    /**
     * Menghitung total harga sebelum diskon untuk kuantitas tertentu.
     */
    public double hitungSubtotal(int jumlah) {
        if (jumlah <= 0) {
            return 0.0;
        }
        return this.harga * jumlah;
    }

    /**
     * Mengurangi stok saat terjadi pembelian.
     * @param jumlah kuantitas yang terjual
     */
    public void kurangiStok(int jumlah) {
        if (jumlah < 0) {
            throw new IllegalArgumentException("Jumlah pengurangan stok tidak boleh negatif.");
        }
        if (this.stok < jumlah) {
            throw new IllegalStateException("Stok produk '" + nama + "' tidak mencukupi (Tersedia: " + stok + ", Diminta: " + jumlah + ").");
        }
        this.stok -= jumlah;
    }

    /**
     * Menambah stok produk (misal saat restock).
     * @param jumlah kuantitas penambahan
     */
    public void tambahStok(int jumlah) {
        if (jumlah < 0) {
            throw new IllegalArgumentException("Jumlah penambahan stok tidak boleh negatif.");
        }
        this.stok += jumlah;
    }

    // ==========================================
    // GETTER & SETTER (ENKAPSULASI DENGAN VALIDASI)
    // ==========================================

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        if (kode == null || kode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode produk tidak boleh kosong.");
        }
        this.kode = kode.trim().toUpperCase();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        if (harga < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh bernilai negatif.");
        }
        this.stok = stok;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - Rp %,.0f (Stok: %d)", kode, nama, harga, stok);
    }
}
