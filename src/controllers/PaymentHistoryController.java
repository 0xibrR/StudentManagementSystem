package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Payment;
import models.Student;
import reports.StudentPaymentReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentHistoryController {

    @FXML
    private TableView<Payment> tablePayments;

    @FXML
    private TableColumn<Payment, java.sql.Date> colDate;

    @FXML
    private TableColumn<Payment, String> colMethod;

    @FXML
    private TableColumn<Payment, Double> colAmountDue;

    @FXML
    private TableColumn<Payment, Double> colCash;

    @FXML
    private TableColumn<Payment, Double> colBank;

    @FXML
    private TableColumn<Payment, Double> colPaid;

    @FXML
    private TableColumn<Payment, Double> colRemaining;

    @FXML
    private TableColumn<Payment, String> colNotes;

    @FXML
    private Label lblStudent;

    @FXML
    private Label lblPhone;

    @FXML
    private Label lblRegistrationDate;

    @FXML
    private Label lblPaymentsCount;

    @FXML
    private Label lblCashTotal;

    @FXML
    private Label lblBankTotal;

    @FXML
    private Label lblTotalPaid;

    @FXML
    private Label lblRemainingTotal;

    @FXML
    private VBox remainingCard;

    private Student student;

    private ObservableList<Payment> paymentList;

    @FXML
    public void initialize() {

        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colAmountDue.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colCash.setCellValueFactory(new PropertyValueFactory<>("cashAmount"));
        colBank.setCellValueFactory(new PropertyValueFactory<>("bankAmount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

    }

    public void setStudent(Student student) {

        this.student = student;

        lblStudent.setText(student.getName());
        lblPhone.setText(student.getPhone());
        lblRegistrationDate.setText(student.getEnrollmentDate().toString());

        loadPayments();
        loadStatistics();

    }

    private void loadStatistics() {

        String sql = """
                SELECT
                    COUNT(*) AS payments_count,
                    IFNULL(SUM(cash_amount),0) AS cash_total,
                    IFNULL(SUM(bank_amount),0) AS bank_total,
                    IFNULL(SUM(cash_amount + bank_amount),0) AS paid_total,
                    IFNULL(SUM(total_amount - (cash_amount + bank_amount)),0) AS remaining_total
                FROM payments
                WHERE student_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                lblPaymentsCount.setText(
                        String.valueOf(rs.getInt("payments_count"))
                );

                lblCashTotal.setText(
                        String.format("%.2f ₪", rs.getDouble("cash_total"))
                );

                lblBankTotal.setText(
                        String.format("%.2f ₪", rs.getDouble("bank_total"))
                );

                lblTotalPaid.setText(
                        String.format("%.2f ₪", rs.getDouble("paid_total"))
                );

                double remaining = rs.getDouble("remaining_total");

                lblRemainingTotal.setText(
                        String.format("%.2f ₪", remaining)
                );

                if (remaining <= 0) {

                    remainingCard.setStyle("""
                                -fx-background-color:#4CAF50;
                                -fx-background-radius:10;
                                -fx-padding:10;
                            """);

                } else {

                    remainingCard.setStyle("""
                                -fx-background-color:#F57C00;
                                -fx-background-radius:10;
                                -fx-padding:10;
                            """);

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void loadPayments() {

        paymentList = FXCollections.observableArrayList();

        String sql = """
                SELECT *
                FROM payments
                WHERE student_id = ?
                ORDER BY payment_date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                paymentList.add(new Payment(

                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("payment_method"),
                        rs.getDouble("cash_amount"),
                        rs.getDouble("bank_amount"),
                        rs.getDouble("total_amount"),
                        rs.getDate("payment_date"),
                        rs.getString("notes")

                ));

            }

            tablePayments.setItems(paymentList);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @FXML
    private void handleAddPayment() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_form.fxml"));

            Stage stage = new Stage();

            stage.setTitle("Add Payment");

            stage.setScene(new Scene(loader.load()));

            PaymentController controller = loader.getController();

            controller.setStudent(student);

            stage.showAndWait();

            loadPayments();
            loadStatistics();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @FXML
    private void handleEdit() {

        Payment selectedPayment = tablePayments.getSelectionModel().getSelectedItem();

        if (selectedPayment == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a payment first.");
            alert.showAndWait();

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_form.fxml"));

            Stage stage = new Stage();

            stage.setTitle("Edit Payment");

            stage.setScene(new Scene(loader.load()));

            PaymentController controller = loader.getController();

            controller.setStudent(student);

            controller.setPayment(selectedPayment);

            stage.showAndWait();

            loadPayments();
            loadStatistics();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @FXML
    private void handleDelete() {

        Payment selectedPayment = tablePayments.getSelectionModel().getSelectedItem();

        if (selectedPayment == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a payment first.");
            alert.showAndWait();

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Payment");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this payment?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String sql = """
            DELETE FROM payments
            WHERE id = ?
            AND student_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedPayment.getId());
            ps.setInt(2, student.getId());

            ps.executeUpdate();

            loadPayments();
            loadStatistics();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("Payment deleted successfully.");
            success.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Database Error");
            error.setHeaderText(null);
            error.setContentText(e.getMessage());
            error.showAndWait();

        }

    }

    @FXML
    private void handleExportStudentReport() {

        StudentPaymentReport.generate(
                tablePayments.getScene().getWindow(),
                student
        );

    }

    @FXML
    private void handleClose() {

        Stage stage = (Stage) tablePayments.getScene().getWindow();

        stage.close();

    }

}