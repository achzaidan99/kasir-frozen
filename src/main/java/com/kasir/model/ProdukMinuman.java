package com.kasir.model;

/**
 * Class ProdukMinuman
 * Menerapkan prinsip:
 * 1. PEWARISAN (Inheritance): Mengembangkan kelas induk Produk.
 * 2. POLIMORFISME: Perilaku hitungDiskon dan hitungPajak berbeda dengan ProdukMakanan.
 */
public class ProdukMinuman extends Produk {

    // Atribut tambahan spesifik Minuman
    private boolean dingin;

    public ProdukMinuman(String kode, String nama, double harga, int stok, boolean dingin) {
        super(kode, nama, harga, stok);
        this.dingin = dingin;
    }

    public ProdukMinuman(String kode, String nama, double harga, int stok) {
        this(kode, nama, harga, stok, true);
    }

    @Override
    public String getKategori() {
        return "Minuman";
    }

    /**
     * Pajak untuk minuman sebesar 5%.
     */
    @Override
    public double hitungPajak() {
        return getHarga() * 0.05;
    }

    /**
     * Polimorfisme Overriding:
     * Promo Minuman: Beli 3 atau lebih, diskon 5%.
     */
    @Override
    public double hitungDiskon(int jumlah) {
        if (jumlah >= 3) {
            return (getHarga() * jumlah) * 0.05;
        }
        return 0.0;
    }

    @Override
    public String getKeteranganKhusus() {
        return dingin ? "Sajian Dingin/Es (Pajak 5%, Diskon 5% min. 3)" : "Sajian Hangat (Pajak 5%, Diskon 5% min. 3)";
    }

    public boolean isDingin() {
        return dingin;
    }

    public void setDingin(boolean dingin) {
        this.dingin = dingin;
    }
}
