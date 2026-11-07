package org.delcom.ifs25023pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat uang: 1000000 -> "Rp 1,000,000.00" (negatif: "-Rp 1,000.00"). */
    public String formatMoney(double amount) {
        String formatted = String.format(Locale.US, "Rp %,.2f", Math.abs(amount));
        boolean negative = amount < 0 && !formatted.equals("Rp 0.00");
        return negative ? "-" + formatted : formatted;
    }

    /** Label jenis transaksi untuk ditampilkan. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label opsi pengurutan untuk ditampilkan. */
    public String formatSortOption(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat satu transaksi: [id] keterangan | Rp 1,000,000.00 | [Pemasukan]. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat saldo: Saldo: Rp 4,975,000.00. */
    public String showBalance(double balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Memformat daftar transaksi menjadi teks multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        String label = transaction.isIncome() ? "pemasukan" : "pengeluaran";
        return "Berhasil menambah " + label + ": " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan saat pilihan tidak dikenali. */
    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan saat jumlah yang dimasukkan tidak valid. */
    public String showInvalidAmount() {
        return "[!] Jumlah tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
