package models;

import java.math.BigDecimal;

public class DashboardSummary {
    private final int students;
    private final int payments;
    private final BigDecimal cash;
    private final BigDecimal bank;
    private final BigDecimal remaining;
    private final BigDecimal income;

    public DashboardSummary(int students, int payments, BigDecimal cash,
                            BigDecimal bank, BigDecimal remaining, BigDecimal income) {
        this.students = students;
        this.payments = payments;
        this.cash = cash;
        this.bank = bank;
        this.remaining = remaining;
        this.income = income;
    }

    public int getStudents() { return students; }
    public int getPayments() { return payments; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getBank() { return bank; }
    public BigDecimal getRemaining() { return remaining; }
    public BigDecimal getIncome() { return income; }
}
