package models;

import java.sql.Date;
import java.math.BigDecimal;

public class Payment {

    private int id;
    private int studentId;
    private String paymentMethod;
    private BigDecimal cashAmount;
    private BigDecimal bankAmount;
    private BigDecimal totalAmount;
    private BigDecimal paid;
    private BigDecimal remaining;
    private Date paymentDate;
    private String notes;

    public Payment(int id,
                   int studentId,
                   String paymentMethod,
                   BigDecimal cashAmount,
                   BigDecimal bankAmount,
                   BigDecimal totalAmount,
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

        this.paid = cashAmount.add(bankAmount);
        this.remaining = totalAmount.subtract(this.paid);
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

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public BigDecimal getBankAmount() {
        return bankAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getNotes() {
        return notes;
    }

}
