package com.library.desktop.ui;

/**
 * File: MainFrame.java
 * Mô tả: Cửa sổ chính của ứng dụng desktop. Hiển thị header chào mừng và các tab tương ứng
 * với các chức năng mà `AccessProfile` cho phép (Dashboard, Thể loại, Sách, Độc giả, Phiếu mượn, Đặt trước, Audit).
 */

import com.library.desktop.model.AppUser;
import com.library.desktop.security.AccessProfile;
import com.library.desktop.ui.panel.BookPanel;
import com.library.desktop.ui.panel.CategoryPanel;
import com.library.desktop.ui.panel.DashboardPanel;
import com.library.desktop.ui.panel.LoanPanel;
import com.library.desktop.ui.panel.ReservationPanel;
import com.library.desktop.ui.panel.ReaderPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

public class MainFrame extends JFrame {
    private final AccessProfile accessProfile;

    public MainFrame(AppUser user) {
        /**
         * Constructor: khởi tạo giao diện chính cho người dùng `user`.
         * Thiết lập quyền hiển thị tab theo `AccessProfile` của user.
         */
        this.accessProfile = AccessProfile.fromRole(user.role());

        setTitle("Quản lý thư viện - " + user.fullName() + " (" + accessProfile.role() + ")");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(AppTheme.BACKGROUND);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(AppTheme.BACKGROUND);

        root.add(buildHeader(user), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (accessProfile.viewDashboard()) {
            tabs.addTab("Báo cáo", new DashboardPanel());
        }
        if (accessProfile.viewCategories()) {
            tabs.addTab("Thể loại", new CategoryPanel(accessProfile));
        }
        if (accessProfile.viewBooks()) {
            tabs.addTab("Sách", new BookPanel(accessProfile));
        }
        if (accessProfile.viewReaders()) {
            tabs.addTab("Độc giả", new ReaderPanel(accessProfile));
        }
        if (accessProfile.viewLoans()) {
            tabs.addTab("Phiếu mượn", new LoanPanel(accessProfile));
        }
        if (accessProfile.viewReservations()) {
            tabs.addTab("Đặt trước", new ReservationPanel(accessProfile));
        }
        if (accessProfile.manageAudit()) {
            tabs.addTab("Audit", new com.library.desktop.ui.panel.AuditPanel());
        }

        tabs.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tabs.setBackground(AppTheme.BACKGROUND);

        root.add(tabs, BorderLayout.CENTER);
        add(root);
    }

    private JPanel buildHeader(AppUser user) {
        JPanel header = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(new GradientPaint(0, 0, AppTheme.PRIMARY, getWidth(), getHeight(), AppTheme.ACCENT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 6));
        left.setOpaque(false);
        JLabel title = new JLabel("Quản lý thư viện");
        title.setForeground(Color.WHITE);
        title.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 26));
        JLabel subtitle = new JLabel("Xin chào, " + user.fullName() + " | " + (accessProfile.isStaff() ? "Nhân viên" : "Người dùng"));
        subtitle.setForeground(new Color(226, 232, 240));
        subtitle.setFont(new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 14));
        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel roleBadge = new JLabel(accessProfile.isStaff() ? "Nhân viên" : "Người dùng");
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(255, 255, 255, 36));
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JButton logoutButton = new JButton("Đăng xuất");
        AppTheme.neutralButton(logoutButton);
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        right.add(roleBadge);
        right.add(logoutButton);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}
