package reports;

import java.awt.Color;
import java.awt.Desktop;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import com.lowagie.text.pdf.*;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import models.Student;

import java.time.LocalDate;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

public class StudentsReport {

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

    private static File chooseSaveLocation(Window owner, String defaultFileName) {

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

    private static PdfPCell createCell(String text, Color background) {

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
        public void onEndPage(
                PdfWriter writer,
                Document document
        ) {

            PdfContentByte canvas = writer.getDirectContent();

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "Page " + writer.getPageNumber(),
                            FOOTER_FONT
                    ),
                    (document.left() + document.right()) / 2,
                    document.bottom() - 15,
                    0
            );

        }

    }

    // ==========================================
    // Generate Students Report
    // ==========================================

    public static void generate(Window owner, List<Student> students) {

        File file = chooseSaveLocation(
                owner,
                "Students_Report.pdf"
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
                    "Students Report",
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
                    "Total Students : " + students.size(),
                    NORMAL_FONT
            ));

            document.add(new Paragraph(" "));

            // ==========================================
            // Students Table
            // ==========================================

            PdfPTable table = new PdfPTable(6);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                    1f,   // ID
                    3f,   // Name
                    2f,   // Birth Year
                    2.5f, // Enrollment Date
                    3f,   // Phone
                    4f    // Notes
            });

            // Header
            table.addCell(createHeaderCell("ID"));
            table.addCell(createHeaderCell("Student Name"));
            table.addCell(createHeaderCell("Birth Year"));
            table.addCell(createHeaderCell("Registration Date"));
            table.addCell(createHeaderCell("Phone"));
            table.addCell(createHeaderCell("Notes"));
            table.setHeaderRows(1);

            // ==========================================
            // Students Data
            // ==========================================

            boolean even = false;

            for (Student student : students) {

                Color background = even ? ROW_COLOR : WHITE_COLOR;

                table.addCell(createCell(
                        String.valueOf(student.getId()),
                        background
                ));

                table.addCell(createCell(
                        student.getName(),
                        background
                ));

                table.addCell(createCell(
                        String.valueOf(student.getYearOfBirth()),
                        background
                ));

                table.addCell(createCell(
                        String.valueOf(student.getEnrollmentDate()),
                        background
                ));

                table.addCell(createCell(
                        student.getPhone(),
                        background
                ));

                table.addCell(createCell(
                        student.getNotes(),
                        background
                ));

                even = !even;
            }

            // ==========================================
            // Add Table
            // ==========================================

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
            // Finish
            // ==========================================

            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Students report created successfully.");

            alert.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");

            alert.setHeaderText(null);

            alert.setContentText("Failed to create PDF report.");

            alert.showAndWait();

        }

    }

}