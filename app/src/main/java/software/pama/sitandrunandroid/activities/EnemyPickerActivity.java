package software.pama.sitandrunandroid.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunStartInfo;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.activities.helpers.IntentParams;
import software.pama.sitandrunandroid.activities.tasks.AsyncTaskResponse;
import software.pama.sitandrunandroid.integration.IntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayerMock;

import static android.location.LocationManager.GPS_PROVIDER;

public class EnemyPickerActivity extends Activity implements AsyncTaskResponse<Integer>, LocationListener {

    private Button hostForFriendButton;
    private Button joinFriendButton;
    private EditText aspirationEditText;
    private EditText reservationEditText;
    private EditText friendsLoginText;
    private Button runButton;
    private IntegrationLayer theIntegrationLayer;
    private Preferences preferences;
    private String friendsLogin;
    private RunWithEnemyTask runWithEnemyTask;
    private boolean hostForFriend;
    private boolean hasToCheckHost;
    private TextWatcher friendsLoginTextWatcher;
    private Toast preferenceValidationToast;
    private Toast differenceValidationToast;
    private NetworkStateReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enemy_picker);
        theIntegrationLayer = TheIntegrationLayerMock.getInstance();
//        theIntegrationLayer = TheIntegrationLayer.getInstance();
        hostForFriendButton = (Button) findViewById(R.id.hostForFriendButton);
        joinFriendButton = (Button) findViewById(R.id.joinFriendButton);
        aspirationEditText = (EditText) findViewById(R.id.desiredDistance);
        reservationEditText = (EditText) findViewById(R.id.acceptableDistance);
        friendsLoginText = (EditText) findViewById(R.id.friendsLogin);
        runButton = (Button) findViewById(R.id.runButton);
        preferenceValidationToast = Toast.makeText(this, "Both values must be greater than 500", Toast.LENGTH_SHORT);
        differenceValidationToast = Toast.makeText(this, "Difference must be greater than 100", Toast.LENGTH_SHORT);
        LocationManager locationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(GPS_PROVIDER, 2000, 0, this);
