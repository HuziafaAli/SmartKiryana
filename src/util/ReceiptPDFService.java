package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.Bill;
import model.BillItem;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReceiptPDFService {

    private static final String RECEIPT_DIR = "receipts";

    // Generates a formatted PDF receipt and saves it to the receipts folder
    public static String saveReceiptAsPDF(Bill bill) {
        File dir = new File(RECEIPT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("Receipt_Bill%d_%s.pdf", 
                bill.getBillId(), 
                bill.getBillDate().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));
        File file = new File(dir, fileName);

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            Paragraph title = new Paragraph("SMART KIRYANA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph address = new Paragraph("Main Market, City Center\nPhone: 0300-1234567\n\n", normalFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(createNoBorderCell("Bill ID: #" + bill.getBillId(), normalFont, Element.ALIGN_LEFT));
            infoTable.addCell(createNoBorderCell("Date: " + bill.getBillDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), normalFont, Element.ALIGN_RIGHT));
            infoTable.addCell(createNoBorderCell("Cashier: " + (bill.getUser() != null ? bill.getUser().getFullName() : "System"), normalFont, Element.ALIGN_LEFT));
            infoTable.addCell(createNoBorderCell("Status: PAID", normalFont, Element.ALIGN_RIGHT));
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1, 1});

            table.addCell(new Phrase("Item", headerFont));
            table.addCell(new Phrase("Qty", headerFont));
            table.addCell(new Phrase("Price", headerFont));
            table.addCell(new Phrase("Total", headerFont));

            for (BillItem item : bill.getItems()) {
                table.addCell(new Phrase(item.getProduct().getName(), normalFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                table.addCell(new Phrase(String.format("%.0f", item.getUnitPrice()), normalFont));
                table.addCell(new Phrase(String.format("%.0f", item.getSubtotal()), normalFont));
            }
            document.add(table);
            document.add(new Paragraph("\n"));

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(40);
            summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            summary.addCell(createNoBorderCell("Sub Total:", normalFont, Element.ALIGN_LEFT));
            summary.addCell(createNoBorderCell(String.format("Rs. %.2f", bill.getTotalAmount() + bill.getDiscountAmount()), normalFont, Element.ALIGN_RIGHT));
            
            if (bill.getDiscountAmount() > 0) {
                summary.addCell(createNoBorderCell("Discount:", normalFont, Element.ALIGN_LEFT));
                summary.addCell(createNoBorderCell(String.format("-Rs. %.2f", bill.getDiscountAmount()), normalFont, Element.ALIGN_RIGHT));
            }

            PdfPCell totalLabel = createNoBorderCell("TOTAL:", headerFont, Element.ALIGN_LEFT);
            PdfPCell totalVal = createNoBorderCell(String.format("Rs. %.2f", bill.getTotalAmount()), headerFont, Element.ALIGN_RIGHT);
            summary.addCell(totalLabel);
            summary.addCell(totalVal);

            document.add(summary);
            
            document.add(new Paragraph("\n\nThank you for shopping!\nPowered by SmartKiryana", normalFont));

            document.close();
            return file.getAbsolutePath();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPCell createNoBorderCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }
}
