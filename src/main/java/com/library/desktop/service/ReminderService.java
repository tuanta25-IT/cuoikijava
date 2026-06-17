package com.library.desktop.service;

/**
 * File: ReminderService.java
 * Mô tả: Dịch vụ gửi email nhắc trả sách. Tìm các phiếu mượn sắp đến hạn và gửi email nhắc.
 */

import com.library.desktop.dao.LoanDao;
import com.library.desktop.util.EmailSender;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReminderService {
    private final LoanDao loanDao = new LoanDao();

    public void sendDueReminders(int daysBefore) throws SQLException {
        /**
         * Gửi email nhắc trả cho các phiếu mượn có `NgayTraDuKien` bằng ngày mục tiêu (hôm nay + daysBefore).
         */
        LocalDate target = LocalDate.now().plusDays(daysBefore);
        java.util.List<com.library.desktop.model.LoanInfo> loans = loanDao.findActiveLoansDueOn(target);
        for (com.library.desktop.model.LoanInfo loan : loans) {
            String to = loan.readerEmail;
            if (to == null || to.isBlank()) continue;
            String subject = "Nhắc trả sách - Còn " + daysBefore + " ngày";
            StringBuilder body = new StringBuilder();
            body.append("<p>Xin chào <strong>").append(loan.readerName).append("</strong>,</p>");
            body.append("<p>Bạn có các sách cần trả vào <strong>").append(loan.dueDate).append("</strong>:</p>");
            // collect items
            java.util.List<com.library.desktop.model.LoanItem> items = loanDao.findItemsByLoanId(loan.id);
            body.append("<ul>");
            for (com.library.desktop.model.LoanItem item : items) {
                body.append("<li>").append(item.bookTitle()).append("</li>");
            }
            body.append("</ul>");
            body.append("<p>Vui lòng trả hoặc gia hạn kịp thời để tránh phí.</p>");
            try {
                EmailSender.send(to, subject, body.toString());
            } catch (Exception ex) {
                System.err.println("Không gửi được email tới " + to + ": " + ex.getMessage());
            }
        }
    }
}
