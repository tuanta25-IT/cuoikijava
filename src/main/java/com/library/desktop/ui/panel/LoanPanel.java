/**
 * File: LoanPanel.java
 * Mô tả: Panel quản lý danh sách phiếu mượn trong giao diện chính. Hiển thị bảng
 * phiếu mượn, hỗ trợ tìm kiếm, lọc, tạo phiếu mới, đánh dấu trả và xuất báo cáo.
 */

package com.library.desktop.ui.panel;

import com.library.desktop.dao.LoanDao;
import com.library.desktop.dao.ReaderDao;
import com.library.desktop.model.Loan;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.ui.FormUiHelper;
import com.library.desktop.ui.dialog.LoanDetailDialog;
import com.library.desktop.util.StatusText;
import com.library.desktop.util.TableExportUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanPanel extends JPanel {
    private final LoanDao loanDao = new LoanDao();
    private final ReaderDao readerDao = new ReaderDao();
    private final AccessProfile accessProfile;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã phiếu", "Mã độc giả", "Độc giả", "Ngày mượn", "Ngày trả dự kiến", "Ngày trả", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final List<Loan> loans = new ArrayList<>();

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<FilterItem> statusFilterBox = new JComboBox<>(new FilterItem[]{
            new FilterItem("ALL", "Tất cả"),
            new FilterItem("BORROWING", "Đang mượn"),
            new FilterItem("RETURNED", "Đã trả"),
            new FilterItem("OVERDUE", "Quá hạn"),
            new FilterItem("PENDING", "Chờ xử lý")
    });
    private final JSpinner readerIdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
    private final JSpinner dueDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 3650, 1));
    private final JButton createBtn = new JButton("Tạo phiếu");
    private final JButton detailBtn = new JButton("Chi tiết");
    private final JButton returnBtn = new JButton("Đánh dấu đã trả");
    private final JButton refreshBtn = new JButton("Làm mới");
    private final JButton excelBtn = new JButton("Excel");
    private final JButton pdfBtn = new JButton("PDF");
    private final JButton remindBtn = new JButton("Gửi nhắc ngay");
    private Integer selectedLoanId;

    public LoanPanel() {
        this(AccessProfile.fromRole("ROLE_ADMIN"));
    }

    /**
     * Constructor mặc định với `AccessProfile` được khởi tạo từ role mặc định.
     */

    public LoanPanel(AccessProfile accessProfile) {
        this.accessProfile = accessProfile;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppTheme.BACKGROUND);

        FormUiHelper.configureTextField(searchField, 14);
        FormUiHelper.configureSpinner(readerIdSpinner);
        FormUiHelper.configureSpinner(dueDaysSpinner);

        JPanel top = new JPanel();
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel filterRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        filterRow.add(new JLabel("Tìm kiếm:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Lọc trạng thái:"));
        filterRow.add(statusFilterBox);

        JPanel formRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        formRow.add(new JLabel("Mã độc giả:"));
        formRow.add(readerIdSpinner);
        formRow.add(new JLabel("Số ngày mượn:"));
        formRow.add(dueDaysSpinner);

        AppTheme.primaryButton(createBtn);
        AppTheme.accentButton(detailBtn);
        AppTheme.successButton(returnBtn);
        AppTheme.neutralButton(refreshBtn);
        AppTheme.successButton(excelBtn);
        AppTheme.warningButton(pdfBtn);
        AppTheme.warningButton(remindBtn);

        JPanel actionRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        actionRow.add(createBtn);
        actionRow.add(detailBtn);
        actionRow.add(returnBtn);
        actionRow.add(refreshBtn);
        actionRow.add(excelBtn);
        actionRow.add(pdfBtn);
        actionRow.add(remindBtn);

        createBtn.addActionListener(e -> createLoan());
        detailBtn.addActionListener(e -> openSelectedDetail());
        returnBtn.addActionListener(e -> markReturned());
        refreshBtn.addActionListener(e -> loadData());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "phieu_muon", table));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "phieu_muon", table));
        remindBtn.addActionListener(e -> sendRemindersImmediate());

        top.add(FormUiHelper.createSection("Tìm kiếm và lọc", filterRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Tạo phiếu mượn", formRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Thao tác", actionRow));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelect());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedDetail();
                }
            }
        });
        AppTheme.decorateTable(table);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        statusFilterBox.addActionListener(e -> applyFilters());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        applyPermissions();
        loadData();
    }

    /**
     * Áp dụng quyền hiển thị/cho phép thao tác theo `accessProfile`.
     */

    private void applyPermissions() {
        boolean editable = accessProfile == null || accessProfile.manageLoans();
        createBtn.setEnabled(editable);
        detailBtn.setEnabled(accessProfile != null && accessProfile.viewLoans());
        returnBtn.setEnabled(editable);
        readerIdSpinner.setEnabled(editable);
        dueDaysSpinner.setEnabled(editable);
        searchField.setEnabled(true);
        statusFilterBox.setEnabled(true);
    }

    /**
     * Tải dữ liệu phiếu mượn từ cơ sở dữ liệu và cập nhật bảng hiển thị.
     */

    private void loadData() {
        model.setRowCount(0);
        loans.clear();
        selectedLoanId = null;

        try {
            List<Loan> all = loanDao.findAll();
            loans.addAll(all);
            for (Loan loan : all) {
                model.addRow(new Object[]{
                        loan.id(),
                        loan.readerId(),
                        loan.readerName(),
                        loan.borrowDate(),
                        loan.dueDate(),
                        loan.returnedDate(),
                        StatusText.loan(loan.status())
                });
            }
            applyFilters();
            decorateOverdueRows();
        } catch (Exception ex) {
            FormUiHelper.showError(this, ex.getMessage());
        }
    }

    /**
     * Trang trí các hàng quá hạn bằng màu nền khác nhau để dễ nhận diện.
     */

    private void decorateOverdueRows() {
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    int modelRow = table.convertRowIndexToModel(row);
                    Object due = model.getValueAt(modelRow, 4);
                    LocalDate dueDate = due instanceof LocalDate ? (LocalDate) due : null;
                    if (!isSelected) {
                        if (dueDate != null) {
                            LocalDate now = LocalDate.now();
                            if (dueDate.isBefore(now)) {
                                c.setBackground(new java.awt.Color(255, 230, 230));
                            } else if (!dueDate.isAfter(now.plusDays(3))) {
                                c.setBackground(new java.awt.Color(255, 244, 224));
                            } else {
                                c.setBackground(AppTheme.BACKGROUND);
                            }
                        } else {
                            c.setBackground(AppTheme.BACKGROUND);
                        }
                    }
                } catch (Exception ignored) {
                }
                return c;
            }
        });
        table.repaint();
    }

    /**
     * Gửi email nhắc trả ngay cho các phiếu đến hạn trong số ngày được chỉ định.
     */

    private void sendRemindersImmediate() {
        String input = JOptionPane.showInputDialog(this, "Gửi nhắc cho các phiếu đến hạn trong (ngày):", "2");
        if (input == null) {
            return;
        }
        int days;
        try {
            days = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            FormUiHelper.showError(this, "Giá trị ngày không hợp lệ.");
            return;
        }
        if (!com.library.desktop.util.EmailSender.isEnabled()) {
            FormUiHelper.showInfo(this, "Email chưa được cấu hình. Vui lòng chỉnh desktop.properties.");
            return;
        }

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                new com.library.desktop.service.ReminderService().sendDueReminders(days);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    FormUiHelper.showInfo(LoanPanel.this, "Đã gửi nhắc (nếu có địa chỉ email).");
                } catch (Exception ex) {
                    FormUiHelper.showError(LoanPanel.this, "Lỗi khi gửi nhắc: " + ex.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Xử lý khi người dùng chọn một hàng trong bảng; lưu `selectedLoanId`.
     */

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= loans.size()) {
            return;
        }
        selectedLoanId = loans.get(modelRow).id();
    }

    /**
     * Tạo một phiếu mượn mới dựa trên `readerId` và `dueDays`.
     */

    private void createLoan() {
        try {
            int readerId = (Integer) readerIdSpinner.getValue();
            int dueDays = (Integer) dueDaysSpinner.getValue();
            if (readerDao.findById(readerId) == null) {
                FormUiHelper.showWarning(this, "Không tìm thấy độc giả với mã này.");
                return;
            }
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(dueDays);
            int loanId = loanDao.create(readerId, borrowDate, dueDate);
            loadData();
            openDetail(loanId);
        } catch (Exception ex) {
            FormUiHelper.showError(this, ex.getMessage());
        }
    }

    /**
     * Đánh dấu phiếu mượn đã được trả.
     */

    private void markReturned() {
        if (selectedLoanId == null) {
            FormUiHelper.showWarning(this, "Chọn phiếu mượn cần cập nhật.");
            return;
        }

        try {
            loanDao.markReturned(selectedLoanId, LocalDate.now());
            loadData();
        } catch (Exception ex) {
            FormUiHelper.showError(this, ex.getMessage());
        }
    }

    /**
     * Mở dialog chi tiết cho phiếu đang chọn.
     */

    private void openSelectedDetail() {
        if (selectedLoanId == null) {
            FormUiHelper.showWarning(this, "Chọn phiếu mượn cần xem chi tiết.");
            return;
        }
        openDetail(selectedLoanId);
    }

    /**
     * Mở dialog chi tiết cho `loanId` cụ thể.
     */

    private void openDetail(int loanId) {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        LoanDetailDialog dialog = new LoanDetailDialog(owner, loanId, accessProfile);
        dialog.setVisible(true);
        loadData();
    }

    /**
     * Áp dụng bộ lọc tìm kiếm và lọc trạng thái lên `TableRowSorter`.
     */

    private void applyFilters() {
        String keyword = searchField.getText().trim().toLowerCase();
        FilterItem selectedStatus = (FilterItem) statusFilterBox.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String loanId = value(entry, 0);
                String readerId = value(entry, 1);
                String readerName = value(entry, 2);
                String status = value(entry, 6);

                boolean matchesKeyword = keyword.isEmpty()
                        || loanId.contains(keyword)
                        || readerId.contains(keyword)
                        || readerName.contains(keyword)
                        || status.contains(keyword);

                boolean matchesStatus = selectedStatus == null || "ALL".equals(selectedStatus.code) || status.equalsIgnoreCase(selectedStatus.label.toLowerCase());
                return matchesKeyword && matchesStatus;
            }

            private String value(Entry<? extends DefaultTableModel, ? extends Integer> entry, int column) {
                Object raw = entry.getValue(column);
                return raw == null ? "" : raw.toString().toLowerCase();
            }
        });
    }

    private static final class FilterItem {
        private final String code;
        private final String label;

        private FilterItem(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class SimpleDocumentListener implements DocumentListener {
        private final Runnable change;

        private SimpleDocumentListener(Runnable change) {
            this.change = change;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            change.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            change.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            change.run();
        }
    }
}
