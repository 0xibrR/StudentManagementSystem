package controllers;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void handleLogin() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Error", "Please enter username and password.");
            return;
        }

        String sql = "SELECT id, full_name FROM users WHERE username=? AND password=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Session.setUserId(rs.getInt("id"));
                Session.setFullName(rs.getString("full_name"));

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Student Management System");

            } else {

                showError("Login Failed", "Invalid username or password.");

            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    private void goToSignUp() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/signup.fxml"));

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            stage.setScene(new Scene(loader.load()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }

}