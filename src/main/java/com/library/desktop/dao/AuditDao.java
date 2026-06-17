package com.library.desktop.dao;

/**
 * File: AuditDao.java
 * Mô tả: DAO ghi và truy vấn nhật ký audit (AuditLog). Dùng để theo dõi các hành động của người dùng.
 */

import com.library.desktop.db.DbTemplate;
import com.library.desktop.model.AuditEntry;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditDao {
    public void log(String username, String module, String action, String details) throws SQLException {
        String sql = "INSERT INTO AuditLog (Username, Module, Action, Details, CreatedAt) VALUES (?, ?, ?, ?, ?)";
        DbTemplate.update(sql, username, module, action, details, java.sql.Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<AuditEntry> findAll() throws SQLException {
        String sql = "SELECT Id, Username, Module, Action, Details, CreatedAt FROM AuditLog ORDER BY CreatedAt DESC";
        List<AuditEntry> out = new ArrayList<>();
        DbTemplate.query(sql, rs -> {
            out.add(new AuditEntry(
                    rs.getInt("Id"),
                    rs.getString("Username"),
                    rs.getString("Module"),
                    rs.getString("Action"),
                    rs.getString("Details"),
                    rs.getTimestamp("CreatedAt").toLocalDateTime()
            ));
            return null;
        });
        return out;
    }
}
