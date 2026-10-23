package database;

import models.DashboardSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardRepository {
    public DashboardSummary getSummary(int userId) throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT s.id) AS student_count,
                       COUNT(p.id) AS payment_count,
                       IFNULL(SUM(p.cash_amount),0) AS cash_total,
                       IFNULL(SUM(p.bank_amount),0) AS bank_total,
                       IFNULL(SUM(p.total_amount - (p.cash_amount + p.bank_amount)),0) AS remaining_total,
                       IFNULL(SUM(p.cash_amount + p.bank_amount),0) AS income_total
                FROM students s LEFT JOIN payments p ON p.student_id=s.id
                WHERE s.user_id=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return new DashboardSummary(rs.getInt("student_count"), rs.getInt("payment_count"),
                        rs.getBigDecimal("cash_total"), rs.getBigDecimal("bank_total"),
                        rs.getBigDecimal("remaining_total"), rs.getBigDecimal("income_total"));
            }
        }
    }
}
