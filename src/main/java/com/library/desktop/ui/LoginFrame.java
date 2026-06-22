package com.library.desktop.ui;

/**
 * File: LoginFrame.java
 * Mô tả: Cửa sổ đăng nhập của ứng dụng. Cho phép người dùng nhập tên đăng nhập/email và mật khẩu,
 * đăng nhập hoặc mở dialog đăng ký.
 */

import com.library.desktop.dao.AuthDao;
import com.library.desktop.model.AppUser;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final AuthDao authDao = new AuthDao();

    public LoginFrame() {
        /**
         * Constructor: khởi tạo giao diện đăng nhập, banner thông tin và card form.
         */
        setTitle("Đăng nhập - Quản lý thư viện");
        setSize(940, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BACKGROUND);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(AppTheme.BACKGROUND);

        JPanel banner = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(new GradientPaint(0, 0, AppTheme.PRIMARY_DARK, 0, getHeight(), AppTheme.ACCENT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        JLabel title = new JLabel("Quản lý thư viện");
        title.setForeground(Color.WHITE);
        title.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 30));
        JLabel subtitle = new JLabel("Ứng dụng desktop cho thư viện");
        subtitle.setForeground(new Color(230, 244, 255));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 8));
        bannerText.setOpaque(false);
        bannerText.add(title);
        bannerText.add(subtitle);

        JPanel featureList = new JPanel(new GridLayout(3, 1, 0, 12));
        featureList.setOpaque(false);
        featureList.add(featureLabel("Đăng nhập bằng username hoặc email"));
        featureList.add(featureLabel("Quản lý sách, độc giả, phiếu mượn, đặt trước"));
        featureList.add(featureLabel("Xuất Excel, PDF và phân quyền theo vai trò"));

        banner.add(bannerText, BorderLayout.NORTH);
        banner.add(featureList, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.setBackground(AppTheme.SURFACE);
        card.setPreferredSize(new Dimension(460, 0));

        JLabel loginTitle = new JLabel("Đăng nhập hệ thống");
        loginTitle.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 24));
        JLabel loginSubtitle = new JLabel("Nhập thông tin tài khoản để tiếp tục");
        loginSubtitle.setForeground(AppTheme.MUTED);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.setOpaque(false);
        heading.add(loginTitle);
        heading.add(loginSubtitle);

        usernameField.setColumns(16);
        passwordField.setColumns(16);
        usernameField.setPreferredSize(new Dimension(240, 34));
        usernameField.setMaximumSize(new Dimension(240, 34));
        usernameField.setMinimumSize(new Dimension(240, 34));
        passwordField.setPreferredSize(new Dimension(240, 34));
        passwordField.setMaximumSize(new Dimension(240, 34));
        passwordField.setMinimumSize(new Dimension(240, 34));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(fieldGroup("Tên đăng nhập / Email", usernameField));
        form.add(Box.createVerticalStrut(12));
        form.add(fieldGroup("Mật khẩu", passwordField));

        JButton loginBtn = new JButton("Đăng nhập");
        AppTheme.primaryButton(loginBtn);
        loginBtn.addActionListener(e -> login());

        JButton registerBtn = new JButton("Đăng ký tài khoản");
        registerBtn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        registerBtn.setContentAreaFilled(false);
        registerBtn.setForeground(AppTheme.PRIMARY);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> openRegisterDialog());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(registerBtn);
        actions.add(loginBtn);

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        actionRow.add(actions, BorderLayout.EAST);

        JPanel cardContent = new JPanel(new BorderLayout(0, 18));
        cardContent.setOpaque(false);
        cardContent.add(heading, BorderLayout.NORTH);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.add(form, BorderLayout.NORTH);
        cardContent.add(formWrap, BorderLayout.CENTER);
        cardContent.add(actionRow, BorderLayout.SOUTH);

        JScrollPane cardScroll = new JScrollPane(cardContent);
        cardScroll.setBorder(BorderFactory.createEmptyBorder());
        cardScroll.getVerticalScrollBar().setUnitIncrement(16);
        cardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(cardScroll, BorderLayout.CENTER);

        root.add(banner, BorderLayout.WEST);
        root.add(card, BorderLayout.CENTER);
        banner.setPreferredSize(new java.awt.Dimension(430, 0));
        add(root);
    }

    private JPanel fieldGroup(String labelText, javax.swing.JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 15));
        label.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        group.add(label, BorderLayout.NORTH);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        field.setPreferredSize(new Dimension(240, 34));
        field.setMaximumSize(new Dimension(240, 34));
        field.setMinimumSize(new Dimension(240, 34));
        group.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return group;
    }

    private void openRegisterDialog() {
        /**
         * Mở dialog đăng ký tài khoản (sử dụng `AuthDao`).
         */
        RegisterDialog dialog = new RegisterDialog(this, authDao);
        dialog.setVisible(true);
    }

    private void login() {
        /**
         * Xử lý đăng nhập: đọc form, validate, gọi `AuthDao.authenticate` và khởi tạo
         * `MainFrame` nếu thành công.
         */
        String usernameOrEmail = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AppUser user = authDao.authenticate(usernameOrEmail, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dispose();
            com.library.desktop.security.Session.setCurrentUser(user);
            new MainFrame(user).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể đăng nhập: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel featureLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 255, 26));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return label;
    }
}
