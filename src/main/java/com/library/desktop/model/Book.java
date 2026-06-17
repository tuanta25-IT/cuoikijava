package com.library.desktop.model;

public record Book(
        int id,
        String title,
        String author,
        Integer categoryId,
        String categoryName,
        String status,
        Integer quantity
) {
}
