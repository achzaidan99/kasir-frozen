/**
 * ========================================================
 * KASIR FROZEN - WEB APPLICATION LOGIC (OOP & POS ENGINE)
 * ========================================================
 */

// Format mata uang Rupiah
function formatRupiah(number) {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0
  }).format(number);
}

// ========================================================
// 1. OOP MODEL CLASSES (ES6)
// ========================================================

/**
 * Kelas Dasar Produk (Abstraksi & Enkapsulasi)
 */
class Produk {
  constructor(kode, nama, harga, stok, opsiKhusus = false) {
    this._kode = kode.toUpperCase();
    this._nama = nama;
    this._harga = Number(harga);
    this._stok = Number(stok);
    this._opsiKhusus = Boolean(opsiKhusus);
  }

  get kode() { return this._kode; }
  get nama() { return this._nama; }
  get harga() { return this._harga; }
  get stok() { return this._stok; }
  get opsiKhusus() { return this._opsiKhusus; }

  set stok(val) {
    if (val < 0) throw new Error("Stok tidak boleh negatif.");
    this._stok = val;
  }

  // Method bisnis
  hitungSubtotal(qty) {
    return this._harga * qty;
  }

  kurangiStok(qty) {
    if (this._stok < qty) {
      throw new Error(`Stok '${this._nama}' tidak mencukupi (Tersedia: ${this._stok}).`);
    }
    this._stok -= qty;
  }

  // Abstract methods (di-override oleh subclass)
  getKategori() { return "Umum"; }
  hitungPajak() { return 0; }
  hitungDiskon(qty) { return 0; }
  getKeteranganKhusus() { return "-"; }

  toJSON() {
    return {
      kode: this._kode,
      nama: this._nama,
      kategori: this.getKategori(),
      harga: this._harga,
      stok: this._stok,
      opsiKhusus: this._opsiKhusus
    };
  }
}

/**
 * Subclass ProdukMakanan (Pewarisan & Polimorfisme)
 */
class ProdukMakanan extends Produk {
  getKategori() {
    return "Makanan";
  }

  // Pajak restoran / PB1 10%
  hitungPajak() {
    return this.harga * 0.10;
  }

  // Promo Makanan Beku: Beli >= 5 porsi dapat diskon 10%
  hitungDiskon(qty) {
    if (qty >= 5) {
      return (this.harga * qty) * 0.10;
    }
    return 0;
  }

  getKeteranganKhusus() {
    return this.opsiKhusus 
      ? "Varian Pedas (PB1 10%, Diskon 10% min. 5)" 
      : "Varian Reguler (PB1 10%, Diskon 10% min. 5)";
  }
}

/**
 * Subclass ProdukMinuman (Pewarisan & Polimorfisme)
 */
class ProdukMinuman extends Produk {
  getKategori() {
    return "Minuman";
  }

  // Pajak minuman 5%
  hitungPajak() {
    return this.harga * 0.05;
  }

  // Promo Minuman/Es: Beli >= 3 cup dapat diskon 5%
  hitungDiskon(qty) {
    if (qty >= 3) {
      return (this.harga * qty) * 0.05;
    }
    return 0;
  }

  getKeteranganKhusus() {
    return this.opsiKhusus 
      ? "Sajian Dingin/Es Beku (Pajak 5%, Diskon 5% min. 3)" 
      : "Sajian Normal (Pajak 5%, Diskon 5% min. 3)";
  }
}

// Factory helper instansiasi produk
function createProdukInstance(data) {
  if (data.kategori === "Makanan") {
    return new ProdukMakanan(data.kode, data.nama, data.harga, data.stok, data.opsiKhusus);
  } else {
    return new ProdukMinuman(data.kode, data.nama, data.harga, data.stok, data.opsiKhusus);
  }
}

// ========================================================
// 2. STATE APLIKASI
// ========================================================

