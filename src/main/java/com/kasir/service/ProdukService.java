package com.kasir.service;

import com.kasir.model.Produk;
import com.kasir.model.ProdukMakanan;
import com.kasir.model.ProdukMinuman;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service untuk operasi CRUD (Create, Read, Update, Delete) Produk.
 * Mengelola daftar produk yang tersedia di kasir.
 */
public class ProdukService {

    // Penerapan Polimorfisme: List dapat menampung objek turunan apa pun dari Produk
    private final List<Produk> daftarProduk;

    public ProdukService() {
        this.daftarProduk = new ArrayList<>();
        inisialisasiDataAwal();
    }

    /**
     * Data awal (sample/seed data) agar aplikasi langsung dapat dicoba saat dijalankan.
     */
    private void inisialisasiDataAwal() {
        // Objek ProdukMakanan - Aneka Frozen Food (Pewarisan & Polimorfisme)
        daftarProduk.add(new ProdukMakanan("FRZ01", "Nugget Ayam Crispy 500g", 38000, 30, false));
        daftarProduk.add(new ProdukMakanan("FRZ02", "Sosis Sapi Bakar 500g", 45000, 25, true));
        daftarProduk.add(new ProdukMakanan("FRZ03", "Bakso Sapi Urat Frozen 50 Biji", 52000, 20, false));
        daftarProduk.add(new ProdukMakanan("FRZ04", "Dimsum Ayam Udang Frozen 10 Pcs", 32000, 40, true));
        daftarProduk.add(new ProdukMakanan("FRZ05", "Kentang Shoestring French Fries 1kg", 36000, 35, false));

        // Objek ProdukMinuman - Minuman Dingin & Es Beku (Pewarisan & Polimorfisme)
        daftarProduk.add(new ProdukMinuman("MNM01", "Es Krim Durian Cup 100ml", 15000, 50, true));
        daftarProduk.add(new ProdukMinuman("MNM02", "Frozen Yoghurt Strawberry 250ml", 18000, 30, true));
        daftarProduk.add(new ProdukMinuman("MNM03", "Susu Pasteurisasi Frozen 1 Liter", 22000, 25, true));
        daftarProduk.add(new ProdukMinuman("MNM04", "Es Cincau Susu Brown Sugar", 10000, 40, true));
    }

    // ==========================================
    // 1. CREATE (Tambah Produk Baru)
    // ==========================================
    public boolean tambahProduk(Produk produk) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh kosong.");
        }
        if (cariBerdasarkanKode(produk.getKode()) != null) {
            throw new IllegalArgumentException("Kode produk '" + produk.getKode() + "' sudah digunakan!");
        }
        return daftarProduk.add(produk);
    }

    // ==========================================
    // 2. READ (Membaca Data Produk)
    // ==========================================
    public List<Produk> getSemuaProduk() {
        return Collections.unmodifiableList(new ArrayList<>(daftarProduk));
    }

    public Produk cariBerdasarkanKode(String kode) {
        if (kode == null || kode.trim().isEmpty()) {
            return null;
        }
        for (Produk p : daftarProduk) {
            if (p.getKode().equalsIgnoreCase(kode.trim())) {
                return p;
            }
        }
        return null;
    }

    public List<Produk> cariProduk(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getSemuaProduk();
        }
        String q = keyword.trim().toLowerCase();
        List<Produk> hasil = new ArrayList<>();
        for (Produk p : daftarProduk) {
            if (p.getKode().toLowerCase().contains(q) ||
                p.getNama().toLowerCase().contains(q) ||
                p.getKategori().toLowerCase().contains(q)) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    // ==========================================
    // 3. UPDATE (Perbarui Data Produk)
    // ==========================================
    public boolean perbaruiProduk(String kodeLama, Produk produkBaru) {
        if (kodeLama == null || produkBaru == null) {
            return false;
        }
        
        for (int i = 0; i < daftarProduk.size(); i++) {
            Produk lama = daftarProduk.get(i);
            if (lama.getKode().equalsIgnoreCase(kodeLama)) {
                // Jika kode berubah, pastikan kode baru belum terpakai oleh produk lain
                if (!kodeLama.equalsIgnoreCase(produkBaru.getKode())) {
                    Produk duplikat = cariBerdasarkanKode(produkBaru.getKode());
                    if (duplikat != null) {
                        throw new IllegalArgumentException("Kode produk baru '" + produkBaru.getKode() + "' sudah terdaftar.");
                    }
                }
                daftarProduk.set(i, produkBaru);
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // 4. DELETE (Hapus Produk)
    // ==========================================
    public boolean hapusProduk(String kode) {
        Produk target = cariBerdasarkanKode(kode);
        if (target != null) {
            return daftarProduk.remove(target);
        }
        return false;
    }
}
