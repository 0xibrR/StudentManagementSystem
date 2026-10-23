package controllers;

import ui.WindowChrome;
import ui.I18n;

import database.DashboardRepository;
import database.PaymentRepository;
import database.StudentRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Session;
import models.Student;
import reports.StudentsReport;
import reports.PaymentsReport;
import reports.CsvExporter;
import models.PaymentReportRow;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Comparator;

import java.sql.Date;
import java.math.BigDecimal;
import java.util.List;

public class HomeController {

    @FXML
    private TableView<Student> tableStudents;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbBirthYearFilter;
    @FXML
    private Label lblStudentResults;

    @FXML
    private TableColumn<Student, Integer> colId;

    @FXML
    private TableColumn<Student, String> colName;

    @FXML
    private TableColumn<Student, Integer> colYear;

    @FXML
    private TableColumn<Student, String> colPhone;

    @FXML
    private TableColumn<Student, Date> colEnrollmentDate;

    @FXML
    private TableColumn<Student, String> colNotes;

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblTotalStudents;

    @FXML
    private Label lblTotalPayments;

    @FXML
    private Label lblCashIncome;

    @FXML
    private Label lblBankIncome;

    @FXML
    private Label lblRemaining;

    @FXML
    private Label lblTotalIncome;

    @FXML
    private VBox remainingCard;

    private final ObservableList<Student> allStudents = FXCollections.observableArrayList();
    private FilteredList<Student> filteredStudents;
    private SortedList<Student> sortedStudents;
    private final StudentRepository studentRepository = new StudentRepository();
    private final DashboardRepository dashboardRepository = new DashboardRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("yearOfBirth"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEnrollmentDate.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        filteredStudents = new FilteredList<>(allStudents, student -> true);
        sortedStudents = new SortedList<>(filteredStudents);
        sortedStudents.comparatorProperty().bind(tableStudents.comparatorProperty());
        tableStudents.setItems(sortedStudents);
        cmbBirthYearFilter.getItems().setAll(I18n.text("All years"));
        cmbBirthYearFilter.setValue(I18n.text("All years"));

        I18n.setText(lblWelcome, "Welcome, " + Session.getFullName() + "!");

        loadStudents();
        loadDashboard();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyStudentFilters());
        cmbBirthYearFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyStudentFilters());
    }

    public void loadStudents() {
        try {
            allStudents.setAll(studentRepository.findByUser(Session.getUserId(), null));
            refreshBirthYearFilter();
            applyStudentFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshBirthYearFilter() {
        String selected = cmbBirthYearFilter.getValue();
        TreeSet<String> years = new TreeSet<>(Comparator.reverseOrder());
        for (Student student : allStudents) years.add(String.valueOf(student.getYearOfBirth()));
        cmbBirthYearFilter.getItems().setAll(I18n.text("All years"));
        cmbBirthYearFilter.getItems().addAll(years);
        cmbBirthYearFilter.setValue(selected != null && cmbBirthYearFilter.getItems().contains(selected)
                ? selected : I18n.text("All years"));
    }

    private void applyStudentFilters() {
        if (filteredStudents == null) return;
        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        String selectedYear = cmbBirthYearFilter.getValue();
        filteredStudents.setPredicate(student -> {
            if (selectedYear != null && !selectedYear.equals(I18n.text("All years"))
                    && !String.valueOf(student.getYearOfBirth()).equals(selectedYear)) return false;
            if (query.isEmpty()) return true;
            String searchable = (student.getId() + " " + student.getName() + " " + student.getYearOfBirth() + " "
                    + safe(student.getPhone()) + " " + safe(student.getNotes())).toLowerCase(Locale.ROOT);
            return searchable.contains(query);
        });
        I18n.setText(lblStudentResults, filteredStudents.size() + " of " + allStudents.size() + " students");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void loadDashboard() {

        try {
            var summary = dashboardRepository.getSummary(Session.getUserId());
            lblTotalStudents.setText(String.valueOf(summary.getStudents()));
            lblTotalPayments.setText(String.valueOf(summary.getPayments()));
            lblCashIncome.setText(String.format("%.2f ₪", summary.getCash()));
            lblBankIncome.setText(String.format("%.2f ₪", summary.getBank()));
            lblRemaining.setText(String.format("%.2f ₪", summary.getRemaining()));
            lblTotalIncome.setText(String.format("%.2f ₪", summary.getIncome()));
            remainingCard.setStyle(summary.getRemaining().compareTo(BigDecimal.ZERO) <= 0
                    ? "-fx-border-color:#22a06b; -fx-border-radius:12;"
                    : "-fx-border-color:#e79218; -fx-border-radius:12;");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @FXML
    private void handleAdd() {
        openStudentForm(null);
    }

    @FXML
    private void handleEdit() {

        Student student = tableStudents.getSelectionModel().getSelectedItem();

        if (student != null) {
            openStudentForm(student);
        }

    }

    @FXML
    private void handleDelete() {

        Student student = tableStudents.getSelectionModel().getSelectedItem();

        if (student == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a student first.");
            I18n.localize(alert);
            alert.showAndWait();

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Student");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this student?");
        I18n.localize(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            studentRepository.delete(student.getId(), Session.getUserId());

            loadStudents();
            loadDashboard();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to delete student.");
            I18n.localize(alert);
            alert.showAndWait();

        }

    }

    @FXML
    private void handleExportStudentsReport() {

        StudentsReport.generate(
                tableStudents.getScene().getWindow(),
                new ArrayList<>(tableStudents.getItems())
        );

    }

    @FXML
    private void handleExportStudentsCsv() {
        CsvExporter.exportStudents(tableStudents.getScene().getWindow(), new ArrayList<>(tableStudents.getItems()));
    }

    @FXML
    private void handleExportPaymentsCsv() {
        try {
            List<PaymentReportRow> rows = paymentRepository.findReportRowsForUser(Session.getUserId());
            CsvExporter.exportPayments(tableStudents.getScene().getWindow(), rows);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not load payment data for export.");
            I18n.localize(alert);
            alert.showAndWait();
        }
    }
    @FXML
    private void handleExportPaymentsReport() {

        PaymentsReport.generate(
                tableStudents.getScene().getWindow()
        );

    }

    @FXML
    private void handleRefresh() {

        loadStudents();
        loadDashboard();
    }

    @FXML
    private void handleLogout() {

        Session.clear();

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/login.fxml"));

            Stage stage =
                    (Stage) tableStudents.getScene().getWindow();

            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setTitle("Student Management System");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openStudentForm(Student student) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/student_form.fxml"));

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            stage.setTitle(student == null ? "Add Student" : "Edit Student");

            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));

            StudentFormController controller = loader.getController();

            controller.setHomeController(this);
            controller.setStudent(student);

            stage.showAndWait();

            loadStudents();
            loadDashboard();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handlePayments() {

        Student student = tableStudents.getSelectionModel().getSelectedItem();

        if (student == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a student first.");
            I18n.localize(alert);
            alert.showAndWait();

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_history.fxml"));

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            stage.setTitle("Payment History");

            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));

            PaymentHistoryController controller = loader.getController();

            controller.setStudent(student);

            stage.showAndWait();

            loadStudents();
            loadDashboard();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @FXML
    private void handleAttendance() {
        AttendanceController.open(tableStudents.getScene().getWindow());
    }

}