const DEFAULT_PRODUCTS = [
  { kode: "FRZ01", nama: "Nugget Ayam Crispy 500g", kategori: "Makanan", harga: 38000, stok: 30, opsiKhusus: false },
  { kode: "FRZ02", nama: "Sosis Sapi Bakar 500g", kategori: "Makanan", harga: 45000, stok: 25, opsiKhusus: true },
  { kode: "FRZ03", nama: "Bakso Sapi Urat Frozen 50 Biji", kategori: "Makanan", harga: 52000, stok: 20, opsiKhusus: false },
  { kode: "FRZ04", nama: "Dimsum Ayam Udang Frozen 10 Pcs", kategori: "Makanan", harga: 32000, stok: 40, opsiKhusus: true },
  { kode: "FRZ05", nama: "Kentang Shoestring French Fries 1kg", kategori: "Makanan", harga: 36000, stok: 35, opsiKhusus: false },
  { kode: "MNM01", nama: "Es Krim Durian Cup 100ml", kategori: "Minuman", harga: 15000, stok: 50, opsiKhusus: true },
  { kode: "MNM02", nama: "Frozen Yoghurt Strawberry 250ml", kategori: "Minuman", harga: 18000, stok: 30, opsiKhusus: true },
  { kode: "MNM03", nama: "Susu Pasteurisasi Frozen 1 Liter", kategori: "Minuman", harga: 22000, stok: 25, opsiKhusus: true },
  { kode: "MNM04", nama: "Es Cincau Susu Brown Sugar", kategori: "Minuman", harga: 10000, stok: 40, opsiKhusus: true }
];

let products = [];
let cart = []; // Array of { produk: Produk, qty: number }
let orderHistory = [];
let activeCategoryFilter = "ALL";
let posSearchQuery = "";
let lastCompletedOrder = null;

// Inisialisasi data dari LocalStorage
function loadInitialData() {
  const storedProd = localStorage.getItem("kasir_frozen_products");
  if (storedProd) {
    try {
      const parsed = JSON.parse(storedProd);
      products = parsed.map(p => createProdukInstance(p));
    } catch (e) {
      products = DEFAULT_PRODUCTS.map(p => createProdukInstance(p));
    }
  } else {
    products = DEFAULT_PRODUCTS.map(p => createProdukInstance(p));
    saveProductsToStorage();
  }

  const storedHist = localStorage.getItem("kasir_frozen_history");
  if (storedHist) {
    try {
      orderHistory = JSON.parse(storedHist);
    } catch (e) {
      orderHistory = [];
    }
  }
}

function saveProductsToStorage() {
  const jsonArr = products.map(p => p.toJSON());
  localStorage.setItem("kasir_frozen_products", JSON.stringify(jsonArr));
}

function saveHistoryToStorage() {
  localStorage.setItem("kasir_frozen_history", JSON.stringify(orderHistory));
}

// ========================================================
// 3. RENDER VIEWS & UI LOGIC
// ========================================================

