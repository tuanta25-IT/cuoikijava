package com.library.desktop.util;

/**
 * File: TableExportUtils.java
 * Mô tả: Các tiện ích xuất dữ liệu từ `JTable` sang file Excel (`.xlsx`) và PDF.
 * - `exportExcel`/`exportPdf`: hiển thị hộp thoại chọn file và thực hiện xuất.
 * - `writeExcel`/`writePdf`: thực thi việc ghi dữ liệu ra file.
 * Các phương thức private hỗ trợ đảm nhiệm phần lựa chọn file và chuẩn hóa tên.
 */

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public final class TableExportUtils {
    private TableExportUtils() {
    }

    public static void exportExcel(Component parent, String title, JTable table) {
        /**
         * Hiển thị hộp thoại lưu file và xuất dữ liệu bảng ra file Excel.
         * 
         * @param parent thành phần cha để hiển thị dialog
         * @param title  tiêu đề/tiền tố tên file
         * @param table  JTable chứa dữ liệu cần xuất
         */
        File file = chooseFile(parent, title, "xlsx", "Excel files");
        if (file == null) {
            return;
        }
        try {
            writeExcel(file, title, table.getModel());
            JOptionPane.showMessageDialog(parent, "Đã xuất Excel: " + file.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void exportPdf(Component parent, String title, JTable table) {
        /**
         * Hiển thị hộp thoại lưu file và xuất dữ liệu bảng ra file PDF.
         */
        File file = chooseFile(parent, title, "pdf", "PDF files");
        if (file == null) {
            return;
        }
        try {
            writePdf(file, title, table.getModel());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static File chooseFile(Component parent, String title, String extension, String description) {
        /**
         * Hiển thị `JFileChooser` để người dùng chọn đường dẫn lưu file; đảm bảo phần
         * mở rộng.
         */
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu " + title);
        chooser.setFileFilter(new FileNameExtensionFilter(description, extension));
        chooser.setSelectedFile(new File(safeName(title) + "." + extension));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            file = new File(file.getParentFile(), file.getName() + "." + extension);
        }
        return file;
    }

    private static void writeExcel(File file, String title, TableModel model) throws IOException {
        /**
         * Ghi dữ liệu `TableModel` vào file Excel (.xlsx) sử dụng Apache POI.
         */
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet(safeName(title));
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row header = sheet.createRow(0);
            for (int c = 0; c < model.getColumnCount(); c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(model.getColumnName(c));
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < model.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object value = model.getValueAt(r, c);
                    row.createCell(c).setCellValue(value == null ? "" : String.valueOf(value));
                }
            }

            for (int c = 0; c < model.getColumnCount(); c++) {
                sheet.autoSizeColumn(c);
            }
            workbook.write(out);
        }
    }

    private static void writePdf(File file, String title, TableModel model) throws Exception {
        /**
         * Ghi dữ liệu `TableModel` vào file PDF sử dụng iText (lowagie).
         */
        try (FileOutputStream out = new FileOutputStream(file)) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph heading = new Paragraph(title, titleFont);
            heading.setAlignment(Element.ALIGN_CENTER);
            document.add(heading);
            document.add(new Paragraph(Chunk.NEWLINE));

            PdfPTable pdfTable = new PdfPTable(model.getColumnCount());
            pdfTable.setWidthPercentage(100);
            for (int c = 0; c < model.getColumnCount(); c++) {
                PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(c)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new java.awt.Color(226, 232, 240));
                pdfTable.addCell(cell);
            }
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object value = model.getValueAt(r, c);
                    pdfTable.addCell(value == null ? "" : String.valueOf(value));
                }
            }
            document.add(pdfTable);
            document.close();
        }
    }

    private static String safeName(String title) {
        /**
         * Chuẩn hóa tên tiêu đề thành tên file hợp lệ (chỉ chứa ký tự a-z0-9 và dấu
         * gạch _).
         */
        String normalized = title == null || title.isBlank() ? "export" : title.trim().toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^a-z0-9]+", "_").replaceAll("_+", "_");
    }
}
