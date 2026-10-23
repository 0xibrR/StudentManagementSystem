package controllers;

import ui.WindowChrome;
import ui.I18n;

import database.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {

    private final UserRepository userRepository = new UserRepository();

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private void initialize() {
        txtUsername.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[A-Za-z0-9._-]{0,50}") ? change : null));
    }

    @FXML
    private void handleSignUp() {

        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

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
        if (!username.matches("[A-Za-z0-9._-]{4,50}")) {
            showAlert("Error", "Username must be 4-50 characters using English letters, numbers, dots, underscores, or hyphens.");
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

        try {
            if (!userRepository.register(fullName, username, password)) {
                showAlert("Error", "Username already exists.");
                return;
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

            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        I18n.localize(alert);

        alert.showAndWait();

    }

}
