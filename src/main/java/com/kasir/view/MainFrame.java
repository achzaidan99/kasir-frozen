package com.kasir.view;

import com.kasir.service.ProdukService;
import com.kasir.service.TransaksiService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * MainFrame Window Utama Aplikasi Kasir.
 * Menghubungkan Tab Point of Sales (POS) dan Tab Manajemen Produk (CRUD).
 */
public class MainFrame extends JFrame {

    private final ProdukService produkService;
    private final TransaksiService transaksiService;

    private PanelPOS panelPOS;
    private PanelProduk panelProduk;
    private JTabbedPane tabbedPane;

    public MainFrame() {
        super("Kasir Frozen - Sistem Point of Sales & Kelola Produk (Java OOP)");
        
        this.produkService = new ProdukService();
        this.transaksiService = new TransaksiService();

        inisialisasiUI();
    }

    private void inisialisasiUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1020, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. HEADER ATAS
        // ==========================================
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 2, 2));
        panelHeader.setBackground(new Color(24, 43, 73));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel lblJudul = new JLabel("KASIR FROZEN - POINT OF SALES (POS)");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJudul.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Aplikasi Kasir Frozen Food Berorientasi Objek: Enkapsulasi, Abstraksi, Pewarisan, Polimorfisme");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 220, 240));

        panelHeader.add(lblJudul);
        panelHeader.add(lblSub);
        add(panelHeader, BorderLayout.NORTH);

        // ==========================================
        // 2. TAB KONTEN UTAMA
        // ==========================================
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Panel POS & Panel Produk saling terhubung sinkronisasi data
        panelProduk = new PanelProduk(produkService, () -> {
            panelPOS.segarkanKatalogDanKeranjang();
        });

        panelPOS = new PanelPOS(produkService, transaksiService, () -> {
            panelProduk.refreshData();
        });

        tabbedPane.addTab("  Point of Sales (Kasir)  ", panelPOS);
        tabbedPane.addTab("  Kelola Produk (CRUD)  ", panelProduk);

        // Saat kasir berpindah tab, otomatis sinkronkan data
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                panelPOS.segarkanKatalogDanKeranjang();
            } else if (selectedIndex == 1) {
                panelProduk.refreshData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // ==========================================
        // 3. STATUS BAR BAWAH
        // ==========================================
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 215, 225)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        statusBar.setBackground(new Color(245, 247, 250));

        JLabel lblStatus = new JLabel("Status: Kasir Frozen Siap Melayani  |  Operator: Kasir Aktif");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(90, 100, 110));

        JLabel lblVersi = new JLabel("NetBeans Ready  |  Java SE 17+");
        lblVersi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersi.setForeground(new Color(110, 120, 130));

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblVersi, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }
}
