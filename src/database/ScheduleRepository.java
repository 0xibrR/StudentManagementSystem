package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Stores effective-dated schedule versions so old attendance keeps its original context. */
public class ScheduleRepository {
    public Set<Integer> findCurrentDays(int studentId, int userId) throws SQLException {
        String sql = """
                SELECT d.day_of_week
                FROM student_schedules s JOIN student_schedule_days d ON d.schedule_id=s.id
                WHERE s.student_id=? AND s.user_id=? AND s.effective_to IS NULL
                ORDER BY d.day_of_week
                """;
        Set<Integer> days = new HashSet<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) days.add(result.getInt(1));
            }
        }
        return days;
    }

    public LocalDate findCurrentEffectiveFrom(int studentId, int userId) throws SQLException {
        String sql = "SELECT effective_from FROM student_schedules WHERE student_id=? AND user_id=? AND effective_to IS NULL ORDER BY effective_from DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getDate(1).toLocalDate() : null;
            }
        }
    }

    public void saveSchedule(int studentId, int userId, int markedBy, LocalDate effectiveFrom,
                             Set<Integer> days) throws SQLException {
        if (days == null || days.isEmpty()) throw new SQLException("Select at least one attendance day.");
        if (effectiveFrom == null) throw new SQLException("Choose when the schedule becomes effective.");
        for (int day : days) if (day < 1 || day > 7) throw new SQLException("Invalid weekday selected.");

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int scheduleId = -1;
                LocalDate currentFrom = null;
                Set<Integer> currentDays = new HashSet<>();
                String currentSql = "SELECT id,effective_from FROM student_schedules WHERE student_id=? AND user_id=? AND effective_to IS NULL ORDER BY effective_from DESC LIMIT 1 FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(currentSql)) {
                    statement.setInt(1, studentId);
                    statement.setInt(2, userId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            scheduleId = result.getInt("id");
                            currentFrom = result.getDate("effective_from").toLocalDate();
                        }
                    }
                }
                if (scheduleId >= 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT day_of_week FROM student_schedule_days WHERE schedule_id=?")) {
                        statement.setInt(1, scheduleId);
                        try (ResultSet result = statement.executeQuery()) {
                            while (result.next()) currentDays.add(result.getInt(1));
                        }
                    }
                }
                if (scheduleId >= 0 && currentDays.equals(days)) {
                    connection.commit();
                    return;
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT 1 FROM students WHERE id=? AND user_id=? FOR UPDATE")) {
                    statement.setInt(1, studentId);
                    statement.setInt(2, userId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Student record is not available for this account.");
                    }
                }

                if (scheduleId >= 0) {
                    LocalDate latestAttendance = null;
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT MAX(attendance_date) FROM attendance WHERE student_id=? AND user_id=?")) {
                        statement.setInt(1, studentId);
                        statement.setInt(2, userId);
                        try (ResultSet result = statement.executeQuery()) {
                            if (result.next() && result.getDate(1) != null) latestAttendance = result.getDate(1).toLocalDate();
                        }
                    }

                    if (!effectiveFrom.isAfter(currentFrom)) {
                        throw new SQLException("A schedule change must start after the current schedule's effective date.");
                    }
                    if (latestAttendance != null && !effectiveFrom.isAfter(latestAttendance)) {
                        throw new SQLException("The new schedule must start after the latest recorded attendance.");
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE student_schedules SET effective_to=? WHERE id=? AND student_id=? AND user_id=?")) {
                        statement.setDate(1, Date.valueOf(effectiveFrom.minusDays(1)));
                        statement.setInt(2, scheduleId);
                        statement.setInt(3, studentId);
                        statement.setInt(4, userId);
                        statement.executeUpdate();
                    }
                }

                int newScheduleId;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO student_schedules(student_id,user_id,effective_from,created_by) VALUES(?,?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, studentId);
                    statement.setInt(2, userId);
                    statement.setDate(3, Date.valueOf(effectiveFrom));
                    statement.setInt(4, markedBy);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Could not create the schedule record.");
                        newScheduleId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO student_schedule_days(schedule_id,day_of_week) VALUES(?,?)")) {
                    for (int day : days) {
                        statement.setInt(1, newScheduleId);
                        statement.setInt(2, day);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Could not save the weekly schedule.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
