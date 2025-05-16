package com.example.n03_quanlychitieu.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PDFGenerator {
    private Context context;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public PDFGenerator(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void generateFinancialReport(String fileName,
                                        double totalIncome,
                                        double totalExpense,
                                        double balance,
                                        List<Incomes> incomes,
                                        List<Expenses> expenses,
                                        Map<Categories, Double> categoryStats,
                                        Map<String, Double> timeStats) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            addTitle(document, "BÁO CÁO TÀI CHÍNH");
            addSummarySection(document, totalIncome, totalExpense, balance);

            if (incomes != null && !incomes.isEmpty()) {
                addSectionTitle(document, "DANH SÁCH THU NHẬP");
                addIncomeTable(document, incomes);
            }

            if (expenses != null && !expenses.isEmpty()) {
                addSectionTitle(document, "DANH SÁCH CHI TIÊU");
                addExpenseTable(document, expenses);
            }

            if (categoryStats != null && !categoryStats.isEmpty()) {
                addSectionTitle(document, "THỐNG KÊ THEO DANH MỤC");
                addCategoryStatsTable(document, categoryStats);
            }

            if (timeStats != null && !timeStats.isEmpty()) {
                addSectionTitle(document, "THỐNG KÊ THEO THỜI GIAN");
                addTimeStatsTable(document, timeStats);
            }

            addFooter(document);
            document.close();

            Toast.makeText(context, "Đã lưu báo cáo vào " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Lỗi khi tạo báo cáo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDFGenerator", "Error generating PDF", e);
        }
    }

    private void addTitle(Document document, String title) {
        Paragraph titleParagraph = new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setMarginBottom(20);
        document.add(titleParagraph);

        SimpleDateFormat reportDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Paragraph dateParagraph = new Paragraph("Ngày tạo: " + reportDateFormat.format(new Date()))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginBottom(30);
        document.add(dateParagraph);
    }

    private void addSummarySection(Document document, double totalIncome, double totalExpense, double balance) {
        float[] columnWidths = {1, 1, 1};
        Table table = new Table(columnWidths);
        table.setWidth(500);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setMarginBottom(30);

        table.addCell(createCell("TỔNG THU", true, ColorConstants.GREEN));
        table.addCell(createCell("TỔNG CHI", true, ColorConstants.RED));
        table.addCell(createCell("SỐ DƯ", true, ColorConstants.BLUE));

        table.addCell(createCell(currencyFormat.format(totalIncome), false, ColorConstants.GREEN));
        table.addCell(createCell(currencyFormat.format(totalExpense), false, ColorConstants.RED));
        table.addCell(createCell(currencyFormat.format(balance), false, ColorConstants.BLUE));

        document.add(table);
    }

    private void addIncomeTable(Document document, List<Incomes> incomes) {
        float[] columnWidths = {1, 3, 2, 2};
        Table table = new Table(columnWidths);
        table.setWidth(500);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setMarginBottom(20);

        table.addCell(createCell("STT", true));
        table.addCell(createCell("Mô tả", true));
        table.addCell(createCell("Số tiền", true));
        table.addCell(createCell("Thời gian", true));

        for (int i = 0; i < incomes.size(); i++) {
            Incomes income = incomes.get(i);
            table.addCell(createCell(String.valueOf(i + 1), false));
            table.addCell(createCell(income.getDescription(), false));
            table.addCell(createCell(currencyFormat.format(income.getAmount()), false, ColorConstants.GREEN));
            table.addCell(createCell(dateFormat.format(new Date(Long.parseLong(income.getCreate_at()))), false));
        }

        document.add(table);
    }

    private void addExpenseTable(Document document, List<Expenses> expenses) {
        float[] columnWidths = {1, 3, 2, 2};
        Table table = new Table(columnWidths);
        table.setWidth(500);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setMarginBottom(20);

        table.addCell(createCell("STT", true));
        table.addCell(createCell("Mô tả", true));
        table.addCell(createCell("Số tiền", true));
        table.addCell(createCell("Thời gian", true));

        for (int i = 0; i < expenses.size(); i++) {
            Expenses expense = expenses.get(i);
            table.addCell(createCell(String.valueOf(i + 1), false));
            table.addCell(createCell(expense.getDescription(), false));
            table.addCell(createCell(currencyFormat.format(expense.getAmount()), false, ColorConstants.RED));
            table.addCell(createCell(dateFormat.format(new Date(Long.parseLong(expense.getCreate_at()))), false));
        }

        document.add(table);
    }

    private void addCategoryStatsTable(Document document, Map<Categories, Double> categoryStats) {
        float[] columnWidths = {1, 3, 2};
        Table table = new Table(columnWidths);
        table.setWidth(500);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setMarginBottom(20);

        table.addCell(createCell("STT", true));
        table.addCell(createCell("Danh mục", true));
        table.addCell(createCell("Tổng", true));

        int i = 1;
        for (Map.Entry<Categories, Double> entry : categoryStats.entrySet()) {
            table.addCell(createCell(String.valueOf(i++), false));
            table.addCell(createCell(entry.getKey().getName(), false));
            table.addCell(createCell(currencyFormat.format(entry.getValue()), false));
        }

        document.add(table);
    }

    private void addTimeStatsTable(Document document, Map<String, Double> timeStats) {
        float[] columnWidths = {1, 3, 2};
        Table table = new Table(columnWidths);
        table.setWidth(500);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setMarginBottom(20);

        table.addCell(createCell("STT", true));
        table.addCell(createCell("Thời gian", true));
        table.addCell(createCell("Tổng", true));

        int i = 1;
        for (Map.Entry<String, Double> entry : timeStats.entrySet()) {
            table.addCell(createCell(String.valueOf(i++), false));
            table.addCell(createCell(entry.getKey(), false));
            table.addCell(createCell(currencyFormat.format(entry.getValue()), false));
        }

        document.add(table);
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph paragraph = new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(paragraph);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("Báo cáo được tạo bởi ứng dụng Quản lý chi tiêu cá nhân")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setItalic()
                .setMarginTop(30);
        document.add(footer);
    }

    private Cell createCell(String content, boolean isHeader) {
        return createCell(content, isHeader, ColorConstants.BLACK);
    }

    private Cell createCell(String content, boolean isHeader, com.itextpdf.kernel.colors.Color color) {
        Paragraph paragraph = new Paragraph(content)
                .setFontColor(color)
                .setTextAlignment(TextAlignment.CENTER);

        Cell cell = new Cell()
                .add(paragraph)
                .setPadding(5)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (isHeader) {
            cell.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }

        return cell;
    }
}