//        Toast.makeText(this, "GPS and internet connection is required", Toast.LENGTH_SHORT).show();
//        networkReceiver = new NetworkStateReceiver();
//        registerReceiver(networkReceiver, new IntentFilter());
        // sprawdzic czy jest GPS i czy jest internet, dodac jakiegos sluchacza, jesli internet jest wylaczony
        // to wtedy dac info i disable przycisk biegu
        if (isOnline()) {
            Toast.makeText(this, "Internet is connected", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Action invoked on button click
     */
    public void runWithRandom(View v) {
        clearAllData();
        preparePreferencesInput();
        runButton.setVisibility(View.VISIBLE);
        runWithEnemyTask = new RunWithRandomTask();
    }

    /**
     * Action invoked on button click
     */
    public void runWithFriend(View v) {
        clearAllData();
        theIntegrationLayer = TheIntegrationLayerMock.getInstance();
        joinFriendButton.setVisibility(View.VISIBLE);
        hostForFriendButton.setVisibility(View.VISIBLE);
        runButton.setVisibility(View.VISIBLE);
    }

    private void clearAllData() {
        clearInput(aspirationEditText);
        clearInput(reservationEditText);
        clearInput(friendsLoginText);
        runButton.setVisibility(View.INVISIBLE);
        hostForFriendButton.setVisibility(View.INVISIBLE);
        joinFriendButton.setVisibility(View.INVISIBLE);
        removeFriendsLoginValidator();
    }

    private void removeFriendsLoginValidator() {
        friendsLoginText.removeTextChangedListener(friendsLoginTextWatcher);
    }

    private void clearInput(EditText input) {
        input.setText("");
        input.setVisibility(View.INVISIBLE);
    }

    /**
     * Action invoked on button click
     */
    public void hostForFriend(View v) {
        preparePreferencesInput();
        prepareFriendsLoginInput();
        hostForFriend = true;
    }

    /**
     * Action invoked on button click
     */
    public void joinFriend(View v) {
        clearInput(friendsLoginText);
        removeFriendsLoginValidator();
        preparePreferencesInput();
        runWithEnemyTask = new JoinFriendTask();
    }

    /**
     * Action invoked on button click
     */
    public void run(View v) {
        setupPreferences();
        setupFriendsLogin();
        if (hostForFriend) {
            HostForFriendTask hostForFriendTask = new HostForFriendTask();
            hostForFriendTask.delegate = new HostForFriendResponse();
            hostForFriendTask.execute();
        }
        else
            runWithEnemyTask.execute();
        runButton.setEnabled(false);
    }

    private void setupFriendsLoginValidator() {
        friendsLoginTextWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cancelValidationToasts();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && preferencesAreValid(aspirationEditText.getText(), reservationEditText.getText()))
                    runButton.setEnabled(true);
                else {
                    runButton.setEnabled(false);
                    differenceValidationToast.show();
                }
            }
        };
        friendsLoginText.addTextChangedListener(friendsLoginTextWatcher);
    }

    private void setupPreferencesValidator() {
        aspirationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cancelValidationToasts();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePreferences(s, reservationEditText.getText());
            }

        });

        reservationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cancelValidationToasts();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePreferences(s, aspirationEditText.getText());
            }
        });
    }

    private void cancelValidationToasts() {
//        preferenceValidationToast.cancel();
//        differenceValidationToast.cancel();
    }

    private void validatePreferences(Editable s, Editable p) {
        if (preferencesAreValid(s, p))
            runButton.setEnabled(true);
        else {
            runButton.setEnabled(false);
        }
    }

    private boolean preferencesAreValid(Editable s, Editable p) {
        boolean preferencesValid = isPreferenceValid(s) && isPreferenceValid(p);
        boolean differenceValid = isDifferenceValid(s, p);
        if (!preferencesValid)
            preferenceValidationToast.show();
        if (!differenceValid)
            differenceValidationToast.show();
        return preferencesValid && differenceValid;
    }

    private boolean isPreferenceValid(Editable editable) {
        return getInt(editable) >= 500;
    }

    private int getInt(Editable editable) {
        String text = editable.toString();
        if (!text.isEmpty())
            return Integer.parseInt(text);
        else
            return Integer.MIN_VALUE;
    }

    private boolean isDifferenceValid(Editable s, Editable p) {
        if (s == null || p == null)
            return false;
        return Math.abs(getInt(s) - getInt(p)) >= 100;
    }

    private void prepareFriendsLoginInput() {
        friendsLoginText.setVisibility(View.VISIBLE);
        setupFriendsLoginValidator();
    }

    public void preparePreferencesInput() {
        aspirationEditText.setVisibility(View.VISIBLE);
        reservationEditText.setVisibility(View.VISIBLE);
        setupPreferencesValidator();
    }

    private boolean validateResult(int result) {
        switch (Integer.signum(result)) {
            case -1:
                Toast.makeText(this, "Server connection problems", Toast.LENGTH_SHORT).show();
                return false;
            case 0:
                Toast.makeText(this, "Couldn't find enemy", Toast.LENGTH_SHORT).show();
                return false;
            case 1:
                return true;
        }
        // won't happen
        throw new RuntimeException();
    }

    private void startRun(int countdown) {
        Intent intent = new Intent(this, RunActivity.class);
        if (hasToCheckHost)
            intent.putExtra(IntentParams.CHECK_HOST, true);
        intent.putExtra(IntentParams.COUNTDOWN_SECONDS, countdown);
        startActivity(intent);
    }

    private void setupPreferences() {
        int aspiration = getInt(aspirationEditText.getText());
        int reservation = getInt(reservationEditText.getText());
        preferences = new Preferences().setAspiration(aspiration).setReservation(reservation);
    }

    private void setupFriendsLogin() {
        friendsLogin = friendsLoginText.getText().toString();
    }

    @Override
    public void onTaskFinish(Integer result) {
        if (result > 0) {
            startRun(result);
        } else
            Toast.makeText(this, "Couldn't find enemy", Toast.LENGTH_SHORT).show();
    }

    private abstract class RunWithEnemyTask extends AsyncTask<Void, Void, Integer> {}

    private class RunWithRandomTask extends RunWithEnemyTask {

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                RunStartInfo runStartInfo = theIntegrationLayer.startRunWithRandom(preferences);
                int countdown = runStartInfo.getTime();
                int distance = runStartInfo.getDistance();
                Logger.getAnonymousLogger().log(Level.INFO, "Gotowy do rywalizacji, odliczam " + countdown + " sekund");
                // przekazac klase, potem w intent przekazac parametry, potem wykorzysac parametry do uruchomienia biegu
                return countdown;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    runButton.setEnabled(true);
                    runWithEnemyTask = new RunWithRandomTask();
                }
            });
            return -1;
        }

        @Override
        protected void onPostExecute(Integer countdown) {
            super.onPostExecute(countdown);
            startRun(countdown);
        }
    }

    private class HostForFriendTask extends AsyncTask<Void, Void, Boolean>  {

        public AsyncTaskResponse<Boolean> delegate = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            String friendsLogin = friendsLoginText.getText().toString();
            boolean runCreated = false;
            try {
                runCreated = theIntegrationLayer.hostRunWithFriend(friendsLogin, preferences);
                Logger.getAnonymousLogger().log(Level.INFO, "RunThread created");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            return runCreated;
        }

        @Override
        protected void onPostExecute(Boolean runCreated) {
            super.onPostExecute(runCreated);
            delegate.onTaskFinish(runCreated);
        }
    }

    private class JoinFriendTask extends RunWithEnemyTask {

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                final int result = theIntegrationLayer.joinFriend(preferences);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (validateResult(result)) {
                            hasToCheckHost = true;
                            startRun(result);
                        }
                        else {
                            runButton.setEnabled(true);
                            runWithEnemyTask = new JoinFriendTask();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private void startRunAfterFriendJoins() throws IOException {
        TimerTask task = new TimerTask() {

            int requestCounter;

            @Override
            public void run() {
                try {
                    // mozliwe ze tu taska trzeba zrobic
                    int result = theIntegrationLayer.startRunWithFriend(preferences);
                    requestCounter++;
                    Logger.getAnonymousLogger().log(Level.INFO, "Looking for friend");
                    if (result > 0) {
                        startRun(result);
                        this.cancel();
                    }
                    else if (requestCounter == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enemyNotFound();
                            }
                        });
                        Logger.getAnonymousLogger().log(Level.INFO, "Couldn't find enemy");
                        this.cancel();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 4000);
    }

    public void enemyNotFound() {
        Toast.makeText(this, "Couldn't find enemy", Toast.LENGTH_SHORT).show();
        runButton.setEnabled(true);
    }

    private class HostForFriendResponse implements AsyncTaskResponse<Boolean> {

        @Override
        public void onTaskFinish(Boolean result) {
            if (result) {
                try {
                    startRunAfterFriendJoins();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show();
        if (location.getAccuracy() < 50)
            runButton.setEnabled(true);
        else {
            Toast.makeText(this, "Need better GPS connection", Toast.LENGTH_SHORT);
            runButton.setEnabled(false);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        runButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
//        int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
//        ComponentName component=new ComponentName(getApplicationContext(), NetworkStateReceiver.class);
//
//        getPackageManager().setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);

//        unregisterReceiver(networkReceiver);
//        ComponentName receiver = new ComponentName(this, NetworkStateReceiver.class);
//        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enemy_picker, menu);
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
