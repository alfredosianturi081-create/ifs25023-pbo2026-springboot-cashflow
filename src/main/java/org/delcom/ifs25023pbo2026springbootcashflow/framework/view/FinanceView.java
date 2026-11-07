package org.delcom.ifs25023pbo2026springbootcashflow.framework.view;

import org.delcom.ifs25023pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25023pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25023pbo2026springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Tambah pemasukan / pengeluaran lewat dialog, cari berdasarkan keterangan,
 * urutkan lewat pilihan SortOption atau klik header kolom, hapus via dialog konfirmasi.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {
    private static final String COLOR_INCOME = "#059669";
    private static final String COLOR_EXPENSE = "#E11D48";
    private static final String COLOR_DANGER = "#991B1B";
    private static final String EMPTY_DEFAULT = "- Data transaksi belum tersedia!";
    private static final String EMPTY_SEARCH = "- Transaksi tidak ditemukan!";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_DEFAULT);

    private final Span balanceBadge = new Span("Saldo: Rp 0.00");
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceValue = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("1200px");
        getStyle().set("margin", "0 auto");

        configureGrid();
        configureToolbar();

        add(buildBanner(), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        setFlexGrow(1, grid);
        refreshAll();
    }

    // ------------------------------------------------------------------ Header

    private Div buildBanner() {
        Icon walletIcon = VaadinIcon.WALLET.create();
        walletIcon.setSize("1.75rem");
        walletIcon.getStyle().set("color", "#FFFFFF");

        H1 title = new H1("Cashflow");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.75rem");

        Paragraph subtitle = new Paragraph("Catatan keuangan pribadi · Saldo = Pemasukan − Pengeluaran");
        subtitle.getStyle()
                .set("margin", "0.15rem 0 0")
                .set("color", "#E0E7FF")
                .set("font-size", "0.9rem");

        Div titleBlock = new Div(title, subtitle);

        HorizontalLayout left = new HorizontalLayout(walletIcon, titleBlock);
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);

        balanceBadge.getStyle()
                .set("background", "rgba(255,255,255,0.18)")
                .set("border", "1px solid rgba(255,255,255,0.35)")
                .set("color", "#FFFFFF")
                .set("font-weight", "700")
                .set("font-size", "1rem")
                .set("padding", "0.5rem 1rem")
                .set("border-radius", "999px")
                .set("white-space", "nowrap");

        Div banner = new Div(left, balanceBadge);
        banner.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "0.75rem")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("padding", "1.25rem 1.5rem")
                .set("border-radius", "16px")
                .set("background", "linear-gradient(135deg, #2563EB 0%, #7C3AED 100%)")
                .set("box-shadow", "0 8px 20px rgba(79, 70, 229, 0.25)");
        return banner;
    }

    // ------------------------------------------------------------ Summary cards

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", VaadinIcon.LIST, totalCount, "#DBEAFE", "#1D4ED8", "#1E3A8A"),
                createSummaryCard("Total Pemasukan", VaadinIcon.ARROW_CIRCLE_DOWN, incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", VaadinIcon.ARROW_CIRCLE_UP, expenseTotal, "#FFE4E6", "#E11D48", "#9F1239"),
                createSummaryCard("Saldo", VaadinIcon.COIN_PILES, balanceValue, "#EDE9FE", "#7C3AED", "#5B21B6")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle()
                .set("align-items", "stretch")
                .set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            VaadinIcon iconType,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Icon icon = iconType.create();
        icon.setSize("1rem");
        icon.getStyle().set("color", accent);

        Span caption = new Span(label);
        caption.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("color", accent);

        Div captionRow = new Div(icon, caption);
        captionRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.4rem");

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.4rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.2")
                .set("color", valueColor);

        Div card = new Div(captionRow, value);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        double balance = financeUseCase.getBalance();
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balanceValue.setText(presenter.formatMoney(balance));
        balanceValue.getStyle().set("color", balance < 0 ? "#B91C1C" : "#5B21B6");
        balanceBadge.setText(presenter.showBalance(balance));
    }

    // --------------------------------------------------------------------- Grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::createAmountCell)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
        grid.setMinHeight("320px");
    }

    private Span createAmountCell(Transaction transaction) {
        Span amount = new Span(presenter.formatMoney(transaction.getAmount()));
        amount.getStyle()
                .set("font-weight", "600")
                .set("font-variant-numeric", "tabular-nums")
                .set("color", transaction.isIncome() ? COLOR_INCOME : COLOR_EXPENSE);
        return amount;
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        badge.getElement().getThemeList().add(transaction.isIncome() ? "badge success" : "badge error");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ Toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addKeyDownListener(Key.ENTER, event -> applyFilters());

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.setClearButtonVisible(true);
        sortBox.setWidth("200px");
        sortBox.addValueChangeListener(event -> applyFilters());
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> applyFilters());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button incomeButton = new Button("Pemasukan", VaadinIcon.PLUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Pengeluaran", VaadinIcon.MINUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                sortBox,
                refreshButton,
                incomeButton,
                expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ------------------------------------------------------------------ Dialogs

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setPlaceholder(income ? "Contoh: Gaji bulanan" : "Contoh: Makan siang");
        descriptionField.setWidthFull();

        NumberField amountField = new NumberField("Jumlah");
        amountField.setPlaceholder("0");
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setWidthFull();

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        form.setSpacing(true);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (!income) {
            saveButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        }
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        buildDialog(
                dialog,
                income ? "Tambah Pemasukan" : "Tambah Pengeluaran",
                income ? COLOR_INCOME : COLOR_EXPENSE,
                form,
                cancelButton,
                saveButton
        );
        dialog.open();
        descriptionField.focus();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription() + "\" ("
                        + presenter.formatMoney(transaction.getAmount()) + ", ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        buildDialog(dialog, "Hapus Transaksi", COLOR_DANGER, message, cancelButton, confirmButton);
        dialog.open();
    }

    /** Merakit dialog dengan header berwarna, body putih, dan footer abu-abu. */
    private void buildDialog(
            Dialog dialog,
            String title,
            String headerColor,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerColor)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
    }

    // ------------------------------------------------------------------ Actions

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    /** Menerapkan pencarian (keterangan) dan urutan (SortOption) yang sedang dipilih. */
    private void applyFilters() {
        String keyword = searchField.getValue();
        SortOption option = sortBox.getValue();
        boolean searching = keyword != null && !keyword.isBlank();

        List<Transaction> results = financeUseCase.searchTransactions(keyword, option);
        String title = searching ? "Hasil Pencarian: \"" + keyword.trim() + "\"" : "Daftar Transaksi";
        if (option != null) {
            title += " · " + presenter.formatSortOption(option);
        }
        emptyState.setText(searching ? EMPTY_SEARCH : EMPTY_DEFAULT);
        refreshGrid(results, title);
    }

    /** Mengosongkan pencarian & urutan lalu memuat ulang semua transaksi. */
    private void refreshAll() {
        searchField.clear();
        sortBox.clear();
        applyFilters();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
