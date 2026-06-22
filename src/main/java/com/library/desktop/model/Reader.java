package com.library.desktop.model;

/**
 * File: Reader.java
 * Mô tả: Model đại diện cho độc giả của thư viện, chứa thông tin cơ bản như họ
 * tên, liên hệ,
 * mã thẻ, loại độc giả, trạng thái hoạt động và điểm uy tín.
 */

public record Reader(
                int id,
                String fullName,
                String phone,
                String email,
                String cardCode,
                String readerType,
                boolean active,
                Integer trustScore) {
}
