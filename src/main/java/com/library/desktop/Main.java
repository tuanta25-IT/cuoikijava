package com.library.desktop;

/**
 * File: Main.java
 * Mô tả: Điểm vào (entry point) của ứng dụng desktop. Thiết lập giao diện (theme),
 * chạy các migration cần thiết và khởi động `LoginFrame` trên Event Dispatch Thread.
 */

import com.library.desktop.ui.LoginFrame;
import com.library.desktop.ui.AppTheme;

import javax.swing.SwingUtilities;

public class Main {
    /**
     * Hàm main: khởi tạo cấu hình UI và chạy ứng dụng.
     * @param args tham số dòng lệnh (không sử dụng)
     */
    public static void main(String[] args) {
        AppTheme.install();
        // ensure required tables exist
        com.library.desktop.db.DBMigration.runMigrations();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
