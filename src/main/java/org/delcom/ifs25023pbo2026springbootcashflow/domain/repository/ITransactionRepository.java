package org.delcom.ifs25023pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi baru (pemasukan / pengeluaran)
 * dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /**
     * Menyimpan transaksi pemasukan baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveIncome(String description, double amount);

    /**
     * Menyimpan transaksi pengeluaran baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveExpense(String description, double amount);
}
