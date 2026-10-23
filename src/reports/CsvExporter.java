package reports;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Payment;
import models.PaymentReportRow;
import models.Student;
import models.AttendanceReportRow;
import ui.I18n;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** CSV exports with a UTF-8 BOM so Excel recognizes Arabic text. */
public final class CsvExporter {
    private CsvExporter() {}

    public static void exportStudents(Window owner, List<Student> students) {
        List<List<String>> rows = new ArrayList<>();
        for (Student student : students) {
            rows.add(List.of(value(student.getId()), value(student.getName()),
                    value(student.getYearOfBirth()), value(student.getPhone()),
                    value(student.getEnrollmentDate()), value(student.getNotes())));
        }
        export(owner, ReportFileNames.create("Students_Report", null, "csv"),
                List.of("ID", "Student Name", "Birth Year", "Phone", "Registration Date", "Notes"), rows);
    }

    public static void exportPayments(Window owner, List<PaymentReportRow> payments) {
        List<List<String>> rows = new ArrayList<>();
        for (PaymentReportRow payment : payments) {
            rows.add(List.of(value(payment.getPaymentDate()), value(payment.getStudentName()),
                    I18n.text(value(payment.getPaymentMethod())), value(payment.getCash()), value(payment.getBank()),
                    value(payment.getTotal()), value(payment.getRemaining()), value(payment.getNotes())));
        }
        export(owner, ReportFileNames.create("Payments_Report", null, "csv"),
                List.of("Date", "Student", "Method", "Cash", "Bank", "Total", "Remaining", "Notes"), rows);
    }

    public static void exportStudentPayments(Window owner, Student student, List<Payment> payments) {
        List<List<String>> rows = new ArrayList<>();
        for (Payment payment : payments) {
            rows.add(List.of(value(student.getName()), value(payment.getPaymentDate()),
                    I18n.text(value(payment.getPaymentMethod())), value(payment.getTotalAmount()),
                    value(payment.getCashAmount()), value(payment.getBankAmount()),
                    value(payment.getPaid()), value(payment.getRemaining()), value(payment.getNotes())));
        }
        export(owner, ReportFileNames.create("Student_Payment_Report", student.getName(), "csv"),
                List.of("Student", "Date", "Method", "Amount Due", "Cash", "Bank", "Paid", "Remaining", "Notes"), rows);
    }

    public static void exportAttendance(Window owner, List<AttendanceReportRow> attendance) {
        List<List<String>> rows = new ArrayList<>();
        for (AttendanceReportRow record : attendance) {
            String status = record.statusCode() == null ? "Not marked" : switch (record.statusCode()) {
                case "PRESENT" -> "Present";
                case "ABSENT" -> "Absent";
                case "LATE" -> "Late";
                case "EXCUSED" -> "Excused absence";
                default -> record.statusCode();
            };
            rows.add(List.of(value(record.date()), value(record.studentId()), value(record.studentName()),
                    I18n.text(status), value(record.notes())));
        }
        export(owner, ReportFileNames.create("Attendance_Report", null, "csv"),
                List.of("Date", "Student ID", "Student", "Attendance status", "Notes"), rows);
    }

    private static void export(Window owner, String defaultName, List<String> headers, List<List<String>> rows) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.text("Export CSV"));
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File selected = chooser.showSaveDialog(owner);
        if (selected == null) return;

        File target = selected.getName().toLowerCase().endsWith(".csv")
                ? selected : new File(selected.getParentFile(), selected.getName() + ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writeRow(writer, headers.stream().map(I18n::text).toList());
            for (List<String> row : rows) writeRow(writer, row);
            show(Alert.AlertType.INFORMATION, "Export complete", "CSV file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            show(Alert.AlertType.ERROR, "Export failed", "Could not save the CSV file: " + e.getMessage());
        }
    }

    private static void writeRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) writer.write(',');
            writer.write(escape(values.get(i)));
        }
        writer.write("\r\n");
    }

    private static String escape(String value) {
        String safeValue = value == null ? "" : value;
        if (!safeValue.isEmpty() && "=+-@\t".indexOf(safeValue.charAt(0)) >= 0) safeValue = "'" + safeValue;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(I18n.text(title));
        alert.setHeaderText(null);
        alert.setContentText(I18n.text(message));
        I18n.localize(alert);
        alert.showAndWait();
    }
}
