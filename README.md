# Kasir Frozen - Aplikasi Point of Sales (POS) & Manajemen Produk (Java SE - NetBeans)

**Kasir Frozen** adalah aplikasi kasir desktop berbasis **Java Swing** bertema toko aneka makanan beku (*frozen food*) dan es/minuman segar. Dirancang khusus untuk memenuhi standar tugas akademik pemrograman berorientasi objek (OOP). Dilengkapi dengan fitur **CRUD (Kelola Produk Frozen)** dan **Point of Sales (Kasir & Cetak Struk)** dengan pemisahan package/folder yang rapi dan modular.

---

## 📁 Struktur Folder & Arsitektur (Separation of Concerns)

```text
kasir/
├── pom.xml                                    # Konfigurasi Maven (NetBeans Native Project)
├── run.bat                                    # Skrip cepat untuk kompilasi & jalankan di Windows
├── buka_web.bat                               # Skrip membuka versi Web langsung di browser
├── README.md                                  # Dokumentasi lengkap
├── web/                                       # Versi Web Application (HTML5, CSS3, ES6 JS)
│   ├── index.html                             # Halaman utama Web POS & Katalog
│   ├── css/
│   │   └── style.css                          # Desain antarmuka bersih & responsif
│   └── js/
│       └── app.js                             # Mesin POS & Logika OOP (ES6 Classes)
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── kasir/
    │               ├── Main.java              # Titik masuk program (Entry Point)
    │               ├── model/                 # Entitas Data & OOP Core
    │               │   ├── Diskonable.java    # Interface (Abstraksi)
    │               │   ├── Produk.java        # Abstract Class (Abstraksi & Enkapsulasi)
    │               │   ├── ProdukMakanan.java # Subclass Makanan (Pewarisan & Polimorfisme)
    │               │   ├── ProdukMinuman.java # Subclass Minuman (Pewarisan & Polimorfisme)
    │               │   ├── ItemTransaksi.java # Representasi item di keranjang belanja
    │               │   └── Transaksi.java     # Kalkulasi total, bayar, kembalian, & struk
    │               ├── service/               # Logika Bisnis & Pengolahan Data
    │               │   ├── ProdukService.java    # Operasi CRUD (Create, Read, Update, Delete)
    │               │   └── TransaksiService.java # Operasi POS Kasir, Checkout, & Potong Stok
    │               ├── util/                  # Helper & Format Data
    │               │   └── RupiahHelper.java  # Pemformatan mata uang Rupiah (Rp)
    │               └── view/                  # Antarmuka Pengguna (GUI Swing)
    │                   ├── MainFrame.java     # Window Utama dengan Tab POS & Tab CRUD
    │                   ├── PanelPOS.java      # Antarmuka Kasir (Katalog, Keranjang, Bayar)
    │                   ├── PanelProduk.java   # Antarmuka Kelola Produk (Form & Tabel CRUD)
    │                   └── DialogStruk.java   # Modal dialog struk transaksi pembayaran
    └── test/
        └── java/
            └── com/kasir/
                └── TestKasir.java             # Pengujian logika otomatis tanpa GUI
```

---

## 🧩 Pemetaan Konsep Pemrograman Berorientasi Objek (OOP)

| Konsep OOP | Implementasi di Proyek | Keterangan / Bukti Kode |
| :--- | :--- | :--- |
| **Atribut & Metode** | Seluruh Class | Setiap class memiliki atribut data (`kode`, `nama`, `harga`, `stok`) dan metode perilaku (`hitungSubtotal()`, `kurangiStok()`, `bayar()`). |
| **Enkapsulasi** | `Produk.java`, `Transaksi.java`, dll. | Atribut berstatus `private`. Akses melalui `getter` dan `setter` dengan validasi ketat (misal: harga dan stok dilarang bernilai negatif). |
| **Abstraksi (Interface)** | `Diskonable.java` | Interface dengan kontrak method `double hitungDiskon(int jumlah)`. |
| **Abstraksi (Abstract Class)**| `Produk.java` | Kelas abstrak yang memiliki abstract method `getKategori()`, `hitungPajak()`, dan `getKeteranganKhusus()`. Tidak dapat diinstansiasi secara langsung. |
| **Pewarisan (Inheritance)** | `ProdukMakanan.java` & `ProdukMinuman.java` | `ProdukMakanan extends Produk` dan `ProdukMinuman extends Produk`. Mewarisi semua field dan method kelas `Produk` menggunakan `super()`. |
| **Polimorfisme (Overriding)**| `hitungDiskon()`, `hitungPajak()`, `getKategori()` | - Makanan menerapkan PB1 10% dan diskon 10% jika beli >= 5.<br>- Minuman menerapkan pajak 5% dan diskon 5% jika beli >= 3. |
| **Polimorfisme (Dinamis)** | `ItemTransaksi.java` & `List<Produk>` | Koleksi `List<Produk>` menampung beragam subclass. Saat `produk.hitungDiskon()` atau `produk.hitungPajak()` dipanggil, Java secara otomatis menjalankan logika dari subclass aslinya. |

