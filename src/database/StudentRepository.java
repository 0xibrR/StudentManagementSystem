package database;

import models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {
    public List<Student> findByUser(int userId, String keyword) throws SQLException {
        boolean searching = keyword != null && !keyword.isBlank();
        String sql = "SELECT * FROM students WHERE user_id=?" +
                (searching ? " AND (name LIKE ? OR phone LIKE ? OR CAST(yearOfBirth AS CHAR) LIKE ?)" : "") +
                " ORDER BY id";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            if (searching) {
                String query = "%" + keyword.trim() + "%";
                statement.setString(2, query);
                statement.setString(3, query);
                statement.setString(4, query);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(rs.getInt("id"), rs.getString("name"),
                            rs.getInt("yearOfBirth"), rs.getString("phone"),
                            rs.getDate("enrollment_date"), rs.getString("notes")));
                }
            }
        }
        return students;
    }

    public int save(Student student, int userId) throws SQLException {
        if (student.getId() == 0) {
            String sql = "INSERT INTO students(user_id,name,yearOfBirth,phone,enrollment_date,notes) VALUES(?,?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, userId);
                bindStudent(statement, student, 2);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Could not create student record.");
                    return keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE students SET name=?,yearOfBirth=?,phone=?,enrollment_date=?,notes=? WHERE id=? AND user_id=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql)) {
                bindStudent(statement, student, 1);
                statement.setInt(6, student.getId());
                statement.setInt(7, userId);
                statement.executeUpdate();
                return student.getId();
            }
        }
    }

    private void bindStudent(PreparedStatement statement, Student student, int start) throws SQLException {
        statement.setString(start, student.getName());
        statement.setInt(start + 1, student.getYearOfBirth());
        statement.setString(start + 2, student.getPhone());
        statement.setDate(start + 3, student.getEnrollmentDate());
        statement.setString(start + 4, student.getNotes());
    }

    public boolean delete(int studentId, int userId) throws SQLException {
        String sql = "DELETE FROM students WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }
}
