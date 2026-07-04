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

        // Validate Empty Fields
        if (fullName.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            showAlert("Error", "Please fill all fields.");
            return;
        }

        // Validate Full Name
        if (fullName.length() < 2) {

            showAlert("Error", "Full name must be at least 2 characters.");
            return;
        }

        // Validate Username
        if (username.length() < 4) {

            showAlert("Error", "Username must be at least 4 characters.");
            return;
        }

        // Validate Password
        if (password.length() < 6) {

            showAlert("Error", "Password must be at least 6 characters.");
            return;
        }

        // Confirm Password
        if (!password.equals(confirmPassword)) {

            showAlert("Error", "Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check Username
            String checkSql = "SELECT 1 FROM users WHERE username = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, username);

                try (ResultSet rs = checkStmt.executeQuery()) {

                    if (rs.next()) {

                        showAlert("Error", "Username already exists.");
                        return;

                    }

                }

            }

            // Create Account
            String insertSql =
                    "INSERT INTO users(full_name, username, password) VALUES(?,?,?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                stmt.setString(1, fullName);
                stmt.setString(2, username);
                stmt.setString(3, password);

                stmt.executeUpdate();

            }

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