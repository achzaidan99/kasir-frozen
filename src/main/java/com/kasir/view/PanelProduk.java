package com.kasir.view;

import com.kasir.model.Produk;
import com.kasir.model.ProdukMakanan;
import com.kasir.model.ProdukMinuman;
import com.kasir.service.ProdukService;
import com.kasir.util.RupiahHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Manajemen Produk (CRUD - Create, Read, Update, Delete)
 * Mengizinkan kasir/admin mengelola data barang.
 */
public class PanelProduk extends JPanel {

    private final ProdukService produkService;
    private final Runnable onDataChangedCallback;

    // Komponen Form Input
    private JTextField txtKode;
    private JTextField txtNama;
    private JComboBox<String> cmbKategori;
    private JTextField txtHarga;
    private JSpinner spnStok;
    private JCheckBox chkOpsiKhusus;
    private JLabel lblOpsiKhusus;

    // Tombol Aksi
    private JButton btnSimpan;
    private JButton btnUpdate;
    private JButton btnHapus;
    private JButton btnReset;

    // Filter & Tabel
    private JTextField txtCari;
    private JTable tabelProduk;
    private DefaultTableModel modelTabel;

    // Tracking baris terpilih
    private String kodeTerpilih = null;

    public PanelProduk(ProdukService produkService, Runnable onDataChangedCallback) {
        this.produkService = produkService;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        inisialisasiKomponen();
        muatDataTabel(produkService.getSemuaProduk());
    }

