package com.library.desktop.dao;

/**
 * File: DashboardDao.java
 * Mô tả: DAO cung cấp các truy vấn thống kê cho dashboard: tổng quan, phân bố trạng thái,
 * xu hướng theo thời gian và top readers.
 */

import com.library.desktop.db.DbTemplate;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDao {
    public Map<String, Integer> getSummaryStats() throws SQLException {
        /**
         * Trả về bản đồ các chỉ tiêu tổng quan (tổng sách, tổng độc giả, ...).
         */
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("Tổng số sách", count("SELECT COUNT(*) AS total FROM Sach"));
        stats.put("Tổng số độc giả", count("SELECT COUNT(*) AS total FROM DocGia"));
        stats.put("Phiếu mượn đang mượn", count("SELECT COUNT(*) AS total FROM PhieuMuon WHERE TrangThai = N'BORROWING'"));
        stats.put("Phiếu mượn quá hạn", count("SELECT COUNT(*) AS total FROM PhieuMuon WHERE TrangThai = N'OVERDUE'"));
        stats.put("Đặt trước sách", count("SELECT COUNT(*) AS total FROM DatTruocSach WHERE TrangThai = N'ACTIVE'"));
        return stats;
    }

    public Map<String, Integer> getBookStatusBreakdown() throws SQLException {
        return groupedCounts("SELECT COALESCE(TrangThai, N'UNKNOWN') AS label, COUNT(*) AS total FROM Sach GROUP BY COALESCE(TrangThai, N'UNKNOWN') ORDER BY total DESC");
    }

    public Map<String, Integer> getLoanStatusBreakdown() throws SQLException {
        return groupedCounts("SELECT COALESCE(TrangThai, N'UNKNOWN') AS label, COUNT(*) AS total FROM PhieuMuon GROUP BY COALESCE(TrangThai, N'UNKNOWN') ORDER BY total DESC");
    }

    public Map<String, Integer> getReaderTypeBreakdown() throws SQLException {
        return groupedCounts("SELECT COALESCE(LoaiDocGia, N'Không rõ') AS label, COUNT(*) AS total FROM DocGia GROUP BY COALESCE(LoaiDocGia, N'Không rõ') ORDER BY total DESC");
    }

    public Map<String, Integer> getReaderActivityBreakdown() throws SQLException {
        return groupedCounts("SELECT CASE WHEN COALESCE(TrangThai, 1) = 1 THEN N'Hoạt động' ELSE N'Ngừng hoạt động' END AS label, COUNT(*) AS total FROM DocGia GROUP BY CASE WHEN COALESCE(TrangThai, 1) = 1 THEN N'Hoạt động' ELSE N'Ngừng hoạt động' END ORDER BY total DESC");
    }

    public Map<java.time.LocalDate, Integer> getLoanTrend(java.time.LocalDate from, java.time.LocalDate to) throws SQLException {
        /**
         * Trả về số phiếu mượn theo ngày trong khoảng `from`..`to`.
         */
        Map<java.time.LocalDate, Integer> out = new java.util.LinkedHashMap<>();
        String sql = "SELECT NgayMuon, COUNT(*) AS total FROM PhieuMuon WHERE NgayMuon BETWEEN ? AND ? GROUP BY NgayMuon ORDER BY NgayMuon";
        DbTemplate.query(sql, rs -> {
            java.sql.Date d = rs.getDate("NgayMuon");
            out.put(d.toLocalDate(), rs.getInt("total"));
            return null;
        }, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
        return out;
    }

    public Map<String, Integer> getTopReaders(int limit) throws SQLException {
        Map<String, Integer> out = new java.util.LinkedHashMap<>();
        // Simple scoring: +10 for on-time return, -2 for late return
        String sql = "SELECT TOP (" + limit + ") dg.HoTen AS name, SUM(CASE WHEN pm.NgayTraThuc <= pm.NgayTraDuKien THEN 10 ELSE -2 END) AS score FROM PhieuMuon pm JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia WHERE pm.NgayTraThuc IS NOT NULL GROUP BY dg.HoTen ORDER BY score DESC";
        DbTemplate.query(sql, rs -> {
            out.put(rs.getString("name"), rs.getInt("score"));
            return null;
        });
        return out;
    }

    private int count(String sql) throws SQLException {
        /**
         * Thực hiện truy vấn đếm đơn giản và trả về giá trị số nguyên.
         */
        Integer total = DbTemplate.queryOne(sql, rs -> rs.getInt("total"));
        return total == null ? 0 : total;
    }

    private Map<String, Integer> groupedCounts(String sql) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        DbTemplate.query(sql, rs -> {
            result.put(rs.getString("label"), rs.getInt("total"));
            return null;
        });
        return result;
    }
}
