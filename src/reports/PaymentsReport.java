package reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import database.PaymentRepository;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Session;
import models.PaymentReportRow;
import ui.I18n;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class PaymentsReport {

    // ==========================================
    // Fonts
    // ==========================================

    private static final Font TITLE_FONT = PdfFonts.title(Color.BLUE);

    private static final Font SUBTITLE_FONT = PdfFonts.subtitle(Color.DARK_GRAY);

    private static final Font HEADER_FONT = PdfFonts.header(Color.WHITE);

    private static final Font NORMAL_FONT = PdfFonts.normal(Color.BLACK);

    private static final Font FOOTER_FONT = PdfFonts.footer(Color.GRAY);

    // ==========================================
    // Colors
    // ==========================================

    private static final Color HEADER_COLOR =
            new Color(52, 73, 94);

    private static final Color ROW_COLOR =
            new Color(245, 245, 245);

    private static final Color WHITE_COLOR =
            Color.WHITE;

    // ==========================================
    // Choose Save Location
    // ==========================================

    private static File chooseSaveLocation(
            Window owner,
            String defaultFileName
    ) {

        FileChooser chooser = new FileChooser();

        chooser.setTitle(I18n.text("Save PDF Report"));

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        I18n.text("PDF Files"),
                        "*.pdf"
                )
        );

        chooser.setInitialFileName(defaultFileName);

        return chooser.showSaveDialog(owner);

    }

    // ==========================================
    // Header Cell
    // ==========================================

    private static PdfPCell createHeaderCell(String text) {

        text = I18n.text(text);

        PdfPCell cell = new PdfPCell(
                new Phrase(text, HEADER_FONT)
        );

        PdfFonts.enableRtl(cell, text);

        cell.setBackgroundColor(HEADER_COLOR);

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(8);

        return cell;

    }

    // ==========================================
    // Normal Cell
    // ==========================================

    private static PdfPCell createCell(
            String text,
            Color background
    ) {

        text = I18n.text(text);

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text == null ? "" : text,
                        NORMAL_FONT
                )
        );

        PdfFonts.enableRtl(cell, text);

        cell.setBackgroundColor(background);

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(6);

        return cell;

    }

    // ==========================================
    // Page Number Event
    // ==========================================

    private static class PageNumberEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            PdfContentByte cb = writer.getDirectContent();

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "Page " + writer.getPageNumber(),
                            FOOTER_FONT
                    ),
                    (document.right() + document.left()) / 2,
                    document.bottom() - 18,
                    0
            );

        }

    }

    // ==========================================
    // Generate Payments Report
    // ==========================================

    public static void generate(Window owner) {

        File file = chooseSaveLocation(
                owner,
                ReportFileNames.create("Payments_Report", null, "pdf")
        );

        if (file == null) {
            return;
        }

        Document document = new Document(PageSize.A4.rotate());

        try {

            PdfWriter writer = PdfWriter.getInstance(
                    document,
                    new FileOutputStream(file)
            );

            writer.setPageEvent(new PageNumberEvent());

            document.open();

            // ==========================================
            // Title
            // ==========================================

            Paragraph title = new Paragraph(
                    "STUDENT MANAGEMENT SYSTEM",
                    TITLE_FONT
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);


            // ==========================================
            // Subtitle
            // ==========================================

            Paragraph subTitle = new Paragraph(
                    "Payments Report",
                    SUBTITLE_FONT
            );

            subTitle.setAlignment(Element.ALIGN_CENTER);

            document.add(subTitle);

            document.add(new Paragraph(" "));

            // ==========================================
            // Report Information
            // ==========================================

            document.add(new Paragraph(
                    "Generated Date : " + LocalDate.now(),
                    NORMAL_FONT
            ));

            document.add(new Paragraph(
                    "Generated Time : " +
                            LocalTime.now().format(
                                    DateTimeFormatter.ofPattern(
                                            "hh:mm a",
                                            Locale.ENGLISH
                                    )
                            ),
                    NORMAL_FONT
            ));

            document.add(new Paragraph(" "));

            // ==========================================
            // Load Payments
            // ==========================================

            List<PaymentReportRow> rows = new PaymentRepository()
                    .findReportRowsForUser(Session.getUserId());

            // ==========================================
            // Summary
            // ==========================================

            int totalPayments = rows.size();

            BigDecimal cashIncome = BigDecimal.ZERO;

            BigDecimal bankIncome = BigDecimal.ZERO;

            BigDecimal totalIncome = BigDecimal.ZERO;

            BigDecimal remainingIncome = BigDecimal.ZERO;

            // ==========================================
            // Payments Table
            // ==========================================

            PdfPTable table = new PdfPTable(7);

            table.setWidthPercentage(100);

            table.setSpacingBefore(10);

            table.setWidths(new float[]{
                    2.5f, // Date
                    3.5f, // Student
                    2.5f, // Method
                    2f,   // Cash
                    2f,   // Bank
                    2f,   // Total
                    2f    // Remaining
            });


            // Header

            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Student"));
            table.addCell(createHeaderCell("Method"));
            table.addCell(createHeaderCell("Cash"));
            table.addCell(createHeaderCell("Bank"));
            table.addCell(createHeaderCell("Total"));
            table.addCell(createHeaderCell("Remaining"));

            table.setHeaderRows(1);

            // ==========================================
            // Payments Data
            // ==========================================

            boolean even = false;

            for (PaymentReportRow row : rows) {

                Color background = even ? ROW_COLOR : WHITE_COLOR;

                BigDecimal cash = row.getCash();
                BigDecimal bank = row.getBank();
                BigDecimal total = row.getTotal();
                BigDecimal remaining = row.getRemaining();

                cashIncome = cashIncome.add(cash);

                bankIncome = bankIncome.add(bank);

                totalIncome = totalIncome.add(cash).add(bank);

                remainingIncome = remainingIncome.add(remaining);

                table.addCell(createCell(
                        row.getPaymentDate().toString(),
                        background
                ));

                table.addCell(createCell(
                        row.getStudentName(),
                        background
                ));

                table.addCell(createCell(
                        row.getPaymentMethod(),
                        background
                ));

                table.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f", cash),
                        background
                ));

                table.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f", bank),
                        background
                ));

                table.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f", total),
                        background
                ));

                table.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f", remaining),
                        background
                ));

                even = !even;
            }

            // ==========================================
            // Payment Summary
            // ==========================================

            PdfPTable summaryTable = new PdfPTable(2);

            summaryTable.setWidthPercentage(45);

            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            summaryTable.addCell(createHeaderCell("Payment Summary"));
            summaryTable.addCell(createHeaderCell("Value"));

            summaryTable.addCell(createCell("Total Payments", WHITE_COLOR));
            summaryTable.addCell(createCell(String.valueOf(totalPayments), WHITE_COLOR));

            summaryTable.addCell(createCell("Cash Income", ROW_COLOR));
            summaryTable.addCell(createCell(String.format(Locale.ENGLISH, "%.2f", cashIncome), ROW_COLOR));

            summaryTable.addCell(createCell("Bank Income", WHITE_COLOR));
            summaryTable.addCell(createCell(String.format(Locale.ENGLISH, "%.2f", bankIncome), WHITE_COLOR));

            summaryTable.addCell(createCell("Total Income", ROW_COLOR));
            summaryTable.addCell(createCell(String.format(Locale.ENGLISH, "%.2f", totalIncome), ROW_COLOR));

            summaryTable.addCell(createCell("Remaining", WHITE_COLOR));
            summaryTable.addCell(createCell(String.format(Locale.ENGLISH, "%.2f", remainingIncome), WHITE_COLOR));

            document.add(summaryTable);

            document.add(new Paragraph(" "));

            // ==========================================
            // Payment List
            // ==========================================

            Paragraph paymentListTitle = new Paragraph(
                    "Payment List",
                    SUBTITLE_FONT
            );

            paymentListTitle.setAlignment(Element.ALIGN_LEFT);

            document.add(paymentListTitle);

            document.add(new Paragraph(" "));

            document.add(table);

            document.add(new Paragraph(" "));

            // ==========================================
            // Footer
            // ==========================================

            Paragraph footer = new Paragraph(
                    "Generated by Student Management System",
                    FOOTER_FONT
            );

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            // ==========================================
            // Finish PDF
            // ==========================================

            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Payments report created successfully.");
            I18n.localize(alert);

            alert.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to create PDF report.");
            I18n.localize(alert);

            alert.showAndWait();

        }

    }

}
