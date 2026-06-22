package com.library.desktop.ui.panel;

/**
 * File: BookPanel.java
 * Mô tả: Panel quản lý sách, bao gồm tìm kiếm, thêm/sửa/xóa sách, xuất báo cáo và quản lý thể loại.
 */

import com.library.desktop.dao.BookDao;
import com.library.desktop.dao.CategoryDao;
import com.library.desktop.model.Book;
import com.library.desktop.model.Category;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.ui.FormUiHelper;
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

public class BookPanel extends JPanel {
    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final AccessProfile accessProfile;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã", "Tiêu đề", "Tác giả", "Thể loại", "Trạng thái", "Số lượng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final List<Book> currentBooks = new ArrayList<>();

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<FilterItem> filterStatusBox = new JComboBox<>(new FilterItem[] {
            new FilterItem("ALL", "Tất cả"),
            new FilterItem("AVAILABLE", "Còn sách"),
            new FilterItem("BORROWING", "Đang mượn"),
            new FilterItem("LOST", "Bị mất"),
            new FilterItem("DAMAGED", "Bị hỏng")
    });
    private final JTextField titleField = new JTextField(14);
    private final JTextField authorField = new JTextField(14);
    private final JComboBox<StatusItem> statusBox = new JComboBox<>(new StatusItem[] {
            new StatusItem("AVAILABLE", "Còn sách"),
            new StatusItem("BORROWING", "Đang mượn"),
            new StatusItem("LOST", "Bị mất"),
            new StatusItem("DAMAGED", "Bị hỏng")
    });
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
    private final JComboBox<CategoryItem> categoryBox = new JComboBox<>();
    private final JButton addBtn = new JButton("Thêm");
    private final JButton updateBtn = new JButton("Sửa");
    private final JButton deleteBtn = new JButton("Xóa");
    private final JButton refreshBtn = new JButton("Làm mới");
    private final JButton excelBtn = new JButton("Excel");
    private final JButton pdfBtn = new JButton("PDF");

    private Integer selectedId;

    /**
     * Constructor mặc định sử dụng role `ROLE_ADMIN`.
     */
    public BookPanel() {
        this(AccessProfile.fromRole("ROLE_ADMIN"));
    }

    /**
     * Constructor: khởi tạo panel với profile truy cập.
     */
    public BookPanel(AccessProfile accessProfile) {
        this.accessProfile = accessProfile;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppTheme.BACKGROUND);

        FormUiHelper.configureTextField(searchField, 14);
        FormUiHelper.configureTextField(titleField, 14);
        FormUiHelper.configureTextField(authorField, 14);
        FormUiHelper.configureSpinner(quantitySpinner);

        JPanel top = new JPanel();
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel filterRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        filterRow.add(new JLabel("Tìm kiếm:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Lọc trạng thái:"));
        filterRow.add(filterStatusBox);

        JPanel formRow = FormUiHelper.createFlowRow(FlowLayout.LEFT, 10, 8);
        formRow.add(new JLabel("Tiêu đề:"));
        formRow.add(titleField);
        formRow.add(new JLabel("Tác giả:"));
        formRow.add(authorField);
        formRow.add(new JLabel("Thể loại:"));
        formRow.add(categoryBox);
        formRow.add(new JLabel("Trạng thái:"));
        formRow.add(statusBox);
        formRow.add(new JLabel("Số lượng:"));
        formRow.add(quantitySpinner);

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

