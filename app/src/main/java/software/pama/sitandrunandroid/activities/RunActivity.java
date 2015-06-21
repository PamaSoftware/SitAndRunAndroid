package software.pama.sitandrunandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;

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
import software.pama.sitandrunandroid.run.RunManager;

public class RunActivity extends Activity {

    public static final int FORECAST_SECONDS = 10;
    public static final int SLEEP_SECONDS = 2;
    private RunManager runManager;
    private TextView txtDifference;
    private TextView txtSatellites;
    private TextView txtDistance;
    private TextView txtTime;
    private TextView txtEnemyDistance;
    private TextView txtEnemyTime;
    private TextView txtRunOver;
    private DateFormat formatter = new SimpleDateFormat("mm:ss:SSS");
    private IntegrationLayer theIntegrationLayer = TheIntegrationLayerMock.getInstance();
    private ScheduledExecutorService executor;
    private boolean runOver = false;
    private EnemyResultTime enemyResultTime;
    private long totalWaitingTime;
    private long previousTime;
    private int enemyDistance;
    private CountDownTimer countDownTimer;
    private RunResult userResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_run);
        setupView();
        executor = Executors.newScheduledThreadPool(4);
        runManager = new RunManager(this, RunDistance.FIVE);
        scheduleTask(updateSattelites);
        checkHostIfRequired();
        countDownToStart();
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

            private TextView txtCountdown = ((TextView) findViewById(R.id.countdownText));

            public void onTick(long millisUntilFinished) {
                String result = new DecimalFormat("##.00").format(millisUntilFinished / 1000.0);
                txtCountdown.setText("Start in: " + result);
            }

            public void onFinish() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                txtCountdown.setText("RUN FOREST!");
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
        stopRunManager();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pickEnemy();
                    }
                });
            }
        }, 3000);
    }

    private void pickEnemy() {
        startActivity(new Intent(this, EnemyPickerActivity.class));
    }

    private Runnable updateSattelites = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String text = "Sattelites: " + runManager.getSatellitesNumber();
                        txtSatellites.setText(text);
//                        Logger.getLogger("").log(Level.INFO, text);
                    }
                });
        }
    };

    public void startRunService() {
        runManager.startRun();
        scheduleTask(updateActivity);
        scheduleTask(updateDifference);
        new GetEnemyResult().execute();
    }

    private void scheduleTask(Runnable updateLocation) {
        executor.scheduleAtFixedRate(updateLocation, 0, SLEEP_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        stopRunManager();
        super.onDestroy();
    }

    private void stopRunManager() {
        runManager.stopModule();
    }

    private Runnable updateDifference = new Runnable() {
        @Override
        public void run() {
            // TODO?
        }
    };

    private Runnable updateActivity = new Runnable() {

        @Override
        public void run() {
            updateMainInformation();
            updateUserResult();
            // jeśli minęły dwie sekundy to update przeciwnika bo to jego prawdziwy czas
            if (enemyResultTime != null) {
                // update difference co równy czas, ale update wyniku nie 
                updateEnemyResult(enemyResultTime.enemyResult);
                updateDifference(enemyDistance);
            }

        }

        private void updateMainInformation() {
            userResult = runManager.getUserResult();
            runOver = runManager.isRunOver();
        }

        private void updateUserResult() {
            if (userResult != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtDistance.setText(Float.toString(userResult.getTotalDistance()) + " m");
                        long totalTime = userResult.getTotalTime();
                        txtTime.setText(formatter.format(new Date(totalTime)) + " s");
                        if (runOver) {
                            txtRunOver.setText("RunThread over!");
                            executor.shutdown();
                        }
                    }
                });
            }
        }

        private void updateEnemyResult(final RunResultPiece enemyResult) {
            long timePassedMs = System.currentTimeMillis() - enemyResultTime.systemTimeMs;
            totalWaitingTime = enemyResultTime.operationRunningTimeMs + timePassedMs;
            if (totalWaitingTime > FORECAST_SECONDS * 1000) {
                enemyDistance = enemyResultTime.enemyResult.getDistance();
                // potem zmienić na wspólny czas, ale dopiero jak się okaże że działa
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtEnemyDistance.setText(Float.toString(enemyResult.getDistance()) + " m");
                        txtEnemyTime.setText(formatter.format(new Date(enemyResult.getTime() * 1000)) + " s");
                    }
                });
            }
        }

        private void updateDifference(final int enemyDistance) {
            if (userResult != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int diff = enemyDistance - (int) userResult.getTotalDistance();
                        String strDiff;
                        if (diff >= 0)
                            strDiff = "+" + diff;
                        else
                            strDiff = diff + "";
                        txtDifference.setText(strDiff);
                    }
                });
            }
        }
    };

    private class GetEnemyResult extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
                RunResult prevResult = null;
                while (!runOver) {
                    prevResult = getRunResult(prevResult);
                }
                userResult = runManager.getUserResult();
                getRunResult(prevResult);
            return null;
        }

        private RunResult getRunResult(RunResult prevResult) {
            try {
                long systemTimeMs = System.currentTimeMillis();
                // zapytania co tyle o ile do przodu dostaję wynik przeciwnika
                while (systemTimeMs - previousTime > FORECAST_SECONDS * 1000 + SLEEP_SECONDS * 1000) {
                    previousTime = systemTimeMs;
                    if (userResult != null && userResult.greaterThan(prevResult)) {
                        Logger.getAnonymousLogger().log(Level.INFO, "Moj czas " + userResult.getTotalTime() + " dystans " + userResult.getTotalDistance());
                        long timeMs = System.currentTimeMillis();
                        RunResultPiece enemyResult = theIntegrationLayer.getEnemyResult(FORECAST_SECONDS, userResult);
                        long currentTimeMs = System.currentTimeMillis();
                        long diffMs = currentTimeMs - timeMs;
                        enemyResultTime = new EnemyResultTime(enemyResult, currentTimeMs, diffMs);
                        Logger.getAnonymousLogger().log(Level.INFO, "Dystans przeciwnika: " + enemyResult.getDistance());
                        prevResult = userResult;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prevResult;
        }

    }

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

    private class EnemyResultTime {

        public final RunResultPiece enemyResult;
        public final long systemTimeMs;
        public final long operationRunningTimeMs;

        public EnemyResultTime(RunResultPiece enemyResult, long systemTimeMs, long operationRunningTimeMs) {
            this.enemyResult = enemyResult;
            this.systemTimeMs = systemTimeMs;
            this.operationRunningTimeMs = operationRunningTimeMs;
        }
    }
}
