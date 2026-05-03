package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.MonthlyReport;
import model.Product;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportPDFService {
    private static final String REPORT_DIR = "reports/monthly_sales";

    // Exports the monthly sales report as a formatted PDF file
    public static String exportMonthlyReport(MonthlyReport report, String month, int year) {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("Monthly_Sales_Report_%s_%d.pdf", month, year);
        File file = new File(dir, fileName);

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);

            Paragraph title = new Paragraph("SmartKiryana - Monthly Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph period = new Paragraph(String.format("Period: %s %d", month, year), headerFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(30);
            document.add(period);

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setSpacingAfter(30);
            
            summary.addCell(createCell("Total Sales:", headerFont, Color.decode("#f8fafc")));
            summary.addCell(createCell(String.format("Rs. %,.0f", report.getTotalSales()), normalFont, Color.WHITE));
            summary.addCell(createCell("Total Returns:", headerFont, Color.decode("#f8fafc")));
            summary.addCell(createCell(String.format("Rs. %,.0f", report.getTotalReturns()), normalFont, Color.WHITE));
            summary.addCell(createCell("Net Revenue:", headerFont, Color.decode("#eff6ff")));
            summary.addCell(createCell(String.format("Rs. %,.0f", report.getNetRevenue()), headerFont, Color.decode("#eff6ff")));
            document.add(summary);

            Paragraph topTitle = new Paragraph("Top Performing Products", headerFont);
            topTitle.setSpacingAfter(10);
            document.add(topTitle);
            
            PdfPTable productsTable = new PdfPTable(3);
            productsTable.setWidthPercentage(100);
            productsTable.setWidths(new float[]{3, 2, 1});
            
            productsTable.addCell(createHeaderCell("Product Name", headerFont));
            productsTable.addCell(createHeaderCell("Category", headerFont));
            productsTable.addCell(createHeaderCell("Qty Sold", headerFont));

            for (Product p : report.getTopProducts()) {
                productsTable.addCell(createCell(p.getName(), normalFont, Color.WHITE));
                productsTable.addCell(createCell(p.getCategory() != null ? p.getCategory().getCategoryName() : "General", normalFont, Color.WHITE));
                productsTable.addCell(createCell(String.valueOf(p.getSalesQuantity()), normalFont, Color.WHITE));
            }
            document.add(productsTable);
            
            Paragraph footer = new Paragraph("\n\nGenerated automatically by SmartKiryana POS", 
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return file.getAbsolutePath();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPCell createCell(String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setBackgroundColor(bgColor);
        return cell;
    }
    
    private static PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setBackgroundColor(Color.decode("#e2e8f0"));
        return cell;
    }
}
