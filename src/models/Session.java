package models;

public class Session {

    private static int userId;
    private static String currentUsername;

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setFullName(String username) {
        currentUsername = username;
    }

    public static String getFullName() {
        return currentUsername;
    }

    public static void clear() {
        userId = 0;
        currentUsername = null;
    }

}