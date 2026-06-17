package com.library.desktop.ui.panel;

/**
 * File: DashboardPanel.java
 * Mô tả: Panel tổng quan hiển thị các chỉ số (thống kê) của hệ thống, các biểu đồ
 * và bảng số liệu. Cho phép xuất báo cáo và lọc theo khoảng thời gian.
 */

import com.library.desktop.dao.DashboardDao;
import com.library.desktop.ui.AppTheme;
import com.library.desktop.util.StatusText;
import com.library.desktop.util.TableExportUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class DashboardPanel extends JPanel {
    private static final int BASE_CHART_CARD_HEIGHT = 340;
    private static final int BASE_CHART_CONTENT_HEIGHT = 220;

    private final JPanel summaryCards = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JPanel leftColumn = new JPanel();
    private final JPanel rightColumn = new JPanel();
    private final DashboardDao dashboardDao = new DashboardDao();
    private final JSpinner fromSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner toSpinner = new JSpinner(new SpinnerDateModel());
    private final DefaultTableModel statsModel = new DefaultTableModel(new Object[]{"Chỉ tiêu", "Giá trị"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable statsTable = new JTable(statsModel);

    public DashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppTheme.BACKGROUND);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.setOpaque(false);
        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(new Font("Times New Roman", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Biểu đồ về trạng thái, loại độc giả và chỉ số chính của thư viện");
        subtitle.setForeground(AppTheme.MUTED);
        heading.add(title);
        heading.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JPanel dateControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        dateControls.setOpaque(false);
        dateControls.add(new JLabel("Từ (dd/MM/yyyy):"));
        configureDateSpinner(fromSpinner, LocalDate.now().minusMonths(1));
        dateControls.add(fromSpinner);
        dateControls.add(new JLabel("Đến (dd/MM/yyyy):"));
        configureDateSpinner(toSpinner, LocalDate.now());
        dateControls.add(toSpinner);
        JButton applyRange = new JButton("Áp dụng");
        dateControls.add(applyRange);
        applyRange.addActionListener(e -> applyDateRange());
        JButton refreshBtn = new JButton("Làm mới");
        JButton excelBtn = new JButton("Excel");
        JButton pdfBtn = new JButton("PDF");
        AppTheme.primaryButton(refreshBtn);
        AppTheme.successButton(excelBtn);
        AppTheme.warningButton(pdfBtn);
        refreshBtn.addActionListener(e -> loadStats());
        excelBtn.addActionListener(e -> TableExportUtils.exportExcel(this, "dashboard_thong_ke", statsTable));
        pdfBtn.addActionListener(e -> TableExportUtils.exportPdf(this, "dashboard_thong_ke", statsTable));
        actions.add(refreshBtn);
        actions.add(excelBtn);
        actions.add(pdfBtn);

        top.add(heading, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(dateControls, BorderLayout.SOUTH);

        summaryCards.setOpaque(false);
        AppTheme.decorateTable(statsTable);

        leftColumn.setLayout(new javax.swing.BoxLayout(leftColumn, javax.swing.BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        rightColumn.setLayout(new javax.swing.BoxLayout(rightColumn, javax.swing.BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 12, 12));
        chartsRow.setOpaque(false);
        chartsRow.add(leftColumn);
        chartsRow.add(rightColumn);

        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int chartAreaHeight = Math.max(700, Math.min(920, (int) (screenHeight * 0.72)));

        JScrollPane chartScroll = new JScrollPane(chartsRow);
        chartScroll.setBorder(BorderFactory.createEmptyBorder());
        chartScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chartScroll.setPreferredSize(new Dimension(0, chartAreaHeight));
        chartScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, chartAreaHeight));

        JScrollPane tableScroll = new JScrollPane(statsTable);
        tableScroll.setPreferredSize(new Dimension(0, 240));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(summaryCards);
        content.add(javax.swing.Box.createVerticalStrut(12));
        content.add(chartScroll);
        content.add(javax.swing.Box.createVerticalStrut(12));
        content.add(tableScroll);

        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loadStats();
    }

    private void loadStats() {
        /**
         * Tải các thống kê, chuẩn bị dữ liệu cho biểu đồ và thẻ tóm tắt.
         */
        summaryCards.removeAll();
        statsModel.setRowCount(0);

        try {
            Map<String, Integer> stats = dashboardDao.getSummaryStats();
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                statsModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                summaryCards.add(statCard(entry.getKey(), entry.getValue()));
            }

            leftColumn.removeAll();
            rightColumn.removeAll();

            Map<String, Integer> bookStatus = mapWithLabels(dashboardDao.getBookStatusBreakdown(), StatusText::book);
            Map<String, Integer> loanStatus = mapWithLabels(dashboardDao.getLoanStatusBreakdown(), StatusText::loan);
            Map<String, Integer> readerType = mapReaderTypes(dashboardDao.getReaderTypeBreakdown());
            Map<String, Integer> readerActivity = dashboardDao.getReaderActivityBreakdown();

            leftColumn.add(chartCard("Sách theo trạng thái", bookStatus));
            leftColumn.add(javax.swing.Box.createVerticalStrut(12));
            leftColumn.add(chartCard("Phiếu mượn theo trạng thái", loanStatus));

            rightColumn.add(chartCard("Độc giả theo loại", readerType));
            rightColumn.add(javax.swing.Box.createVerticalStrut(12));
            rightColumn.add(chartCard("Độc giả hoạt động/ngừng hoạt động", readerActivity));
        } catch (Exception ex) {
            summaryCards.add(new JLabel("Không tải được thống kê: " + ex.getMessage()));
        }

        revalidate();
        repaint();
    }

    private void applyDateRange() {
        /**
         * Tạo báo cáo theo khoảng thời gian: đọc từ/to, lấy xu hướng và top readers,
         * sau đó hiển thị kết quả trong hộp thoại.
         */
        try {
            LocalDate from = toLocalDate((Date) fromSpinner.getValue());
            LocalDate to = toLocalDate((Date) toSpinner.getValue());
            var trend = dashboardDao.getLoanTrend(from, to);
            var top = dashboardDao.getTopReaders(10);
            StringBuilder msg = new StringBuilder();
            msg.append("Xu hướng phiếu mượn:\n");
            for (var e : trend.entrySet()) {
                msg.append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
            }
            msg.append("\nTop độc giả:\n");
            for (var e : top.entrySet()) {
                msg.append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
            }
            javax.swing.JOptionPane.showMessageDialog(this, msg.toString(), "Báo cáo theo thời gian", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi tải báo cáo: " + ex.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configureDateSpinner(JSpinner spinner, LocalDate value) {
        /**
         * Cấu hình spinner hiển thị ngày theo định dạng `dd/MM/yyyy` và đặt giá trị.
         */
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private LocalDate toLocalDate(Date value) {
        /**
         * Chuyển `Date` sang `LocalDate` (dùng cho truy vấn thống kê).
         */
        return Instant.ofEpochMilli(value.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JPanel statCard(String label, int value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setBackground(AppTheme.SURFACE);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        JLabel amount = new JLabel(String.valueOf(value));
        amount.setFont(amount.getFont().deriveFont(28f));
        amount.setForeground(AppTheme.PRIMARY_DARK);
        card.add(name, BorderLayout.NORTH);
        card.add(amount, BorderLayout.CENTER);
        return card;
    }

    private JPanel chartCard(String title, Map<String, Integer> data) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        card.setBackground(AppTheme.SURFACE);
        card.setPreferredSize(new Dimension(0, BASE_CHART_CARD_HEIGHT));
        card.setMinimumSize(new Dimension(0, BASE_CHART_CARD_HEIGHT));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Times New Roman", Font.BOLD, 18));
        JLabel hint = new JLabel("Biểu đồ cột ngang");
        hint.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        hint.setForeground(AppTheme.MUTED);
        header.add(heading, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(new BarChartComponent(data), BorderLayout.CENTER);
        body.add(buildLegend(data), BorderLayout.SOUTH);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private Map<String, Integer> mapWithLabels(Map<String, Integer> source, Function<String, String> mapper) {
        Map<String, Integer> mapped = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            mapped.put(mapper.apply(entry.getKey()), entry.getValue());
        }
        return mapped;
    }

    private Map<String, Integer> mapReaderTypes(Map<String, Integer> source) {
        Map<String, Integer> mapped = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            mapped.put(StatusText.readerType(entry.getKey()), entry.getValue());
        }
        return mapped;
    }

    private JPanel buildLegend(Map<String, Integer> data) {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        int index = 0;
        for (String label : data.keySet()) {
            Color color = palette(index++);
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            item.setOpaque(false);
            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new Dimension(12, 12));
            swatch.setMaximumSize(new Dimension(12, 12));
            swatch.setBackground(color);
            swatch.setBorder(BorderFactory.createLineBorder(color.darker()));
            JLabel text = new JLabel(label);
            text.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            item.add(swatch);
            item.add(text);
            legend.add(item);
        }
        return legend;
    }

    private Color palette(int index) {
        return switch (index % 6) {
            case 0 -> new Color(33, 110, 196);
            case 1 -> new Color(0, 151, 167);
            case 2 -> new Color(46, 125, 50);
            case 3 -> new Color(245, 124, 0);
            case 4 -> new Color(173, 20, 87);
            default -> new Color(94, 53, 177);
        };
    }

    private static final class BarChartComponent extends JComponent {
        private final Map<String, Integer> data;

        private BarChartComponent(Map<String, Integer> data) {
            this.data = new LinkedHashMap<>(data);
            setPreferredSize(new Dimension(0, BASE_CHART_CONTENT_HEIGHT));
            setMinimumSize(new Dimension(0, BASE_CHART_CONTENT_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                g.drawString("Không có dữ liệu", 20, 20);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int padding = 18;
            int labelWidth = 190;
            int barAreaWidth = Math.max(60, getWidth() - labelWidth - 72);
            int availableHeight = getHeight() - padding * 2;
            int rowHeight = Math.max(32, availableHeight / Math.max(data.size(), 1));
            int barHeight = Math.max(16, rowHeight - 12);
            int max = Math.max(1, data.values().stream().mapToInt(Integer::intValue).max().orElse(1));
            int y = padding + 6;
            int index = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int value = entry.getValue();
                int barWidth = (int) Math.round(barAreaWidth * (value / (double) max));
                Color base = palette(index++);
                Color soft = new Color(base.getRed(), base.getGreen(), base.getBlue(), 70);

                g2.setColor(AppTheme.TEXT);
                g2.drawString(entry.getKey(), 8, y + barHeight - 2);

                g2.setColor(new Color(233, 238, 246));
                g2.fillRoundRect(labelWidth, y, barAreaWidth, barHeight, 12, 12);

                g2.setPaint(new GradientPaint(labelWidth, y, soft, labelWidth, y + barHeight, new Color(255, 255, 255, 0)));
                g2.fillRoundRect(labelWidth, y, barAreaWidth, barHeight, 12, 12);

                Paint oldPaint = g2.getPaint();
                g2.setPaint(new GradientPaint(labelWidth, y, base, labelWidth + barWidth, y + barHeight, base.darker()));
                g2.fillRoundRect(labelWidth, y, barWidth, barHeight, 12, 12);
                g2.setPaint(oldPaint);

                g2.setColor(AppTheme.TEXT);
                g2.drawString(String.valueOf(value), labelWidth + barWidth + 10, y + barHeight - 2);
                y += rowHeight;
            }
            g2.dispose();
        }

        private Color palette(int index) {
            return switch (index % 6) {
                case 0 -> new Color(33, 110, 196);
                case 1 -> new Color(0, 151, 167);
                case 2 -> new Color(46, 125, 50);
                case 3 -> new Color(245, 124, 0);
                case 4 -> new Color(173, 20, 87);
                default -> new Color(94, 53, 177);
            };
        }
    }
}
