package controllers;

import ui.WindowChrome;
import ui.I18n;

import database.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Session;
import models.User;

import java.io.IOException;

public class LoginController {

    private final UserRepository userRepository = new UserRepository();

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void handleLogin() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Error", "Please enter username and password.");
            return;
        }

        try {
            User user = userRepository.authenticate(username, password);
            if (user == null) {
                showError("Login Failed", "Invalid username or password.");
                return;
            }

            Session.signIn(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));
            stage.setTitle("Student Management System");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", "Could not connect to the database. Check your database configuration.");
        }
    }

    @FXML
    private void goToSignUp() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/signup.fxml"));

            Stage stage = (Stage) txtUsername.getScene().getWindow();

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

    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        I18n.localize(alert);

        alert.showAndWait();

    }

}
