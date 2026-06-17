package com.library.desktop.security;

/**
 * File: Session.java
 * Mô tả: Lưu trữ thông tin người dùng hiện hành trong biến tĩnh để truy cập từ khắp ứng dụng.
 * Lưu ý: Thiết kế đơn giản cho ứng dụng desktop (không dùng cho multi-user trên server).
 */

import com.library.desktop.model.AppUser;

public final class Session {
    private static volatile AppUser currentUser;

    private Session() {}

    public static void setCurrentUser(AppUser user) {
        /**
         * Thiết lập user hiện hành cho session.
         */
        currentUser = user;
    }

    public static AppUser getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
