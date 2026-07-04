package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Session;
import models.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HomeController {

    @FXML
    private TableView<Student> tableStudents;

    @FXML
    private TextField txtSearch;

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

    private ObservableList<Student> studentsList;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("yearOfBirth"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEnrollmentDate.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        lblWelcome.setText("Welcome, " + Session.getFullName() + "!");

        loadStudents();
        loadDashboard();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchStudents(newValue);
        });
    }

    public void loadStudents() {

        studentsList = FXCollections.observableArrayList();

        String sql = """
                SELECT *
                FROM students
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Student student = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("yearOfBirth"),
                        rs.getString("phone"),
                        rs.getDate("enrollment_date"),
                        rs.getString("notes")
                );

                studentsList.add(student);

            }

            tableStudents.setItems(studentsList);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void loadDashboard() {

        try (Connection conn = DatabaseConnection.getConnection()) {

            // ==========================
            // Total Students
            // ==========================

            String sqlStudents = """
                    SELECT COUNT(*)
                    FROM students
                    WHERE user_id = ?
                    """;

            PreparedStatement psStudents = conn.prepareStatement(sqlStudents);
            psStudents.setInt(1, Session.getUserId());

            ResultSet rsStudents = psStudents.executeQuery();

            if (rsStudents.next()) {
                lblTotalStudents.setText(String.valueOf(rsStudents.getInt(1)));
            }

            // ==========================
            // Total Payments
            // ==========================

            String sqlPayments = """
                    SELECT COUNT(*)
                    FROM payments p
                    JOIN students s
                    ON p.student_id = s.id
                    WHERE s.user_id = ?
                    """;

            PreparedStatement psPayments = conn.prepareStatement(sqlPayments);
            psPayments.setInt(1, Session.getUserId());

            ResultSet rsPayments = psPayments.executeQuery();

            if (rsPayments.next()) {
                lblTotalPayments.setText(String.valueOf(rsPayments.getInt(1)));
            }

            // ==========================
            // Cash Income
            // ==========================

            String sqlCash = """
                    SELECT IFNULL(SUM(cash_amount),0)
                    FROM payments p
                    JOIN students s
                    ON p.student_id = s.id
                    WHERE s.user_id = ?
                    """;

            PreparedStatement psCash = conn.prepareStatement(sqlCash);
            psCash.setInt(1, Session.getUserId());

            ResultSet rsCash = psCash.executeQuery();

            if (rsCash.next()) {
                lblCashIncome.setText(
                        String.format("%.2f ₪", rsCash.getDouble(1))
                );
            }

            // ==========================
            // Bank Income
            // ==========================

            String sqlBank = """
                    SELECT IFNULL(SUM(bank_amount),0)
                    FROM payments p
                    JOIN students s
                    ON p.student_id = s.id
                    WHERE s.user_id = ?
                    """;

            PreparedStatement psBank = conn.prepareStatement(sqlBank);
            psBank.setInt(1, Session.getUserId());

            ResultSet rsBank = psBank.executeQuery();

            if (rsBank.next()) {
                lblBankIncome.setText(
                        String.format("%.2f ₪", rsBank.getDouble(1))
                );
            }

            // ==========================
            // Remaining
            // ==========================

            String sqlRemaining = """
                    SELECT IFNULL(
                        SUM(total_amount - (cash_amount + bank_amount)),
                        0
                    )
                    FROM payments p
                    JOIN students s
                    ON p.student_id = s.id
                    WHERE s.user_id = ?
                    """;

            PreparedStatement psRemaining = conn.prepareStatement(sqlRemaining);

            psRemaining.setInt(1, Session.getUserId());

            ResultSet rsRemaining = psRemaining.executeQuery();

            if (rsRemaining.next()) {

                double remaining = rsRemaining.getDouble(1);

                lblRemaining.setText(
                        String.format("%.2f ₪", remaining)
                );

                if (remaining <= 0) {

                    remainingCard.setStyle("""
                            -fx-background-color:#4CAF50;
                            -fx-background-radius:10;
                            -fx-padding:10;
                            """);

                } else {

                    remainingCard.setStyle("""
                            -fx-background-color:#FF9800;
                            -fx-background-radius:10;
                            -fx-padding:10;
                            """);

                }

            }

            // ==========================
            // Total Income
            // ==========================

            String sqlTotalIncome = """
                    SELECT IFNULL(
                        SUM(cash_amount + bank_amount),
                        0
                    )
                    FROM payments p
                    JOIN students s
                    ON p.student_id = s.id
                    WHERE s.user_id = ?
                    """;

            PreparedStatement psTotalIncome = conn.prepareStatement(sqlTotalIncome);
            psTotalIncome.setInt(1, Session.getUserId());

            ResultSet rsTotalIncome = psTotalIncome.executeQuery();

            if (rsTotalIncome.next()) {
                lblTotalIncome.setText(
                        String.format("%.2f ₪", rsTotalIncome.getDouble(1))
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void searchStudents(String keyword) {

        studentsList = FXCollections.observableArrayList();

        String sql = """
                SELECT *
                FROM students
                WHERE user_id = ?
                AND (
                    name LIKE ?
                    OR phone LIKE ?
                    OR CAST(yearOfBirth AS CHAR) LIKE ?
                )
                ORDER BY id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setInt(1, Session.getUserId());
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Student student = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("yearOfBirth"),
                        rs.getString("phone"),
                        rs.getDate("enrollment_date"),
                        rs.getString("notes")
                );

                studentsList.add(student);

            }

            tableStudents.setItems(studentsList);

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
            alert.showAndWait();

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Student");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this student?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String sql = """
                DELETE FROM students
                WHERE id = ?
                AND user_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getId());
            ps.setInt(2, Session.getUserId());

            ps.executeUpdate();

            loadStudents();
            loadDashboard();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to delete student.");
            alert.showAndWait();

        }

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

            stage.setScene(new Scene(loader.load()));
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

            stage.setTitle(student == null ? "Add Student" : "Edit Student");

            stage.setScene(new Scene(loader.load()));

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
            alert.showAndWait();

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_history.fxml"));

            Stage stage = new Stage();

            stage.setTitle("Payment History");

            stage.setScene(new Scene(loader.load()));

            PaymentHistoryController controller = loader.getController();

            controller.setStudent(student);

            stage.showAndWait();

            loadStudents();
            loadDashboard();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}