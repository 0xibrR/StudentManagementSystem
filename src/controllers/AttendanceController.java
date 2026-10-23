package controllers;

import database.AttendanceRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.AttendanceReportRow;
import models.AttendanceRow;
import models.AttendanceStatus;
import models.Session;
import reports.CsvExporter;
import ui.I18n;
import ui.WindowChrome;

import java.time.LocalDate;
import java.util.List;

public class AttendanceController {
    @FXML private DatePicker dpAttendanceDate;
    @FXML private DatePicker dpReportFrom;
    @FXML private DatePicker dpReportTo;
    @FXML private TableView<AttendanceRow> tableAttendance;
    @FXML private TableColumn<AttendanceRow, Integer> colStudentId;
    @FXML private TableColumn<AttendanceRow, String> colStudentName;
    @FXML private TableColumn<AttendanceRow, AttendanceStatus> colStatus;
    @FXML private TableColumn<AttendanceRow, String> colNotes;
    @FXML private Label lblCount;
    @FXML private Label lblEmpty;
    @FXML private Label lblReportFrom;
    @FXML private Label lblReportTo;
    @FXML private Button btnLoadDay;
    @FXML private Button btnMarkAllPresent;
    @FXML private Button btnSaveAttendance;
    @FXML private Button btnExportReport;
    @FXML private Button btnClose;

    private final AttendanceRepository attendanceRepository = new AttendanceRepository();

    @FXML
    public void initialize() {
        dpAttendanceDate.setValue(LocalDate.now());
        dpReportFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpReportTo.setValue(LocalDate.now());
        I18n.setText(btnLoadDay, "Load day");
        I18n.setText(btnMarkAllPresent, "Mark all present");
        I18n.setText(btnSaveAttendance, "Save attendance");
        I18n.setText(btnExportReport, "Export period report");
        I18n.setText(btnClose, "Close");
        I18n.setText(lblEmpty, "No content in table");
        I18n.setText(lblReportFrom, "Report from");
        I18n.setText(lblReportTo, "Report to");
        I18n.setText(colStudentId, "ID");
        I18n.setText(colStudentName, "Student Name");
        I18n.setText(colStatus, "Attendance status");
        I18n.setText(colNotes, "Attendance notes");
        tableAttendance.setPlaceholder(lblEmpty);
        colStudentId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getStudentId()).asObject());
        colStudentName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStudentName()));
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStatus.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<AttendanceStatus>() {
            @Override public String toString(AttendanceStatus status) {
                return status == null ? I18n.text("Not marked") : I18n.text(status.getLabel());
            }
            @Override public AttendanceStatus fromString(String value) { return null; }
        }, FXCollections.observableArrayList(AttendanceStatus.values())));
        colStatus.setOnEditCommit(event -> event.getRowValue().setStatus(event.getNewValue()));
        colNotes.setCellValueFactory(data -> data.getValue().notesProperty());
        colNotes.setCellFactory(TextFieldTableCell.forTableColumn());
        colNotes.setOnEditCommit(event -> event.getRowValue().setNotes(event.getNewValue()));
        tableAttendance.setEditable(true);
        loadDay();
    }

    @FXML
    private void loadDay() {
        LocalDate date = dpAttendanceDate.getValue();
        if (date == null) {
            show(Alert.AlertType.WARNING, "Error", "Choose a valid date range.");
            return;
        }
        try {
            List<AttendanceRow> rows = attendanceRepository.findScheduledForDate(Session.getUserId(), date);
            tableAttendance.setItems(FXCollections.observableArrayList(rows));
            btnMarkAllPresent.setDisable(rows.isEmpty());
            lblCount.setText(rows.size() + " " + I18n.text("Scheduled students"));
            if (rows.isEmpty()) show(Alert.AlertType.INFORMATION, "Attendance",
                    "No students are scheduled for this date. Add weekly days in the student's details.");
        } catch (Exception exception) {
            exception.printStackTrace();
            show(Alert.AlertType.ERROR, "Error", "Could not load scheduled students for this date.");
        }
    }

    @FXML
    private void markAllPresent() {
        if (tableAttendance.getItems().isEmpty()) return;
        tableAttendance.getItems().forEach(row -> row.setStatus(AttendanceStatus.PRESENT));
        tableAttendance.refresh();
    }

    @FXML
    private void saveAttendance() {
        tableAttendance.edit(-1, null);
        LocalDate date = dpAttendanceDate.getValue();
        if (date == null) {
            show(Alert.AlertType.WARNING, "Error", "Choose a valid date range.");
            return;
        }
        if (tableAttendance.getItems().stream().noneMatch(row -> row.getStatus() != null)) {
            show(Alert.AlertType.WARNING, "Warning", "Mark at least one student's attendance before saving.");
            return;
        }
        try {
            attendanceRepository.saveDate(Session.getUserId(), date, tableAttendance.getItems());
            loadDay();
            show(Alert.AlertType.INFORMATION, "Success", "Attendance saved successfully.");
        } catch (Exception exception) {
            exception.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.text("Error"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.text(exception.getMessage() == null
                    ? "Could not save attendance." : exception.getMessage()));
            I18n.localize(alert);
            alert.showAndWait();
        }
    }

    @FXML
    private void exportReport() {
        LocalDate start = dpReportFrom.getValue();
        LocalDate end = dpReportTo.getValue();
        try {
            List<AttendanceReportRow> rows = attendanceRepository.findRecordedBetween(Session.getUserId(), start, end);
            if (rows.isEmpty()) {
                show(Alert.AlertType.INFORMATION, "Attendance", "No saved attendance records were found in this period.");
                return;
            }
            CsvExporter.exportAttendance(tableAttendance.getScene().getWindow(), rows);
        } catch (Exception exception) {
            exception.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.text("Export failed"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.text(exception.getMessage() == null
                    ? "Could not load scheduled students for this date." : exception.getMessage()));
            I18n.localize(alert);
            alert.showAndWait();
        }
    }

    @FXML
    private void close() {
        ((Stage) tableAttendance.getScene().getWindow()).close();
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(I18n.text(title));
        alert.setHeaderText(null);
        alert.setContentText(I18n.text(message));
        I18n.localize(alert);
        alert.showAndWait();
    }

    public static void open(javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(AttendanceController.class.getResource("/views/attendance.fxml"));
            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initOwner(owner);
            stage.setTitle("Attendance");
            stage.setResizable(true);
            stage.setMinWidth(850);
            stage.setMinHeight(600);
            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));
            stage.showAndWait();
        } catch (Exception exception) {
            exception.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.text("Error"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.text("Could not load scheduled students for this date."));
            I18n.localize(alert);
            alert.showAndWait();
        }
    }
}
