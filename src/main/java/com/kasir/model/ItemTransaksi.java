package com.kasir.model;

/**
 * Class ItemTransaksi
 * Mewakili satu baris belanjaan di dalam transaksi kasir (Point of Sales).
 * Menggunakan Polimorfisme secara elegan: objek produk di dalamnya bisa berupa
 * ProdukMakanan atau ProdukMinuman, dan perhitungan diskon/pajak akan berjalan otomatis
 * sesuai tipe aslinya.
 */
public class ItemTransaksi {

    private Produk produk;
    private int jumlah;

    public ItemTransaksi(Produk produk, int jumlah) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh bernilai null.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah item harus lebih dari 0.");
        }
        this.produk = produk;
        this.jumlah = jumlah;
    }

    public Produk getProduk() {
        return produk;
    }

    public void setProduk(Produk produk) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh null.");
        }
        this.produk = produk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah harus minimal 1.");
        }
        this.jumlah = jumlah;
    }

    /**
     * Menghitung subtotal kotor (Harga * Qty).
     */
    public double getSubtotal() {
        return produk.getHarga() * jumlah;
    }

    /**
     * Penerapan POLIMORFISME:
     * Memanggil hitungDiskon() pada referensi Produk.
     * Java secara otomatis mengeksekusi hitungDiskon milik ProdukMakanan atau ProdukMinuman!
     */
    public double getDiskon() {
        return produk.hitungDiskon(jumlah);
    }

    /**
     * Penerapan POLIMORFISME:
     * Memanggil hitungPajak() sesuai subclass Produk terkait.
     */
    public double getPajak() {
        return produk.hitungPajak() * jumlah;
    }

    /**
     * Total bersih untuk baris ini (Subtotal - Diskon + Pajak).
     */
    public double getTotalBersih() {
        return getSubtotal() - getDiskon() + getPajak();
    }
}
