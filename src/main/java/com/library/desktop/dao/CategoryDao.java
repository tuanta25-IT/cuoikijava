package com.library.desktop.dao;

/**
 * File: CategoryDao.java
 * Mô tả: DAO cho bảng thể loại (TheLoai) cung cấp các thao tác CRUD cơ bản.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryDao {
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT MaTheLoai, TenTheLoai FROM TheLoai ORDER BY MaTheLoai ASC";
        return DbTemplate.query(sql, rs -> new Category(
                rs.getInt("MaTheLoai"),
                rs.getString("TenTheLoai")
        ));
    }

    public void create(String name) throws SQLException {
        DbTemplate.update("INSERT INTO TheLoai (TenTheLoai) VALUES (?)", name);
    }

    public void update(int id, String name) throws SQLException {
        DbTemplate.update("UPDATE TheLoai SET TenTheLoai = ? WHERE MaTheLoai = ?", name, id);
    }

    public void delete(int id) throws SQLException {
        DbTemplate.update("DELETE FROM TheLoai WHERE MaTheLoai = ?", id);
    }
}
