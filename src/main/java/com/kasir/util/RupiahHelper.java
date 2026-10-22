package com.kasir.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility Helper untuk pemformatan mata uang Rupiah (IDR).
 */
public class RupiahHelper {

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(INDONESIA);

    static {
        CURRENCY_FORMAT.setMaximumFractionDigits(0);
        CURRENCY_FORMAT.setMinimumFractionDigits(0);
    }

    /**
     * Format angka ke bentuk Rupiah, contoh: Rp 25.000
     */
    public static synchronized String format(double nominal) {
        return CURRENCY_FORMAT.format(nominal).replace(",00", "");
    }

    /**
     * Parse string angka atau rupiah kembali menjadi double murni.
     * Mendukung input seperti: "25000", "25.000", "Rp 25.000".
     */
    public static double parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0.0;
        }
        // Bersihkan karakter non-digit selain koma/titik jika ada
        String sanitized = input.replaceAll("[^0-9]", "");
        if (sanitized.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(sanitized);
    }
}
