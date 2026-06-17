package com.library.desktop.dao;

/**
 * File: AuthDao.java
 * Mô tả: DAO xử lý xác thực và đăng ký người dùng. Sử dụng BCrypt để mã hóa mật khẩu,
 * đảm bảo tính duy nhất của username/email và tạo hồ sơ độc giả khi cần.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.AppUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.util.Locale;

public class AuthDao {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AppUser authenticate(String usernameOrEmail, String rawPassword) throws SQLException {
        String sql = """
                SELECT UserId, Username, Email, Password, FullName, Role, Active
                FROM Users
                WHERE (Username = ? OR Email = ?) AND Active = 1
                """;

        AppUser user = DbTemplate.queryOne(sql, rs -> new AppUser(
                rs.getInt("UserId"),
                rs.getString("Username"),
                rs.getString("Email"),
                rs.getString("Password"),
                rs.getString("FullName"),
                rs.getString("Role"),
                rs.getBoolean("Active")
        ), usernameOrEmail, usernameOrEmail);

        if (user == null) {
            return null;
        }
        return encoder.matches(rawPassword, user.passwordHash()) ? user : null;
    }

    public AppUser registerUser(String username, String email, String fullName, String role, String rawPassword) throws SQLException {
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        String normalizedRole = role == null || role.isBlank() ? "ROLE_USER" : role;

        String sql = """
                INSERT INTO Users (Username, Email, Password, FullName, Role, Active)
                VALUES (?, ?, ?, ?, ?, 1)
                """;

        DbTemplate.update(sql, username, email, encoder.encode(rawPassword), fullName, normalizedRole);
        ensureReaderProfile(fullName, email, normalizedRole);
        // try send welcome email if configured
        try {
            if (com.library.desktop.util.EmailSender.isEnabled()) {
                String subject = "Chào mừng đến với thư viện";
                String body = "<p>Xin chào <strong>" + fullName + "</strong>,</p><p>Cảm ơn bạn đã đăng ký tài khoản.</p>";
                com.library.desktop.util.EmailSender.send(email, subject, body);
            }
        } catch (Exception ignored) {}
        return authenticate(username, rawPassword);
    }

    public void ensureReaderProfileFor(String fullName, String email, String role) throws SQLException {
        ensureReaderProfile(fullName, email, role);
    }

    private void ensureReaderProfile(String fullName, String email, String role) throws SQLException {
        if (readerExistsByEmail(email)) {
            return;
        }

        String sql = """
                INSERT INTO DocGia (HoTen, SoDienThoai, Email, MaThe, LoaiDocGia, DiemUyTin, TrangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        DbTemplate.update(
                sql,
                fullName,
                "",
                email,
                nextCardCode(),
                mapReaderType(role),
                0,
                1
        );
    }

    private boolean readerExistsByEmail(String email) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM DocGia WHERE Email = ?";
        return DbTemplate.queryOne(sql, rs -> rs.getInt(1), email) != null;
    }

    private String nextCardCode() throws SQLException {
        String prefix = "DG";
        String sql = "SELECT TOP 1 1 FROM DocGia WHERE MaThe = ?";
        for (int i = 0; i < 20; i++) {
            String code = prefix + System.currentTimeMillis() + (int) (Math.random() * 90 + 10);
            if (DbTemplate.queryOne(sql, rs -> rs.getInt(1), code) == null) {
                return code;
            }
        }
        throw new SQLException("Không thể tạo mã thẻ độc giả duy nhất. Vui lòng thử lại.");
    }

    private String mapReaderType(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ROLE_ADMIN", "ROLE_LIBRARIAN" -> "STAFF";
            default -> "STUDENT";
        };
    }

    private boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM Users WHERE Username = ?";
        return DbTemplate.queryOne(sql, rs -> rs.getInt(1), username) != null;
    }

    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM Users WHERE Email = ?";
        return DbTemplate.queryOne(sql, rs -> rs.getInt(1), email) != null;
    }
}
