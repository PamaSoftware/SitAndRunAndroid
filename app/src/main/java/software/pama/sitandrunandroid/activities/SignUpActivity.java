package software.pama.sitandrunandroid.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import software.pama.sitandrunandroid.activities.authorization.Authorization;
import software.pama.sitandrunandroid.activities.preferences.PreferencesUtils;
import software.pama.sitandrunandroid.integration.IntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayer;
//import software.pama.sitandrunandroid.integration.TheIntegrationLayerMock;

public class SignUpActivity extends Activity {

    private static final int PICK_ACCOUNT_ACTIVITY = 0;
    private GoogleAccountCredential credential;
    private SharedPreferences preferences;
    private Button signUpButton;
    private EditText loginTextField;
    private IntegrationLayer integrationLayer;
    private ProgressDialog progress;
    private boolean signUpError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        loginTextField = (EditText) findViewById(R.id.login);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        preferences = PreferencesUtils.getInstance(this);
        credential = Authorization.initializeCredential(this);
//        integrationLayer = TheIntegrationLayerMock.getInstance();
        integrationLayer = TheIntegrationLayer.getInstance();
        integrationLayer = integrationLayer.initialize(credential);
        updateAccountInfo();
        setupLoginWatcher();
        manageListeners();
        prepareProgressBar();
    }

    private void updateAccountInfo() {
        Account[] googleAccounts = AccountManager.get(this).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (googleAccounts == null || googleAccounts.length == 0 || googleAccounts.length > 1)
            pickAccount();
        else
            manageAccountName(googleAccounts[0].name);

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
                if (s.length() == 0) {
                    signUpButton.setEnabled(false);
                    signUpButton.setTextColor(0x53ff3800);
                }
                else {
                    signUpButton.setEnabled(true);
                    signUpButton.setTextColor(0xbaff3800);
                }
            }
        });
    }

    private void manageListeners() {
        loginTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginTextField.setHint("");
            }
        });
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
        credential.setSelectedAccountName(accountName);
        PreferencesUtils.storeAccountName(preferences, accountName);
    }

    /**
     * On click action for Sign Up Button
     */
    public void signUp(View view) {
        // jeśli login jest zajęty to dajemy taką informację, robimy jakieś drganie ekranu
        // jeśli login nie jest zajęty, wyświetlamy informację o udanym utworzeniu konta (jakaś zielona otoczka wokół ekranu)
        try {
            new SignUpTask().execute(loginTextField.getText().toString());
            progress.show();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void prepareProgressBar() {
        progress = new ProgressDialog(this);
        progress.setTitle("Wait");
        progress.setMessage("Signing In...");
    }

    private class SignUpTask extends AsyncTask<String, Void, Profile> {

        @Override
        protected Profile doInBackground(String... params) {
            try {
                Profile profile = integrationLayer.signUp(params[0]);
                if (profile != null)
                    Logger.getAnonymousLogger().log(Level.INFO, "Login: " + profile.getLogin());
                return profile;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
                signUpError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile profile) {
            findViewById(R.id.signUpInfoText);
            TextView signUpInfoText = (TextView) findViewById(R.id.signUpInfoText);
            progress.dismiss();
            if (profile == null && signUpError)
                signUpInfoText.setText("Couldn't connect with server");
            else if (profile == null)
                signUpInfoText.setText("Account Name has already been used");
            else
                startEnemyPickerActivity();
        }
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

    @Override
    public void onBackPressed() {
    }

}
