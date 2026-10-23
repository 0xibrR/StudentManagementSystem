package database;

import models.Payment;
import models.PaymentSummary;
import models.PaymentReportRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {
    public List<Payment> findByStudent(int studentId, int userId) throws SQLException {
        String sql = "SELECT p.* FROM payments p JOIN students s ON p.student_id=s.id " +
                "WHERE p.student_id=? AND s.user_id=? ORDER BY p.payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) payments.add(mapPayment(rs));
            }
        }
        return payments;
    }

    public PaymentSummary getSummary(int studentId, int userId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS payment_count,
                       IFNULL(SUM(cash_amount),0) AS cash_total,
                       IFNULL(SUM(bank_amount),0) AS bank_total,
                       IFNULL(SUM(cash_amount + bank_amount),0) AS paid_total,
                       IFNULL(SUM(total_amount - (cash_amount + bank_amount)),0) AS remaining_total
                FROM payments p JOIN students s ON p.student_id=s.id
                WHERE p.student_id=? AND s.user_id=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return new PaymentSummary(rs.getInt("payment_count"), rs.getBigDecimal("cash_total"),
                        rs.getBigDecimal("bank_total"), rs.getBigDecimal("paid_total"),
                        rs.getBigDecimal("remaining_total"));
            }
        }
    }

    public List<PaymentReportRow> findReportRowsForStudent(int studentId, int userId) throws SQLException {
        String sql = "SELECT p.payment_date,s.name AS student_name,p.payment_method," +
                "p.cash_amount,p.bank_amount,p.total_amount,p.notes " +
                "FROM payments p JOIN students s ON p.student_id=s.id " +
                "WHERE p.student_id=? AND s.user_id=? ORDER BY p.payment_date DESC";
        return findReportRows(sql, studentId, userId);
    }

    public List<PaymentReportRow> findReportRowsForUser(int userId) throws SQLException {
        String sql = "SELECT p.payment_date,s.name AS student_name,p.payment_method," +
                "p.cash_amount,p.bank_amount,p.total_amount,p.notes " +
                "FROM payments p JOIN students s ON p.student_id=s.id " +
                "WHERE s.user_id=? ORDER BY p.payment_date DESC";
        return findReportRows(sql, userId, null);
    }

    private List<PaymentReportRow> findReportRows(String sql, int id, Integer secondId) throws SQLException {
        List<PaymentReportRow> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (secondId != null) statement.setInt(2, secondId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new PaymentReportRow(rs.getDate("payment_date"),
                            rs.getString("student_name"),
                            rs.getString("payment_method"), rs.getBigDecimal("cash_amount"),
                            rs.getBigDecimal("bank_amount"), rs.getBigDecimal("total_amount"),
                            rs.getString("notes")));
                }
            }
        }
        return rows;
    }

    public boolean save(Payment payment, int userId) throws SQLException {
        boolean insert = payment.getId() == 0;
        String sql = insert
                ? "INSERT INTO payments(student_id,payment_method,cash_amount,bank_amount,total_amount,payment_date,notes) " +
                  "SELECT ?,?,?,?,?,?,? FROM students WHERE id=? AND user_id=?"
                : "UPDATE payments p JOIN students s ON p.student_id=s.id " +
                  "SET p.payment_method=?,p.cash_amount=?,p.bank_amount=?,p.total_amount=?,p.notes=? " +
                  "WHERE p.id=? AND p.student_id=? AND s.user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            if (insert) {
                statement.setInt(1, payment.getStudentId());
                statement.setString(2, payment.getPaymentMethod());
                statement.setBigDecimal(3, payment.getCashAmount());
                statement.setBigDecimal(4, payment.getBankAmount());
                statement.setBigDecimal(5, payment.getTotalAmount());
                statement.setDate(6, payment.getPaymentDate());
                statement.setString(7, payment.getNotes());
                statement.setInt(8, payment.getStudentId());
                statement.setInt(9, userId);
            } else {
                statement.setString(1, payment.getPaymentMethod());
                statement.setBigDecimal(2, payment.getCashAmount());
                statement.setBigDecimal(3, payment.getBankAmount());
                statement.setBigDecimal(4, payment.getTotalAmount());
                statement.setString(5, payment.getNotes());
                statement.setInt(6, payment.getId());
                statement.setInt(7, payment.getStudentId());
                statement.setInt(8, userId);
            }
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int paymentId, int studentId, int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "DELETE p FROM payments p JOIN students s ON p.student_id=s.id " +
                             "WHERE p.id=? AND p.student_id=? AND s.user_id=?")) {
            statement.setInt(1, paymentId);
            statement.setInt(2, studentId);
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(rs.getInt("id"), rs.getInt("student_id"), rs.getString("payment_method"),
                rs.getBigDecimal("cash_amount"), rs.getBigDecimal("bank_amount"), rs.getBigDecimal("total_amount"),
                rs.getDate("payment_date"), rs.getString("notes"));
    }
}
