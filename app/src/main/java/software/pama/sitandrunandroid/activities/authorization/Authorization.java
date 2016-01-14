package software.pama.sitandrunandroid.activities.authorization;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class Authorization {

    private static final String WEB_CLIENT_ID = "719222349392-l7voj898f7hgf9996e0k2l1j2pk8s5hu.apps.googleusercontent.com";

    public static GoogleAccountCredential initializeCredential(Context context) {
        return GoogleAccountCredential.usingAudience(context, "server:client_id:" + WEB_CLIENT_ID);
    }

}