    private void inisialisasiKomponen() {
        // ==========================================
        // 1. PANEL KIRI: FORM INPUT CRUD
        // ==========================================
        JPanel panelKiri = new JPanel(new BorderLayout(10, 10));
        panelKiri.setPreferredSize(new Dimension(360, 0));
        panelKiri.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            "Form Input Data Produk (CRUD)",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 13)
        ));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabel = new Font("Segoe UI", Font.PLAIN, 12);
        Font fontInput = new Font("Segoe UI", Font.PLAIN, 12);

        // Baris 0: Kode Produk
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblKode = new JLabel("Kode Produk:");
        lblKode.setFont(fontLabel);
        panelForm.add(lblKode, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        txtKode = new JTextField();
        txtKode.setFont(fontInput);
        panelForm.add(txtKode, gbc);

        // Baris 1: Nama Produk
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblNama = new JLabel("Nama Produk:");
        lblNama.setFont(fontLabel);
        panelForm.add(lblNama, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        txtNama = new JTextField();
        txtNama.setFont(fontInput);
        panelForm.add(txtNama, gbc);

        // Baris 2: Kategori (Menentukan Subclass: Makanan / Minuman)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblKategori = new JLabel("Kategori:");
        lblKategori.setFont(fontLabel);
        panelForm.add(lblKategori, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        cmbKategori = new JComboBox<>(new String[]{"Makanan", "Minuman"});
        cmbKategori.setFont(fontInput);
        cmbKategori.addActionListener(e -> sesuaikanOpsiKategori());
        panelForm.add(cmbKategori, gbc);

        // Baris 3: Harga
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblHarga = new JLabel("Harga Satuan (Rp):");
        lblHarga.setFont(fontLabel);
        panelForm.add(lblHarga, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        txtHarga = new JTextField();
        txtHarga.setFont(fontInput);
        panelForm.add(txtHarga, gbc);

        // Baris 4: Stok
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblStok = new JLabel("Jumlah Stok:");
        lblStok.setFont(fontLabel);
        panelForm.add(lblStok, gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        spnStok = new JSpinner(new SpinnerNumberModel(10, 0, 99999, 1));
        spnStok.setFont(fontInput);
        panelForm.add(spnStok, gbc);

        // Baris 5: Opsi Polimorfisme Khusus (Pedas / Dingin)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        lblOpsiKhusus = new JLabel("Varian Pedas:");
        lblOpsiKhusus.setFont(fontLabel);
        panelForm.add(lblOpsiKhusus, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        chkOpsiKhusus = new JCheckBox("Ya");
        chkOpsiKhusus.setFont(fontInput);
        panelForm.add(chkOpsiKhusus, gbc);

        panelKiri.add(panelForm, BorderLayout.CENTER);

        // Panel Tombol Aksi CRUD
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));

        btnSimpan = new JButton("Simpan");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(230, 245, 230));
        btnSimpan.addActionListener(e -> aksiSimpan());

        btnUpdate = new JButton("Perbarui");
        btnUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> aksiUpdate());

        btnHapus = new JButton("Hapus");
        btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapus.setForeground(new Color(180, 20, 20));
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> aksiHapus());

        btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.addActionListener(e -> resetForm());

        panelTombol.add(btnSimpan);
        panelTombol.add(btnUpdate);
        panelTombol.add(btnHapus);
        panelTombol.add(btnReset);

        panelKiri.add(panelTombol, BorderLayout.SOUTH);
        add(panelKiri, BorderLayout.WEST);

        // ==========================================
        // 2. PANEL KANAN: PENCARIAN & TABEL DATA
        // ==========================================
        JPanel panelKanan = new JPanel(new BorderLayout(10, 10));
        panelKanan.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 215)),
            "Daftar Produk Terdaftar",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Bar Pencarian
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JLabel lblCari = new JLabel("Pencarian:");
        lblCari.setFont(fontLabel);
        txtCari = new JTextField(25);
        txtCari.setFont(fontInput);
        txtCari.setToolTipText("Cari berdasarkan kode, nama, atau kategori...");
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { saringTabel(); }
            @Override public void removeUpdate(DocumentEvent e) { saringTabel(); }
            @Override public void changedUpdate(DocumentEvent e) { saringTabel(); }
        });

        panelCari.add(lblCari);
        panelCari.add(txtCari);
        panelKanan.add(panelCari, BorderLayout.NORTH);

        // Tabel Data
        String[] kolom = {"Kode", "Nama Produk", "Kategori", "Harga", "Stok", "Keterangan"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel read-only di grid, diedit via form
            }
        };

        tabelProduk = new JTable(modelTabel);
        tabelProduk.setFont(fontInput);
        tabelProduk.setRowHeight(24);
        tabelProduk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelProduk.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Event saat baris tabel diklik -> isi otomatis ke form
        tabelProduk.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBarisTabel();
            }
        });

        JScrollPane scrollTabel = new JScrollPane(tabelProduk);
        panelKanan.add(scrollTabel, BorderLayout.CENTER);

        add(panelKanan, BorderLayout.CENTER);
    }

    private void sesuaikanOpsiKategori() {
        String kat = (String) cmbKategori.getSelectedItem();
        if ("Makanan".equalsIgnoreCase(kat)) {
            lblOpsiKhusus.setText("Varian Pedas:");
            chkOpsiKhusus.setText("Pedas");
        } else {
            lblOpsiKhusus.setText("Sajian Minuman:");
            chkOpsiKhusus.setText("Dingin / Es");
        }
    }

    public void muatDataTabel(List<Produk> list) {
        modelTabel.setRowCount(0);
        for (Produk p : list) {
            modelTabel.addRow(new Object[]{
                p.getKode(),
                p.getNama(),
                p.getKategori(),
                RupiahHelper.format(p.getHarga()),
                p.getStok(),
                p.getKeteranganKhusus()
            });
        }
    }

    public void refreshData() {
        saringTabel();
    }

    private void saringTabel() {
        String keyword = txtCari.getText();
        List<Produk> hasil = produkService.cariProduk(keyword);
        muatDataTabel(hasil);
    }

    private void pilihBarisTabel() {
        int row = tabelProduk.getSelectedRow();
        if (row >= 0) {
            String kode = (String) modelTabel.getValueAt(row, 0);
            Produk p = produkService.cariBerdasarkanKode(kode);
            if (p != null) {
                this.kodeTerpilih = p.getKode();
                txtKode.setText(p.getKode());
                txtKode.setEditable(false); // Kunci kode saat edit
                txtNama.setText(p.getNama());
                cmbKategori.setSelectedItem(p.getKategori());
                txtHarga.setText(String.valueOf((long) p.getHarga()));
                spnStok.setValue(p.getStok());

                if (p instanceof ProdukMakanan) {
                    chkOpsiKhusus.setSelected(((ProdukMakanan) p).isPedas());
                } else if (p instanceof ProdukMinuman) {
                    chkOpsiKhusus.setSelected(((ProdukMinuman) p).isDingin());
                }

                btnSimpan.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnHapus.setEnabled(true);
            }
        }
    }

    private void aksiSimpan() {
        try {
            Produk baru = buatObjekProdukDariForm();
            produkService.tambahProduk(baru);
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            refreshData();
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiUpdate() {
        if (kodeTerpilih == null) return;
        try {
            Produk produkUpdate = buatObjekProdukDariForm();
            boolean sukses = produkService.perbaruiProduk(kodeTerpilih, produkUpdate);
            if (sukses) {
                JOptionPane.showMessageDialog(this, "Data produk berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                refreshData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui produk.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiHapus() {
        if (kodeTerpilih == null) return;
        int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin menghapus produk dengan kode '" + kodeTerpilih + "'?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (konfirmasi == JOptionPane.YES_OPTION) {
            boolean sukses = produkService.hapusProduk(kodeTerpilih);
            if (sukses) {
                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                refreshData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Produk tidak ditemukan atau gagal dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Produk buatObjekProdukDariForm() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        int stok = (int) spnStok.getValue();
        String kategori = (String) cmbKategori.getSelectedItem();
        boolean opsi = chkOpsiKhusus.isSelected();

        if (kode.isEmpty()) throw new IllegalArgumentException("Kode produk wajib diisi.");
        if (nama.isEmpty()) throw new IllegalArgumentException("Nama produk wajib diisi.");
        if (hargaStr.isEmpty()) throw new IllegalArgumentException("Harga wajib diisi.");

        double harga = RupiahHelper.parse(hargaStr);
        if (harga <= 0) throw new IllegalArgumentException("Harga harus bernilai positif lebih dari Rp 0.");

        // Polimorfisme: Instansiasi subclass berdasarkan pilihan user
        if ("Makanan".equalsIgnoreCase(kategori)) {
            return new ProdukMakanan(kode, nama, harga, stok, opsi);
        } else {
            return new ProdukMinuman(kode, nama, harga, stok, opsi);
        }
    }

    public void resetForm() {
        kodeTerpilih = null;
        txtKode.setText("");
        txtKode.setEditable(true);
        txtNama.setText("");
        cmbKategori.setSelectedIndex(0);
        txtHarga.setText("");
        spnStok.setValue(10);
        chkOpsiKhusus.setSelected(false);
        sesuaikanOpsiKategori();
        tabelProduk.clearSelection();

        btnSimpan.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }
}
