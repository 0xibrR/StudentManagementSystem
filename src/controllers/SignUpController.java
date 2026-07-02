package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private void handleSignUp() {

        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        if (fullName.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            showAlert("Error", "Please fill all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {

            showAlert("Error", "Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // التحقق من وجود اسم المستخدم
            String checkSql = "SELECT id FROM users WHERE username = ?";

            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {

                showAlert("Error", "Username already exists.");
                return;
            }

            // إنشاء الحساب
            String insertSql =
                    "INSERT INTO users(full_name, username, password) VALUES(?,?,?)";

            PreparedStatement stmt = conn.prepareStatement(insertSql);

            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, password);

            stmt.executeUpdate();

            showAlert("Success", "Account created successfully.");

            goToLogin();

        } catch (Exception e) {

            e.printStackTrace();
            showAlert("Database Error", e.getMessage());

        }

    }

    @FXML
    private void goToLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/login.fxml"));

            Stage stage = (Stage) txtFullName.getScene().getWindow();

            stage.setScene(new Scene(loader.load()));

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }

}