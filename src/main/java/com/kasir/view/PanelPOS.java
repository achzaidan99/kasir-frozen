package com.kasir.view;

import com.kasir.model.ItemTransaksi;
import com.kasir.model.Produk;
import com.kasir.model.Transaksi;
import com.kasir.service.ProdukService;
import com.kasir.service.TransaksiService;
import com.kasir.util.RupiahHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Point of Sales (POS - Kasir)
 * Antarmuka utama tempat kasir melayani transaksi pelanggan:
 * - Memilih produk & menentukan kuantitas
 * - Keranjang belanja dengan kalkulasi diskon & pajak otomatis (Polimorfisme)
 * - Input pembayaran & perhitungan kembalian instan
 * - Cetak struk belanja
 */
public class PanelPOS extends JPanel {

    private final ProdukService produkService;
    private final TransaksiService transaksiService;
    private final Runnable onDataUpdatedCallback;

    // Komponen Katalog Produk (Kiri)
    private JTextField txtCariProduk;
    private JTable tabelKatalog;
    private DefaultTableModel modelKatalog;
    private JSpinner spnQty;
    private JButton btnTambahKeranjang;

    // Komponen Keranjang (Kanan)
    private JTable tabelKeranjang;
    private DefaultTableModel modelKeranjang;
    private JButton btnHapusItem;
    private JButton btnResetKeranjang;

    // Label Rincian Biaya
    private JLabel lblSubtotal;
    private JLabel lblDiskon;
    private JLabel lblPajak;
    private JLabel lblGrandTotal;

    // Pembayaran & Kembalian
    private JTextField txtBayar;
    private JLabel lblKembalian;
    private JButton btnCheckout;

    public PanelPOS(ProdukService produkService, TransaksiService transaksiService, Runnable onDataUpdatedCallback) {
        this.produkService = produkService;
        this.transaksiService = transaksiService;
        this.onDataUpdatedCallback = onDataUpdatedCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        inisialisasiUI();
        muatKatalog(produkService.getSemuaProduk());
        segarkanKeranjang();
    }