// Render Katalog POS
function renderPOSCatalog() {
  const container = document.getElementById("posProductsContainer");
  container.innerHTML = "";

  const filtered = products.filter(p => {
    const matchCategory = activeCategoryFilter === "ALL" || p.getKategori() === activeCategoryFilter;
    const matchSearch = p.nama.toLowerCase().includes(posSearchQuery) || p.kode.toLowerCase().includes(posSearchQuery);
    return matchCategory && matchSearch;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 2.5rem; color: #64748b;">
        <p style="font-weight: 600;">Tidak ada produk yang cocok</p>
        <small>Coba gunakan kata kunci pencarian yang lain.</small>
      </div>
    `;
    return;
  }

  filtered.forEach(p => {
    const card = document.createElement("div");
    card.className = "product-card";
    const isOutOfStock = p.stok <= 0;

    card.innerHTML = `
      <div>
        <div class="card-top">
          <span class="badge-code">${p.kode}</span>
          <span class="badge-cat">${p.getKategori()}</span>
        </div>
        <h3 class="card-title">${p.nama}</h3>
        <p class="card-option">${p.getKeteranganKhusus()}</p>
      </div>
      <div class="card-footer">
        <div class="price-stock">
          <span class="product-price">${formatRupiah(p.harga)}</span>
          <span class="product-stock ${isOutOfStock ? 'out-of-stock' : ''}">
            ${isOutOfStock ? 'Stok Habis' : 'Stok: ' + p.stok}
          </span>
        </div>
        <button type="button" class="btn-add-cart" data-kode="${p.kode}" ${isOutOfStock ? 'disabled' : ''}>
          + Tambah
        </button>
      </div>
    `;

    const addBtn = card.querySelector(".btn-add-cart");
    addBtn.addEventListener("click", () => addToCart(p.kode));

    container.appendChild(card);
  });
}

// Tambah item ke keranjang
function addToCart(kode) {
  const prod = products.find(p => p.kode === kode);
  if (!prod) return;

  const existing = cart.find(c => c.produk.kode === kode);
  const currentInCart = existing ? existing.qty : 0;

  if (currentInCart + 1 > prod.stok) {
    alert(`Stok '${prod.nama}' tidak mencukupi! Tersedia hanya ${prod.stok}.`);
    return;
  }

  if (existing) {
    existing.qty += 1;
  } else {
    cart.push({ produk: prod, qty: 1 });
  }

  renderCart();
}

function updateCartQty(index, delta) {
  const item = cart[index];
  if (!item) return;

  const newQty = item.qty + delta;
  if (newQty <= 0) {
    cart.splice(index, 1);
  } else if (newQty > item.produk.stok) {
    alert(`Maksimal stok yang tersedia hanya ${item.produk.stok}.`);
    return;
  } else {
    item.qty = newQty;
  }

  renderCart();
}

function removeFromCart(index) {
  cart.splice(index, 1);
  renderCart();
}

function resetCart() {
  if (cart.length === 0) return;
  if (confirm("Kosongkan keranjang belanja?")) {
    cart = [];
    document.getElementById("inputUangBayar").value = "";
    renderCart();
  }
}

// Render Keranjang Belanja & Ringkasan Biaya
function renderCart() {
  const tbody = document.getElementById("cartTableBody");
  const emptyMsg = document.getElementById("emptyCartMessage");
  tbody.innerHTML = "";

  if (cart.length === 0) {
    emptyMsg.style.display = "block";
  } else {
    emptyMsg.style.display = "none";
    cart.forEach((item, idx) => {
      const subtotal = item.produk.hitungSubtotal(item.qty);
      const diskon = item.produk.hitungDiskon(item.qty);
      const row = document.createElement("tr");

      row.innerHTML = `
        <td>
          <span class="cart-item-name">${item.produk.nama}</span>
          <span class="cart-item-meta">${formatRupiah(item.produk.harga)}</span>
          ${diskon > 0 ? `<div style="color: #16a34a; font-size: 0.72rem;">Promo: -${formatRupiah(diskon)}</div>` : ''}
        </td>
        <td>
          <div class="cart-qty-ctrl">
            <button type="button" class="btn-qty-minus">-</button>
            <span>${item.qty}</span>
            <button type="button" class="btn-qty-plus">+</button>
          </div>
        </td>
        <td style="font-weight: 600;">${formatRupiah(subtotal)}</td>
        <td>
          <button type="button" class="btn-remove-item">&times;</button>
        </td>
      `;

      row.querySelector(".btn-qty-minus").addEventListener("click", () => updateCartQty(idx, -1));
      row.querySelector(".btn-qty-plus").addEventListener("click", () => updateCartQty(idx, 1));
      row.querySelector(".btn-remove-item").addEventListener("click", () => removeFromCart(idx));

      tbody.appendChild(row);
    });
  }

  // Hitung Total, Diskon, Pajak secara Polimorfis
  let totalKotor = 0;
  let totalDiskon = 0;
  let totalPajak = 0;

  cart.forEach(item => {
    totalKotor += item.produk.hitungSubtotal(item.qty);
    totalDiskon += item.produk.hitungDiskon(item.qty);
    totalPajak += item.produk.hitungPajak() * item.qty;
  });

  const grandTotal = Math.max(0, totalKotor - totalDiskon + totalPajak);

  document.getElementById("summarySubtotal").textContent = formatRupiah(totalKotor);
  
  const promoRow = document.getElementById("promoRow");
  if (totalDiskon > 0) {
    promoRow.style.display = "flex";
    document.getElementById("summaryDiskon").textContent = "- " + formatRupiah(totalDiskon);
  } else {
    promoRow.style.display = "none";
  }

  document.getElementById("summaryPajak").textContent = "+ " + formatRupiah(totalPajak);
  document.getElementById("summaryGrandTotal").textContent = formatRupiah(grandTotal);

  updateKembalian(grandTotal);
}

// Update Perhitungan Kembalian
function updateKembalian(grandTotal) {
  const bayarInput = document.getElementById("inputUangBayar");
  const uangBayar = Number(bayarInput.value) || 0;
  const labelKembali = document.getElementById("labelKembalian");
  const btnCheckout = document.getElementById("btnCheckout");

  if (cart.length > 0 && uangBayar >= grandTotal && grandTotal > 0) {
    const kembali = uangBayar - grandTotal;
    labelKembali.textContent = formatRupiah(kembali);
    labelKembali.style.color = "var(--success)";
    btnCheckout.disabled = false;
  } else {
    labelKembali.textContent = "Rp 0";
    labelKembali.style.color = "var(--text-muted)";
    btnCheckout.disabled = true;
  }
}

// ========================================================
// 4. PROSES CHECKOUT & STRUK
// ========================================================

function generateOrderId() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const rand = Math.floor(1000 + Math.random() * 9000);
  return `TRX-${year}${month}${day}-${rand}`;
}

function processCheckout() {
  if (cart.length === 0) return;

  let totalKotor = 0;
  let totalDiskon = 0;
  let totalPajak = 0;

  cart.forEach(item => {
    totalKotor += item.produk.hitungSubtotal(item.qty);
    totalDiskon += item.produk.hitungDiskon(item.qty);
    totalPajak += item.produk.hitungPajak() * item.qty;
  });

  const grandTotal = Math.max(0, totalKotor - totalDiskon + totalPajak);
  const uangBayar = Number(document.getElementById("inputUangBayar").value) || 0;

  if (uangBayar < grandTotal) {
    alert("Nominal pembayaran kurang!");
    return;
  }

  // 1. Potong stok tiap produk
  cart.forEach(item => {
    item.produk.kurangiStok(item.qty);
  });
  saveProductsToStorage();

  // 2. Simpan order ke riwayat
  const orderData = {
    id: generateOrderId(),
    waktu: new Date().toLocaleString('id-ID'),
    items: cart.map(c => ({
      nama: c.produk.nama,
      harga: c.produk.harga,
      qty: c.qty,
      subtotal: c.produk.hitungSubtotal(c.qty),
      diskon: c.produk.hitungDiskon(c.qty)
    })),
    totalKotor,
    totalDiskon,
    totalPajak,
    grandTotal,
    uangBayar,
    kembalian: uangBayar - grandTotal
  };

  orderHistory.unshift(orderData);
  saveHistoryToStorage();
  lastCompletedOrder = orderData;

  // 3. Reset form pembayaran & keranjang
  cart = [];
  document.getElementById("inputUangBayar").value = "";
  renderCart();
  renderPOSCatalog();
  renderCRUDTable();
  renderHistoryTable();

  // 4. Buka dialog struk pembayaran
  showReceiptModal(orderData);
}

// Generate teks thermal receipt
function formatReceiptText(order) {
  let text = "";
  text += "========================================\n";
  text += "              KASIR FROZEN              \n";
  text += "   Spesialis Aneka Frozen Food & Es     \n";
  text += "       Jl. Merdeka No. 45, Indonesia    \n";
  text += "========================================\n";
  text += `No. TRX   : ${order.id}\n`;
  text += `Waktu     : ${order.waktu}\n`;
  text += "----------------------------------------\n";
  text += "Item                 Qty    Harga  Total\n";
  text += "----------------------------------------\n";

  order.items.forEach(item => {
    let namaShort = item.nama.length > 18 ? item.nama.substring(0, 15) + "..." : item.nama;
    text += `${namaShort.padEnd(20)} ${String(item.qty).padStart(3)}  ${String(item.harga).padStart(6)}  ${String(item.subtotal).padStart(6)}\n`;
    if (item.diskon > 0) {
      text += `  > Promo Diskon: -${formatRupiah(item.diskon)}\n`;
    }
  });

  text += "----------------------------------------\n";
  text += `Subtotal                 : ${formatRupiah(order.totalKotor).padStart(13)}\n`;
  text += `Total Diskon             : -${formatRupiah(order.totalDiskon).padStart(12)}\n`;
  text += `Pajak (PB1/PPN)          : +${formatRupiah(order.totalPajak).padStart(12)}\n`;
  text += "========================================\n";
  text += `GRAND TOTAL              : ${formatRupiah(order.grandTotal).padStart(13)}\n`;
  text += `Tunai / Bayar            : ${formatRupiah(order.uangBayar).padStart(13)}\n`;
  text += `Kembalian                : ${formatRupiah(order.kembalian).padStart(13)}\n`;
  text += "========================================\n";
  text += "      Terima Kasih Atas Kunjungan Anda   \n";
  text += "        Barang yang sudah dibeli        \n";
  text += "         tidak dapat ditukar            \n";
  text += "========================================\n";

  return text;
}

function showReceiptModal(order) {
  const container = document.getElementById("receiptTextContainer");
  container.textContent = formatReceiptText(order);
  document.getElementById("modalStruk").classList.add("open");
}

// ========================================================
// 5. CRUD VIEW LOGIC
// ========================================================

function renderCRUDTable() {
  const tbody = document.getElementById("crudTableBody");
  const query = (document.getElementById("crudSearchInput").value || "").toLowerCase().trim();
  tbody.innerHTML = "";

  const filtered = products.filter(p => 
    p.nama.toLowerCase().includes(query) || 
    p.kode.toLowerCase().includes(query) || 
    p.getKategori().toLowerCase().includes(query)
  );

  filtered.forEach((p, index) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td><strong>${p.kode}</strong></td>
      <td>${p.nama}</td>
      <td><span class="badge-cat">${p.getKategori()}</span></td>
      <td>${formatRupiah(p.harga)}</td>
      <td>${p.stok}</td>
      <td><small style="color: #64748b;">${p.getKeteranganKhusus()}</small></td>
      <td>
        <div class="action-btn-group">
          <button type="button" class="btn-tbl-action edit-btn">Edit</button>
          <button type="button" class="btn-tbl-action delete delete-btn">Hapus</button>
        </div>
      </td>
    `;

    row.querySelector(".edit-btn").addEventListener("click", () => populateFormForEdit(p.kode));
    row.querySelector(".delete-btn").addEventListener("click", () => deleteProduct(p.kode));

    tbody.appendChild(row);
  });
}

