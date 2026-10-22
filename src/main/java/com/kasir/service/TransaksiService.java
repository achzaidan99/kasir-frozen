package com.kasir.service;

import com.kasir.model.ItemTransaksi;
import com.kasir.model.Produk;
import com.kasir.model.Transaksi;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service untuk menangani logika alur transaksi Point of Sales (POS).
 */
public class TransaksiService {

    private final List<Transaksi> riwayatTransaksi;
    private Transaksi transaksiAktif;
    private int counterHarian = 1;

    public TransaksiService() {
        this.riwayatTransaksi = new ArrayList<>();
        mulaiTransaksiBaru();
    }

    /**
     * Membuat ID transaksi unik baru berbasis tanggal, misal: TRX-20261022-0001
     */
    private String generateIdTransaksiBaru() {
        String tanggal = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TRX-%s-%04d", tanggal, counterHarian++);
    }

    /**
     * Memulai sesi transaksi belanja baru.
     */
    public void mulaiTransaksiBaru() {
        this.transaksiAktif = new Transaksi(generateIdTransaksiBaru());
    }

    public Transaksi getTransaksiAktif() {
        return transaksiAktif;
    }

    /**
     * Menambahkan item belanja ke keranjang transaksi aktif.
     */
    public void tambahItemKeKeranjang(Produk produk, int jumlah) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh kosong.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah harus minimal 1.");
        }

        // Hitung total kuantitas produk ini yang sudah ada di keranjang
        int qtyDiKeranjang = 0;
        for (ItemTransaksi item : transaksiAktif.getDaftarItem()) {
            if (item.getProduk().getKode().equalsIgnoreCase(produk.getKode())) {
                qtyDiKeranjang += item.getJumlah();
            }
        }

        if (qtyDiKeranjang + jumlah > produk.getStok()) {
            throw new IllegalStateException(
                "Stok '" + produk.getNama() + "' tidak cukup!\n" +
                "Tersedia di gudang: " + produk.getStok() + "\n" +
                "Sudah di keranjang: " + qtyDiKeranjang + "\n" +
                "Maksimal bisa ditambah: " + (produk.getStok() - qtyDiKeranjang)
            );
        }

        transaksiAktif.tambahItem(produk, jumlah);
    }

    /**
     * Menghapus baris item dari keranjang aktif.
     */
    public void hapusItemDariKeranjang(int index) {
        transaksiAktif.hapusItem(index);
    }

    /**
     * Mengosongkan keranjang aktif.
     */
    public void resetKeranjang() {
        transaksiAktif.kosongkan();
    }

    /**
     * Menyelesaikan checkout pembayaran:
     * 1. Validasi kecukupan uang tunai
     * 2. Kurangi stok setiap produk secara permanen
     * 3. Simpan transaksi ke riwayat
     * 4. Siapkan transaksi baru untuk pembeli selanjutnya
     * 
     * @param uangBayar jumlah nominal uang tunai dari pelanggan
     * @return Objek Transaksi yang telah selesai (lengkap dengan struk)
     */
    public Transaksi prosesCheckout(double uangBayar) {
        if (transaksiAktif.getDaftarItem().isEmpty()) {
            throw new IllegalStateException("Keranjang belanja masih kosong! Tambahkan produk terlebih dahulu.");
        }

        if (uangBayar < transaksiAktif.getGrandTotal()) {
            throw new IllegalArgumentException(
                "Uang bayar kurang! Total: " + transaksiAktif.getGrandTotal() + ", Diterima: " + uangBayar
            );
        }

        // Kurangi stok riil tiap produk
        for (ItemTransaksi item : transaksiAktif.getDaftarItem()) {
            item.getProduk().kurangiStok(item.getJumlah());
        }

        // Catat pembayaran
        boolean sukses = transaksiAktif.bayar(uangBayar);
        if (!sukses) {
            throw new IllegalStateException("Gagal memproses pembayaran.");
        }

        Transaksi transaksiSelesai = this.transaksiAktif;
        riwayatTransaksi.add(transaksiSelesai);

        // Siapkan sesi transaksi baru
        mulaiTransaksiBaru();

        return transaksiSelesai;
    }

    public List<Transaksi> getRiwayatTransaksi() {
        return Collections.unmodifiableList(riwayatTransaksi);
    }
}
