package com.library.desktop.ui.panel;

/**
 * File: ReservationPanel.java
 * Mô tả: Panel quản lý các đặt trước sách của độc giả. Cung cấp giao diện để tạo,
 * hủy đặt trước và xuất báo cáo danh sách đặt trước.
 */

import com.library.desktop.dao.ReservationDao;
import com.library.desktop.model.Reservation;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.util.TableExportUtils;
import com.library.desktop.util.StatusText;

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
import java.util.ArrayList;
import java.util.List;

public class ReservationPanel extends JPanel {
    private final ReservationDao reservationDao = new ReservationDao();
    private final AccessProfile accessProfile;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã đặt", "Mã độc giả", "Độc giả", "Mã sách", "Tiêu đề", "Ngày đặt", "Hạn giữ",
                    "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final List<Reservation> reservations = new ArrayList<>();

    private final JTextField readerIdField = new JTextField(6);
    private final JTextField bookIdField = new JTextField(6);
    private final JTextField holdHoursField = new JTextField("24", 4);
    private final JButton createBtn = new JButton("Đặt trước");
    private final JButton cancelBtn = new JButton("Hủy đặt");
    private final JButton refreshBtn = new JButton("Làm mới");
    private final JButton excelBtn = new JButton("Excel");
    private final JButton pdfBtn = new JButton("PDF");

    private Integer selectedReservationId;

    /**
     * Constructor mặc định dùng role `ROLE_ADMIN` để khởi tạo panel.
     */
    public ReservationPanel() {
        this(AccessProfile.fromRole("ROLE_ADMIN"));
    }

    /**
     * Constructor: khởi tạo panel với `AccessProfile` cụ thể để điều khiển quyền
     * thao tác.
     */
    public ReservationPanel(AccessProfile accessProfile) {
        this.accessProfile = accessProfile;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppTheme.BACKGROUND);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        formRow.setOpaque(false);
        formRow.add(new JLabel("Mã độc giả:"));
        formRow.add(readerIdField);
        formRow.add(new JLabel("Mã sách:"));
        formRow.add(bookIdField);
        formRow.add(new JLabel("Giữ (giờ):"));
        formRow.add(holdHoursField);

        createBtn.addActionListener(e -> createReservation());
        cancelBtn.addActionListener(e -> cancelReservation());
        refreshBtn.addActionListener(e -> loadData());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "dat_truoc", table));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "dat_truoc", table));

        AppTheme.primaryButton(createBtn);
        AppTheme.dangerButton(cancelBtn);
        AppTheme.neutralButton(refreshBtn);
        AppTheme.successButton(excelBtn);
        AppTheme.warningButton(pdfBtn);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionRow.setOpaque(false);
        actionRow.add(createBtn);
        actionRow.add(cancelBtn);
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
     * Áp dụng quyền (enable/disable) các điều khiển theo `accessProfile`.
     */
    private void applyPermissions() {
        boolean editable = accessProfile == null || accessProfile.manageReservations();
        createBtn.setEnabled(editable);
        cancelBtn.setEnabled(editable);
        readerIdField.setEnabled(editable);
        bookIdField.setEnabled(editable);
        holdHoursField.setEnabled(editable);
    }

    /**
     * Tải danh sách đặt trước từ DAO và hiển thị lên bảng.
     */
    private void loadData() {
        model.setRowCount(0);
        reservations.clear();
        selectedReservationId = null;

        try {
            List<Reservation> all = reservationDao.findAll();
            reservations.addAll(all);
            for (Reservation r : all) {
                model.addRow(new Object[] {
                        r.id(), r.readerId(), r.readerName(), r.bookId(), r.bookTitle(), r.reservedAt(), r.holdUntil(),
                        StatusText.reservation(r.status())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý khi người dùng chọn một hàng trong bảng; lưu lại
     * `selectedReservationId`.
     */
    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= reservations.size()) {
            return;
        }
        selectedReservationId = reservations.get(row).id();
    }

    /**
     * Tạo một đặt trước mới dựa trên giá trị nhập vào (readerId, bookId,
     * holdHours).
     * Thực hiện validate và gọi DAO.
     */
    private void createReservation() {
        try {
            int readerId = Integer.parseInt(readerIdField.getText().trim());
            int bookId = Integer.parseInt(bookIdField.getText().trim());
            int holdHours = Integer.parseInt(holdHoursField.getText().trim());
            reservationDao.create(readerId, bookId, holdHours);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã độc giả, mã sách và giờ giữ phải là số nguyên.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Hủy đặt trước đã chọn (gọi DAO để cập nhật trạng thái hủy).
     */
    private void cancelReservation() {
        if (selectedReservationId == null) {
            JOptionPane.showMessageDialog(this, "Chọn đặt trước cần hủy.");
            return;
        }

        try {
            reservationDao.cancel(selectedReservationId);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
