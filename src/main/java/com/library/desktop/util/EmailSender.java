package com.library.desktop.util;

/**
 * File: EmailSender.java
 * Mô tả: Tiện ích gửi email dùng JavaMail. Đọc cấu hình từ `desktop.properties`.
 * - Biến tĩnh được khởi tạo trong block static để nạp cấu hình.
 * - `isEnabled()` kiểm tra cấu hình đã bật hay chưa.
 * - `send()` gửi email HTML.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class EmailSender {
    private static final Properties config = new Properties();
    private static final boolean enabled;
    private static final String smtpHost;
    private static final String smtpPort;
    private static final String username;
    private static final String password;
    private static final String from;

    static {
        try (InputStream in = EmailSender.class.getResourceAsStream("/desktop.properties")) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException ignored) {
        }
        enabled = Boolean.parseBoolean(resolveProperty(config.getProperty("mail.enabled", "false")));
        smtpHost = resolveProperty(config.getProperty("mail.smtp.host", ""));
        smtpPort = resolveProperty(config.getProperty("mail.smtp.port", "587"));
        username = resolveProperty(config.getProperty("mail.username", ""));
        password = resolveProperty(config.getProperty("mail.password", ""));
        from = resolveProperty(config.getProperty("mail.from", "no-reply@library.local"));
    }

    private EmailSender() {
    }

    public static boolean isEnabled() {
        /**
         * Kiểm tra email đã được cấu hình đầy đủ và bật hay chưa.
         */
        return enabled && !smtpHost.isBlank();
    }

    public static void send(String to, String subject, String htmlBody) throws MessagingException {
        /**
         * Gửi email HTML tới địa chỉ `to` với tiêu đề `subject` và nội dung `htmlBody`.
         * Ném `IllegalStateException` nếu cấu hình chưa bật.
         */
        if (!isEnabled()) {
            throw new IllegalStateException("Email sending is not enabled or SMTP host not configured.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        Session sess = Session.getInstance(props, auth);
        Message msg = new MimeMessage(sess);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(msg);
    }

    private static String resolveProperty(String raw) {
        /**
         * Thay thế tham số dạng ${ENV:default} bằng giá trị environment hoặc giá trị
         * mặc định.
         */
        if (raw == null)
            return null;
        String s = raw;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\$\\{([^:}]+):([^}]+)\\}");
        java.util.regex.Matcher m = p.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String env = m.group(1);
            String def = m.group(2);
            String val = System.getenv(env);
            if (val == null || val.isBlank())
                val = def;
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
