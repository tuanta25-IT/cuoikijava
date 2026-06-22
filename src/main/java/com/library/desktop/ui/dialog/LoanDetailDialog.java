package com.library.desktop.ui.dialog;

/*
 * File: LoanDetailDialog.java
 * Mô tả: Dialog hiển thị chi tiết một phiếu mượn, cho phép chỉnh sửa ngày mượn/ngày trả dự kiến,
 * thêm/xóa sách trong phiếu, gia hạn và đánh dấu đã trả. Hỗ trợ xuất báo cáo.
 */

import com.library.desktop.dao.BookDao;
import com.library.desktop.dao.LoanDao;
import com.library.desktop.model.Book;
import com.library.desktop.model.Loan;
import com.library.desktop.model.LoanItem;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.util.TableExportUtils;
import com.library.desktop.util.StatusText;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoanDetailDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LoanDao loanDao = new LoanDao();
    private final BookDao bookDao = new BookDao();
    private final AccessProfile accessProfile;
    private final int loanId;

    private final JLabel loanIdValue = new JLabel();
    private final JLabel readerValue = new JLabel();
    private final JLabel borrowDateValue = new JLabel();
    private final JLabel dueDateValue = new JLabel();
    private final JLabel returnedDateValue = new JLabel();
    private final JLabel statusValue = new JLabel();
    private final JSpinner borrowDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner dueDateSpinner = new JSpinner(new SpinnerDateModel());

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Ma chi tiet", "Ma sach", "Tieu de sach", "So luong" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<BookItem> bookBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final JButton addButton = new JButton("Thêm sách");
    private final JButton removeButton = new JButton("Xóa dòng");
    private final JButton returnButton = new JButton("Đánh dấu đã trả");
    private final JButton renewButton = new JButton("Gia hạn 7 ngày");
    private final JButton saveDatesButton = new JButton("Lưu ngày mượn");
    private final JButton exportExcelButton = new JButton("Xuất Excel");
    private final JButton exportPdfButton = new JButton("Xuất PDF");

    private Loan currentLoan;
    private final List<LoanItem> items = new ArrayList<>();

    /**
     * Constructor: khởi tạo dialog chi tiết cho `loanId` và `accessProfile` cụ thể.
     */
    public LoanDetailDialog(Window owner, int loanId, AccessProfile accessProfile) {
        super(owner, "Chi tiết phiếu mượn", ModalityType.APPLICATION_MODAL);
        this.loanId = loanId;
        this.accessProfile = accessProfile;

        setSize(980, 640);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(AppTheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadBooks();
        loadLoan();
        loadItems();
        applyPermissions();
    }

    private JPanel buildHeader() {
        /**
         * Xây dựng phần header của dialog (thông tin tóm tắt phiếu: mã, độc giả, ngày,
         * trạng thái).
         */
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));

        JPanel banner = new JPanel(new BorderLayout(12, 12));
        banner.setBackground(AppTheme.PRIMARY);
        banner.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Chi tiết phiếu mượn");
        title.setForeground(java.awt.Color.WHITE);
        title.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 24));
        JLabel subtitle = new JLabel("Xem chi tiết, cập nhật, trả sách và xuất báo cáo");
        subtitle.setForeground(new java.awt.Color(219, 234, 254));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);
        banner.add(left, BorderLayout.WEST);

        JPanel badges = new JPanel(new GridLayout(3, 2, 10, 8));
        badges.setOpaque(false);
        addBadge(badges, "Mã phiếu", loanIdValue);
        addBadge(badges, "Độc giả", readerValue);
        addBadge(badges, "Ngày mượn", borrowDateValue);
        addBadge(badges, "Ngày trả dự kiến", dueDateValue);
        addBadge(badges, "Ngày trả thực", returnedDateValue);
        addBadge(badges, "Tình trạng", statusValue);
        banner.add(badges, BorderLayout.EAST);

        outer.add(banner, BorderLayout.CENTER);
        return outer;
    }

    private void addBadge(JPanel parent, String label, JLabel value) {
        /**
         * Thêm một badge (card nhỏ) hiển thị nhãn và giá trị lên header.
         */
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(new java.awt.Color(255, 255, 255, 26));
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel key = new JLabel(label);
        key.setForeground(new java.awt.Color(219, 234, 254));
        key.setFont(key.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
        value.setForeground(java.awt.Color.WHITE);
        value.setFont(value.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        card.add(key);
        card.add(value);
        parent.add(card);
    }

    private JPanel buildCenter() {
        /**
         * Xây dựng phần chính (center) của dialog, bao gồm form thêm sách và trình
         * chỉnh sửa ngày.
         */
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        form.setBackground(AppTheme.SURFACE);
        form.setBorder(BorderFactory.createTitledBorder("Thêm sách vào phiếu mượn"));

        JPanel dateEditor = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        dateEditor.setBackground(AppTheme.SURFACE);
        dateEditor.setBorder(BorderFactory.createTitledBorder("Chỉnh sửa ngày mượn"));
        configureDateSpinner(borrowDateSpinner);
        configureDateSpinner(dueDateSpinner);
        dateEditor.add(new JLabel("Ngày mượn"));
        dateEditor.add(borrowDateSpinner);
        dateEditor.add(new JLabel("Ngày trả dự kiến"));
        dateEditor.add(dueDateSpinner);
        AppTheme.primaryButton(saveDatesButton);
        saveDatesButton.addActionListener(e -> saveDates());
        dateEditor.add(saveDatesButton);

        form.add(new JLabel("Sách"));
        form.add(bookBox);
        form.add(new JLabel("Số lượng"));
        form.add(quantitySpinner);
        AppTheme.successButton(addButton);
        AppTheme.dangerButton(removeButton);
        AppTheme.primaryButton(returnButton);
        AppTheme.accentButton(renewButton);
        AppTheme.primaryButton(exportExcelButton);
        AppTheme.accentButton(exportPdfButton);

        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeItem());
        returnButton.addActionListener(e -> markReturned());
        renewButton.addActionListener(e -> renew());
        exportExcelButton.addActionListener(
                e -> TableExportUtils.exportExcel(this, "phieu_muon_" + loanId + "_chi_tiet", table));
        exportPdfButton
                .addActionListener(e -> TableExportUtils.exportPdf(this, "phieu_muon_" + loanId + "_chi_tiet", table));

        form.add(addButton);
        form.add(removeButton);
        form.add(returnButton);
        form.add(renewButton);
        form.add(exportExcelButton);
        form.add(exportPdfButton);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> refreshActionStates());
        AppTheme.decorateTable(table);

        center.add(form, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(12, 12));
        middle.setOpaque(false);
        middle.add(dateEditor, BorderLayout.NORTH);
        middle.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(middle, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildFooter() {
        /**
         * Xây dựng footer chứa nút đóng.
         */
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        JButton closeButton = new JButton("Đóng");
        AppTheme.neutralButton(closeButton);
        closeButton.addActionListener(e -> dispose());
        footer.add(closeButton);
        return footer;
    }

    private void loadBooks() {
        /**
         * Tải danh sách sách từ DB để đổ vào combobox `bookBox`.
         */
        try {
            bookBox.removeAllItems();
            for (Book book : bookDao.findAll()) {
                bookBox.addItem(new BookItem(book.id(), book.title()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách sách: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLoan() {
        /**
         * Tải thông tin phiếu mượn hiện tại (currentLoan) và cập nhật UI hiển thị.
         */
        try {
            currentLoan = loanDao.findById(loanId);
            if (currentLoan == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            loanIdValue.setText(String.valueOf(currentLoan.id()));
            readerValue.setText(currentLoan.readerName() + " (" + currentLoan.readerId() + ")");
            borrowDateValue.setText(formatDate(currentLoan.borrowDate()));
            dueDateValue.setText(formatDate(currentLoan.dueDate()));
            returnedDateValue.setText(formatDate(currentLoan.returnedDate()));
            statusValue.setText(StatusText.loan(currentLoan.status()));
            borrowDateSpinner.setValue(java.sql.Date
                    .valueOf(currentLoan.borrowDate() == null ? LocalDate.now() : currentLoan.borrowDate()));
            dueDateSpinner.setValue(
                    java.sql.Date.valueOf(currentLoan.dueDate() == null ? LocalDate.now() : currentLoan.dueDate()));
            applyPermissions();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được phiếu mượn: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItems() {
        /**
         * Tải danh sách chi tiết (items) cho phiếu mượn và hiển thị vào bảng.
         */
        try {
            items.clear();
            items.addAll(loanDao.findItemsByLoanId(loanId));
            model.setRowCount(0);
            for (LoanItem item : items) {
                model.addRow(new Object[] { item.detailId(), item.bookId(), item.bookTitle(), item.quantity() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được chi tiết: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyPermissions() {
        /**
         * Áp dụng quyền sử dụng (enable/disable) các điều khiển theo trạng thái phiếu
         * và quyền người dùng.
         */
        boolean editableLoan = currentLoan != null && !"RETURNED".equalsIgnoreCase(currentLoan.status());
        boolean canEdit = accessProfile != null && accessProfile.manageLoans() && editableLoan;
        addButton.setEnabled(canEdit);
        returnButton.setEnabled(accessProfile != null && accessProfile.manageLoans() && editableLoan);
        renewButton.setEnabled(accessProfile != null && accessProfile.manageLoans() && editableLoan);
        saveDatesButton.setEnabled(accessProfile != null && accessProfile.manageLoans() && editableLoan);
        bookBox.setEnabled(canEdit);
        quantitySpinner.setEnabled(canEdit);
        borrowDateSpinner.setEnabled(accessProfile != null && accessProfile.manageLoans() && editableLoan);
        dueDateSpinner.setEnabled(accessProfile != null && accessProfile.manageLoans() && editableLoan);
        refreshActionStates();
    }

    private void refreshActionStates() {
        /**
         * Cập nhật trạng thái các nút hành động (ví dụ bật/tắt nút xóa dựa trên
         * selection).
         */
        boolean editableLoan = currentLoan != null && !"RETURNED".equalsIgnoreCase(currentLoan.status());
        boolean canEdit = accessProfile != null && accessProfile.manageLoans() && editableLoan;
        removeButton.setEnabled(canEdit && table.getSelectedRow() >= 0);
    }

    private void addItem() {
        /**
         * Thêm một mục sách vào phiếu mượn (gọi DAO). Thực hiện trong background.
         */
        BookItem selected = (BookItem) bookBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn sách.");
            return;
        }
        addButton.setEnabled(false);
        new javax.swing.SwingWorker<Void, Void>() {
            private Exception workerEx = null;

            @Override
            protected Void doInBackground() {
                try {
                    int quantity = (Integer) quantitySpinner.getValue();
                    loanDao.addItem(loanId, selected.id(), quantity);
                } catch (Exception ex) {
                    workerEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                addButton.setEnabled(true);
                if (workerEx != null) {
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, workerEx.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    loadLoan();
                    loadItems();
                }
            }
        }.execute();
    }

    private void removeItem() {
        /**
         * Xóa một mục sách khỏi phiếu mượn (gọi DAO). Thực hiện trong background.
         */
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn dòng cần xóa.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa sách khỏi phiếu mượn?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int detailId = items.get(row).detailId();
        removeButton.setEnabled(false);
        new javax.swing.SwingWorker<Void, Void>() {
            private Exception workerEx = null;

            @Override
            protected Void doInBackground() {
                try {
                    loanDao.removeItem(detailId);
                } catch (Exception ex) {
                    workerEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                removeButton.setEnabled(true);
                if (workerEx != null) {
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, workerEx.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    loadLoan();
                    loadItems();
                }
            }
        }.execute();
    }

    private void markReturned() {
        /**
         * Đánh dấu phiếu mượn là đã trả, gửi email xác nhận nếu có thể và cập nhật giao
         * diện.
         */
        String to = currentLoan == null ? null : currentLoan.readerEmail();
        String readerName = currentLoan == null ? "" : currentLoan.readerName();
        returnButton.setEnabled(false);
        new javax.swing.SwingWorker<Void, Void>() {
            private Exception workerEx = null;

            @Override
            protected Void doInBackground() {
                try {
                    loanDao.markReturned(loanId, LocalDate.now());
                    // try send return notification
                    try {
                        if (to != null && !to.isBlank() && com.library.desktop.util.EmailSender.isEnabled()) {
                            String subject = "Xác nhận đã trả sách";
                            String body = "<p>Xin chào <strong>" + readerName + "</strong>,</p>" +
                                    "<p>Cảm ơn bạn đã trả sách vào ngày " + java.time.LocalDate.now() + "</p>";
                            com.library.desktop.util.EmailSender.send(to, subject, body);
                        }
                    } catch (Exception ex) {
                        // swallow, will surface after
                    }
                } catch (Exception ex) {
                    workerEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                returnButton.setEnabled(true);
                if (workerEx != null) {
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, workerEx.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
                loadLoan();
                loadItems();
                // refresh UI rendering
                LoanDetailDialog.this.revalidate();
                LoanDetailDialog.this.repaint();
            }
        }.execute();
    }

    private void renew() {
        /**
         * Gia hạn phiếu mượn thêm 7 ngày (gọi DAO). Thực hiện trong background.
         */
        renewButton.setEnabled(false);
        new javax.swing.SwingWorker<Void, Void>() {
            private Exception workerEx = null;

            @Override
            protected Void doInBackground() {
                try {
                    loanDao.renew(loanId, 7);
                } catch (Exception ex) {
                    workerEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                renewButton.setEnabled(true);
                if (workerEx != null) {
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, workerEx.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    loadLoan();
                    loadItems();
                }
            }
        }.execute();
    }

    private void saveDates() {
        /**
         * Lưu các ngày (Ngày mượn, Ngày trả dự kiến) đã chỉnh sửa; chạy DAO cập nhật
         * trong background.
         */
        saveDatesButton.setEnabled(false);
        new javax.swing.SwingWorker<Void, Void>() {
            private Exception workerEx = null;

            @Override
            protected Void doInBackground() {
                try {
                    LocalDate borrowDate = toLocalDate((Date) borrowDateSpinner.getValue());
                    LocalDate dueDate = toLocalDate((Date) dueDateSpinner.getValue());
                    loanDao.updateBorrowDates(loanId, borrowDate, dueDate);
                } catch (Exception ex) {
                    workerEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                saveDatesButton.setEnabled(true);
                if (workerEx != null) {
                    // log stacktrace to stderr
                    workerEx.printStackTrace();
                    // show detailed error in scrollable area so user can report it
                    javax.swing.JTextArea area = new javax.swing.JTextArea();
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    workerEx.printStackTrace(pw);
                    area.setText(sw.toString());
                    area.setEditable(false);
                    area.setRows(15);
                    area.setColumns(80);
                    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(area);
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, scroll,
                            "Lỗi khi cập nhật ngày: " + workerEx.getMessage(), JOptionPane.ERROR_MESSAGE);
                } else {
                    loadLoan();
                    loadItems();
                    JOptionPane.showMessageDialog(LoanDetailDialog.this, "Đã cập nhật ngày mượn.", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }.execute();
    }

    private void configureDateSpinner(JSpinner spinner) {
        /**
         * Cấu hình editor cho spinner ngày theo định dạng `dd/MM/yyyy`.
         */
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setColumns(10);
    }

    private LocalDate toLocalDate(Date date) {
        /**
         * Chuyển `java.util.Date` sang `LocalDate`.
         */
        return date == null ? null : date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private String formatDate(LocalDate date) {
        /**
         * Định dạng `LocalDate` thành chuỗi theo định dạng ngày chuẩn.
         */
        return date == null ? "-" : DATE_FORMAT.format(date);
    }

    private record BookItem(int id, String title) {
        @Override
        public String toString() {
            return title + " (#" + id + ")";
        }
    }
}
