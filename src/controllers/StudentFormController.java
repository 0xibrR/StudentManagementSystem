package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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

            Date enrollmentDate =
                    Date.valueOf(dpEnrollmentDate.getValue());

            Connection conn = DatabaseConnection.getConnection();

            PreparedStatement stmt;

            if (student == null) {

                String sql = """
                    INSERT INTO students
                    (name,
                     yearOfBirth,
                     phone,
                     enrollment_date,
                     notes)
                    VALUES (?, ?, ?, ?, ?)
                    """;

                stmt = conn.prepareStatement(sql);

                stmt.setString(1, name);
                stmt.setInt(2, year);
                stmt.setString(3, phone);
                stmt.setDate(4, enrollmentDate);
                stmt.setString(5, notes);

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
                    """;

                stmt = conn.prepareStatement(sql);

                stmt.setString(1, name);
                stmt.setInt(2, year);
                stmt.setString(3, phone);
                stmt.setDate(4, enrollmentDate);
                stmt.setString(5, notes);
                stmt.setInt(6, student.getId());

            }

            stmt.executeUpdate();

            stmt.close();
            conn.close();

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