---

## ⚡ Fitur Utama Aplikasi

### 1. Point of Sales (POS / Kasir)
- **Katalog Produk:** Menampilkan daftar produk siap jual lengkap dengan sisa stok secara *real-time*.
- **Pencarian Cepat:** Mencari nama atau kode barang secara instan saat mengetik.
- **Keranjang Belanja:** Memilih jumlah (quantity), menghitung subtotal, diskon otomatis, dan pajak restoran secara transparan.
- **Validasi Stok:** Mencegah kasir memesan melebihi stok yang tersedia di gudang.
- **Kalkulasi Pembayaran:** Menghitung total belanja, tombol uang pas / pecahan cepat (50k, 100k), dan menampilkan uang kembalian secara instan.
- **Cetak Struk Transaksi:** Menghasilkan struk belanja ala thermal printer dengan opsi salin teks.
- **Pengurangan Stok Otomatis:** Stok berkurang otomatis pada katalog dan tab manajemen produk saat transaksi berhasil.

### 2. Kelola Produk (CRUD)
- **Create (Tambah):** Menambahkan produk baru (bisa memilih kategori Makanan atau Minuman, varian pedas/dingin).
- **Read (Tampil):** Menampilkan semua produk dalam tabel dengan filter pencarian langsung.
- **Update (Perbarui):** Klik salah satu baris produk pada tabel, form otomatis terisi, ubah data lalu klik tombol *Perbarui*.
- **Delete (Hapus):** Menghapus produk dari database lokal dengan dialog konfirmasi keamanan.

---

## 🚀 Panduan Membuka & Menjalankan di NetBeans

1. **Buka NetBeans IDE**.
2. Klik menu **File** -> **Open Project...**
3. Arahkan ke folder proyek: `kasir` (NetBeans akan mendeteksi ikon cangkir kopi / Maven).
4. Klik tombol **Open Project**.
5. Tunggu sesaat hingga NetBeans selesai memuat dependensi.
6. Klik kanan pada proyek **Kasir Frozen POS** di panel *Projects* -> pilih **Run** (atau tekan **F6** / tombol ikon segitiga hijau).
7. Jika diminta memilih *Main Class*, pilih: `com.kasir.Main`.

---

## 💻 Menjalankan Tanpa NetBeans (Direct Run via Windows)

Anda juga dapat langsung menjalankan aplikasi desktop tanpa membuka IDE:
- Cukup **klik dua kali (double click)** file `run.bat` pada folder proyek.
- Skrip akan otomatis mengompilasi file Java dan langsung membuka GUI Kasir Frozen.

---

## 🌐 Menjalankan Versi Website (Web POS & Katalog)

Aplikasi ini juga dilengkapi dengan antarmuka web modern responsif:
- Cukup **klik dua kali** file `buka_web.bat` atau buka file `web/index.html` langsung di browser Anda (Chrome, Edge, Firefox).
- Fitur versi web:
  - **Katalog & Keranjang POS:** Tambah produk, atur kuantitas, hitung otomatis subtotal, promo diskon, pajak, dan kembalian.
  - **Cetak Struk Thermal:** Struk belanja lengkap dengan opsi salin teks & cetak langsung (*print ready*).
  - **CRUD Produk:** Menambah, mengedit, dan menghapus produk dengan penyimpanan otomatis di *LocalStorage*.
  - **Riwayat Transaksi:** Mencatat seluruh transaksi yang telah berhasil dilakukan.
