package com.library.desktop.dao;

/**
 * File: ReservationDao.java
 * Mô tả: DAO cho chức năng đặt trước sách. Cung cấp các phương thức tìm, tạo và hủy đặt trước.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.Reservation;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDao {
    public List<Reservation> findAll() throws SQLException {
        /**
         * Lấy danh sách đặt trước kèm thông tin độc giả và sách.
         */
        String sql = """
                SELECT dts.MaDatTruoc, dts.MaDocGia, dg.HoTen, dts.MaSach, s.TieuDe,
                       dts.NgayDat, dts.HanGiu, dts.TrangThai
                FROM DatTruocSach dts
                JOIN DocGia dg ON dts.MaDocGia = dg.MaDocGia
                JOIN Sach s ON dts.MaSach = s.MaSach
                ORDER BY dts.MaDatTruoc ASC
                """;

        return DbTemplate.query(sql, rs -> new Reservation(
                rs.getInt("MaDatTruoc"),
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                rs.getInt("MaSach"),
                rs.getString("TieuDe"),
                toLocalDateTime(rs.getTimestamp("NgayDat")),
                toLocalDateTime(rs.getTimestamp("HanGiu")),
                rs.getString("TrangThai")));
    }

    public void create(int readerId, int bookId, int holdHours) throws SQLException {
        /**
         * Tạo một đặt trước mới với thời gian giữ `holdHours` tính từ thời điểm hiện
         * tại.
         */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime holdUntil = now.plusHours(holdHours);
        String sql = """
                INSERT INTO DatTruocSach (MaDocGia, MaSach, NgayDat, HanGiu, TrangThai)
                VALUES (?, ?, ?, ?, N'ACTIVE')
                """;
        DbTemplate.update(sql, readerId, bookId, Timestamp.valueOf(now), Timestamp.valueOf(holdUntil));
    }

    public void cancel(int reservationId) throws SQLException {
        /**
         * Hủy đặt trước theo `reservationId` (cập nhật trạng thái).
         */
        DbTemplate.update("UPDATE DatTruocSach SET TrangThai = N'CANCELLED' WHERE MaDatTruoc = ?", reservationId);
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
