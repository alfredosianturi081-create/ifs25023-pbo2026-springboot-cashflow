package org.delcom.ifs25023pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Urutan deklarasi dipakai sebagai urutan natural (INCOME lebih dulu).
 */
public enum TransactionType {
    /** Pemasukan: menambah saldo. */
    INCOME,

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE
}
