package com.library.desktop.dao;

/**
 * File: BookDao.java
 * Mô tả: DAO quản lý sách. Cung cấp các phương thức tìm kiếm, thêm, sửa, xóa sách.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.Book;

import java.sql.SQLException;
import java.util.List;

public class BookDao {
    public List<Book> findAll() throws SQLException {
        String sql = """
                SELECT s.MaSach, s.TieuDe, s.TacGia, s.MaTheLoai, t.TenTheLoai, s.TrangThai, s.SoLuong
                FROM Sach s
                LEFT JOIN TheLoai t ON s.MaTheLoai = t.MaTheLoai
                ORDER BY s.MaSach ASC
                """;

        return DbTemplate.query(sql, rs -> new Book(
                rs.getInt("MaSach"),
                rs.getString("TieuDe"),
                rs.getString("TacGia"),
                (Integer) rs.getObject("MaTheLoai"),
                rs.getString("TenTheLoai"),
                rs.getString("TrangThai"),
                (Integer) rs.getObject("SoLuong")));
    }

    public void create(Book b) throws SQLException {
        String sql = """
                INSERT INTO Sach (TieuDe, TacGia, MaTheLoai, TrangThai, SoLuong)
                VALUES (?, ?, ?, ?, ?)
                """;
        DbTemplate.update(sql, b.title(), b.author(), b.categoryId(), b.status(), b.quantity());
    }

    public void update(Book b) throws SQLException {
        String sql = """
                UPDATE Sach
                SET TieuDe = ?, TacGia = ?, MaTheLoai = ?, TrangThai = ?, SoLuong = ?
                WHERE MaSach = ?
                """;
        DbTemplate.update(sql, b.title(), b.author(), b.categoryId(), b.status(), b.quantity(), b.id());
    }

    public void delete(int id) throws SQLException {
        DbTemplate.update("DELETE FROM Sach WHERE MaSach = ?", id);
    }
}
