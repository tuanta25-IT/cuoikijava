package com.library.desktop.dao;

/**
 * File: LoanDao.java
 * Mô tả: Lớp truy cập dữ liệu (DAO) cho thực thể phiếu mượn. Chứa các phương thức
 * để truy vấn, tạo, cập nhật trạng thái và quản lý chi tiết phiếu mượn.
 */

import com.library.desktop.db.DatabaseManager;
import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.LoanItem;
import com.library.desktop.model.Loan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class LoanDao {
    public List<Loan> findAll() throws SQLException {
        /**
         * Lấy tất cả phiếu mượn (kèm thông tin độc giả) từ cơ sở dữ liệu.
         */
        String sql = """
                SELECT pm.MaPhieuMuon, pm.MaDocGia, dg.HoTen,
                   dg.Email as Email, pm.NgayMuon, pm.NgayTraDuKien, pm.NgayTraThuc, pm.TrangThai
                FROM PhieuMuon pm
                JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia
                ORDER BY pm.MaPhieuMuon ASC
                """;

        return DbTemplate.query(sql, rs -> new Loan(
                rs.getInt("MaPhieuMuon"),
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("Email"),
                toLocalDate(rs.getDate("NgayMuon")),
                toLocalDate(rs.getDate("NgayTraDuKien")),
                toLocalDate(rs.getDate("NgayTraThuc")),
                rs.getString("TrangThai")));
    }

    public Loan findById(int loanId) throws SQLException {
        /**
         * Lấy phiếu mượn theo `loanId`.
         */
        String sql = """
                SELECT pm.MaPhieuMuon, pm.MaDocGia, dg.HoTen,
                   dg.Email as Email, pm.NgayMuon, pm.NgayTraDuKien, pm.NgayTraThuc, pm.TrangThai
                FROM PhieuMuon pm
                JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia
                WHERE pm.MaPhieuMuon = ?
                """;

        return DbTemplate.queryOne(sql, rs -> new Loan(
                rs.getInt("MaPhieuMuon"),
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("Email"),
                toLocalDate(rs.getDate("NgayMuon")),
                toLocalDate(rs.getDate("NgayTraDuKien")),
                toLocalDate(rs.getDate("NgayTraThuc")),
                rs.getString("TrangThai")), loanId);
    }

    public List<LoanItem> findItemsByLoanId(int loanId) throws SQLException {
        /**
         * Lấy danh sách chi tiết (LoanItem) cho một phiếu mượn.
         */
        String sql = """
                SELECT ct.MaChiTiet, ct.MaPhieuMuon, ct.MaSach, s.TieuDe, ct.SoLuong
                FROM ChiTietPhieuMuon ct
                JOIN Sach s ON ct.MaSach = s.MaSach
                WHERE ct.MaPhieuMuon = ?
                ORDER BY ct.MaChiTiet ASC
                """;

        return DbTemplate.query(sql, rs -> new LoanItem(
                rs.getInt("MaChiTiet"),
                rs.getInt("MaPhieuMuon"),
                rs.getInt("MaSach"),
                rs.getString("TieuDe"),
                (Integer) rs.getObject("SoLuong")), loanId);
    }

    public int create(int readerId, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        /**
         * Tạo một phiếu mượn mới và trả về id được sinh tự động.
         */
        String sql = """
                INSERT INTO PhieuMuon (MaDocGia, NgayMuon, NgayTraDuKien, TrangThai, SoNgayGiaHan)
                VALUES (?, ?, ?, N'BORROWING', 0)
                """;
        try (Connection con = DatabaseManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, readerId);
            ps.setDate(2, Date.valueOf(borrowDate));
            ps.setDate(3, Date.valueOf(dueDate));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    try {
                        var user = com.library.desktop.security.Session.getCurrentUser();
                        if (user != null) {
                            new com.library.desktop.dao.AuditDao().log(user.username(), "PhieuMuon", "CREATE",
                                    "Tạo phiếu mượn: " + id + " cho độc giả " + readerId);
                        }
                    } catch (Exception ignored) {
                    }
                    return id;
                }
            }
        }
        throw new SQLException("Không tạo được phiếu mượn");
    }

    public void markReturned(int loanId, LocalDate returnedDate) throws SQLException {
        /**
         * Đánh dấu một phiếu mượn là đã trả, cập nhật kho (restore stock) và điều chỉnh
         * điểm độc giả.
         */
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = readLoanStatus(con, loanId);
                if (status == null) {
                    throw new SQLException("Không tìm thấy phiếu mượn: " + loanId);
                }
                if (!"RETURNED".equalsIgnoreCase(status)) {
                    restoreStockForLoan(con, loanId);
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE PhieuMuon
                        SET NgayTraThuc = ?, TrangThai = N'RETURNED'
                        WHERE MaPhieuMuon = ?
                        """)) {
                    ps.setDate(1, Date.valueOf(returnedDate));
                    ps.setInt(2, loanId);
                    ps.executeUpdate();
                }
                // adjust reader points based on returned date vs due date
                try {
                    LocalDate due = null;
                    try (PreparedStatement q = con
                            .prepareStatement("SELECT MaDocGia, NgayTraDuKien FROM PhieuMuon WHERE MaPhieuMuon = ?")) {
                        q.setInt(1, loanId);
                        try (java.sql.ResultSet rs = q.executeQuery()) {
                            if (rs.next()) {
                                int readerId = rs.getInt("MaDocGia");
                                java.sql.Date d = rs.getDate("NgayTraDuKien");
                                due = d == null ? null : d.toLocalDate();
                                if (due != null) {
                                    int delta = returnedDate.isBefore(due) || returnedDate.isEqual(due) ? 10 : -2;
                                    // update DocGia.DiemUyTin
                                    try (PreparedStatement up = con.prepareStatement(
                                            "UPDATE DocGia SET DiemUyTin = COALESCE(DiemUyTin,0) + ? WHERE MaDocGia = ?")) {
                                        up.setInt(1, delta);
                                        up.setInt(2, readerId);
                                        up.executeUpdate();
                                    }
                                    // insert into PointHistory
                                    try (PreparedStatement ph = con.prepareStatement(
                                            "INSERT INTO PointHistory (MaDocGia, ChangeValue, Reason, CreatedAt, CreatedBy) VALUES (?, ?, ?, ?, ?)")) {
                                        ph.setInt(1, readerId);
                                        ph.setInt(2, delta);
                                        ph.setString(3, delta > 0 ? "Trả đúng hạn" : "Trả trễ");
                                        ph.setTimestamp(4, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                                        var user = com.library.desktop.security.Session.getCurrentUser();
                                        ph.setString(5, user == null ? "system" : user.username());
                                        ph.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }

                try {
                    var user = com.library.desktop.security.Session.getCurrentUser();
                    if (user != null) {
                        new com.library.desktop.dao.AuditDao().log(user.username(), "PhieuMuon", "RETURN",
                                "Đánh dấu trả: " + loanId);
                    }
                } catch (Exception ignored) {
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void renew(int loanId, int days) throws SQLException {
        /**
         * Gia hạn phiếu mượn thêm `days` ngày (cập nhật NgayTraDuKien và SoNgayGiaHan).
         */
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = readLoanStatus(con, loanId);
                if (status == null) {
                    throw new SQLException("Không tìm thấy phiếu mượn: " + loanId);
                }
                if ("RETURNED".equalsIgnoreCase(status)) {
                    throw new SQLException("Không thể gia hạn phiếu mượn đã trả");
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE PhieuMuon
                        SET NgayTraDuKien = DATEADD(day, ?, NgayTraDuKien),
                            SoNgayGiaHan = COALESCE(SoNgayGiaHan, 0) + ?
                        WHERE MaPhieuMuon = ?
                        """)) {
                    ps.setInt(1, days);
                    ps.setInt(2, days);
                    ps.setInt(3, loanId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void updateBorrowDates(int loanId, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        /**
         * Cập nhật NgayMuon và NgayTraDuKien cho phiếu mượn; kiểm tra tính hợp lệ trước
         * khi cập nhật.
         */
        if (borrowDate == null || dueDate == null) {
            throw new SQLException("Ngày mượn và ngày trả dự kiến không được để trống");
        }
        if (dueDate.isBefore(borrowDate)) {
            throw new SQLException("Ngày trả dự kiến phải lớn hơn hoặc bằng ngày mượn");
        }

        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = readLoanStatus(con, loanId);
                if (status == null) {
                    throw new SQLException("Không tìm thấy phiếu mượn: " + loanId);
                }
                if ("RETURNED".equalsIgnoreCase(status)) {
                    throw new SQLException("Không thể chỉnh sửa ngày của phiếu mượn đã trả");
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE PhieuMuon
                        SET NgayMuon = ?, NgayTraDuKien = ?
                        WHERE MaPhieuMuon = ?
                        """)) {
                    ps.setDate(1, Date.valueOf(borrowDate));
                    ps.setDate(2, Date.valueOf(dueDate));
                    ps.setInt(3, loanId);
                    ps.executeUpdate();
                }

                try {
                    var user = com.library.desktop.security.Session.getCurrentUser();
                    if (user != null) {
                        new com.library.desktop.dao.AuditDao().log(
                                user.username(),
                                "PhieuMuon",
                                "UPDATE",
                                "Cập nhật ngày mượn/ngày trả dự kiến cho phiếu: " + loanId);
                    }
                } catch (Exception ignored) {
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void addItem(int loanId, int bookId, int quantity) throws SQLException {
        /**
         * Thêm một mục sách vào chi tiết phiếu mượn và điều chỉnh tồn kho nếu cần.
         */
        int qty = Math.max(quantity, 1);
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String status = readLoanStatus(con, loanId);
                if (status == null) {
                    throw new SQLException("Không tìm thấy phiếu mượn: " + loanId);
                }

                int stock = readStock(con, bookId);
                if (!"PENDING".equalsIgnoreCase(status) && stock < qty) {
                    throw new SQLException("Sách không đủ số lượng");
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO ChiTietPhieuMuon (MaPhieuMuon, MaSach, SoLuong)
                        VALUES (?, ?, ?)
                        """)) {
                    ps.setInt(1, loanId);
                    ps.setInt(2, bookId);
                    ps.setInt(3, qty);
                    ps.executeUpdate();
                }

                if (!"PENDING".equalsIgnoreCase(status)) {
                    adjustStock(con, bookId, -qty);
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void removeItem(int detailId) throws SQLException {
        /**
         * Xóa một chi tiết phiếu mượn và trả lại tồn kho nếu phiếu không ở trạng thái
         * PENDING.
         */
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer loanId = null;
                Integer bookId = null;
                Integer quantity = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT MaPhieuMuon, MaSach, SoLuong FROM ChiTietPhieuMuon WHERE MaChiTiet = ?")) {
                    ps.setInt(1, detailId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            loanId = rs.getInt("MaPhieuMuon");
                            bookId = rs.getInt("MaSach");
                            quantity = (Integer) rs.getObject("SoLuong");
                        }
                    }
                }

                if (loanId == null) {
                    throw new SQLException("Không tìm thấy chi tiết phiếu mượn: " + detailId);
                }

                String status = readLoanStatus(con, loanId);
                if (status == null) {
                    throw new SQLException("Không tìm thấy phiếu mượn: " + loanId);
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuMuon WHERE MaChiTiet = ?")) {
                    ps.setInt(1, detailId);
                    ps.executeUpdate();
                }

                if (!"PENDING".equalsIgnoreCase(status)) {
                    adjustStock(con, bookId, quantity == null ? 1 : quantity);
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private LocalDate toLocalDate(Date date) {
        /**
         * Chuyển `java.sql.Date` thành `java.time.LocalDate`.
         */
        return date == null ? null : date.toLocalDate();
    }

    private String readLoanStatus(Connection con, int loanId) throws SQLException {
        /**
         * Đọc trạng thái (`TrangThai`) của phiếu mượn từ cơ sở dữ liệu.
         */
        try (PreparedStatement ps = con.prepareStatement("SELECT TrangThai FROM PhieuMuon WHERE MaPhieuMuon = ?")) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private int readStock(Connection con, int bookId) throws SQLException {
        /**
         * Đọc số lượng tồn kho hiện tại của sách.
         */
        try (PreparedStatement ps = con.prepareStatement("SELECT SoLuong FROM Sach WHERE MaSach = ?")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object value = rs.getObject(1);
                    return value == null ? 0 : ((Number) value).intValue();
                }
            }
        }
        return 0;
    }

    private void adjustStock(Connection con, int bookId, int delta) throws SQLException {
        /**
         * Điều chỉnh tồn kho cho sách (tăng/giảm theo `delta`).
         */
        try (PreparedStatement ps = con
                .prepareStatement("UPDATE Sach SET SoLuong = COALESCE(SoLuong, 0) + ? WHERE MaSach = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    private void restoreStockForLoan(Connection con, int loanId) throws SQLException {
        /**
         * Trả lại tồn kho cho tất cả sách thuộc chi tiết phiếu mượn (khi phiếu được
         * đánh dấu đã trả).
         */
        try (PreparedStatement ps = con
                .prepareStatement("SELECT MaSach, SoLuong FROM ChiTietPhieuMuon WHERE MaPhieuMuon = ?")) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bookId = rs.getInt("MaSach");
                    int quantity = rs.getObject("SoLuong") == null ? 1 : ((Number) rs.getObject("SoLuong")).intValue();
                    adjustStock(con, bookId, quantity);
                }
            }
        }
    }

    public java.util.List<com.library.desktop.model.LoanInfo> findActiveLoansDueOn(java.time.LocalDate date)
            throws SQLException {
        /**
         * Tìm các phiếu mượn đang mượn (BORROWING) có `NgayTraDuKien` bằng `date`.
         */
        String sql = """
                SELECT pm.MaPhieuMuon, pm.MaDocGia, dg.HoTen, dg.Email, pm.NgayTraDuKien
                FROM PhieuMuon pm
                JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia
                WHERE pm.TrangThai = N'BORROWING' AND pm.NgayTraDuKien = ?
                """;
        return DbTemplate.query(sql, rs -> {
            com.library.desktop.model.LoanInfo info = new com.library.desktop.model.LoanInfo(
                    rs.getInt("MaPhieuMuon"),
                    rs.getInt("MaDocGia"),
                    rs.getString("HoTen"),
                    rs.getString("Email"),
                    toLocalDate(rs.getDate("NgayTraDuKien")));
            return info;
        }, java.sql.Date.valueOf(date));
    }
}
