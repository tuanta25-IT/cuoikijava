package com.library.desktop.ui.panel;

/**
 * File: ReaderPanel.java
 * Mô tả: Panel quản lý độc giả - hiển thị danh sách, thêm, sửa, xóa, xuất báo cáo
 * và xem lịch sử điểm. Chứa form nhập liệu cho thông tin độc giả và bộ lọc.
 */

import com.library.desktop.dao.ReaderDao;
import com.library.desktop.model.Reader;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.FormUiHelper;
import com.library.desktop.ui.AppTheme;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReaderPanel extends JPanel {
    private final ReaderDao readerDao = new ReaderDao();
    private final AccessProfile accessProfile;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã", "Họ tên", "Số điện thoại", "Email", "Mã thẻ", "Loại", "Trạng thái", "Điểm" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final List<Reader> readers = new ArrayList<>();

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<StatusItem> filterStatusBox = new JComboBox<>(new StatusItem[] {
            new StatusItem(null, "Tất cả"),
            new StatusItem(true, "Hoạt động"),
            new StatusItem(false, "Ngừng hoạt động")
    });

    private final JTextField fullNameField = new JTextField(14);
    private final JTextField phoneField = new JTextField(10);
    private final JTextField emailField = new JTextField(14);
    private final JTextField cardField = new JTextField(10);
    private final JTextField typeField = new JTextField(8);
    private final JComboBox<StatusItem> statusBox = new JComboBox<>(new StatusItem[] {
            new StatusItem(true, "Hoạt động"),
            new StatusItem(false, "Ngừng hoạt động")
    });
    private final JSpinner trustSpinner = new JSpinner(new SpinnerNumberModel(0, -9999, 9999, 1));
    private final JButton addBtn = new JButton("Thêm");
    private final JButton updateBtn = new JButton("Sửa");
    private final JButton deleteBtn = new JButton("Xóa");
    private final JButton refreshBtn = new JButton("Làm mới");
    private final JButton excelBtn = new JButton("Excel");
    private final JButton pdfBtn = new JButton("PDF");
    private final JButton pointsBtn = new JButton("Lịch sử điểm");

    private Integer selectedId;

    /**
     * Constructor mặc định sử dụng role `ROLE_ADMIN`.
     */
    public ReaderPanel() {
        this(AccessProfile.fromRole("ROLE_ADMIN"));
    }

    /**
     * Constructor: khởi tạo panel với profile truy cập cụ thể.
     */
    public ReaderPanel(AccessProfile accessProfile) {
        this.accessProfile = accessProfile;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppTheme.BACKGROUND);

        JPanel top = new JPanel();
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.setOpaque(false);

        FormUiHelper.configureTextField(searchField, 14);
        FormUiHelper.configureTextField(fullNameField, 14);
        FormUiHelper.configureTextField(phoneField, 10);
        FormUiHelper.configureTextField(emailField, 14);
        FormUiHelper.configureTextField(cardField, 10);
        FormUiHelper.configureTextField(typeField, 8);
        FormUiHelper.configureSpinner(trustSpinner);

        JPanel filterRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        filterRow.add(new JLabel("Tìm kiếm:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Lọc trạng thái:"));
        filterRow.add(filterStatusBox);

        JPanel formRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        formRow.add(new JLabel("Họ tên:"));
        formRow.add(fullNameField);
        formRow.add(new JLabel("SĐT:"));
        formRow.add(phoneField);
        formRow.add(new JLabel("Email:"));
        formRow.add(emailField);
        formRow.add(new JLabel("Mã thẻ:"));
        formRow.add(cardField);
        formRow.add(new JLabel("Loại độc giả:"));
        formRow.add(typeField);
        formRow.add(new JLabel("Trạng thái:"));
        formRow.add(statusBox);
        formRow.add(new JLabel("Điểm uy tín:"));
        formRow.add(trustSpinner);

        addBtn.addActionListener(e -> create());
        updateBtn.addActionListener(e -> update());
        deleteBtn.addActionListener(e -> delete());
        refreshBtn.addActionListener(e -> loadData());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "doc_gia", table));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "doc_gia", table));

        AppTheme.primaryButton(addBtn);
        AppTheme.accentButton(updateBtn);
        AppTheme.dangerButton(deleteBtn);
        AppTheme.neutralButton(refreshBtn);
        AppTheme.successButton(excelBtn);
        AppTheme.warningButton(pdfBtn);

        JPanel actionRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        actionRow.add(addBtn);
        actionRow.add(updateBtn);
        actionRow.add(deleteBtn);
        actionRow.add(refreshBtn);
        actionRow.add(excelBtn);
        actionRow.add(pdfBtn);
        actionRow.add(pointsBtn);

        top.add(FormUiHelper.createSection("Tìm kiếm và lọc", filterRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Thông tin độc giả", formRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Thao tác", actionRow));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelect());
        AppTheme.decorateTable(table);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        filterStatusBox.addActionListener(e -> applyFilters());
        pointsBtn.addActionListener(e -> openPointsDialog());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        applyPermissions();
        loadData();
    }

    /**
     * Bật/tắt các điều khiển theo quyền người dùng (manageReaders).
     */
    private void applyPermissions() {
        boolean editable = accessProfile == null || accessProfile.manageReaders();
        addBtn.setEnabled(editable);
        updateBtn.setEnabled(editable);
        deleteBtn.setEnabled(editable);
        fullNameField.setEnabled(editable);
        phoneField.setEnabled(editable);
        emailField.setEnabled(editable);
        cardField.setEnabled(editable);
        typeField.setEnabled(editable);
        statusBox.setEnabled(editable);
        trustSpinner.setEnabled(editable);
        searchField.setEnabled(true);
        filterStatusBox.setEnabled(true);
    }

    /**
     * Tải dữ liệu độc giả từ DAO và nạp vào `model` của bảng.
     */
    private void loadData() {
        model.setRowCount(0);
        readers.clear();
        selectedId = null;

        try {
            List<Reader> all = readerDao.findAll();
            readers.addAll(all);
            for (Reader r : all) {
                model.addRow(new Object[] {
                        r.id(), r.fullName(), r.phone(), r.email(), r.cardCode(), StatusText.readerType(r.readerType()),
                        StatusText.reader(r.active()), r.trustScore()
                });
            }
            applyFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Khi chọn dòng trong bảng, điền dữ liệu vào form để sửa/xóa.
     */
    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= readers.size()) {
            return;
        }

        Reader r = readers.get(modelRow);
        selectedId = r.id();
        fullNameField.setText(r.fullName());
        phoneField.setText(r.phone());
        emailField.setText(r.email());
        cardField.setText(r.cardCode());
        typeField.setText(r.readerType());
        statusBox.setSelectedItem(findStatusItem(r.active()));
        trustSpinner.setValue(r.trustScore() == null ? 0 : r.trustScore());
    }

    /**
     * Tạo độc giả mới từ dữ liệu form (gọi DAO).
     */
    private void create() {
        Reader reader = readForm(0);
        if (reader == null) {
            return;
        }
        try {
            readerDao.create(reader);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cập nhật độc giả đã chọn theo dữ liệu trong form.
     */
    private void update() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn độc giả cần sửa.");
            return;
        }
        Reader reader = readForm(selectedId);
        if (reader == null) {
            return;
        }
        try {
            readerDao.update(reader);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xóa độc giả đã chọn (xác nhận trước khi xóa).
     */
    private void delete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn độc giả cần xóa.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa độc giả đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            readerDao.delete(selectedId);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mở dialog hiển thị lịch sử điểm của độc giả đã chọn.
     */
    private void openPointsDialog() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn độc giả trước khi xem lịch sử điểm.");
            return;
        }
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        com.library.desktop.ui.dialog.ReaderPointsDialog dialog = new com.library.desktop.ui.dialog.ReaderPointsDialog(
                owner, selectedId);
        dialog.setVisible(true);
        loadData();
    }

    /**
     * Đọc và validate dữ liệu từ form, trả về đối tượng `Reader` hoặc null nếu
     * invalid.
     */
    private Reader readForm(int id) {
        String fullName = fullNameField.getText().trim();
        if (fullName.length() < 2) {
            FormUiHelper.showWarning(this, "Họ tên phải có ít nhất 2 ký tự.");
            return null;
        }

        String email = emailField.getText().trim();
        if (!FormUiHelper.isValidEmail(email)) {
            FormUiHelper.showWarning(this, "Email không hợp lệ.");
            return null;
        }

        String phone = phoneField.getText().trim();
        if (!FormUiHelper.isValidPhone(phone)) {
            FormUiHelper.showWarning(this, "Số điện thoại không hợp lệ.");
            return null;
        }

        String cardCode = cardField.getText().trim();
        if (cardCode.isEmpty()) {
            FormUiHelper.showWarning(this, "Mã thẻ không được rỗng.");
            return null;
        }

        String readerType = typeField.getText().trim();
        if (readerType.isEmpty()) {
            FormUiHelper.showWarning(this, "Loại độc giả không được rỗng.");
            return null;
        }

        StatusItem status = (StatusItem) statusBox.getSelectedItem();
        return new Reader(
                id,
                fullName,
                phone,
                email,
                cardCode,
                readerType,
                status == null || status.active == null || status.active,
                (Integer) trustSpinner.getValue());
    }

    /**
     * Áp dụng bộ lọc tìm kiếm và lọc trạng thái lên TableRowSorter.
     */
    private void applyFilters() {
        String keyword = searchField.getText().trim().toLowerCase();
        StatusItem statusFilter = (StatusItem) filterStatusBox.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String fullName = value(entry, 1);
                String phone = value(entry, 2);
                String email = value(entry, 3);
                String card = value(entry, 4);
                String type = value(entry, 5);
                String status = value(entry, 6);

                boolean matchesKeyword = keyword.isEmpty()
                        || fullName.contains(keyword)
                        || phone.contains(keyword)
                        || email.contains(keyword)
                        || card.contains(keyword)
                        || type.contains(keyword)
                        || status.contains(keyword);

                boolean matchesStatus = statusFilter == null || statusFilter.active == null
                        || status.equalsIgnoreCase(statusFilter.label.toLowerCase());
                return matchesKeyword && matchesStatus;
            }

            private String value(Entry<? extends DefaultTableModel, ? extends Integer> entry, int column) {
                Object raw = entry.getValue(column);
                return raw == null ? "" : raw.toString().toLowerCase();
            }
        });
    }

    /**
     * Tìm item trong `statusBox` tương ứng với trạng thái active.
     */
    private StatusItem findStatusItem(boolean active) {
        for (int i = 0; i < statusBox.getItemCount(); i++) {
            StatusItem item = statusBox.getItemAt(i);
            if (item.active != null && item.active == active) {
                return item;
            }
        }
        return statusBox.getItemAt(0);
    }

    private static final class StatusItem {
        private final Boolean active;
        private final String label;

        private StatusItem(Boolean active, String label) {
            this.active = active;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StatusItem other)) {
                return false;
            }
            return Objects.equals(active, other.active);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(active);
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
