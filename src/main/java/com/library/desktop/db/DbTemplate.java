package com.library.desktop.db;

/**
 * File: DbTemplate.java
 * Mô tả: Tiện ích JDBC đơn giản để thực hiện các thao tác truy vấn/ cập nhật.
 * Cung cấp các phương thức `update`, `query` và `queryOne` để giảm boilerplate khi
 * làm việc với `Connection`/`PreparedStatement`/`ResultSet`.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DbTemplate {
    private DbTemplate() {
    }

    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Thực thi câu lệnh INSERT/UPDATE/DELETE với tham số.
     * @return số dòng bị ảnh hưởng
     */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * Thực thi truy vấn trả về nhiều dòng; sử dụng `RowMapper` để ánh xạ từng `ResultSet`.
     */
    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> items = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapper.map(rs));
                }
            }
        }
        return items;
    }

    /**
     * Thực thi truy vấn mong đợi một dòng kết quả. Trả về null nếu không có dòng nào.
     */
    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> result = query(sql, mapper, params);
        return result.isEmpty() ? null : result.get(0);
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        /**
         * Gán tham số vào `PreparedStatement` bắt đầu từ vị trí 1.
         */
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
