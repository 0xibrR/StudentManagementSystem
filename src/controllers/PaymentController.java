package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Payment;
import models.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class PaymentController {

    @FXML
    private Label lblStudent;

    @FXML
    private ComboBox<String> cmbMethod;

    @FXML
    private TextField txtCash;

    @FXML
    private TextField txtBank;

    @FXML
    private TextField txtAmountDue;

    @FXML
    private TextField txtPaid;

    @FXML
    private TextField txtRemaining;

    @FXML
    private TextArea txtNotes;

    private Student student;
    private Payment payment;

    @FXML
    public void initialize() {

        cmbMethod.setItems(FXCollections.observableArrayList(
                "Cash",
                "Bank",
                "Mixed"
        ));

        txtCash.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        txtBank.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        txtAmountDue.textProperty().addListener((obs, oldVal, newVal) -> calculate());

    }

    public void setStudent(Student student) {

        this.student = student;

        if (student != null) {
            lblStudent.setText(student.getName());
        }

    }

    public void setPayment(Payment payment) {

        this.payment = payment;

        if (payment == null) {
            return;
        }

        cmbMethod.setValue(payment.getPaymentMethod());

        txtCash.setText(String.valueOf(payment.getCashAmount()));

        txtBank.setText(String.valueOf(payment.getBankAmount()));

        txtAmountDue.setText(String.valueOf(payment.getTotalAmount()));

        txtNotes.setText(payment.getNotes());

        calculate();

    }

    private void calculate() {

        try {

            double cash = txtCash.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtCash.getText());

            double bank = txtBank.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtBank.getText());

            double amountDue = txtAmountDue.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtAmountDue.getText());

            double paid = cash + bank;

            double remaining = amountDue - paid;

            txtPaid.setText(String.format("%.2f", paid));

            txtRemaining.setText(String.format("%.2f", remaining));

        } catch (NumberFormatException e) {

            txtPaid.clear();
            txtRemaining.clear();

        }

    }

    @FXML
    private void handleSave() {

        try {

            if (cmbMethod.getValue() == null) {

                showAlert("Error", "Please select a payment method.");
                return;

            }

            if (txtAmountDue.getText().isBlank()) {

                showAlert("Error", "Please enter Amount Due.");
                return;

            }

            double cash = txtCash.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtCash.getText());

            double bank = txtBank.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtBank.getText());

            double total = Double.parseDouble(txtAmountDue.getText());

            // ===== Validation =====

            if (cash < 0) {

                showAlert("Error", "Cash Amount cannot be negative.");
                return;

            }

            if (bank < 0) {

                showAlert("Error", "Bank Amount cannot be negative.");
                return;

            }

            if (total <= 0) {

                showAlert("Error", "Amount Due must be greater than zero.");
                return;

            }

            String method = cmbMethod.getValue();

            if (method.equals("Cash")) {

                if (cash <= 0) {

                    showAlert("Error", "Please enter the Cash Amount.");
                    return;

                }

                if (bank != 0) {

                    showAlert("Error", "Bank Amount must be 0 when payment method is Cash.");
                    return;

                }

            }

            if (method.equals("Bank")) {

                if (bank <= 0) {

                    showAlert("Error", "Please enter the Bank Amount.");
                    return;

                }

                if (cash != 0) {

                    showAlert("Error", "Cash Amount must be 0 when payment method is Bank.");
                    return;

                }

            }

            if (method.equals("Mixed")) {

                if (cash <= 0 && bank <= 0) {

                    showAlert("Error", "Enter Cash or Bank Amount.");
                    return;

                }

            }

            if ((cash + bank) > total) {

                showAlert("Error",
                        "Paid amount cannot be greater than Amount Due.");

                return;

            }

            Connection con = DatabaseConnection.getConnection();

            PreparedStatement ps;

            if (payment == null) {

                String sql = """
                    INSERT INTO payments
                    (
                        student_id,
                        payment_method,
                        cash_amount,
                        bank_amount,
                        total_amount,
                        payment_date,
                        notes
                    )
                    VALUES (?,?,?,?,?,?,?)
                    """;

                ps = con.prepareStatement(sql);

                ps.setInt(1, student.getId());
                ps.setString(2, method);
                ps.setDouble(3, cash);
                ps.setDouble(4, bank);
                ps.setDouble(5, total);
                ps.setDate(6, Date.valueOf(LocalDate.now()));
                ps.setString(7, txtNotes.getText());

            } else {

                String sql = """
                    UPDATE payments
                    SET
                        payment_method=?,
                        cash_amount=?,
                        bank_amount=?,
                        total_amount=?,
                        notes=?
                    WHERE id=?
                    AND student_id=?
                    """;

                ps = con.prepareStatement(sql);

                ps.setString(1, method);
                ps.setDouble(2, cash);
                ps.setDouble(3, bank);
                ps.setDouble(4, total);
                ps.setString(5, txtNotes.getText());
                ps.setInt(6, payment.getId());
                ps.setInt(7, student.getId());

            }

            ps.executeUpdate();

            ps.close();
            con.close();

            if (payment == null) {
                showAlert("Success", "Payment added successfully.");
            } else {
                showAlert("Success", "Payment updated successfully.");
            }

            closeWindow();

        } catch (NumberFormatException e) {

            showAlert("Error", "Please enter valid numeric values.");

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

        Stage stage = (Stage) lblStudent.getScene().getWindow();

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