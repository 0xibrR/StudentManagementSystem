package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Payment;
import models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentHistoryController {

    @FXML
    private Label lblStudent;

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

        loadPayments();

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

        if (confirm.showAndWait().get() != ButtonType.OK) {
            return;
        }

        String sql = "DELETE FROM payments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedPayment.getId());

            ps.executeUpdate();

            loadPayments();

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
    private void handleClose() {

        Stage stage = (Stage) tablePayments.getScene().getWindow();

        stage.close();

    }

}