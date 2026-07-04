package models;

public class Session {

    private static int userId;
    private static String fullName;

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setFullName(String name) {
        fullName = name;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void clear() {
        userId = 0;
        fullName = null;
    }

}