package com.library.desktop.ui.panel;

/**
 * File: AuditPanel.java
 * Mô tả: Panel hiển thị nhật ký audit của hệ thống. Cho phép làm mới và xuất audit log.
 */

import com.library.desktop.dao.AuditDao;
import com.library.desktop.model.AuditEntry;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.util.TableExportUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditPanel extends JPanel {
    private final AuditDao auditDao = new AuditDao();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Người", "Module", "Hành động", "Chi tiết", "Thời gian"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public AuditPanel() {
        /**
         * Constructor: khởi tạo bảng audit và các nút thao tác.
         */
        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.BACKGROUND);
        JTable table = new JTable(model);
        AppTheme.decorateTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        top.setOpaque(false);
        JButton refresh = new JButton("Làm mới");
        JButton excel = new JButton("Excel");
        refresh.addActionListener(e -> loadData());
        excel.addActionListener(e -> TableExportUtils.exportExcel(this, "audit_log", table));
        top.add(refresh);
        top.add(excel);
        add(top, BorderLayout.NORTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<AuditEntry> rows = auditDao.findAll();
            for (AuditEntry a : rows) {
                model.addRow(new Object[]{a.id(), a.username(), a.module(), a.action(), a.details(), a.createdAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được audit log: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
