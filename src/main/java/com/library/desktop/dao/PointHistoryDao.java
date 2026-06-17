package com.library.desktop.dao;

import com.library.desktop.db.DbTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointHistoryDao {
    public List<com.library.desktop.model.AuditEntry> findByReader(int readerId) throws SQLException {
        String sql = "SELECT Id AS Id, CreatedBy AS Username, NULL AS Module, NULL AS Action, Reason AS Details, CreatedAt FROM PointHistory WHERE MaDocGia = ? ORDER BY CreatedAt DESC";
        List<com.library.desktop.model.AuditEntry> out = new ArrayList<>();
        DbTemplate.query(sql, rs -> {
            out.add(new com.library.desktop.model.AuditEntry(
                    rs.getInt("Id"),
                    rs.getString("Username"),
                    "PointHistory",
                    "CHANGE",
                    rs.getString("Details"),
                    rs.getTimestamp("CreatedAt").toLocalDateTime()
            ));
            return null;
        }, readerId);
        return out;
    }

    public void addEntry(int readerId, int change, String reason, String by) throws SQLException {
        DbTemplate.update("INSERT INTO PointHistory (MaDocGia, ChangeValue, Reason, CreatedAt, CreatedBy) VALUES (?, ?, ?, ?, ?)", readerId, change, reason, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()), by);
    }
}
