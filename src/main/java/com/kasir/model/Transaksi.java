package com.kasir.model;

import com.kasir.util.RupiahHelper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class Transaksi
 * Mengelola satu sesi transaksi penjualan kasir (Point of Sales).
 */
public class Transaksi {

    private String idTransaksi;
    private LocalDateTime waktu;
    private List<ItemTransaksi> daftarItem;
    private double totalKotor;
    private double totalDiskon;
    private double totalPajak;
    private double grandTotal;
    private double jumlahBayar;
    private double kembalian;
    private boolean sudahSelesai;

    public Transaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
        this.waktu = LocalDateTime.now();
        this.daftarItem = new ArrayList<>();
        this.sudahSelesai = false;
    }

    public void tambahItem(Produk produk, int jumlah) {
        if (sudahSelesai) {
            throw new IllegalStateException("Transaksi sudah selesai, tidak bisa menambah item.");
        }
        
        // Jika produk sudah ada di keranjang, tambahkan jumlahnya
        for (ItemTransaksi item : daftarItem) {
            if (item.getProduk().getKode().equalsIgnoreCase(produk.getKode())) {
                item.setJumlah(item.getJumlah() + jumlah);
                hitungTotal();
                return;
            }
        }

        // Jika belum ada, buat baris item baru
        daftarItem.add(new ItemTransaksi(produk, jumlah));
        hitungTotal();
    }

    public void hapusItem(int index) {
        if (index >= 0 && index < daftarItem.size()) {
            daftarItem.remove(index);
            hitungTotal();
        }
    }

    public void kosongkan() {
        daftarItem.clear();
        hitungTotal();
    }

    /**
     * Menghitung ulang seluruh komponen biaya dalam transaksi.
     */
    public void hitungTotal() {
        totalKotor = 0.0;
        totalDiskon = 0.0;
        totalPajak = 0.0;

        for (ItemTransaksi item : daftarItem) {
            totalKotor += item.getSubtotal();
            totalDiskon += item.getDiskon();
            totalPajak += item.getPajak();
        }

        grandTotal = totalKotor - totalDiskon + totalPajak;
        if (grandTotal < 0) {
            grandTotal = 0;
        }
    }

    /**
     * Memproses pembayaran kasir.
     * @param bayar Uang tunai yang diserahkan pelanggan
     * @return true jika pembayaran sukses dan cukup
     */
    public boolean bayar(double bayar) {
        if (bayar < grandTotal) {
            return false;
        }
        this.jumlahBayar = bayar;
        this.kembalian = bayar - grandTotal;
        this.sudahSelesai = true;
        this.waktu = LocalDateTime.now();
        return true;
    }

    /**
     * Mencetak struk transaksi dalam format teks terstruktur.
     */
    public String generateStrukTeks() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("              KASIR FROZEN              \n");
        sb.append("   Spesialis Aneka Frozen Food & Es     \n");
        sb.append("       Jl. Merdeka No. 45, Indonesia    \n");
        sb.append("========================================\n");
        sb.append(String.format("No. TRX   : %s\n", idTransaksi));
        sb.append(String.format("Waktu     : %s\n", waktu.format(dtf)));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %8s\n", "Item", "Qty", "Harga", "Total"));
        sb.append("----------------------------------------\n");

        for (ItemTransaksi item : daftarItem) {
            String nama = item.getProduk().getNama();
            if (nama.length() > 18) {
                nama = nama.substring(0, 15) + "...";
            }
            sb.append(String.format("%-18s %4d %8.0f %8.0f\n",
                    nama,
                    item.getJumlah(),
                    item.getProduk().getHarga(),
                    item.getSubtotal()));
            
            if (item.getDiskon() > 0) {
                sb.append(String.format("  > Diskon Promo: -%s\n", RupiahHelper.format(item.getDiskon())));
            }
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-24s : %13s\n", "Subtotal", RupiahHelper.format(totalKotor)));
        sb.append(String.format("%-24s : -%12s\n", "Total Diskon", RupiahHelper.format(totalDiskon)));
        sb.append(String.format("%-24s : +%12s\n", "Pajak (PB1/PPN)", RupiahHelper.format(totalPajak)));
        sb.append("========================================\n");
        sb.append(String.format("%-24s : %13s\n", "GRAND TOTAL", RupiahHelper.format(grandTotal)));
        sb.append(String.format("%-24s : %13s\n", "Tunai / Bayar", RupiahHelper.format(jumlahBayar)));
        sb.append(String.format("%-24s : %13s\n", "Kembalian", RupiahHelper.format(kembalian)));
        sb.append("========================================\n");
        sb.append("      Terima Kasih Atas Kunjungan Anda   \n");
        sb.append("        Barang yang sudah dibeli        \n");
        sb.append("         tidak dapat ditukar            \n");
        sb.append("========================================\n");

        return sb.toString();
    }

    // Getters
    public String getIdTransaksi() {
        return idTransaksi;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public List<ItemTransaksi> getDaftarItem() {
        return Collections.unmodifiableList(daftarItem);
    }

    public double getTotalKotor() {
        return totalKotor;
    }

    public double getTotalDiskon() {
        return totalDiskon;
    }

    public double getTotalPajak() {
        return totalPajak;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public boolean isSudahSelesai() {
        return sudahSelesai;
    }
}
