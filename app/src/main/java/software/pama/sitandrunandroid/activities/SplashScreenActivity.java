package software.pama.sitandrunandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.activities.authorization.Authorization;
import software.pama.sitandrunandroid.activities.preferences.PreferencesUtils;
import software.pama.sitandrunandroid.integration.TheIntegrationLayer;

public class SplashScreenActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    private SharedPreferences preferences;
    private GoogleAccountCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        preferences = PreferencesUtils.getInstance(this);
        credential = Authorization.initializeCredential(getApplicationContext());
        tryToSignIn();
    }

    private void tryToSignIn() {
        String accountName = PreferencesUtils.getAccountName(preferences);
        if (accountName != null) {
            credential.setSelectedAccountName(accountName);
            signIn();
        } else {
            prepareNextScreen();
        }
    }

    private void signIn() {
        try {
            new SignInTask().execute();
        } catch (Exception e) {
            Logger.getLogger("Logger").log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void prepareNextScreen() {
    /* New Handler to start the Menu-Activity
     * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private class SignInTask extends AsyncTask<Void, Void, Profile> {

        @Override
        protected Profile doInBackground(Void... params) {
            Profile profile = null;
            try {
                profile = TheIntegrationLayer.getInstance().initialize(credential).signIn();
                if (profile != null) {
                    PreferencesUtils.storeLogin(preferences, profile.getLogin());
                    startEnemyPickerActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            return profile;
        }

        @Override
        protected void onPostExecute(Profile profile) {
            super.onPostExecute(profile);
            if (profile == null)
                displayError();
        }
    }

    private void startEnemyPickerActivity() {
        Intent intent = new Intent(this, EnemyPickerActivity.class);
        startActivity(intent);
    }

    private void displayError() {
        Toast.makeText(this, "Couldn't connect with server", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
