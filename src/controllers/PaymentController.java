package controllers;

import database.PaymentRepository;
import ui.I18n;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Payment;
import models.Student;
import models.Session;

import java.sql.Date;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PaymentController {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

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
    private final PaymentRepository paymentRepository = new PaymentRepository();

    @FXML
    public void initialize() {

        cmbMethod.setItems(FXCollections.observableArrayList(
                I18n.paymentMethod("Cash"),
                I18n.paymentMethod("Bank"),
                I18n.paymentMethod("Mixed")
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

        cmbMethod.setValue(I18n.paymentMethod(payment.getPaymentMethod()));

        txtCash.setText(String.valueOf(payment.getCashAmount()));

        txtBank.setText(String.valueOf(payment.getBankAmount()));

        txtAmountDue.setText(String.valueOf(payment.getTotalAmount()));

        txtNotes.setText(payment.getNotes());

        calculate();

    }

    private void calculate() {

        try {

            BigDecimal cash = parseAmount(txtCash.getText());

            BigDecimal bank = parseAmount(txtBank.getText());

            BigDecimal amountDue = parseAmount(txtAmountDue.getText());

            BigDecimal paid = cash.add(bank);

            BigDecimal remaining = amountDue.subtract(paid);

            txtPaid.setText(paid.toPlainString());

            txtRemaining.setText(remaining.toPlainString());

        } catch (NumberFormatException | ArithmeticException e) {

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

            BigDecimal cash = parseAmount(txtCash.getText());

            BigDecimal bank = parseAmount(txtBank.getText());

            BigDecimal total = parseAmount(txtAmountDue.getText());

            // ===== Validation =====

            if (cash.compareTo(BigDecimal.ZERO) < 0) {

                showAlert("Error", "Cash Amount cannot be negative.");
                return;

            }

            if (bank.compareTo(BigDecimal.ZERO) < 0) {

                showAlert("Error", "Bank Amount cannot be negative.");
                return;

            }

            if (total.compareTo(BigDecimal.ZERO) <= 0) {

                showAlert("Error", "Amount Due must be greater than zero.");
                return;

            }

            String method = I18n.canonicalPaymentMethod(cmbMethod.getValue());

            if (method.equals("Cash")) {

                if (cash.compareTo(BigDecimal.ZERO) <= 0) {

                    showAlert("Error", "Please enter the Cash Amount.");
                    return;

                }

                if (bank.compareTo(BigDecimal.ZERO) != 0) {

                    showAlert("Error", "Bank Amount must be 0 when payment method is Cash.");
                    return;

                }

            }

            if (method.equals("Bank")) {

                if (bank.compareTo(BigDecimal.ZERO) <= 0) {

                    showAlert("Error", "Please enter the Bank Amount.");
                    return;

                }

                if (cash.compareTo(BigDecimal.ZERO) != 0) {

                    showAlert("Error", "Cash Amount must be 0 when payment method is Bank.");
                    return;

                }

            }

            if (method.equals("Mixed")) {

                if (cash.compareTo(BigDecimal.ZERO) <= 0 || bank.compareTo(BigDecimal.ZERO) <= 0) {

                    showAlert("Error", "Mixed payment requires positive cash and bank amounts.");
                    return;

                }

            }

            if (cash.add(bank).compareTo(total) > 0) {

                showAlert("Error",
                        "Paid amount cannot be greater than Amount Due.");

                return;

            }

            Payment paymentToSave = new Payment(payment == null ? 0 : payment.getId(),
                    student.getId(), method, cash, bank, total,
                    payment == null ? Date.valueOf(LocalDate.now()) : payment.getPaymentDate(),
                    txtNotes.getText());
            if (!paymentRepository.save(paymentToSave, Session.getUserId())) {
                showAlert("Error", "The student or payment record is no longer available.");
                return;
            }

            if (payment == null) {
                showAlert("Success", "Payment added successfully.");
            } else {
                showAlert("Success", "Payment updated successfully.");
            }

            closeWindow();

        } catch (NumberFormatException | ArithmeticException e) {

            showAlert("Error", "Enter valid amounts with no more than 2 decimal places.");

        } catch (Exception e) {

            e.printStackTrace();

            showAlert("Database Error", e.getMessage());

        }

    }

    private BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) return ZERO;
        return new BigDecimal(text.trim()).setScale(2, RoundingMode.UNNECESSARY);
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
        I18n.localize(alert);

        alert.showAndWait();

    }

}