function populateFormForEdit(kode) {
  const prod = products.find(p => p.kode === kode);
  if (!prod) return;

  document.getElementById("formCrudTitle").textContent = "Edit Data Produk";
  document.getElementById("editIndex").value = kode;
  document.getElementById("formKode").value = prod.kode;
  document.getElementById("formKode").readOnly = true;
  document.getElementById("formNama").value = prod.nama;
  document.getElementById("formKategori").value = prod.getKategori();
  document.getElementById("formHarga").value = prod.harga;
  document.getElementById("formStok").value = prod.stok;
  document.getElementById("formOpsiKhusus").checked = prod.opsiKhusus;

  document.getElementById("btnSubmitProduct").textContent = "Perbarui Produk";
  document.getElementById("btnDeleteSelected").style.display = "inline-block";

  updateFormOpsiLabel();
}

function resetCRUDForm() {
  document.getElementById("formCrudTitle").textContent = "Tambah Produk Baru";
  document.getElementById("editIndex").value = "-1";
  document.getElementById("formKode").value = "";
  document.getElementById("formKode").readOnly = false;
  document.getElementById("formNama").value = "";
  document.getElementById("formKategori").value = "Makanan";
  document.getElementById("formHarga").value = "";
  document.getElementById("formStok").value = "20";
  document.getElementById("formOpsiKhusus").checked = false;

  document.getElementById("btnSubmitProduct").textContent = "Simpan Produk";
  document.getElementById("btnDeleteSelected").style.display = "none";

  updateFormOpsiLabel();
}

