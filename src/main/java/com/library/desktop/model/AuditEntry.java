package com.library.desktop.model;

import java.time.LocalDateTime;

public record AuditEntry(int id, String username, String module, String action, String details,
        LocalDateTime createdAt) {
}
