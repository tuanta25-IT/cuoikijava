package com.library.desktop.util;

/**
 * File: StatusText.java
 * Mô tả: Chuyển đổi mã trạng thái (status code) sang chuỗi hiển thị bằng tiếng Việt.
 * Bao gồm trạng thái cho sách, phiếu mượn, đặt trước và thông tin độc giả.
 */

public final class StatusText {
    private StatusText() {
    }

    public static String book(String status) {
        /**
         * Trả về mô tả tiếng Việt cho trạng thái sách.
         */
        return switch (normalize(status)) {
            case "AVAILABLE" -> "Còn sách";
            case "BORROWING" -> "Đang mượn";
            case "LOST" -> "Bị mất";
            case "DAMAGED" -> "Bị hỏng";
            case "REPAIR" -> "Đang sửa chữa";
            case "UNKNOWN" -> "Không rõ";
            default -> fallback(status);
        };
    }

    public static String loan(String status) {
        /**
         * Trả về mô tả tiếng Việt cho trạng thái phiếu mượn.
         */
        return switch (normalize(status)) {
            case "BORROWING" -> "Đang mượn";
            case "RETURNED" -> "Đã trả";
            case "OVERDUE" -> "Quá hạn";
            case "PENDING" -> "Chờ xử lý";
            default -> fallback(status);
        };
    }

    public static String reservation(String status) {
        /**
         * Trả về mô tả tiếng Việt cho trạng thái đặt trước.
         */
        return switch (normalize(status)) {
            case "ACTIVE" -> "Đang giữ";
            case "CANCELLED" -> "Đã hủy";
            case "EXPIRED" -> "Hết hạn";
            default -> fallback(status);
        };
    }

    public static String reader(boolean active) {
        /**
         * Trả về chuỗi mô tả trạng thái hoạt động của độc giả.
         */
        return active ? "Hoạt động" : "Ngừng hoạt động";
    }

    public static String readerType(String type) {
        return switch (normalize(type)) {
            case "STUDENT" -> "Sinh viên";
            case "LECTURER" -> "Giảng viên";
            case "STAFF" -> "Nhân viên";
            default -> fallback(type);
        };
    }

    private static String normalize(String status) {
        /**
         * Chuẩn hóa chuỗi: xử lý null và chuyển về chữ hoa, loại bỏ khoảng trắng thừa.
         */
        return status == null ? "" : status.trim().toUpperCase();
    }

    private static String fallback(String status) {
        return status == null || status.isBlank() ? "-" : status;
    }
}