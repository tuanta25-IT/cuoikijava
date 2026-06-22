package com.library.desktop.db;

/**
 * File: DatabaseManager.java
 * Mô tả: Quản lý kết nối cơ sở dữ liệu. Đọc cấu hình DB từ `DesktopConfig` và trả về `Connection`.
 */

import com.library.desktop.config.DesktopConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        /**
         * Tạo và trả về một `Connection` đến SQL Server dựa trên cấu hình trong
         * `desktop.properties`.
         */
        String host = DesktopConfig.get("db.host");
        String port = DesktopConfig.get("db.port");
        String dbName = DesktopConfig.get("db.name");
        String user = DesktopConfig.get("db.user");
        String password = DesktopConfig.get("db.password");

        String url = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + dbName
                + ";encrypt=true;trustServerCertificate=true";

        return DriverManager.getConnection(url, user, password);
    }
}
