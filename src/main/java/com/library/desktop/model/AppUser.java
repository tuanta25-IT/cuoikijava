package com.library.desktop.model;

public record AppUser(
        int userId,
        String username,
        String email,
        String passwordHash,
        String fullName,
        String role,
        boolean active
) {
}
