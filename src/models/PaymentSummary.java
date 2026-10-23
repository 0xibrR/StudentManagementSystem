package models;

import java.math.BigDecimal;

public class PaymentSummary {
    private final int count;
    private final BigDecimal cash;
    private final BigDecimal bank;
    private final BigDecimal paid;
    private final BigDecimal remaining;

    public PaymentSummary(int count, BigDecimal cash, BigDecimal bank,
                          BigDecimal paid, BigDecimal remaining) {
        this.count = count;
        this.cash = cash;
        this.bank = bank;
        this.paid = paid;
        this.remaining = remaining;
    }

    public int getCount() { return count; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getBank() { return bank; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getRemaining() { return remaining; }
}
