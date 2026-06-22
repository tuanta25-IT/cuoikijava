package com.library.desktop.model;

/**
 * File: LoanItem.java
 * Mô tả: Model cho một dòng chi tiết trong phiếu mượn (Loan item) gồm id chi
 * tiết, id phiếu,
 * id sách, tiêu đề sách và số lượng.
 */

public record LoanItem(
                int detailId,
                int loanId,
                int bookId,
                String bookTitle,
                Integer quantity) {
}
