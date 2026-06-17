package com.library.desktop.model;

/**
 * File: LoanInfo.java
 * Mô tả: Thông tin tóm tắt về phiếu mượn dùng cho mục đích gửi nhắc (bao gồm email độc giả,
 * ngày trả dự kiến và danh sách các LoanItem có thể được gán sau khi khởi tạo).
 */

import java.time.LocalDate;
import java.util.List;

public class LoanInfo {
    public final int id;
    public final int readerId;
    public final String readerName;
    public final String readerEmail;
    public final LocalDate dueDate;
    private List<LoanItem> items;

    public LoanInfo(int id, int readerId, String readerName, String readerEmail, LocalDate dueDate) {
        this.id = id;
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerEmail = readerEmail;
        this.dueDate = dueDate;
    }

    public void setItems(List<LoanItem> items) {
        this.items = items;
    }

    public List<LoanItem> getItems() {
        return items;
    }
}
