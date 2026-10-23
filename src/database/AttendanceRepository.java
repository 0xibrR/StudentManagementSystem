package database;

import models.AttendanceReportRow;
import models.AttendanceRow;
import models.AttendanceStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {
    public List<AttendanceRow> findScheduledForDate(int userId, LocalDate date) throws SQLException {
        String sql = """
                SELECT st.id, st.name, a.status, a.notes
                FROM student_schedules s
                JOIN student_schedule_days d ON d.schedule_id=s.id
                JOIN students st ON st.id=s.student_id AND st.user_id=s.user_id
                LEFT JOIN attendance a ON a.student_id=st.id AND a.user_id=s.user_id AND a.attendance_date=?
                WHERE s.user_id=? AND s.effective_from<=? AND (s.effective_to IS NULL OR s.effective_to>=?)
                  AND d.day_of_week=DAYOFWEEK(?)
                ORDER BY st.name, st.id
                """;
        List<AttendanceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.setInt(2, userId);
            statement.setDate(3, Date.valueOf(date));
            statement.setDate(4, Date.valueOf(date));
            statement.setDate(5, Date.valueOf(date));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) rows.add(new AttendanceRow(result.getInt("id"), result.getString("name"),
                        status(result.getString("status")), result.getString("notes")));
            }
        }
        return rows;
    }

    public void saveDate(int userId, LocalDate date, List<AttendanceRow> rows) throws SQLException {
        String eligible = """
                SELECT COUNT(*) FROM students st
                JOIN student_schedules s ON s.student_id=st.id AND s.user_id=st.user_id
                JOIN student_schedule_days d ON d.schedule_id=s.id
                WHERE st.id=? AND st.user_id=? AND s.effective_from<=?
                  AND (s.effective_to IS NULL OR s.effective_to>=?) AND d.day_of_week=DAYOFWEEK(?)
                """;
        String upsert = """
                INSERT INTO attendance(student_id,user_id,attendance_date,status,notes,marked_by)
                VALUES(?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE status=VALUES(status),notes=VALUES(notes),marked_by=VALUES(marked_by)
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement validate = connection.prepareStatement(eligible);
                 PreparedStatement save = connection.prepareStatement(upsert)) {
                for (AttendanceRow row : rows) {
                    if (row.getStatus() == null) continue; // Unmarked is deliberately not treated as absent.
                    validate.setInt(1, row.getStudentId());
                    validate.setInt(2, userId);
                    validate.setDate(3, Date.valueOf(date));
                    validate.setDate(4, Date.valueOf(date));
                    validate.setDate(5, Date.valueOf(date));
                    try (ResultSet result = validate.executeQuery()) {
                        if (!result.next() || result.getInt(1) == 0) {
                            throw new SQLException("A student is not scheduled for the selected date.");
                        }
                    }
                    save.setInt(1, row.getStudentId());
                    save.setInt(2, userId);
                    save.setDate(3, Date.valueOf(date));
                    save.setString(4, row.getStatus().getCode());
                    save.setString(5, blankToNull(row.getNotes()));
                    save.setInt(6, userId);
                    save.addBatch();
                }
                save.executeBatch();
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Could not save attendance.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<AttendanceReportRow> findRecordedBetween(int userId, LocalDate start, LocalDate end)
            throws SQLException {
        if (start == null || end == null || end.isBefore(start)) throw new SQLException("Choose a valid date range.");
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (days > 366) throw new SQLException("Export a maximum of one year at a time.");

        String sql = """
                SELECT a.attendance_date, st.id, st.name, a.status, a.notes
                FROM attendance a
                JOIN students st ON st.id=a.student_id AND st.user_id=a.user_id
                WHERE a.user_id=? AND a.attendance_date BETWEEN ? AND ?
                ORDER BY a.attendance_date, st.name, st.id
                """;
        List<AttendanceReportRow> report = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(start));
            statement.setDate(3, Date.valueOf(end));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date date = result.getDate("attendance_date");
                    report.add(new AttendanceReportRow(date.toLocalDate(), result.getInt("id"),
                            result.getString("name"), result.getString("status"), result.getString("notes")));
                }
            }
        }
        return report;
    }

    private AttendanceStatus status(String code) {
        if (code == null) return null;
        return switch (code) {
            case "PRESENT" -> AttendanceStatus.PRESENT;
            case "ABSENT" -> AttendanceStatus.ABSENT;
            case "LATE" -> AttendanceStatus.LATE;
            case "EXCUSED" -> AttendanceStatus.EXCUSED;
            default -> null;
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
