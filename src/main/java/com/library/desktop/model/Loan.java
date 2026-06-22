package com.library.desktop.model;

/**
 * File: Loan.java
 * Mô tả: Model đại diện cho phiếu mượn (Loan) – bao gồm thông tin độc giả, ngày mượn,
 * ngày trả dự kiến, ngày trả thực và trạng thái hiện tại.
 */

import java.time.LocalDate;

public record Loan(
                int id,
                int readerId,
                String readerName,
                String readerEmail,
                LocalDate borrowDate,
                LocalDate dueDate,
                LocalDate returnedDate,
                String status) {
}
