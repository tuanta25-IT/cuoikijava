package com.library.desktop.db;

/**
 * File: DBMigration.java
 * Mô tả: Chứa logic kiểm tra và tạo bảng cơ sở dữ liệu cần thiết khi ứng dụng khởi động.
 * - `runMigrations()` đảm bảo các bảng cơ bản tồn tại và seed dữ liệu mẫu khi cần.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DBMigration {
    private DBMigration() {
    }

    public static void runMigrations() {
        /**
         * Kiểm tra và chạy các lệnh tạo bảng nếu chưa tồn tại. Ghi nhật ký lỗi ra
         * stderr nếu có.
         */
        try (Connection con = DatabaseManager.getConnection(); Statement st = con.createStatement()) {
            String audit = "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AuditLog') BEGIN " +
                    "CREATE TABLE AuditLog (Id INT IDENTITY(1,1) PRIMARY KEY, Username NVARCHAR(100), Module NVARCHAR(100), Action NVARCHAR(50), Details NVARCHAR(MAX), CreatedAt DATETIME DEFAULT GETDATE()) END";
            st.execute(audit);

            String points = "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PointHistory') BEGIN " +
                    "CREATE TABLE PointHistory (Id INT IDENTITY(1,1) PRIMARY KEY, MaDocGia INT NOT NULL, ChangeValue INT NOT NULL, Reason NVARCHAR(255), CreatedAt DATETIME DEFAULT GETDATE(), CreatedBy NVARCHAR(100)) END";
            st.execute(points);

            seedPointHistoryIfEmpty(con);
        } catch (SQLException ex) {
            System.err.println("Migration error: " + ex.getMessage());
        }
    }

    private static void seedPointHistoryIfEmpty(Connection con) {
        /**
         * Nếu bảng PointHistory rỗng, tạo dữ liệu seed mẫu để tiện cho demo hoặc phát
         * triển.
         */
        try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*) FROM PointHistory");
                ResultSet rs = count.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("Seed check error: " + ex.getMessage());
            return;
        }

        List<Integer> readerIds = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT TOP (8) MaDocGia FROM DocGia ORDER BY NEWID()");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                readerIds.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            System.err.println("Seed reader query error: " + ex.getMessage());
            return;
        }

        if (readerIds.isEmpty()) {
            return;
        }

        String[] reasons = new String[] {
                "Trả sách đúng hạn",
                "Trả sách sớm",
                "Trả sách trễ",
                "Hoàn thành nhiệm vụ đọc",
                "Điều chỉnh hệ thống",
                "Thưởng thành viên tích cực"
        };
        Random random = new Random();

        try (PreparedStatement insert = con.prepareStatement(
                "INSERT INTO PointHistory (MaDocGia, ChangeValue, Reason, CreatedAt, CreatedBy) VALUES (?, ?, ?, ?, ?)")) {
            for (int readerId : readerIds) {
                int delta = random.nextBoolean() ? (5 + random.nextInt(6)) : -(1 + random.nextInt(4));
                String reason = reasons[random.nextInt(reasons.length)];
                insert.setInt(1, readerId);
                insert.setInt(2, delta);
                insert.setString(3, reason);
                insert.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(90))));
                insert.setString(5, "system");
                insert.addBatch();
            }
            insert.executeBatch();
        } catch (SQLException ex) {
            System.err.println("Seed insert error: " + ex.getMessage());
        }
    }
}
