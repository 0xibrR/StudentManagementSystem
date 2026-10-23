package models;

import java.math.BigDecimal;
import java.sql.Date;

public class PaymentReportRow {
    private final Date paymentDate;
    private final String studentName;
    private final String paymentMethod;
    private final BigDecimal cash;
    private final BigDecimal bank;
    private final BigDecimal total;
    private final String notes;

    public PaymentReportRow(Date paymentDate, String studentName, String paymentMethod,
                            BigDecimal cash, BigDecimal bank, BigDecimal total, String notes) {
        this.paymentDate = paymentDate;
        this.studentName = studentName;
        this.paymentMethod = paymentMethod;
        this.cash = cash;
        this.bank = bank;
        this.total = total;
        this.notes = notes;
    }

    public Date getPaymentDate() { return paymentDate; }
    public String getStudentName() { return studentName; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getBank() { return bank; }
    public BigDecimal getTotal() { return total; }
    public String getNotes() { return notes; }
    public BigDecimal getRemaining() { return total.subtract(cash.add(bank)); }
}
