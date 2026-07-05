package reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import database.DatabaseConnection;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Student;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StudentPaymentReport {

    // ==========================================
    // Fonts
    // ==========================================

    private static final Font TITLE_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    22,
                    Color.BLUE
            );

    private static final Font SUBTITLE_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    16,
                    Color.DARK_GRAY
            );

    private static final Font HEADER_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12,
                    Color.WHITE
            );

    private static final Font NORMAL_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    11,
                    Color.BLACK
            );

    private static final Font FOOTER_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_OBLIQUE,
                    10,
                    Color.GRAY
            );

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

        chooser.setTitle("Save PDF Report");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "PDF Files",
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

        PdfPCell cell = new PdfPCell(
                new Phrase(text, HEADER_FONT)
        );

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

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text == null ? "" : text,
                        NORMAL_FONT
                )
        );

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
                student.getName() + "_Payment_Report.pdf"
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
                    student.getEnrollmentDate().toString(),
                    ROW_COLOR
            ));

            document.add(studentTable);

            document.add(new Paragraph(" "));

            // ==========================================
            // Load Summary
            // ==========================================

            String summarySql = """
        SELECT
            COUNT(*) AS total_payments,
            IFNULL(SUM(cash_amount),0) AS cash_income,
            IFNULL(SUM(bank_amount),0) AS bank_income,
            IFNULL(SUM(cash_amount + bank_amount),0) AS total_paid,
            IFNULL(SUM(total_amount - (cash_amount + bank_amount)),0) AS remaining
        FROM payments
        WHERE student_id = ?
        """;

            Connection conn = DatabaseConnection.getConnection();

            PreparedStatement psSummary = conn.prepareStatement(summarySql);

            psSummary.setInt(1, student.getId());

            ResultSet rsSummary = psSummary.executeQuery();

            // ==========================================
            // Payment Summary
            // ==========================================

            PdfPTable summaryTable = new PdfPTable(2);

            summaryTable.setWidthPercentage(45);

            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            summaryTable.addCell(createHeaderCell("Payment Summary"));
            summaryTable.addCell(createHeaderCell("Value"));

            if (rsSummary.next()) {

                summaryTable.addCell(createCell("Total Payments", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.valueOf(rsSummary.getInt("total_payments")),
                        WHITE_COLOR
                ));

                summaryTable.addCell(createCell("Cash Paid", ROW_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                rsSummary.getDouble("cash_income")),
                        ROW_COLOR
                ));

                summaryTable.addCell(createCell("Bank Paid", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                rsSummary.getDouble("bank_income")),
                        WHITE_COLOR
                ));

                summaryTable.addCell(createCell("Total Paid", ROW_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                rsSummary.getDouble("total_paid")),
                        ROW_COLOR
                ));

                summaryTable.addCell(createCell("Remaining", WHITE_COLOR));
                summaryTable.addCell(createCell(
                        String.format(Locale.ENGLISH, "%.2f",
                                rsSummary.getDouble("remaining")),
                        WHITE_COLOR
                ));

            }

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

            String paymentsSql = """
        SELECT
            payment_date,
            payment_method,
            cash_amount,
            bank_amount,
            total_amount,
            (total_amount - (cash_amount + bank_amount)) AS remaining,
            notes
        FROM payments
        WHERE student_id = ?
        ORDER BY payment_date DESC
        """;

            PreparedStatement psPayments =
                    conn.prepareStatement(paymentsSql);

            psPayments.setInt(1, student.getId());

            ResultSet rsPayments =
                    psPayments.executeQuery();

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

            while (rsPayments.next()) {

                Color background = even ? ROW_COLOR : WHITE_COLOR;

                double cash = rsPayments.getDouble("cash_amount");
                double bank = rsPayments.getDouble("bank_amount");
                double total = rsPayments.getDouble("total_amount");
                double remaining = rsPayments.getDouble("remaining");

                table.addCell(createCell(
                        rsPayments.getDate("payment_date").toString(),
                        background
                ));

                table.addCell(createCell(
                        rsPayments.getString("payment_method"),
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
                        rsPayments.getString("notes"),
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
            // Close Resources
            // ==========================================

            rsPayments.close();
            psPayments.close();

            rsSummary.close();
            psSummary.close();

            conn.close();

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

            alert.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Failed to create PDF report."
            );

            alert.showAndWait();

        }

    }


}
