package org.delcom.ifs25023pbo2026springbootcashflow.usecase;

import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case yang menangani logika bisnis aplikasi cashflow.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan pemasukan baru setelah validasi. */
    public Transaction addIncome(String description, Double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveIncome(description.trim(), amount);
    }

    /** Menambahkan pengeluaran baru setelah validasi. */
    public Transaction addExpense(String description, Double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveExpense(description.trim(), amount);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika ID tidak valid / tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid!");
        }
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return transactionRepository.findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mencari berdasarkan kata kunci lalu mengurutkan hasilnya ({@code option} boleh null = tanpa urutan). */
    public List<Transaction> searchTransactions(String keyword, SortOption option) {
        List<Transaction> found = searchTransactions(keyword);
        if (option == null) {
            return found;
        }
        return found.stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Mengurutkan semua transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public double getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Total seluruh pengeluaran. */
    public double getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Saldo saat ini = total pemasukan - total pengeluaran. */
    public double getBalance() {
        return transactionRepository.findAll().stream()
                .mapToDouble(Transaction::signedAmount)
                .sum();
    }

    private void validate(String description, Double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (amount == null || amount.isNaN() || amount.isInfinite()) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari nol");
        }
    }
}
