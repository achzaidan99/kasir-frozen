package com.kasir.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Dialog Modal untuk menampilkan Struk Pembayaran Kasir.
 */
public class DialogStruk extends JDialog {

    private final JTextArea txtStruk;

    public DialogStruk(Frame parent, String teksStruk) {
        super(parent, "Struk Pembayaran", true);
        setLayout(new BorderLayout(10, 10));
        setSize(420, 560);
        setLocationRelativeTo(parent);

        // Text area dengan font monospaced ala thermal printer kasir
        txtStruk = new JTextArea(teksStruk);
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStruk.setEditable(false);
        txtStruk.setBackground(new Color(250, 250, 250));
        txtStruk.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        add(scrollPane, BorderLayout.CENTER);

        // Panel tombol bawah
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnSalin = new JButton("Salin Teks Struk");
        btnSalin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSalin.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(txtStruk.getText()), null
            );
            JOptionPane.showMessageDialog(this, "Teks struk berhasil disalin ke clipboard!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTutup.setPreferredSize(new Dimension(80, 30));
        btnTutup.addActionListener(e -> dispose());

        panelBawah.add(btnSalin);
        panelBawah.add(btnTutup);
        add(panelBawah, BorderLayout.SOUTH);
    }
}
