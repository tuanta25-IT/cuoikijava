package com.library.desktop.ui.dialog;

import com.library.desktop.dao.PointHistoryDao;
import com.library.desktop.model.AuditEntry;
import com.library.desktop.ui.AppTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReaderPointsDialog extends JDialog {
    private final int readerId;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Người","Ghi chú","Thời gian"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public ReaderPointsDialog(Window owner, int readerId) {
        super(owner, "Lịch sử điểm", ModalityType.APPLICATION_MODAL);
        this.readerId = readerId;
        setSize(640, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));
        JTable table = new JTable(model);
        AppTheme.decorateTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton refresh = new JButton("Làm mới");
        JButton add = new JButton("Thêm/Điều chỉnh điểm");
        top.add(refresh);
        top.add(add);
        add(refresh, BorderLayout.NORTH);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton close = new JButton("Đóng");
        close.addActionListener(e -> dispose());
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadData());
        add.addActionListener(e -> openAdjustDialog());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            PointHistoryDao dao = new PointHistoryDao();
            List<AuditEntry> rows = dao.findByReader(readerId);
            for (AuditEntry a : rows) {
                model.addRow(new Object[]{a.id(), a.username(), a.details(), a.createdAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được lịch sử điểm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAdjustDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JSpinner deltaSpinner = new JSpinner(new SpinnerNumberModel(0, -9999, 9999, 1));
        JTextField reasonField = new JTextField("Điều chỉnh", 18);
        panel.add(new JLabel("Thay đổi điểm:"));
        panel.add(deltaSpinner);
        panel.add(new JLabel("Lý do:"));
        panel.add(reasonField);

        int ok = JOptionPane.showConfirmDialog(this, panel, "Điều chỉnh điểm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        int delta = (Integer) deltaSpinner.getValue();
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            reason = "Điều chỉnh";
        }

        try {
            var user = com.library.desktop.security.Session.getCurrentUser();
            String by = user == null ? "system" : user.username();
            new com.library.desktop.dao.PointHistoryDao().addEntry(readerId, delta, reason, by);
            new com.library.desktop.dao.ReaderDao().adjustPoints(readerId, delta);
            JOptionPane.showMessageDialog(this, "Đã thêm lịch sử điểm.");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
