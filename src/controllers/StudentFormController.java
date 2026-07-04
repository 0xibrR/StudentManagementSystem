package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Session;
import models.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

public class StudentFormController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtYear;

    @FXML
    private TextField txtPhone;

    @FXML
    private DatePicker dpEnrollmentDate;

    @FXML
    private TextArea txtNotes;

    private Student student;

    private HomeController homeController;

    public void setStudent(Student student) {

        this.student = student;

        if (student != null) {

            txtName.setText(student.getName());
            txtYear.setText(String.valueOf(student.getYearOfBirth()));
            txtPhone.setText(student.getPhone());

            if (student.getEnrollmentDate() != null) {
                dpEnrollmentDate.setValue(student.getEnrollmentDate().toLocalDate());
            }

            txtNotes.setText(student.getNotes());

        }

    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void handleSave() {

        String name = txtName.getText().trim();
        String yearText = txtYear.getText().trim();
        String phone = txtPhone.getText().trim();
        String notes = txtNotes.getText().trim();

        if (name.isEmpty()
                || yearText.isEmpty()
                || dpEnrollmentDate.getValue() == null) {

            showAlert("Error", "Please fill all required fields.");
            return;

        }

        try {

            int year = Integer.parseInt(yearText);

            if (year < 1900 || year > 2100) {

                showAlert("Error", "Please enter a valid year.");
                return;

            }

            Date enrollmentDate = Date.valueOf(dpEnrollmentDate.getValue());

            try (Connection conn = DatabaseConnection.getConnection()) {

                if (student == null) {

                    String sql = """
                        INSERT INTO students
                        (
                            user_id,
                            name,
                            yearOfBirth,
                            phone,
                            enrollment_date,
                            notes
                        )
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setInt(1, Session.getUserId());
                        stmt.setString(2, name);
                        stmt.setInt(3, year);
                        stmt.setString(4, phone);
                        stmt.setDate(5, enrollmentDate);
                        stmt.setString(6, notes);

                        stmt.executeUpdate();

                    }

                } else {

                    String sql = """
                        UPDATE students
                        SET
                            name=?,
                            yearOfBirth=?,
                            phone=?,
                            enrollment_date=?,
                            notes=?
                        WHERE id=?
                        AND user_id=?
                        """;

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, name);
                        stmt.setInt(2, year);
                        stmt.setString(3, phone);
                        stmt.setDate(4, enrollmentDate);
                        stmt.setString(5, notes);
                        stmt.setInt(6, student.getId());
                        stmt.setInt(7, Session.getUserId());

                        stmt.executeUpdate();

                    }

                }

            }

            if (homeController != null) {
                homeController.loadStudents();
            }

            closeWindow();

        } catch (NumberFormatException e) {

            showAlert("Error", "Year of Birth must be numeric.");

        } catch (Exception e) {

            e.printStackTrace();
            showAlert("Database Error", e.getMessage());

        }

    }

    @FXML
    private void handleCancel() {

        closeWindow();

    }

    private void closeWindow() {

        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();

    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }

}