package com.library.desktop.ui;

/**
 * File: AppTheme.java
 * Mô tả: Định nghĩa màu sắc, style và helper để áp dụng theme cho toàn bộ ứng dụng.
 * - Cung cấp các màu nền, màu chính, accent, và các phương thức tiện ích như `install()`
 *   để cấu hình FlatLaf và `styleButton`/`decorateTable` để chuẩn hoá giao diện các thành phần.
 */

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Font;

public final class AppTheme {
    public static final Color BACKGROUND = new Color(245, 248, 252);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color ACCENT = new Color(14, 165, 233);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);

    private AppTheme() {
    }

    public static void install() {
        /**
         * Cài đặt theme global cho ứng dụng (FlatLaf + các giá trị UI defaults).
         */
        FlatLightLaf.setup();
        // Set default font to Times New Roman for Vietnamese with diacritics
        UIManager.put("defaultFont", new Font("Times New Roman", Font.PLAIN, 14));
        UIManager.put("Component.arc", 16);
        UIManager.put("Button.arc", 16);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TableHeader.height", 36);
        UIManager.put("Table.selectionInactiveBackground", new Color(219, 234, 254));
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
    }

    public static JButton primaryButton(JButton button) {
        return styleButton(button, PRIMARY, Color.WHITE);
    }

    public static JButton accentButton(JButton button) {
        return styleButton(button, ACCENT, Color.WHITE);
    }

    public static JButton successButton(JButton button) {
        return styleButton(button, SUCCESS, Color.WHITE);
    }

    public static JButton warningButton(JButton button) {
        return styleButton(button, WARNING, Color.WHITE);
    }

    public static JButton dangerButton(JButton button) {
        return styleButton(button, DANGER, Color.WHITE);
    }

    public static JButton neutralButton(JButton button) {
        return styleButton(button, new Color(226, 232, 240), TEXT);
    }

    public static void decorateTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(219, 234, 254));
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Times New Roman", Font.BOLD, header.getFont().getSize()));
        header.setBackground(new Color(226, 232, 240));
        header.setForeground(TEXT);
        header.setReorderingAllowed(false);
    }

    public static JComponent cardLike(JComponent component) {
        component.setOpaque(true);
        component.setBackground(SURFACE);
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
        return component;
    }

    private static JButton styleButton(JButton button, Color background, Color foreground) {
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }
}
