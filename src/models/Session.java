package models;

public class Session {
    private static String currentUsername;

    public static void setFullName(String username) {
        currentUsername = username;
    }

    public static String getFullName() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
    }
}
