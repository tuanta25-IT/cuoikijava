package com.library.desktop.ui.panel;

/**
 * File: CategoryPanel.java
 * Mô tả: Panel quản lý các thể loại sách: thêm, sửa, xóa và hiển thị danh sách.
 */

import com.library.desktop.dao.CategoryDao;
import com.library.desktop.model.Category;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.util.TableExportUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class CategoryPanel extends JPanel {
    private final CategoryDao categoryDao = new CategoryDao();
    private final AccessProfile accessProfile;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Mã", "Tên thể loại"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField nameField = new JTextField(28);
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
    public CategoryPanel() {
        this(AccessProfile.fromRole("ROLE_ADMIN"));
    }

    /**
     * Constructor: khởi tạo panel với `AccessProfile` cụ thể để kiểm soát quyền.
     */
    public CategoryPanel(AccessProfile accessProfile) {
        this.accessProfile = accessProfile;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppTheme.BACKGROUND);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        formRow.setOpaque(false);
        formRow.add(new JLabel("Tên thể loại:"));
        formRow.add(nameField);

        addBtn.addActionListener(e -> create());
        updateBtn.addActionListener(e -> update());
        deleteBtn.addActionListener(e -> delete());
        refreshBtn.addActionListener(e -> loadData());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "the_loai", table));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "the_loai", table));

        AppTheme.primaryButton(addBtn);
        AppTheme.accentButton(updateBtn);
        AppTheme.dangerButton(deleteBtn);
        AppTheme.neutralButton(refreshBtn);
        AppTheme.successButton(excelBtn);
        AppTheme.warningButton(pdfBtn);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionRow.setOpaque(false);
        actionRow.add(addBtn);
        actionRow.add(updateBtn);
        actionRow.add(deleteBtn);
        actionRow.add(refreshBtn);
        actionRow.add(excelBtn);
        actionRow.add(pdfBtn);

        top.add(formRow, BorderLayout.CENTER);
        top.add(actionRow, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onSelect());
        AppTheme.decorateTable(table);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        applyPermissions();
        loadData();
    }

    /**
     * Áp dụng quyền thao tác (enable/disable) cho các nút dựa trên `accessProfile`.
     */
    private void applyPermissions() {
        boolean editable = accessProfile == null || accessProfile.manageCategories();
        addBtn.setEnabled(editable);
        updateBtn.setEnabled(editable);
        deleteBtn.setEnabled(editable);
    }

    /**
     * Tải danh sách thể loại từ DB và hiển thị trong bảng.
     */
    private void loadData() {
        model.setRowCount(0);
        selectedId = null;
        nameField.setText("");
        try {
            List<Category> categories = categoryDao.findAll();
            for (Category c : categories) {
                model.addRow(new Object[]{c.id(), c.name()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Khi chọn một dòng thể loại, nạp dữ liệu vào form để sửa/xóa.
     */
    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (Integer) model.getValueAt(row, 0);
        nameField.setText(String.valueOf(model.getValueAt(row, 1)));
    }

    /**
     * Thêm thể loại mới (gọi DAO) sau khi validate tên.
     */
    private void create() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên thể loại không được rỗng.");
            return;
        }
        try {
            categoryDao.create(name);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cập nhật thể loại đã chọn với tên mới.
     */
    private void update() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn thể loại cần sửa.");
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên thể loại không được rỗng.");
            return;
        }
        try {
            categoryDao.update(selectedId, name);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xóa thể loại đã chọn (xác nhận trước khi xóa).
     */
    private void delete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Chọn thể loại cần xóa.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Xóa thể loại đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            categoryDao.delete(selectedId);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
