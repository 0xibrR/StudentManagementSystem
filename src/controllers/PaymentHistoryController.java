package controllers;

import ui.WindowChrome;
import ui.I18n;

import database.PaymentRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Payment;
import models.PaymentSummary;
import models.Session;
import models.Student;
import reports.StudentPaymentReport;
import reports.CsvExporter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class PaymentHistoryController {

    @FXML
    private TableView<Payment> tablePayments;
    @FXML
    private TextField txtPaymentSearch;
    @FXML
    private ComboBox<String> cmbPaymentMethod;
    @FXML
    private DatePicker dpFrom;
    @FXML
    private DatePicker dpTo;
    @FXML
    private Label lblPaymentResults;

    @FXML
    private TableColumn<Payment, java.sql.Date> colDate;

    @FXML
    private TableColumn<Payment, String> colMethod;

    @FXML
    private TableColumn<Payment, BigDecimal> colAmountDue;

    @FXML
    private TableColumn<Payment, BigDecimal> colCash;

    @FXML
    private TableColumn<Payment, BigDecimal> colBank;

    @FXML
    private TableColumn<Payment, BigDecimal> colPaid;

    @FXML
    private TableColumn<Payment, BigDecimal> colRemaining;

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

    private final ObservableList<Payment> allPayments = FXCollections.observableArrayList();
    private FilteredList<Payment> filteredPayments;
    private SortedList<Payment> sortedPayments;
    private final PaymentRepository paymentRepository = new PaymentRepository();

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

        filteredPayments = new FilteredList<>(allPayments, payment -> true);
        sortedPayments = new SortedList<>(filteredPayments);
        sortedPayments.comparatorProperty().bind(tablePayments.comparatorProperty());
        tablePayments.setItems(sortedPayments);
        cmbPaymentMethod.getItems().setAll(I18n.text("All methods"), I18n.paymentMethod("Cash"),
                I18n.paymentMethod("Bank"), I18n.paymentMethod("Mixed"));
        cmbPaymentMethod.setValue(I18n.text("All methods"));
        txtPaymentSearch.textProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());
        cmbPaymentMethod.valueProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());
        dpFrom.valueProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());
        dpTo.valueProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());

    }

    public void setStudent(Student student) {

        this.student = student;

        lblStudent.setText(student.getName());
        lblPhone.setText(student.getPhone());
        lblRegistrationDate.setText(student.getEnrollmentDate() == null
                ? "" : student.getEnrollmentDate().toString());

        loadPayments();
        loadStatistics();

    }

    private void loadStatistics() {

        try {
            PaymentSummary summary = paymentRepository.getSummary(student.getId(), Session.getUserId());
            lblPaymentsCount.setText(String.valueOf(summary.getCount()));
            lblCashTotal.setText(String.format("%.2f ₪", summary.getCash()));
            lblBankTotal.setText(String.format("%.2f ₪", summary.getBank()));
            lblTotalPaid.setText(String.format("%.2f ₪", summary.getPaid()));
            BigDecimal remaining = summary.getRemaining();
            lblRemainingTotal.setText(String.format("%.2f ₪", remaining));
            remainingCard.setStyle(remaining.compareTo(BigDecimal.ZERO) <= 0
                    ? "-fx-border-color:#22a06b; -fx-border-radius:12;"
                    : "-fx-border-color:#e79218; -fx-border-radius:12;");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void loadPayments() {

        try {
            allPayments.setAll(paymentRepository.findByStudent(student.getId(), Session.getUserId()));
            applyPaymentFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyPaymentFilters() {
        if (filteredPayments == null) return;
        String query = txtPaymentSearch.getText() == null
                ? "" : txtPaymentSearch.getText().trim().toLowerCase(Locale.ROOT);
        String method = cmbPaymentMethod.getValue();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        filteredPayments.setPredicate(payment -> {
            if (method != null && !method.equals(I18n.text("All methods"))
                    && !matchesMethod(I18n.canonicalPaymentMethod(method), payment.getPaymentMethod())) return false;
            LocalDate date = payment.getPaymentDate() == null ? null : payment.getPaymentDate().toLocalDate();
            if (from != null && (date == null || date.isBefore(from))) return false;
            if (to != null && (date == null || date.isAfter(to))) return false;
            if (from != null && to != null && from.isAfter(to)) return false;
            if (query.isEmpty()) return true;
            String searchable = (safe(payment.getPaymentMethod()) + " " + safe(payment.getNotes()) + " "
                    + safe(payment.getPaymentDate()) + " " + safe(payment.getTotalAmount()) + " "
                    + safe(payment.getCashAmount()) + " " + safe(payment.getBankAmount()) + " "
                    + safe(payment.getPaid()) + " " + safe(payment.getRemaining())).toLowerCase(Locale.ROOT);
            return searchable.contains(query);
        });
        I18n.setText(lblPaymentResults, filteredPayments.size() + " of " + allPayments.size() + " payments");
    }

    private boolean matchesMethod(String selectedMethod, String paymentMethod) {
        if (paymentMethod == null) return false;
        if (selectedMethod.equals("Mixed")) {
            return paymentMethod.equalsIgnoreCase("Mixed") || paymentMethod.equalsIgnoreCase("Cash + Bank");
        }
        return selectedMethod.equalsIgnoreCase(paymentMethod);
    }
    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    @FXML
    private void handleClearFilters() {
        txtPaymentSearch.clear();
        cmbPaymentMethod.setValue(I18n.text("All methods"));
        dpFrom.setValue(null);
        dpTo.setValue(null);
    }

    @FXML
    private void handleAddPayment() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_form.fxml"));

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            stage.setTitle("Add Payment");

            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));

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
            I18n.localize(alert);
            alert.showAndWait();

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/payment_form.fxml"));

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            stage.setTitle("Edit Payment");

            stage.setScene(new Scene(WindowChrome.wrap(stage, loader.load())));

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
            I18n.localize(alert);
            alert.showAndWait();

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Payment");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this payment?");
        I18n.localize(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            if (!paymentRepository.delete(selectedPayment.getId(), student.getId(), Session.getUserId())) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Not Found");
                error.setHeaderText(null);
                error.setContentText("The payment record is no longer available.");
                I18n.localize(error);
                error.showAndWait();
                return;
            }

            loadPayments();
            loadStatistics();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("Payment deleted successfully.");
            I18n.localize(success);
            success.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Database Error");
            error.setHeaderText(null);
            error.setContentText(e.getMessage());
            I18n.localize(error);
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
    private void handleExportStudentCsv() {
        CsvExporter.exportStudentPayments(tablePayments.getScene().getWindow(), student,
                new ArrayList<>(tablePayments.getItems()));
    }

    @FXML
    private void handleClose() {

        Stage stage = (Stage) tablePayments.getScene().getWindow();

        stage.close();

    }

}
