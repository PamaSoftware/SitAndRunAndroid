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
import software.pama.sitandrunandroid.run.TheRunTimer;

public class RunActivity extends Activity {

//    public static final DecimalFormat TIMER_DECIMAL_FORMAT = new DecimalFormat("##.00");
    public static final DecimalFormat TIMER_DECIMAL_FORMAT = new DecimalFormat("##");
    private IntegrationLayer theIntegrationLayer = TheIntegrationLayerMock.getInstance();
    private RunConnector runConnector;
    private TextView txtDifference;
    private TextView txtSatellites;
    private TextView txtDistance;
    private TextView txtTime;
    private TextView txtEnemyDistance;
    private TextView txtEnemyTime;
    private TextView txtRunOver;
    private TextView txtCountdown;
    private DateFormat formatter = new SimpleDateFormat("mm:ss:SSS");
    private ScheduledExecutorService executor;
    private boolean runOver = false;
    private CountDownTimer countDownTimer;
    private RunResult userResultForNow;
    private RunResult enemyResultForNow;
    private TheRunTimer theRunTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_run);
        setupView();
        executor = Executors.newScheduledThreadPool(4);
        runConnector = new RunConnector(this, RunDistance.FIVE);
        theRunTimer = TheRunTimer.getInstance();
        scheduleTask(updateSatellites);
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
        stopRunManager();
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
        scheduleTask(updateDifference);
        theRunTimer.start();
    }

    private void scheduleTask(Runnable updateLocation) {
        executor.scheduleAtFixedRate(updateLocation, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        stopRunManager();
        super.onDestroy();
    }

    private void stopRunManager() {
        runConnector.stopModule();
    }

    private Runnable updateDifference = new Runnable() {
        @Override
        public void run() {
            // TODO?
        }
    };

    private Runnable updateAllData = new Runnable() {

        @Override
        public void run() {
            updateMainInformation();
            updateUserResult();
            updateEnemyResult();
        }

        private void updateMainInformation() {
            userResultForNow = runConnector.getUserResultForNow();
            enemyResultForNow = runConnector.getEnemyResultForNow();
        }

        private void updateUserResult() {
            if (userResultForNow != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtDistance.setText(TIMER_DECIMAL_FORMAT.format(userResultForNow.getTotalDistance()) + " m");
//                        txtTime.setText(formatter.format(new Date(userResultForNow.getTotalTime())) + " s");
                        // testowo
                        txtCountdown.setText(formatter.format(new Date(theRunTimer.getRunDuration())));
                        if (runConnector.isRunOver()) {
                            txtRunOver.setText("RunThread over!");
                            executor.shutdown();
                        }
                    }
                });
            }
        }

        private void updateEnemyResult() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtEnemyDistance.setText(TIMER_DECIMAL_FORMAT.format(enemyResultForNow.getTotalDistance()) + " m");
//                        txtEnemyTime.setText(formatter.format(new Date(enemyResultForNow.getTotalTime())) + " s");
                    }
                });
//            }
        }

//        private void updateDifference(final int enemyDistance) {
//            if (userResultForNow != null) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        int diff = enemyDistance - (int) userResultForNow.getTotalDistance();
//                        String strDiff;
//                        if (diff >= 0)
//                            strDiff = "+" + diff;
//                        else
//                            strDiff = diff + "";
//                        txtDifference.setText(strDiff);
//                    }
//                });
//            }
//        }
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
