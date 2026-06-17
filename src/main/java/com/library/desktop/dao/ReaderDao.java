package com.library.desktop.dao;

/**
 * File: ReaderDao.java
 * Mô tả: DAO quản lý thực thể độc giả (Reader). Cung cấp các thao tác CRUD và một số tiện ích liên quan.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.Reader;

import java.sql.SQLException;
import java.util.List;

public class ReaderDao {
    public List<Reader> findAll() throws SQLException {
        /**
         * Lấy toàn bộ độc giả từ bảng DocGia.
         */
        String sql = """
                SELECT MaDocGia, HoTen, SoDienThoai, Email, MaThe, LoaiDocGia, TrangThai, DiemUyTin
                FROM DocGia
                ORDER BY MaDocGia ASC
                """;

        return DbTemplate.query(sql, rs -> new Reader(
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("MaThe"),
                rs.getString("LoaiDocGia"),
                readActive(rs.getObject("TrangThai")),
                (Integer) rs.getObject("DiemUyTin")
        ));
    }

    public Reader findByEmail(String email) throws SQLException {
        /**
         * Tìm độc giả theo email, trả về null nếu không tồn tại.
         */
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, MaThe, LoaiDocGia, TrangThai, DiemUyTin FROM DocGia WHERE Email = ?";
        return DbTemplate.queryOne(sql, rs -> new Reader(
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("MaThe"),
                rs.getString("LoaiDocGia"),
                readActive(rs.getObject("TrangThai")),
                (Integer) rs.getObject("DiemUyTin")
        ), email);
    }

    public Reader findById(int id) throws SQLException {
        /**
         * Tìm độc giả theo id (MaDocGia).
         */
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, MaThe, LoaiDocGia, TrangThai, DiemUyTin FROM DocGia WHERE MaDocGia = ?";
        return DbTemplate.queryOne(sql, rs -> new Reader(
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("MaThe"),
                rs.getString("LoaiDocGia"),
                readActive(rs.getObject("TrangThai")),
                (Integer) rs.getObject("DiemUyTin")
        ), id);
    }

    public void create(Reader r) throws SQLException {
        /**
         * Tạo một độc giả mới và ghi audit nếu có người dùng hiện hành.
         */
        String sql = """
                INSERT INTO DocGia (HoTen, SoDienThoai, Email, MaThe, LoaiDocGia, DiemUyTin, TrangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        DbTemplate.update(sql, r.fullName(), r.phone(), r.email(), r.cardCode(), r.readerType(), r.trustScore(), r.active() ? 1 : 0);
        try {
            var user = com.library.desktop.security.Session.getCurrentUser();
            if (user != null) {
                new AuditDao().log(user.username(), "DocGia", "CREATE", "Tạo độc giả: " + r.fullName());
            }
        } catch (Exception ignored) {}
    }

    public void update(Reader r) throws SQLException {
        /**
         * Cập nhật thông tin độc giả và ghi audit.
         */
        String sql = """
                UPDATE DocGia
                SET HoTen = ?, SoDienThoai = ?, Email = ?, MaThe = ?, LoaiDocGia = ?, DiemUyTin = ?, TrangThai = ?
                WHERE MaDocGia = ?
                """;
        DbTemplate.update(sql, r.fullName(), r.phone(), r.email(), r.cardCode(), r.readerType(), r.trustScore(), r.active() ? 1 : 0, r.id());
        try {
            var user = com.library.desktop.security.Session.getCurrentUser();
            if (user != null) {
                new AuditDao().log(user.username(), "DocGia", "UPDATE", "Cập nhật độc giả: " + r.id());
            }
        } catch (Exception ignored) {}
    }

    public void delete(int id) throws SQLException {
        /**
         * Xóa độc giả theo id và ghi audit.
         */
        DbTemplate.update("DELETE FROM DocGia WHERE MaDocGia = ?", id);
        try {
            var user = com.library.desktop.security.Session.getCurrentUser();
            if (user != null) {
                new AuditDao().log(user.username(), "DocGia", "DELETE", "Xóa độc giả: " + id);
            }
        } catch (Exception ignored) {}
    }

    public void adjustPoints(int readerId, int delta) throws SQLException {
        /**
         * Điều chỉnh điểm uy tín (DiemUyTin) của độc giả.
         */
        DbTemplate.update("UPDATE DocGia SET DiemUyTin = COALESCE(DiemUyTin,0) + ? WHERE MaDocGia = ?", delta, readerId);
    }

    private boolean readActive(Object value) {
        /**
         * Chuyển giá trị cột TrangThai (nhiều dạng) thành boolean.
         */
        if (value == null) {
            return true;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        String text = value.toString().trim();
        return !("0".equals(text) || "FALSE".equalsIgnoreCase(text));
    }
}
