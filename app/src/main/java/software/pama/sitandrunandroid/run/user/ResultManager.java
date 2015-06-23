package software.pama.sitandrunandroid.run.user;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.integration.IntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayerMock;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.TheRunTimer;
import software.pama.sitandrunandroid.run.user.location.gps.GpsLocationListener;
// TODO sprawdzi� funkcjonalno�� czy ka�dy przypadek jest zawarty
// TODO testy, na przyk�ad czy wy��czane s� wszystkie listenery (logowanie zdarze�)
// TODO przeczyta� o logowaniu zdarze�
public class ResultManager extends Service {

    private static final int FORECAST_SECONDS = 10;
    private static final int SLEEP_SECONDS = 2;
    private static final float BOARD_LINE_FOR_LOCATION = 5;
    private static final int LOCATION_ACCURACY_M = 12;
    private static final int HEAD = 0;
    private float locationUpdateDistanceM = 2 * LOCATION_ACCURACY_M;
    private IntegrationLayer theIntegrationLayer = TheIntegrationLayerMock.getInstance();
    private GpsLocationListener gpsLocationListener;
    private RunThread runThread;
    private long userDistance;
    private int distanceToRun;
    private Location userLocation;
    private boolean runOver;
    private IBinder localBinder;
    private Handler threadHandler;
    private List<RunResult> userResults = new ArrayList<>();
    private List<RunResult> enemyResults = new ArrayList<>();
    private TheRunTimer theRunTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
        threadHandler = new Handler();
        theRunTimer = TheRunTimer.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        distanceToRun = intent.getIntExtra(IntentParams.DISTANCE_TO_RUN_PARAM, -1);
        distanceToRun = 100;
        gpsLocationListener = new GpsLocationListener(this);
        gpsLocationListener.startLocationListener();
        Toast.makeText(this, "GPSLocationListener started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    public void startRun() {
        runThread = new RunThread();
        threadHandler.post(runThread);
        new GetEnemyResult().execute();
    }

    public RunResult getUserResult() {
        int resultsSize = userResults.size();
        if (resultsSize == 0) {
            Logger.getLogger("").info("User distance = null");
            return new RunResult(0, theRunTimer.getRunDuration());
        } else if (resultsSize == 1) {
            return userResults.get(HEAD);
        } else {
            RunResult latestResult = userResults.get(HEAD);
            RunResult nextResult = userResults.get(1);
            float s1 = nextResult.getTotalDistance();
            float s2 = latestResult.getTotalDistance();
            long t1 = nextResult.getTotalTime();
            long t2 = latestResult.getTotalTime();
            long currentRunTime = theRunTimer.getRunDuration(); //t3
            float deltaDistance = ((s2 - s1)/(t2 - t1))*(currentRunTime - t2);
            float distanceForTheMoment = s2 + deltaDistance;
            Logger.getLogger("").info("User distance = " + distanceForTheMoment);
            return new RunResult(distanceForTheMoment, currentRunTime);
        }
    }

    public RunResult getEnemyResult() {
        // po stronie aktywnosci wyswietlac wynik ci�g�y
        int resultsSize = enemyResults.size();
        if (resultsSize == 0) {
            Logger.getLogger("").info("Enemy distance = null");
            return new RunResult(0, theRunTimer.getRunDuration());
        } else if (resultsSize == 1) {
            long currentRunTime = theRunTimer.getRunDuration(); // t1
            RunResult enemyResult = enemyResults.get(HEAD);
            float distanceForTheMoment = enemyResult.getTotalDistance()*(currentRunTime/enemyResult.getTotalTime());
            return new RunResult(distanceForTheMoment ,currentRunTime);
        } else {
            RunResult latestResult = enemyResults.get(HEAD);
            RunResult nextResult = enemyResults.get(1);
            float s1 = nextResult.getTotalDistance();
            float s2 = latestResult.getTotalDistance();
            long t1 = nextResult.getTotalTime();
            long t2 = latestResult.getTotalTime();
            long currentRunTime = theRunTimer.getRunDuration(); //t3
            float distanceForTheMoment;
            if (currentRunTime < t2) {
                Logger.getLogger("").info("Scenariusz przeciwnika");
                // normalny scenariusz - przetestować bo na razie wchodzi zawsze ten drugi (przy testowych wartościach time += 3)
                float deltaDistance = ((s2 - s1)/(t2 - t1))*(currentRunTime - t1);
                distanceForTheMoment = s1 + deltaDistance;
            } else {
                // scenariusz z użytkownika
                Logger.getLogger("").info("Scenariusz użytkownika");
                float deltaDistance = ((s2 - s1)/(t2 - t1))*(currentRunTime - t2);
                distanceForTheMoment = s2 + deltaDistance;
            }
            Logger.getLogger("").info("Enemy distance = " + distanceForTheMoment);
            return new RunResult(distanceForTheMoment, currentRunTime);
        }
    }

