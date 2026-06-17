package com.library.desktop.security;

/**
 * File: AccessProfile.java
 * Mô tả: Đại diện quyền truy cập (role capabilities) trong ứng dụng. Chứa các boolean
 * chỉ ra quyền xem/ quản lý từng phần (sách, độc giả, phiếu mượn, ...).
 */

public record AccessProfile(
        String role,
        boolean viewDashboard,
        boolean viewBooks,
        boolean manageBooks,
        boolean viewCategories,
        boolean manageCategories,
        boolean viewReaders,
        boolean manageReaders,
        boolean viewLoans,
        boolean manageLoans,
        boolean viewReservations,
        boolean manageReservations,
        boolean canExport,
        boolean manageAudit
) {
    public static AccessProfile fromRole(String role) {
        /**
         * Tạo `AccessProfile` tương ứng với role (ROLE_ADMIN, ROLE_LIBRARIAN, ROLE_USER).
         */
        String normalized = role == null ? "ROLE_USER" : role.trim().toUpperCase();
        return switch (normalized) {
                case "ROLE_ADMIN" -> new AccessProfile(
                    normalized, true, true, true, true, true, true, true, true, true, true, true, true, true
                );
                case "ROLE_LIBRARIAN" -> new AccessProfile(
                    normalized, true, true, true, true, true, true, true, true, true, true, true, true, true
                );
                    default -> new AccessProfile(
                        normalized, true, true, false, false, false, false, false, false, false, true, false, true, false
                    );
        };
    }

    public boolean isStaff() {
        return manageBooks || manageCategories || manageReaders || manageLoans || manageReservations;
    }
}
