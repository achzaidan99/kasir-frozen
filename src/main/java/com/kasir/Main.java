package com.kasir;

import com.kasir.view.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry Point Utama Aplikasi Kasir.
 * Menjalankan antarmuka grafis (GUI) di thread Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Terapkan System Look and Feel agar tampilan GUI mengikuti tema OS native dan elegan
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Jika look and feel sistem gagal, fallback otomatis ke default Java
        }

        // Jalankan aplikasi di Swing EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
