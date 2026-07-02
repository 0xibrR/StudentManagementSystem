package database;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTable {

    public static void createTables() {

        try {

            Connection con = DatabaseConnection.getConnection();

            Statement st = con.createStatement();

            String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL
                );
                """;

            String studentsTable = """
        CREATE TABLE IF NOT EXISTS students (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            yearOfBirth INT NOT NULL,
            phone VARCHAR(20),
            notes TEXT
        );
        """;

            String paymentsTable = """
        CREATE TABLE IF NOT EXISTS payments (
            id INT AUTO_INCREMENT PRIMARY KEY,
            student_id INT NOT NULL,
            payment_method ENUM('Cash','Bank','Mixed') NOT NULL,
            cash_amount DOUBLE DEFAULT 0,
            bank_amount DOUBLE DEFAULT 0,
            total_amount DOUBLE NOT NULL,
            payment_date DATE NOT NULL,
            notes TEXT,

            CONSTRAINT fk_student
            FOREIGN KEY (student_id)
            REFERENCES students(id)
            ON DELETE CASCADE
        );
        """;

            st.execute(usersTable);
            st.execute(studentsTable);
            st.execute(paymentsTable);

            System.out.println("Tables created successfully.");

            st.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}