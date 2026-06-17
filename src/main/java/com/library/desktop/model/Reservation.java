package com.library.desktop.model;

/**
 * File: Reservation.java
 * Mô tả: Model đại diện cho một đặt trước sách (Reservation).
 * Trường bao gồm id, độc giả, sách, thời điểm đặt và thời hạn giữ.
 */

import java.time.LocalDateTime;

public record Reservation(
        int id,
        int readerId,
        String readerName,
        int bookId,
        String bookTitle,
        LocalDateTime reservedAt,
        LocalDateTime holdUntil,
        String status
) {
}