        addBtn.addActionListener(e -> create());
        updateBtn.addActionListener(e -> update());
        deleteBtn.addActionListener(e -> delete());
        refreshBtn.addActionListener(e -> loadData());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "sach", table));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "sach", table));

        top.add(FormUiHelper.createSection("Tìm kiếm và lọc", filterRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Thông tin sách", formRow));
        top.add(javax.swing.Box.createVerticalStrut(10));
        top.add(FormUiHelper.createSection("Thao tác", actionRow));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelect());
        AppTheme.decorateTable(table);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        filterStatusBox.addActionListener(e -> applyFilters());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadCategories();
        applyPermissions();
        loadData();
    }

    /**
     * Áp dụng quyền thao tác cho các nút và trường nhập.
     */
    private void applyPermissions() {
        boolean editable = accessProfile == null || accessProfile.manageBooks();
        addBtn.setEnabled(editable);
        updateBtn.setEnabled(editable);
        deleteBtn.setEnabled(editable);
        titleField.setEnabled(editable);
        authorField.setEnabled(editable);
        statusBox.setEnabled(editable);
        quantitySpinner.setEnabled(editable);
        categoryBox.setEnabled(editable);
    }

    /**
     * Tải danh sách thể loại và nạp vào `categoryBox`.
     */
    private void loadCategories() {
        categoryBox.removeAllItems();
        try {
            for (Category c : categoryDao.findAll()) {
                categoryBox.addItem(new CategoryItem(c.id(), c.name()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được thể loại: " + ex.getMessage());
        }
    }

    /**
     * Tải danh sách sách từ DAO và nạp vào bảng.
     */
    private void loadData() {
        model.setRowCount(0);
        currentBooks.clear();
        selectedId = null;

        try {
            List<Book> books = bookDao.findAll();
            currentBooks.addAll(books);
            for (Book b : books) {
                model.addRow(new Object[] {
                        b.id(),
                        b.title(),
                        b.author(),
                        b.categoryName(),
                        StatusText.book(b.status()),
                        b.quantity()
                });
            }
            applyFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Khi chọn sách trên bảng, nạp dữ liệu vào form để sửa/xóa.
     */
    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= currentBooks.size()) {
            return;
        }

        Book b = currentBooks.get(modelRow);
        selectedId = b.id();
        titleField.setText(b.title());
        authorField.setText(b.author());
        quantitySpinner.setValue(b.quantity() == null ? 0 : Math.max(0, b.quantity()));

        for (int i = 0; i < statusBox.getItemCount(); i++) {
            StatusItem item = statusBox.getItemAt(i);
            if (item.code.equalsIgnoreCase(b.status())) {
                statusBox.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            CategoryItem item = categoryBox.getItemAt(i);
            if (item.id != null && item.id.equals(b.categoryId())) {
                categoryBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Tạo sách mới dựa trên dữ liệu form sau khi validate.
     */
    private void create() {
        Book newBook = readForm(0);
        if (newBook == null) {
            return;
        }
        try {
            bookDao.create(newBook);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cập nhật sách đã chọn theo dữ liệu form.
     */
    private void update() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn sách cần sửa.");
            return;
        }
        Book updated = readForm(selectedId);
        if (updated == null) {
            return;
        }

        try {
            bookDao.update(updated);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xóa sách đã chọn (xác nhận trước khi xóa).
     */
    private void delete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn sách cần xóa.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Xóa sách đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            bookDao.delete(selectedId);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Đọc và validate dữ liệu từ form, trả về đối tượng `Book` hoặc null nếu
     * invalid.
     */
    private Book readForm(int id) {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        CategoryItem category = (CategoryItem) categoryBox.getSelectedItem();
        StatusItem status = (StatusItem) statusBox.getSelectedItem();

        if (title.length() < 2) {
            FormUiHelper.showWarning(this, "Tiêu đề phải có ít nhất 2 ký tự.");
            return null;
        }
        if (author.length() < 2) {
            FormUiHelper.showWarning(this, "Tác giả phải có ít nhất 2 ký tự.");
            return null;
        }
        if (category == null) {
            FormUiHelper.showWarning(this, "Vui lòng chọn thể loại sách.");
            return null;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        if (quantity < 0) {
            FormUiHelper.showWarning(this, "Số lượng không được âm.");
            return null;
        }

        return new Book(id, title, author, category == null ? null : category.id, null,
                status == null ? null : status.code, quantity);
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim().toLowerCase();
        FilterItem statusFilter = (FilterItem) filterStatusBox.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String title = value(entry, 1);
                String author = value(entry, 2);
                String category = value(entry, 3);
                String status = value(entry, 4);

                boolean matchesKeyword = keyword.isEmpty()
                        || title.contains(keyword)
                        || author.contains(keyword)
                        || category.contains(keyword)
                        || status.contains(keyword);

                boolean matchesStatus = statusFilter == null || "ALL".equals(statusFilter.code)
                        || status.equalsIgnoreCase(statusFilter.label.toLowerCase());
                return matchesKeyword && matchesStatus;
            }

            private String value(Entry<? extends DefaultTableModel, ? extends Integer> entry, int column) {
                Object raw = entry.getValue(column);
                return raw == null ? "" : raw.toString().toLowerCase();
            }
        });
    }

    private static final class CategoryItem {
        private final Integer id;
        private final String name;

        private CategoryItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class StatusItem {
        private final String code;
        private final String label;

        private StatusItem(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
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
