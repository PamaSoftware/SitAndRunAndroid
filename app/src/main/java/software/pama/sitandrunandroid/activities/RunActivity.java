package software.pama.sitandrunandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.activities.helpers.IntentParams;
import software.pama.sitandrunandroid.integration.IntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayerMock;
import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.RunConnector;
import software.pama.sitandrunandroid.run.RunFinish;

public class RunActivity extends Activity {

//    public static final DecimalFormat TIMER_DECIMAL_FORMAT = new DecimalFormat("##.00");
    private static final DecimalFormat TIMER_DECIMAL_FORMAT = new DecimalFormat("##");
    private static final DecimalFormat DISTANCE_DECIMAL_FORMAT = new DecimalFormat("##");
    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("mm:ss:SSS");
    private IntegrationLayer theIntegrationLayer = TheIntegrationLayerMock.getInstance();
    private ScheduledExecutorService executor;
    private RunConnector runConnector;
    private RunResult userResultForNow;
    private RunResult enemyResultForNow;
    private RunFinish runFinish;
    private long runTime;
    private CountDownTimer countDownTimer;
    private TextView txtDifference;
    private TextView txtSatellites;
    private TextView txtDistance;
    private TextView txtTime;
    private TextView txtEnemyDistance;
    private TextView txtEnemyTime;
    private TextView txtRunOver;
    private TextView txtCountdown;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_run);
        setupView();
        executor = Executors.newScheduledThreadPool(4);
        runConnector = new RunConnector(this, RunDistance.FIVE);
        runFinish = new RunFinish();
        userResultForNow = new RunResult(0, 0);
        enemyResultForNow = new RunResult(0, 0);
        scheduleTask(updateSatellites);
        checkHostIfRequired();
        countDownToStart();
        mp = MediaPlayer.create(getApplicationContext(), R.raw.dancer);
    }

    private void setupView() {
        txtDifference = (TextView) findViewById(R.id.difference);
        txtSatellites = (TextView) findViewById(R.id.txtSattelites);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtEnemyDistance = (TextView) findViewById(R.id.enemyDistance);
        txtEnemyTime = (TextView) findViewById(R.id.enemyTime);
        txtRunOver = (TextView) findViewById(R.id.txtRunOver);
        txtRunOver = (TextView) findViewById(R.id.txtRunOver);
        txtCountdown = ((TextView) findViewById(R.id.countdownText));
    }

    private void checkHostIfRequired() {
        final boolean hasToCheckHost = getIntent().getBooleanExtra(IntentParams.CHECK_HOST, false);
        if (hasToCheckHost) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Logger.getLogger("").log(Level.INFO, "Checking if host joined");
                        boolean hostJoined = new CheckIfHostJoined().execute().get();
                        if (!hostJoined) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    goToLobbyAfterHostDisconnection();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);
        }
    }

    private void countDownToStart() {
        int countdown_seconds = getIntent().getIntExtra(IntentParams.COUNTDOWN_SECONDS, -1);
        countDownTimer = new CountDownTimer(countdown_seconds * 1000, 100) {

            public void onTick(long millisUntilFinished) {
                String result = TIMER_DECIMAL_FORMAT.format(millisUntilFinished / 1000.0);
                txtCountdown.setText("Start in: " + result);
            }

            public void onFinish() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                startRunService();
            }
        };
        countDownTimer.start();
    }

    private void goToLobbyAfterHostDisconnection() {
        // TODO zrobić metodę zamykająca wszystko z tego kodu
        Toast.makeText(this, "Host disconnected...", Toast.LENGTH_SHORT).show();
        countDownTimer.cancel();
        executor.shutdown();
        // po wcisnieciu przycisku 'OK' odpalić aktywność z wyborem przeciwnika, żeby użytkownik wiedział co się dzieje
        stopRun();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        srartEnemyPickerActivity();
                    }
                });
            }
        }, 3000);
    }

    private void srartEnemyPickerActivity() {
        startActivity(new Intent(this, EnemyPickerActivity.class));
    }

    private Runnable updateSatellites = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String text = "Sattelites: " + runConnector.getSatellitesNumber();
                            txtSatellites.setText(text);
//                        Logger.getLogger("").log(Level.INFO, text);
                        }
                    });
        }
    };

    public void startRunService() {
        runConnector.startRun();
        scheduleTask(updateAllData);
    }

    private void scheduleTask(Runnable updateLocation) {
        executor.scheduleAtFixedRate(updateLocation, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        stopRun();
        super.onDestroy();
    }

    private void stopRun() {
        executor.shutdown();
        runConnector.stopModule();
    }

    private Runnable updateAllData = new Runnable() {

        @Override
        public void run() {
            updateMainInformation();
            showUserResult();
            showEnemyResult();
            updateDifference();
            checkFinish();
        }

        private void updateMainInformation() {
            runFinish = runConnector.getRunFinish();
            runTime = runConnector.getRunTime();
            userResultForNow = runConnector.getUserResultForNow();
            enemyResultForNow = runConnector.getEnemyResultForNow();
        }

        private void showUserResult() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtDistance.setText(DISTANCE_DECIMAL_FORMAT.format(userResultForNow.getTotalDistance()) + " m");
                    txtCountdown.setText(TIME_FORMATTER.format(new Date(runTime)));
//                    Logger.getLogger("").log(Level.INFO, "Showing user result " + userResultForNow.getTotalDistance());
                }
            });
        }

        private void showEnemyResult() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtEnemyDistance.setText(DISTANCE_DECIMAL_FORMAT.format(enemyResultForNow.getTotalDistance()) + " m");
//                            txtEnemyTime.setText(TIME_FORMATTER.format(new Date(enemyResultForNow.getTotalTimeMillis())) + " s");
                    Logger.getLogger("").log(Level.INFO, "Showing enemy result " + enemyResultForNow.getTotalDistance());
                }
            });
        }

        private void checkFinish() {
            if (runFinish.isRunOver()) {
                executor.shutdown();
                // wyswietlic buttona i jesli ktos wcisnie go, to jest przejscie do ostatniego ekranu
                Logger.getLogger("").log(Level.INFO, "Run over, shutting down executor");
            }
        }

        private void updateDifference() {
            if (userResultForNow != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float diff = enemyResultForNow.getTotalDistance() - userResultForNow.getTotalDistance();
                        String diffAsString = DISTANCE_DECIMAL_FORMAT.format(diff);
                        if (diff >= 0) {
                            diffAsString = "+" + diffAsString;
                            if (mp.isPlaying())
                                mp.pause();
                            txtDifference.setTextColor(Color.RED);
                        }
                        else {
                            if (!mp.isPlaying())
                                mp.start();
                            txtDifference.setTextColor(Color.GREEN);
                        }
                        txtDifference.setText(diffAsString);
                        if (!mp.isPlaying())
                            mp.start();
                    }
                });
            }
        }
    };

    private class CheckIfHostJoined extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return theIntegrationLayer.checkIfHostAlive();
            } catch (IOException e) {
                // TODO jak nie ma polączenia z siecią musimy wtedy ponowić to zapytanie nawet w trakcie biegu
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
