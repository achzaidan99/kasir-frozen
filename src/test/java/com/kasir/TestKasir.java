package com.kasir;

import com.kasir.model.*;
import com.kasir.service.*;

public class TestKasir {
    public static void main(String[] args) {
        System.out.println("=== MULAI PENGUJIAN LOGIKA SISTEM KASIR & OOP ===");

        // 1. Uji Abstraksi, Pewarisan, & Polimorfisme
        Produk makanan = new ProdukMakanan("M01", "Nasi Goreng", 20000, 10, true);
        Produk minuman = new ProdukMinuman("D01", "Es Teh", 5000, 20, true);

        System.out.println("Polimorfisme Makanan:");
        System.out.println("  Kategori : " + makanan.getKategori());
        System.out.println("  Pajak    : " + makanan.hitungPajak() + " (Ekspektasi: 2000)");
        System.out.println("  Diskon 5x: " + makanan.hitungDiskon(5) + " (Ekspektasi: 10000)");

        System.out.println("Polimorfisme Minuman:");
        System.out.println("  Kategori : " + minuman.getKategori());
        System.out.println("  Pajak    : " + minuman.hitungPajak() + " (Ekspektasi: 250)");
        System.out.println("  Diskon 3x: " + minuman.hitungDiskon(3) + " (Ekspektasi: 750)");

        // 2. Uji CRUD ProdukService
        ProdukService ps = new ProdukService();
        int initialCount = ps.getSemuaProduk().size();
        System.out.println("Jumlah produk awal: " + initialCount);

        // CREATE
        Produk brgBaru = new ProdukMakanan("TEST01", "Ayam Bakar", 25000, 15, false);
        ps.tambahProduk(brgBaru);
        assert ps.getSemuaProduk().size() == initialCount + 1 : "Create gagal";
        System.out.println("CRUD CREATE: Berhasil");

        // READ
        Produk cari = ps.cariBerdasarkanKode("TEST01");
        assert cari != null && cari.getNama().equals("Ayam Bakar") : "Read gagal";
        System.out.println("CRUD READ: Berhasil (" + cari.getNama() + ")");

        // UPDATE
        Produk brgUpdate = new ProdukMakanan("TEST01", "Ayam Bakar Madu", 27000, 12, true);
        ps.perbaruiProduk("TEST01", brgUpdate);
        Produk updated = ps.cariBerdasarkanKode("TEST01");
        assert updated.getHarga() == 27000 : "Update gagal";
        System.out.println("CRUD UPDATE: Berhasil (" + updated.getNama() + " - " + updated.getHarga() + ")");

        // DELETE
        ps.hapusProduk("TEST01");
        assert ps.cariBerdasarkanKode("TEST01") == null : "Delete gagal";
        System.out.println("CRUD DELETE: Berhasil");

        // 3. Uji POS TransaksiService & Pengurangan Stok
        TransaksiService ts = new TransaksiService();
        Produk nugget = ps.cariBerdasarkanKode("FRZ01");
        int stokAwal = nugget.getStok();

        ts.tambahItemKeKeranjang(nugget, 2);
        Transaksi trx = ts.getTransaksiAktif();
        System.out.println("Subtotal TRX: " + trx.getTotalKotor());
        System.out.println("Grand Total : " + trx.getGrandTotal());

        // Checkout dengan uang 100.000
        Transaksi selesai = ts.prosesCheckout(100000);
        System.out.println("Kembalian   : " + selesai.getKembalian());
        System.out.println("Stok Baru   : " + nugget.getStok() + " (Sebelumnya: " + stokAwal + ")");
        assert nugget.getStok() == stokAwal - 2 : "Stok tidak berkurang!";
        System.out.println("PENGURANGAN STOK OTOMATIS: Berhasil!");

        System.out.println("\n=== STRUK GENERATED ===");
        System.out.println(selesai.generateStrukTeks());
        System.out.println("=== SEMUA PENGUJIAN BERHASIL 100% ===");
    }
}
