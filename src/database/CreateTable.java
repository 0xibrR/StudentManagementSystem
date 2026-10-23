package database;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTable {

    public static void createTables() {

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {

            // ==========================
            // Users Table
            // ==========================

            String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL
                );
                """;

            // ==========================
            // Students Table
            // ==========================

            String studentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    yearOfBirth INT NOT NULL,
                    phone VARCHAR(20),
                    enrollment_date DATE,
                    notes TEXT,

                    INDEX idx_user (user_id),

                    CONSTRAINT fk_students_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                );
                """;

            // ==========================
            // Payments Table
            // ==========================

            String paymentsTable = """
                CREATE TABLE IF NOT EXISTS payments (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id INT NOT NULL,
                    payment_method VARCHAR(20) NOT NULL,
                    cash_amount DECIMAL(12,2) NOT NULL,
                    bank_amount DECIMAL(12,2) NOT NULL,
                    total_amount DECIMAL(12,2) NOT NULL,
                    payment_date DATE NOT NULL,
                    notes TEXT,

                    INDEX idx_student (student_id),

                    CONSTRAINT fk_payments_student
                        FOREIGN KEY (student_id)
                        REFERENCES students(id)
                        ON DELETE CASCADE
                );
                """;

            st.execute(usersTable);
            st.execute(studentsTable);
            st.execute(paymentsTable);

            st.execute("""
                CREATE TABLE IF NOT EXISTS student_schedules (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id INT NOT NULL,
                    user_id INT NOT NULL,
                    effective_from DATE NOT NULL,
                    effective_to DATE NULL,
                    created_by INT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_schedule_student_period (student_id, effective_from, effective_to),
                    CONSTRAINT fk_schedule_student FOREIGN KEY (student_id)
                        REFERENCES students(id) ON DELETE CASCADE,
                    CONSTRAINT fk_schedule_user FOREIGN KEY (user_id)
                        REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_schedule_creator FOREIGN KEY (created_by)
                        REFERENCES users(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS student_schedule_days (
                    schedule_id INT NOT NULL,
                    day_of_week TINYINT NOT NULL,
                    PRIMARY KEY (schedule_id, day_of_week),
                    CONSTRAINT chk_schedule_day CHECK (day_of_week BETWEEN 1 AND 7),
                    CONSTRAINT fk_schedule_day_schedule FOREIGN KEY (schedule_id)
                        REFERENCES student_schedules(id) ON DELETE CASCADE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id INT NOT NULL,
                    user_id INT NOT NULL,
                    attendance_date DATE NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    notes VARCHAR(500),
                    marked_by INT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uq_attendance_student_date (student_id, attendance_date),
                    INDEX idx_attendance_user_date (user_id, attendance_date),
                    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
                    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id)
                        REFERENCES students(id) ON DELETE CASCADE,
                    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id)
                        REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_attendance_marker FOREIGN KEY (marked_by)
                        REFERENCES users(id)
                )
                """);

            // Migrate existing DOUBLE columns to exact two-decimal money values.
            st.execute("ALTER TABLE payments MODIFY cash_amount DECIMAL(12,2) NOT NULL");
            st.execute("ALTER TABLE payments MODIFY bank_amount DECIMAL(12,2) NOT NULL");
            st.execute("ALTER TABLE payments MODIFY total_amount DECIMAL(12,2) NOT NULL");

            System.out.println("Tables created successfully.");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