    private void inisialisasiUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buatPanelKiriKatalog(), buatPanelKananKeranjang());
        splitPane.setResizeWeight(0.45);
        splitPane.setDividerSize(6);
        add(splitPane, BorderLayout.CENTER);
    }

    // ==========================================================
    // 1. PANEL KATALOG PILIHAN PRODUK (KIRI)
    // ==========================================================
    private JPanel buatPanelKiriKatalog() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            "Katalog Produk Kasir Frozen",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Pencarian Cepat
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelCari.add(new JLabel("Cari Barang:"));
        txtCariProduk = new JTextField(16);
        txtCariProduk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCariProduk.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { saringKatalog(); }
            @Override public void removeUpdate(DocumentEvent e) { saringKatalog(); }
            @Override public void changedUpdate(DocumentEvent e) { saringKatalog(); }
        });
        panelCari.add(txtCariProduk);
        panel.add(panelCari, BorderLayout.NORTH);

        // Tabel Katalog Produk
        String[] kolom = {"Kode", "Nama Produk", "Kategori", "Harga", "Stok"};
        modelKatalog = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelKatalog = new JTable(modelKatalog);
        tabelKatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelKatalog.setRowHeight(24);
        tabelKatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKatalog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(tabelKatalog), BorderLayout.CENTER);

        // Kontrol Tambah ke Keranjang
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelAksi.add(new JLabel("Qty / Jumlah:"));
        spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        spnQty.setPreferredSize(new Dimension(65, 28));
        panelAksi.add(spnQty);

        btnTambahKeranjang = new JButton("+ Tambah ke Keranjang");
        btnTambahKeranjang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTambahKeranjang.setBackground(new Color(230, 245, 230));
        btnTambahKeranjang.addActionListener(e -> aksiTambahKeKeranjang());
        panelAksi.add(btnTambahKeranjang);

        panel.add(panelAksi, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================================
    // 2. PANEL KERANJANG & CHECKOUT (KANAN)
    // ==========================================================
    private JPanel buatPanelKananKeranjang() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            "Keranjang Belanja & Pembayaran",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Tabel Keranjang
        String[] kolom = {"No", "Nama Produk", "Harga", "Qty", "Diskon", "Subtotal"};
        modelKeranjang = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelKeranjang = new JTable(modelKeranjang);
        tabelKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelKeranjang.setRowHeight(24);
        tabelKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKeranjang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel panelTabelDanTombol = new JPanel(new BorderLayout(5, 5));
        panelTabelDanTombol.add(new JScrollPane(tabelKeranjang), BorderLayout.CENTER);

        // Tombol kontrol keranjang
        JPanel panelBawahTabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        btnHapusItem = new JButton("Hapus Item");
        btnHapusItem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHapusItem.addActionListener(e -> aksiHapusItemKeranjang());

        btnResetKeranjang = new JButton("Kosongkan Keranjang");
        btnResetKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnResetKeranjang.addActionListener(e -> aksiResetKeranjang());

        panelBawahTabel.add(btnHapusItem);
        panelBawahTabel.add(btnResetKeranjang);
        panelTabelDanTombol.add(panelBawahTabel, BorderLayout.SOUTH);

        panel.add(panelTabelDanTombol, BorderLayout.CENTER);

        // Panel Pembayaran & Total Bawah
        panel.add(buatPanelRingkasanBayar(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buatPanelRingkasanBayar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Bagian Kiri: Ringkasan Biaya (Subtotal, Diskon, Pajak, Grand Total)
        JPanel panelRincian = new JPanel(new GridLayout(4, 2, 6, 4));
        Font fontRincian = new Font("Segoe UI", Font.PLAIN, 12);

        JLabel l1 = new JLabel("Subtotal:");
        l1.setFont(fontRincian);
        lblSubtotal = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblSubtotal.setFont(fontRincian);

        JLabel l2 = new JLabel("Total Diskon:");
        l2.setFont(fontRincian);
        lblDiskon = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblDiskon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDiskon.setForeground(new Color(0, 130, 0));

        JLabel l3 = new JLabel("Pajak (PB1/PPN):");
        l3.setFont(fontRincian);
        lblPajak = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblPajak.setFont(fontRincian);

        JLabel l4 = new JLabel("GRAND TOTAL:");
        l4.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGrandTotal = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGrandTotal.setForeground(new Color(20, 40, 120));

        panelRincian.add(l1); panelRincian.add(lblSubtotal);
        panelRincian.add(l2); panelRincian.add(lblDiskon);
        panelRincian.add(l3); panelRincian.add(lblPajak);
        panelRincian.add(l4); panelRincian.add(lblGrandTotal);

        // Bagian Kanan: Input Uang Bayar, Tombol Cepat, Kembalian & Tombol Checkout
        JPanel panelBayar = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        JLabel lblUangBayar = new JLabel("Uang Bayar (Rp):");
        lblUangBayar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBayar.add(lblUangBayar, g);

        g.gridx = 1; g.gridy = 0;
        txtBayar = new JTextField(12);
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtBayar.setHorizontalAlignment(JTextField.RIGHT);
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });
        panelBayar.add(txtBayar, g);

        // Tombol Cepat Uang Pas & Pecahan
        JPanel panelPecahan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        JButton btnPas = new JButton("Pas");
        btnPas.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnPas.addActionListener(e -> {
            txtBayar.setText(String.valueOf((long) transaksiService.getTransaksiAktif().getGrandTotal()));
        });

        JButton btn50k = new JButton("50k");
        btn50k.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn50k.addActionListener(e -> txtBayar.setText("50000"));

        JButton btn100k = new JButton("100k");
        btn100k.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn100k.addActionListener(e -> txtBayar.setText("100000"));

        panelPecahan.add(btnPas);
        panelPecahan.add(btn50k);
        panelPecahan.add(btn100k);

        g.gridx = 1; g.gridy = 1;
        panelBayar.add(panelPecahan, g);

        // Kembalian
        g.gridx = 0; g.gridy = 2;
        JLabel lblLabelKembali = new JLabel("Kembalian:");
        lblLabelKembali.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBayar.add(lblLabelKembali, g);

        g.gridx = 1; g.gridy = 2;
        lblKembalian = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKembalian.setForeground(new Color(15, 120, 30));
        panelBayar.add(lblKembalian, g);

        // Tombol Bayar / Selesai Transaksi
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        btnCheckout = new JButton("PROSES BAYAR & CETAK STRUK");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckout.setPreferredSize(new Dimension(220, 36));
        btnCheckout.setBackground(new Color(20, 110, 190));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFocusPainted(false);
        btnCheckout.addActionListener(e -> aksiCheckout());
        panelBayar.add(btnCheckout, g);

        panel.add(panelRincian, BorderLayout.WEST);
        panel.add(panelBayar, BorderLayout.EAST);

        return panel;
    }

    // ==========================================================
    // LOGIKA OPERASI POS & PEMBAYARAN
    // ==========================================================

    public void muatKatalog(List<Produk> list) {
        modelKatalog.setRowCount(0);
        for (Produk p : list) {
            modelKatalog.addRow(new Object[]{
                p.getKode(),
                p.getNama(),
                p.getKategori(),
                RupiahHelper.format(p.getHarga()),
                p.getStok()
            });
        }
    }

    public void segarkanKatalogDanKeranjang() {
        saringKatalog();
        segarkanKeranjang();
    }

    private void saringKatalog() {
        String keyword = txtCariProduk.getText();
        List<Produk> hasil = produkService.cariProduk(keyword);
        muatKatalog(hasil);
    }

    private void aksiTambahKeKeranjang() {
        int selectedRow = tabelKatalog.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih produk dari tabel katalog terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kode = (String) modelKatalog.getValueAt(selectedRow, 0);
        Produk produk = produkService.cariBerdasarkanKode(kode);
        if (produk == null) return;

        int qty = (int) spnQty.getValue();
        try {
            transaksiService.tambahItemKeKeranjang(produk, qty);
            segarkanKeranjang();
            spnQty.setValue(1); // Reset qty kembali ke 1
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menambah Item", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiHapusItemKeranjang() {
        int row = tabelKeranjang.getSelectedRow();
        if (row >= 0) {
            transaksiService.hapusItemDariKeranjang(row);
            segarkanKeranjang();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih item keranjang yang ingin dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void aksiResetKeranjang() {
        if (transaksiService.getTransaksiAktif().getDaftarItem().isEmpty()) return;
        int konfirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin mengosongkan keranjang belanja?",
            "Konfirmasi",
            JOptionPane.YES_NO_OPTION
        );
        if (konfirm == JOptionPane.YES_OPTION) {
            transaksiService.resetKeranjang();
            segarkanKeranjang();
        }
    }

    private void segarkanKeranjang() {
        modelKeranjang.setRowCount(0);
        Transaksi trx = transaksiService.getTransaksiAktif();

        int no = 1;
        for (ItemTransaksi item : trx.getDaftarItem()) {
            modelKeranjang.addRow(new Object[]{
                no++,
                item.getProduk().getNama(),
                RupiahHelper.format(item.getProduk().getHarga()),
                item.getJumlah(),
                RupiahHelper.format(item.getDiskon()),
                RupiahHelper.format(item.getSubtotal())
            });
        }

        lblSubtotal.setText(RupiahHelper.format(trx.getTotalKotor()));
        lblDiskon.setText(RupiahHelper.format(trx.getTotalDiskon()));
        lblPajak.setText(RupiahHelper.format(trx.getTotalPajak()));
        lblGrandTotal.setText(RupiahHelper.format(trx.getGrandTotal()));

        hitungKembalian();
    }

    private void hitungKembalian() {
        double grandTotal = transaksiService.getTransaksiAktif().getGrandTotal();
        double bayar = RupiahHelper.parse(txtBayar.getText());

        if (bayar >= grandTotal && grandTotal > 0) {
            lblKembalian.setText(RupiahHelper.format(bayar - grandTotal));
            lblKembalian.setForeground(new Color(15, 120, 30));
        } else {
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(Color.GRAY);
        }
    }

    private void aksiCheckout() {
        Transaksi trx = transaksiService.getTransaksiAktif();
        if (trx.getDaftarItem().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double bayar = RupiahHelper.parse(txtBayar.getText());
        if (bayar < trx.getGrandTotal()) {
            JOptionPane.showMessageDialog(this, 
                "Uang bayar kurang! Total: " + RupiahHelper.format(trx.getGrandTotal()) + 
                "\nUang yang diinput: " + RupiahHelper.format(bayar),
                "Pembayaran Kurang",
                JOptionPane.WARNING_MESSAGE);
            txtBayar.requestFocus();
            return;
        }

        try {
            // Proses checkout (mengurangi stok dan mencatat transaksi)
            Transaksi trxSelesai = transaksiService.prosesCheckout(bayar);

            // Bersihkan field bayar
            txtBayar.setText("");

            // Segarkan keranjang dan katalog
            segarkanKatalogDanKeranjang();

            // Panggil callback agar tabel CRUD di tab sebelah juga terbarui stoknya
            if (onDataUpdatedCallback != null) {
                onDataUpdatedCallback.run();
            }

            // Tampilkan Dialog Struk Pembayaran
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            DialogStruk dialog = new DialogStruk(parentFrame, trxSelesai.generateStrukTeks());
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat checkout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