function updateFormOpsiLabel() {
  const kat = document.getElementById("formKategori").value;
  const label = document.getElementById("labelOpsiKhusus");
  if (kat === "Makanan") {
    label.textContent = "Varian Pedas";
  } else {
    label.textContent = "Sajian Dingin / Es Beku";
  }
}

function deleteProduct(kode) {
  const prod = products.find(p => p.kode === kode);
  if (!prod) return;

  if (confirm(`Yakin ingin menghapus produk '${prod.nama}' (${prod.kode})?`)) {
    products = products.filter(p => p.kode !== kode);
    saveProductsToStorage();
    renderCRUDTable();
    renderPOSCatalog();
    resetCRUDForm();
  }
}

// ========================================================
// 6. RIWAYAT TRANSAKSI VIEW
// ========================================================

function renderHistoryTable() {
  const tbody = document.getElementById("historyTableBody");
  const emptyMsg = document.getElementById("emptyHistoryMessage");
  const countBadge = document.getElementById("historyCount");
  tbody.innerHTML = "";

  countBadge.textContent = `${orderHistory.length} Transaksi Selesai`;

  if (orderHistory.length === 0) {
    emptyMsg.style.display = "block";
  } else {
    emptyMsg.style.display = "none";
    orderHistory.forEach(order => {
      const row = document.createElement("tr");
      const totalItemCount = order.items.reduce((acc, i) => acc + i.qty, 0);

      row.innerHTML = `
        <td><strong>${order.id}</strong></td>
        <td><small>${order.waktu}</small></td>
        <td>${totalItemCount} item</td>
        <td><strong>${formatRupiah(order.grandTotal)}</strong></td>
        <td>${formatRupiah(order.uangBayar)}</td>
        <td style="color: var(--success); font-weight: 600;">${formatRupiah(order.kembalian)}</td>
        <td>
          <button type="button" class="btn-tbl-action view-receipt-btn">Lihat Struk</button>
        </td>
      `;

      row.querySelector(".view-receipt-btn").addEventListener("click", () => {
        showReceiptModal(order);
      });

      tbody.appendChild(row);
    });
  }
}

