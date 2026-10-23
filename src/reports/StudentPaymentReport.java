package reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import database.PaymentRepository;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Student;
import ui.I18n;
import models.PaymentReportRow;
import models.PaymentSummary;
import models.Session;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class StudentPaymentReport {

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
    // Generate Student Payment Report
    // ==========================================

    public static void generate(Window owner, Student student) {

        File file = chooseSaveLocation(
                owner,
                ReportFileNames.create("Student_Payment_Report", student.getName(), "pdf")
        );

        if (file == null) {
            return;
        }

        Document document =
                new Document(PageSize.A4.rotate());

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
                    "Student Payment Report",
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
            // Student Information
            // ==========================================

            PdfPTable studentTable = new PdfPTable(2);

            studentTable.setWidthPercentage(45);

            studentTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            studentTable.addCell(createHeaderCell("Student Information"));
            studentTable.addCell(createHeaderCell("Value"));

            studentTable.addCell(createCell("Student Name", WHITE_COLOR));
            studentTable.addCell(createCell(
                    student.getName(),
                    WHITE_COLOR
            ));

            studentTable.addCell(createCell("Birth Year", ROW_COLOR));
            studentTable.addCell(createCell(
                    String.valueOf(student.getYearOfBirth()),
                    ROW_COLOR
            ));

            studentTable.addCell(createCell("Phone", WHITE_COLOR));
            studentTable.addCell(createCell(
                    student.getPhone(),
                    WHITE_COLOR
            ));

            studentTable.addCell(createCell("Enrollment Date", ROW_COLOR));
            studentTable.addCell(createCell(
                    student.getEnrollmentDate() == null ? "" : student.getEnrollmentDate().toString(),
                    ROW_COLOR
            ));

            document.add(studentTable);

            document.add(new Paragraph(" "));

            // ==========================================
            // Load Summary
            // ==========================================

            PaymentRepository paymentRepository = new PaymentRepository();
            PaymentSummary summary = paymentRepository.getSummary(student.getId(), Session.getUserId());

            // ==========================================
            // Payment Summary
            // ==========================================

            PdfPTable summaryTable = new PdfPTable(2);

            summaryTable.setWidthPercentage(45);

            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            summaryTable.addCell(createHeaderCell("Payment Summary"));
            summaryTable.addCell(createHeaderCell("Value"));

                summaryTable.addCell(createCell("Total Payments", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.valueOf(summary.getCount()),
                        WHITE_COLOR
                ));

                summaryTable.addCell(createCell("Cash Paid", ROW_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                summary.getCash()),
                        ROW_COLOR
                ));

                summaryTable.addCell(createCell("Bank Paid", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                summary.getBank()),
                        WHITE_COLOR
                ));

                summaryTable.addCell(createCell("Total Paid", ROW_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                summary.getPaid()),
                        ROW_COLOR
                ));

                summaryTable.addCell(createCell("Remaining", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                summary.getRemaining()),
                        WHITE_COLOR
                ));

            document.add(summaryTable);

            document.add(new Paragraph(" "));

            // ==========================================
            // Payment History
            // ==========================================

            Paragraph historyTitle = new Paragraph(
                    "Payment History",
                    SUBTITLE_FONT
            );

            historyTitle.setAlignment(Element.ALIGN_LEFT);

            document.add(historyTitle);

            document.add(new Paragraph(" "));

            // ==========================================
            // Load Payments
            // ==========================================

            List<PaymentReportRow> paymentRows =
                    paymentRepository.findReportRowsForStudent(student.getId(), Session.getUserId());

            PdfPTable table = new PdfPTable(7);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                    2.5f, // Date
                    2.5f, // Method
                    2f,   // Cash
                    2f,   // Bank
                    2f,   // Total
                    2f,   // Remaining
                    4f    // Notes
            });

            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Method"));
            table.addCell(createHeaderCell("Cash"));
            table.addCell(createHeaderCell("Bank"));
            table.addCell(createHeaderCell("Total"));
            table.addCell(createHeaderCell("Remaining"));
            table.addCell(createHeaderCell("Notes"));

            table.setHeaderRows(1);

            // ==========================================
// Payment Rows
// ==========================================

            boolean even = false;

            for (PaymentReportRow row : paymentRows) {

                Color background = even ? ROW_COLOR : WHITE_COLOR;

                BigDecimal cash = row.getCash();
                BigDecimal bank = row.getBank();
                BigDecimal total = row.getTotal();
                BigDecimal remaining = row.getRemaining();

                table.addCell(createCell(
                        row.getPaymentDate().toString(),
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

                table.addCell(createCell(
                        row.getNotes(),
                        background
                ));

                even = !even;
            }

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
            alert.setContentText("Student payment report created successfully.");
            I18n.localize(alert);

            alert.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Failed to create PDF report."
            );
            I18n.localize(alert);

            alert.showAndWait();

        }

    }


}
