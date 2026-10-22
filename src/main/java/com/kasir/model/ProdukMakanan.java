package com.kasir.model;

/**
 * Class ProdukMakanan
 * Menerapkan prinsip:
 * 1. PEWARISAN (Inheritance): Menurunkan semua atribut dan metode dari kelas induk Produk (extends Produk).
 * 2. POLIMORFISME (Method Overriding): Mengimplementasikan logika spesifik untuk hitungPajak, hitungDiskon, dan getKategori.
 */
public class ProdukMakanan extends Produk {

    // Atribut tambahan spesifik Makanan
    private boolean pedas;

    public ProdukMakanan(String kode, String nama, double harga, int stok, boolean pedas) {
        super(kode, nama, harga, stok); // Memanggil constructor kelas induk (Produk)
        this.pedas = pedas;
    }

    public ProdukMakanan(String kode, String nama, double harga, int stok) {
        this(kode, nama, harga, stok, false);
    }

    @Override
    public String getKategori() {
        return "Makanan";
    }

    /**
     * Pajak restoran untuk makanan sebesar 10% (PB1).
     */
    @Override
    public double hitungPajak() {
        return getHarga() * 0.10;
    }

    /**
     * Polimorfisme Overriding dari interface Diskonable:
     * Promo Makanan: Jika beli 5 porsi atau lebih, dapat diskon 10% dari total pembelian.
     */
    @Override
    public double hitungDiskon(int jumlah) {
        if (jumlah >= 5) {
            return (getHarga() * jumlah) * 0.10;
        }
        return 0.0;
    }

    @Override
    public String getKeteranganKhusus() {
        return pedas ? "Varian Pedas (PB1 10%, Diskon 10% min. 5)" : "Varian Reguler (PB1 10%, Diskon 10% min. 5)";
    }

    public boolean isPedas() {
        return pedas;
    }

    public void setPedas(boolean pedas) {
        this.pedas = pedas;
    }
}