// ========================================================
// 7. EVENT LISTENERS & SETUP
// ========================================================

document.addEventListener("DOMContentLoaded", () => {
  loadInitialData();

  // Tab Navigation
  const tabButtons = document.querySelectorAll(".tab-btn");
  const viewPanels = document.querySelectorAll(".view-panel");

  tabButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      const targetId = btn.getAttribute("data-tab");
      tabButtons.forEach(b => b.classList.remove("active"));
      viewPanels.forEach(p => p.classList.remove("active"));

      btn.classList.add("active");
      document.getElementById(targetId).classList.add("active");
    });
  });

  // Filter Kategori Katalog
  const filterButtons = document.querySelectorAll(".filter-btn");
  filterButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      filterButtons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      activeCategoryFilter = btn.getAttribute("data-category");
      renderPOSCatalog();
    });
  });

  // Search input POS
  const posSearch = document.getElementById("posSearchInput");
  posSearch.addEventListener("input", (e) => {
    posSearchQuery = e.target.value.toLowerCase().trim();
    renderPOSCatalog();
  });

  // Reset Cart
  document.getElementById("btnResetCart").addEventListener("click", resetCart);

  // Input Uang Bayar
  const inputBayar = document.getElementById("inputUangBayar");
  inputBayar.addEventListener("input", () => {
    let totalKotor = 0;
    let totalDiskon = 0;
    let totalPajak = 0;
    cart.forEach(item => {
      totalKotor += item.produk.hitungSubtotal(item.qty);
      totalDiskon += item.produk.hitungDiskon(item.qty);
      totalPajak += item.produk.hitungPajak() * item.qty;
    });
    const grandTotal = Math.max(0, totalKotor - totalDiskon + totalPajak);
    updateKembalian(grandTotal);
  });

  // Quick Cash Buttons
  document.getElementById("btnCashExact").addEventListener("click", () => {
    let totalKotor = 0;
    let totalDiskon = 0;
    let totalPajak = 0;
    cart.forEach(item => {
      totalKotor += item.produk.hitungSubtotal(item.qty);
      totalDiskon += item.produk.hitungDiskon(item.qty);
      totalPajak += item.produk.hitungPajak() * item.qty;
    });
    const grandTotal = Math.max(0, totalKotor - totalDiskon + totalPajak);
    inputBayar.value = grandTotal;
    updateKembalian(grandTotal);
  });

  document.querySelectorAll(".btn-quick-cash[data-amount]").forEach(btn => {
    btn.addEventListener("click", () => {
      inputBayar.value = btn.getAttribute("data-amount");
      let totalKotor = 0;
      let totalDiskon = 0;
      let totalPajak = 0;
      cart.forEach(item => {
        totalKotor += item.produk.hitungSubtotal(item.qty);
        totalDiskon += item.produk.hitungDiskon(item.qty);
        totalPajak += item.produk.hitungPajak() * item.qty;
      });
      const grandTotal = Math.max(0, totalKotor - totalDiskon + totalPajak);
      updateKembalian(grandTotal);
    });
  });

  // Tombol Checkout
  document.getElementById("btnCheckout").addEventListener("click", processCheckout);

  // Modal Struk Actions
  document.getElementById("btnCloseModal").addEventListener("click", () => {
    document.getElementById("modalStruk").classList.remove("open");
  });

  document.getElementById("modalStruk").addEventListener("click", (e) => {
    if (e.target.id === "modalStruk") {
      document.getElementById("modalStruk").classList.remove("open");
    }
  });

  document.getElementById("btnCopyReceipt").addEventListener("click", () => {
    const text = document.getElementById("receiptTextContainer").textContent;
    navigator.clipboard.writeText(text).then(() => {
      alert("Teks struk berhasil disalin ke clipboard!");
    });
  });

  document.getElementById("btnPrintReceipt").addEventListener("click", () => {
    window.print();
  });

  // CRUD Form Form Events
  document.getElementById("formKategori").addEventListener("change", updateFormOpsiLabel);
  document.getElementById("btnResetForm").addEventListener("click", resetCRUDForm);

  document.getElementById("btnDeleteSelected").addEventListener("click", () => {
    const kode = document.getElementById("editIndex").value;
    if (kode !== "-1") {
      deleteProduct(kode);
    }
  });

  document.getElementById("crudSearchInput").addEventListener("input", renderCRUDTable);

  document.getElementById("productCrudForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const editCode = document.getElementById("editIndex").value;
    const kode = document.getElementById("formKode").value.trim().toUpperCase();
    const nama = document.getElementById("formNama").value.trim();
    const kategori = document.getElementById("formKategori").value;
    const harga = Number(document.getElementById("formHarga").value);
    const stok = Number(document.getElementById("formStok").value);
    const opsiKhusus = document.getElementById("formOpsiKhusus").checked;

    if (!kode || !nama || harga <= 0) {
      alert("Mohon lengkapi kode, nama, dan harga dengan benar.");
      return;
    }

    if (editCode === "-1") {
      // Tambah Baru
      if (products.some(p => p.kode === kode)) {
        alert(`Kode produk '${kode}' sudah digunakan!`);
        return;
      }
      const baru = createProdukInstance({ kode, nama, kategori, harga, stok, opsiKhusus });
      products.push(baru);
      alert(`Produk '${nama}' berhasil ditambahkan!`);
    } else {
      // Perbarui
      const idx = products.findIndex(p => p.kode === editCode);
      if (idx !== -1) {
        products[idx] = createProdukInstance({ kode, nama, kategori, harga, stok, opsiKhusus });
        alert(`Produk '${nama}' berhasil diperbarui!`);
      }
    }

    saveProductsToStorage();
    renderCRUDTable();
    renderPOSCatalog();
    resetCRUDForm();
  });

  // Initial renders
  renderPOSCatalog();
  renderCart();
  renderCRUDTable();
  renderHistoryTable();
  updateFormOpsiLabel();
});
