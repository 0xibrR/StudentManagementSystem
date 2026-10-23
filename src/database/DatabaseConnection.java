package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_management";

    public static Connection getConnection() throws SQLException {

        Map<String, String> env = System.getenv();
        String url = env.getOrDefault("DB_URL", DEFAULT_URL);
        String user = env.get("DB_USER");
        String password = env.get("DB_PASSWORD");

        if (user == null || password == null) {
            throw new IllegalStateException(
                    "Database configuration is missing. Set DB_USER and DB_PASSWORD " +
                            "environment variables (DB_URL is optional)."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }
}
