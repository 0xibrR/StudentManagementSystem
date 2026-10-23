package models;

import java.util.prefs.Preferences;

public class Session {

    private static final String REMEMBERED_USER_ID = "rememberedUserId";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Session.class);
    private static int userId;
    private static String fullName;

    public static int getUserId() {
        return userId;
    }

    public static String getFullName() {
        return fullName;
    }

    public static int getRememberedUserId() {
        return PREFERENCES.getInt(REMEMBERED_USER_ID, 0);
    }

    public static void signIn(User user) {
        userId = user.getId();
        fullName = user.getFullName();
        PREFERENCES.putInt(REMEMBERED_USER_ID, userId);
    }

    public static void clear() {
        userId = 0;
        fullName = null;
        PREFERENCES.remove(REMEMBERED_USER_ID);
    }

}
