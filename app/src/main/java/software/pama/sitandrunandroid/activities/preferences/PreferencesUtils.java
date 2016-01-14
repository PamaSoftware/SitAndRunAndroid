package software.pama.sitandrunandroid.activities.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    private static final String PREFERENCES_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final String PREFERENCES_PROFILE_LOGIN = "PROFILE_LOGIN";

    private PreferencesUtils() {}

    public static SharedPreferences getInstance(Context context) {
        return context.getSharedPreferences("SitAndRun", Context.MODE_PRIVATE);
    }

    public static String getAccountName(SharedPreferences preferences) {
        return preferences.getString(PREFERENCES_ACCOUNT_NAME, null);
    }

    public static void storeLogin(SharedPreferences preferences, String login) {
        preferences
            .edit()
            .putString(PREFERENCES_PROFILE_LOGIN, login)
            .commit();
    }

    public static void storeAccountName(SharedPreferences preferences, String accountName) {
        preferences
            .edit()
            .putString(PREFERENCES_ACCOUNT_NAME, accountName)
            .commit();
    }

}
