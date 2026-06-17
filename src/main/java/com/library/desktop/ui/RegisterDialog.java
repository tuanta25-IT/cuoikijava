package com.library.desktop.ui;

/**
 * File: RegisterDialog.java
 * Mô tả: Dialog đăng ký tài khoản người dùng mới. Kiểm tra dữ liệu đầu vào,
 * gọi `AuthDao` để tạo tài khoản và đảm bảo hồ sơ độc giả tồn tại.
 */

import com.library.desktop.dao.AuthDao;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

public class RegisterDialog extends JDialog {
    private final JTextField usernameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JComboBox<RoleItem> roleBox = new JComboBox<>(new RoleItem[]{
            new RoleItem("ROLE_USER", "Người dùng"),
            new RoleItem("ROLE_LIBRARIAN", "Thủ thư"),
            new RoleItem("ROLE_ADMIN", "Quản trị viên")
        });
    private final JCheckBox showPasswordBox = new JCheckBox("Hiện mật khẩu");
    private final AuthDao authDao;

    public RegisterDialog(Frame owner, AuthDao authDao) {
        /**
         * Constructor: tạo dialog đăng ký với DAO xác thực truyền vào.
         */
        super(owner, "Đăng ký tài khoản", true);
        this.authDao = authDao;

        setSize(700, 700);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        root.setBackground(AppTheme.BACKGROUND);

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(22, 22, 22, 22)
        ));

        JLabel title = new JLabel("Tạo tài khoản mới");
        title.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 24));
        JLabel subtitle = new JLabel("Đăng ký để sử dụng ứng dụng");
        subtitle.setForeground(AppTheme.MUTED);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        FormUiHelper.configureTextField(usernameField, 18);
        FormUiHelper.configureTextField(emailField, 18);
        FormUiHelper.configureTextField(fullNameField, 18);
        FormUiHelper.configurePasswordField(passwordField, 18);
        FormUiHelper.configurePasswordField(confirmPasswordField, 18);
        roleBox.setSelectedIndex(0);
        showPasswordBox.setOpaque(false);
        showPasswordBox.addActionListener(e -> togglePasswordVisibility());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(FormUiHelper.createFieldGroup("Tên đăng nhập", usernameField));
        form.add(Box.createVerticalStrut(10));
        form.add(FormUiHelper.createFieldGroup("Email", emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(FormUiHelper.createFieldGroup("Họ và tên", fullNameField));
        form.add(Box.createVerticalStrut(10));
        form.add(FormUiHelper.createFieldGroup("Vai trò", roleBox));
        form.add(Box.createVerticalStrut(10));
        form.add(FormUiHelper.createFieldGroup("Mật khẩu", passwordField));
        form.add(Box.createVerticalStrut(10));
        form.add(FormUiHelper.createFieldGroup("Nhập lại mật khẩu", confirmPasswordField));
        form.add(Box.createVerticalStrut(4));
        form.add(showPasswordBox);

        JButton cancelBtn = new JButton("Hủy");
        AppTheme.neutralButton(cancelBtn);
        cancelBtn.addActionListener(e -> dispose());

        JButton registerBtn = new JButton("Đăng ký");
        AppTheme.primaryButton(registerBtn);
        registerBtn.addActionListener(e -> register());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(registerBtn);

        JPanel cardContent = new JPanel(new BorderLayout(0, 16));
        cardContent.setOpaque(false);
        cardContent.add(heading, BorderLayout.NORTH);
        cardContent.add(form, BorderLayout.CENTER);
        cardContent.add(actions, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(cardContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scrollPane, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);
        add(root);
    }

    private void register() {
        /**
         * Xử lý đăng ký: validate form, gọi `authDao.registerUser`, tạo hồ sơ độc giả nếu cần.
         */
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.length() < 3) {
            FormUiHelper.showWarning(this, "Tên đăng nhập phải có ít nhất 3 ký tự.");
            return;
        }
        if (fullName.length() < 2) {
            FormUiHelper.showWarning(this, "Họ và tên phải có ít nhất 2 ký tự.");
            return;
        }
        if (!FormUiHelper.isValidEmail(email)) {
            FormUiHelper.showWarning(this, "Email không hợp lệ.");
            return;
        }
        if (password.isBlank()) {
            FormUiHelper.showWarning(this, "Mật khẩu không được để trống.");
            return;
        }
        if (password.length() < 6) {
            FormUiHelper.showWarning(this, "Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            FormUiHelper.showWarning(this, "Mật khẩu xác nhận không khớp.");
            return;
        }

        try {
            RoleItem role = (RoleItem) roleBox.getSelectedItem();
            authDao.registerUser(username, email, fullName, role == null ? null : role.code, password);
            // verify reader profile exists; try to create if missing
            com.library.desktop.dao.ReaderDao readerDao = new com.library.desktop.dao.ReaderDao();
            if (readerDao.findByEmail(email) == null) {
                try {
                    authDao.ensureReaderProfileFor(fullName, email, role == null ? null : role.code);
                } catch (Exception ex) {
                    FormUiHelper.showWarning(this, "Đăng ký thành công nhưng không tạo được hồ sơ độc giả: " + ex.getMessage());
                    dispose();
                    return;
                }
            }
            FormUiHelper.showInfo(this, "Đăng ký thành công. Hãy đăng nhập để tiếp tục.");
            dispose();
        } catch (IllegalArgumentException ex) {
            FormUiHelper.showWarning(this, ex.getMessage());
        } catch (Exception ex) {
            FormUiHelper.showError(this, "Không thể đăng ký: " + ex.getMessage());
        }
    }

    private void togglePasswordVisibility() {
        /**
         * Bật/tắt hiển thị ký tự mật khẩu khi người dùng tick checkbox.
         */
        char echo = showPasswordBox.isSelected() ? 0 : '•';
        passwordField.setEchoChar(echo);
        confirmPasswordField.setEchoChar(echo);
    }

    private static final class RoleItem {
        private final String code;
        private final String label;

        private RoleItem(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}