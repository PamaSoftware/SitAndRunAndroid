package software.pama.sitandrunandroid.run.user;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.user.location.gps.GpsLocationListener;
import software.pama.sitandrunandroid.run.user.location.helpers.RunTimeCounter;
import software.pama.sitandrunandroid.run.user.location.intent.IntentParams;

public class UserResultManager extends Service {

    private static final float BOARD_LINE_FOR_LOCATION = 5;
    private static final int LOCATION_ACCURACY_M = 12;
    private float locationUpdateDistanceM = 2 * LOCATION_ACCURACY_M;
    private GpsLocationListener gpsLocationListener;
    private RunTimeCounter runTimeCounter;
    private RunThread runThread;
    private long totalDistance;
    private int distanceToRun;
    private Location currentLocation;
    private boolean runOver;
    private IBinder localBinder;
    private Handler threadHandler;
    private List<RunResult> runResults = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
        runTimeCounter = new RunTimeCounter();
        threadHandler = new Handler();
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
        runTimeCounter.start();
        runThread = new RunThread();
        threadHandler.post(runThread);
    }

    public RunResult getCurrentResult() {
        // pobraæ informacjê z GPS-a
        // pobraæ informacjê z krokomierza
        // wyliczyæ, zaproksymowaæ wartoœæ
        if (!runResults.isEmpty()) {
            RunResult runResult = runResults.get(0);
            Logger.getLogger("").info("getCurrentResult " + runResult.getTotalDistance());
            return runResult;
        }
        Logger.getLogger("").info("Current Result is null");
        return null;
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
        public UserResultManager getUserResultManager() {
            return UserResultManager.this;
        }
    }

    private class RunThread extends Thread {

        @Override
        public void run() {
                totalDistance += 10;
                runTimeCounter.step();
                runResults.add(0, new RunResult(totalDistance, runTimeCounter.totalTime()));
                Logger.getLogger("").info("Run " + totalDistance);
//                Location newLocation = gpsLocationListener.getCurrentLocation();
//                if (newLocation != null && isAccurate(newLocation) && isFarEnough(newLocation)) {
//                    if (currentLocation != null)
//                        totalDistance += currentLocation.distanceTo(newLocation);
//                    currentLocation = newLocation;
//                    runTimeCounter.step();
//                    runResults.add(new RunResult(totalDistance, runTimeCounter.totalTime()));
//
//                    float edge = (distanceToRun - totalDistance) / (float) distanceToRun;
//                    if (edge < 0.05) {
//                        lowerLocationUpdateDistance(0.33f);
//                    }
//                    Log.d("RunThread", "Total distance: " + totalDistance);
                    if (distanceToRun - totalDistance <= 0 || runOver) {
                        runOver = true;
                        gpsLocationListener.stop();
                        Logger.getLogger("").info("Stopping run thread");
                        return;
                    }
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
            if (currentLocation == null)
                return true;
            float distance = currentLocation.distanceTo(location);
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
}
