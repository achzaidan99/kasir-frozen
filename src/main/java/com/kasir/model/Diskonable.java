package com.kasir.model;

/**
 * Interface Diskonable
 * Merupakan contoh penerapan konsep ABSTRAKSI (Interface) dalam OOP.
 * Setiap kelas yang mengimplementasikan interface ini wajib menentukan
 * bagaimana perhitungan diskon dilakukan sesuai aturan bisnisnya.
 */
public interface Diskonable {
    
    /**
     * Menghitung nilai diskon dalam rupiah berdasarkan jumlah item yang dibeli.
     * @param jumlah quantity item yang dibeli
     * @return besar diskon dalam rupiah
     */
    double hitungDiskon(int jumlah);
}
