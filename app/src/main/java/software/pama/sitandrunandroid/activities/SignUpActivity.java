package software.pama.sitandrunandroid.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.integration.TheIntegrationLayer;

public class SignUpActivity extends Activity {

    private static final String WEB_CLIENT_ID = "719222349392-l7voj898f7hgf9996e0k2l1j2pk8s5hu.apps.googleusercontent.com";
    private static final String PREFERENCES_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final String PREFERENCES_PROFILE_LOGIN = "PROFILE_LOGIN";
    private static final int PICK_ACCOUNT_ACTIVITY = 0;
    private GoogleAccountCredential credential;
    private SharedPreferences preferences;
    private Button signUpButton;
    private EditText loginTextField;
    private TheIntegrationLayer integrationLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        loginTextField = (EditText) findViewById(R.id.login);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        setupLoginWatcher();
        preferences = getSharedPreferences("SitAndRun", MODE_PRIVATE);
        initializeCredential();
        manageAndroidAccounts();
        integrationLayer = TheIntegrationLayer.getInstance().initialize(credential);
        signIn();
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, EnemyPickerActivity.class));
            }
        });
        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, EnemyPickerActivity.class));
            }
        });
//        findViewById(R.id.action_bar_activity_content).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(SignUpActivity.this, EnemyPickerActivity.class));
//            }
//        });
        loginTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginTextField.setHint("");
            }
        });
    }

    private void setupLoginWatcher() {
        loginTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0)
                    signUpButton.setEnabled(false);
                else
                    signUpButton.setEnabled(true);
            }
        });
    }

    private void initializeCredential() {
        credential = GoogleAccountCredential.usingAudience(this,
                "server:client_id:" + WEB_CLIENT_ID);
    }

    private void manageAndroidAccounts() {
        String accountName = preferences.getString(PREFERENCES_ACCOUNT_NAME, null);
        if (alreadySignedIn(accountName)) {
            updateCredential(accountName);
        } else {
            updateAccountInfo();
        }
    }

    private void signIn() {
        try {
            new SignInTask().execute();
        } catch (Exception e) {
            Logger.getLogger("Logger").log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean alreadySignedIn(String accountName) {
        return accountName != null;
    }

    private void updateCredential(String accountName) {
        credential.setSelectedAccountName(accountName);
    }

    private void updateAccountInfo() {
        Account[] googleAccounts = AccountManager.get(this).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (googleAccounts != null && googleAccounts.length > 0) {
            manageAccountName(googleAccounts[0].name);
        } else {
            pickAccount();
        }
    }

    private void pickAccount() {
        Intent googleAccountPicker = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
        startActivityForResult(googleAccountPicker, PICK_ACCOUNT_ACTIVITY);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_ACCOUNT_ACTIVITY:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        manageAccountName(accountName);
                        Toast.makeText(this, "Account Name = " + accountName, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void manageAccountName(String accountName) {
        updateCredential(accountName);
        storeInPreferences(accountName);
    }

    private void storeInPreferences(String accountName) {
        preferences
            .edit()
            .putString(PREFERENCES_ACCOUNT_NAME, accountName)
            .commit();
    }

    /**
     * On click action for Sign Up Button
     */
    public void signUp(View view) {
        // jeśli login jest zajęty to dajemy taką informację, robimy jakieś drganie ekranu

        // jeśli login nie jest zajęty, wyświetlamy informację o udanym utworzeniu konta (jakaś zielona otoczka wokół ekranu)
        try {
            new SignUpTask().execute(loginTextField.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * On click action for Confirm Login Button
     */
    public void confirmLogin(View view) {
        startEnemyPickerActivity();
    }

    private void startEnemyPickerActivity() {
        Intent intent = new Intent(this, EnemyPickerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_authentication, menu);
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

    private class SignUpTask extends AsyncTask<String, Void, Profile> {

        @Override
        protected Profile doInBackground(String... params) {
            try {
                Profile profile = integrationLayer.signUp(params[0]);
                Logger.getAnonymousLogger().log(Level.INFO, "Login: " + profile.getLogin());
                return profile;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile profile) {
            findViewById(R.id.signUpInfoText);
            TextView signUpInfoText = (TextView) findViewById(R.id.signUpInfoText);
            if (profile == null)
                signUpInfoText.setText("Nazwa jest zajęta. Podaj inny login.");
            else {
                signUpInfoText.setText("Konto założone pomyślnie z loginem " + profile.getLogin());
                storeProfile(profile);
            }
        }
    }

    private class SignInTask extends AsyncTask<Void, Void, Profile> {

        @Override
        protected Profile doInBackground(Void... params) {
            try {
                 Profile profile = integrationLayer.signIn();
                if (profile != null) {
                    // zapisać profil w jakiś sposób
                    storeProfile(profile);
                    startEnemyPickerActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            return null;
        }

    }

    private void storeProfile(Profile profile) {
        preferences
            .edit()
            .putString(PREFERENCES_PROFILE_LOGIN, profile.getLogin())
            .commit();
    }

}