    public void stopRun() {
        runOver = true;
    }

    public boolean isRunOver() {
       return runOver;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsLocationListener.stop();
        runOver = true;
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public ResultManager getUserResultManager() {
            return ResultManager.this;
        }
    }

    private class RunThread extends Thread {

        @Override
        public void run() {
                userDistance += 10;
                userResults.add(HEAD, new RunResult(userDistance, theRunTimer.getRunDuration()));
                Logger.getLogger("").info("Run " + userDistance);
//                Location newLocation = gpsLocationListener.getCurrentLocation();
//                if (newLocation != null && isAccurate(newLocation) && isFarEnough(newLocation)) {
//                    if (userLocation != null)
//                        userDistance += userLocation.distanceTo(newLocation);
//                    userLocation = newLocation;
//                    userResults.add(HEAD, new RunResult(userDistance, System.currentTimeMillis()));
//
//                    float edge = (distanceToRun - userDistance) / (float) distanceToRun;
//                    if (edge < 0.05) {
//                        lowerLocationUpdateDistance(0.33f);
//                    }
//                    Log.d("RunThread", "Total distance: " + userDistance);
//                    if (distanceToRun - userDistance <= 0 || runOver) {
//                        runOver = true;
//                        gpsLocationListener.stop();
//                        Logger.getLogger("").info("Stopping run thread");
//                        return;
//                    }
//                }
                threadHandler.postDelayed(this, 1000);
        }

        private boolean isAccurate(Location location) {
            if (location == null)
                return false;
            Log.d(getClass().getSimpleName(), "Location accuracy: " + location.getAccuracy());
            return location.getAccuracy() > 0.0 && location.getAccuracy() <= LOCATION_ACCURACY_M;
        }

        private boolean isFarEnough(Location location) {
            if (location == null)
                return false;
            if (userLocation == null)
                return true;
            float distance = userLocation.distanceTo(location);
            Log.d(getClass().getSimpleName(), "Location far enough: " + distance);
            return distance > locationUpdateDistanceM;
        }

        public void lowerLocationUpdateDistance(float lowerPercentage) {
            float result = locationUpdateDistanceM - locationUpdateDistanceM * lowerPercentage;
            if (result >= BOARD_LINE_FOR_LOCATION) {
                locationUpdateDistanceM = result;
                Log.i(getClass().getSimpleName(), "Location update distance changed to " + result);
            }
        }
    }


    private class GetEnemyResult extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RunResult prevResult = null;
            while (!runOver) {
                prevResult = updateUserAndGetEnemyResult(prevResult);

            }
            updateUserAndGetEnemyResult(prevResult);
            return null;
        }

        private RunResult updateUserAndGetEnemyResult(RunResult prevResult) {
            RunResult userResult;
            if (userResults.isEmpty())
                userResult = new RunResult(0, theRunTimer.getRunDuration());
            else
                userResult = userResults.get(HEAD);
            try {
                if (userResult.greaterThan(prevResult)) {
                    Logger.getAnonymousLogger().log(Level.INFO, "Moj czas " + userResult.getTotalTime() + " dystans " + userResult.getTotalDistance());
                    RunResultPiece enemyResultPiece = theIntegrationLayer.getEnemyResult(FORECAST_SECONDS, userResult);
                    enemyResults.add(HEAD, new RunResult(enemyResultPiece.getDistance(), enemyResultPiece.getTime()*1000));
                    if (enemyResultPiece.getDistance() < 0) {
                        // TODO zako�czenie biegu - wy��czenie wszystkich listener�w
                        runOver = true;
                    }
                    Logger.getAnonymousLogger().log(Level.INFO, "Dystans przeciwnika: " + enemyResultPiece.getDistance());
                    prevResult = userResult;
                }
                Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return prevResult;
        }

    }
}
