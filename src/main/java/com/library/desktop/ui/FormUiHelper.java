package com.library.desktop.ui;

/**
 * File: FormUiHelper.java
 * Mô tả: Các helper để cấu hình và tái sử dụng giao diện (text field, spinner, nhóm trường, hộp thoại thông báo).
 * Các phương thức ở đây là các tiện ích tĩnh được dùng trên toàn bộ ứng dụng.
 */

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class FormUiHelper {
    private static final Dimension STANDARD_FIELD_SIZE = new Dimension(360, 34);

    private FormUiHelper() {
    }

    /**
     * Cấu hình kích thước và số columns cho `JTextField`.
     */
    public static void configureTextField(JTextField field, int columns) {
        field.setColumns(columns);
        field.setPreferredSize(STANDARD_FIELD_SIZE);
        field.setMaximumSize(STANDARD_FIELD_SIZE);
        field.setMinimumSize(STANDARD_FIELD_SIZE);
    }

    public static void configurePasswordField(JPasswordField field, int columns) {
        configureTextField(field, columns);
    }

    /**
     * Cấu hình kích thước cho `JSpinner` chung.
     */
    public static void configureSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(160, 34));
        spinner.setMaximumSize(new Dimension(160, 34));
        spinner.setMinimumSize(new Dimension(160, 34));
    }

    /**
     * Cấu hình `JSpinner` hiển thị ngày với giá trị ban đầu là `value`.
     */
    public static void configureDateSpinner(JSpinner spinner, LocalDate value) {
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        configureSpinner(spinner);
    }

    /**
     * Tạo nhóm trường với nhãn (label) và component để tái sử dụng trong form.
     */
    public static JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        group.add(label, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        if (!(field instanceof javax.swing.JComboBox<?>)) {
            field.setPreferredSize(STANDARD_FIELD_SIZE);
            field.setMaximumSize(STANDARD_FIELD_SIZE);
            field.setMinimumSize(STANDARD_FIELD_SIZE);
        }
        group.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return group;
    }

    /**
     * Tạo một section có tiêu đề và nội dung (dùng để nhóm các phần UI).
     */
    public static JPanel createSection(String title, JComponent content) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Times New Roman", Font.BOLD, 16));
        section.add(heading, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    /**
     * Tạo một hàng (row) sử dụng FlowLayout với căn chỉnh và khoảng cách cho trước.
     */
    public static JPanel createFlowRow(int align, int hgap, int vgap) {
        JPanel row = new JPanel(new FlowLayout(align, hgap, vgap));
        row.setOpaque(false);
        return row;
    }

    /**
     * Hiển thị hộp thoại cảnh báo.
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại lỗi.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại thông báo thông thường.
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isValidEmail(String email) {
        String normalized = normalizeText(email);
        return normalized.isEmpty() || normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean isValidPhone(String phone) {
        String normalized = normalizeText(phone);
        return normalized.isEmpty() || normalized.matches("^[0-9+\\-\\s]{8,20}$");
    }

    public static LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}