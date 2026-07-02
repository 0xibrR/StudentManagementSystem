package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Session;
import models.Student;

import java.sql.*;

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
    private TableColumn<Student, String> colNotes;

    @FXML
    private Label lblWelcome;

    private ObservableList<Student> studentsList;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("yearOfBirth"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        lblWelcome.setText("Welcome, " + Session.getFullName() + "!");

        loadStudents();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchStudents(newValue);
        });
    }

    public void loadStudents() {

        studentsList = FXCollections.observableArrayList();

        String sql = "SELECT * FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Student student = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("yearOfBirth"),
                        rs.getString("phone"),
                        rs.getString("notes")
                );

                studentsList.add(student);

            }

            tableStudents.setItems(studentsList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void searchStudents(String keyword) {

        studentsList = FXCollections.observableArrayList();

        String sql = """
            SELECT *
            FROM students
            WHERE
                name LIKE ?
                OR phone LIKE ?
                OR CAST(yearOfBirth AS CHAR) LIKE ?
            ORDER BY id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Student student = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("yearOfBirth"),
                        rs.getString("phone"),
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
            return;
        }

        String sql = "DELETE FROM students WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getId());

            ps.executeUpdate();

            loadStudents();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleRefresh() {

        loadStudents();

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

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}