package models;

import java.sql.Date;

public class Payment {

    private int id;
    private int studentId;
    private String paymentMethod;
    private double cashAmount;
    private double bankAmount;
    private double totalAmount;
    private double paid;
    private double remaining;
    private Date paymentDate;
    private String notes;

    public Payment(int id,
                   int studentId,
                   String paymentMethod,
                   double cashAmount,
                   double bankAmount,
                   double totalAmount,
                   Date paymentDate,
                   String notes) {

        this.id = id;
        this.studentId = studentId;
        this.paymentMethod = paymentMethod;
        this.cashAmount = cashAmount;
        this.bankAmount = bankAmount;
        this.totalAmount = totalAmount;
        this.paymentDate = paymentDate;
        this.notes = notes;

        this.paid = cashAmount + bankAmount;
        this.remaining = totalAmount - this.paid;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getCashAmount() {
        return cashAmount;
    }

    public double getBankAmount() {
        return bankAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPaid() {
        return paid;
    }

    public double getRemaining() {
        return remaining;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getNotes() {
        return notes;
    }

}