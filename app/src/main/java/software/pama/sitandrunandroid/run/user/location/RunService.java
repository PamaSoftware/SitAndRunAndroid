package software.pama.sitandrunandroid.run.user.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.user.location.helpers.RunTimeCounter;
import software.pama.sitandrunandroid.run.user.location.intent.IntentParams;
import software.pama.sitandrunandroid.run.user.location.gps.GpsLocationListener;

/**
 * Serwis pobierający lokalizację użytkownika zarówno gdy aplikacja jest w trakcie działania
 * jak i wtedy gdy jest zatrzymana.
 */
// TODO poprawic zeby nie zatrzymywac aplikacji przy obroceniu ekranu
public class RunService extends Service {

    private GpsLocationListener gpsLocationListener;
    private Run run;
    private IBinder localBinder;
    private boolean runOver;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int distanceToRun = intent.getIntExtra(IntentParams.DISTANCE_TO_RUN_PARAM, -1);
        gpsLocationListener = new GpsLocationListener(this);
        run = new Run(distanceToRun, gpsLocationListener, handler);
        Toast.makeText(this, "Location Module started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    public void startLocationModule() {
        gpsLocationListener.startLocationListener();
    }

    public void startRun() {
        run.run();
        new ModuleManager().run();
    }

    public boolean isRunOver() {
        return runOver;
    }

    public int getFixedSatellitesNumber() {
        return gpsLocationListener.getFixedSatellitesNumber();
    }

    public RunResult getCurrentRunResult() {
        return run.getRunResult();
    }

    @Override
    public void onDestroy() {
        Log.i("STOP_SERVICE", "DONE");
        gpsLocationListener.stop();
        joinDistanceManager();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private void joinDistanceManager() {
        try {
            run.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public RunService getLocationModule() {
            return RunService.this;
        }
    }

    private class ModuleManager extends Thread {

        @Override
        public void run() {
            Log.d("Location Module", "Is run over? " + run.isRunOver());
            if (run.isRunOver()) {
                try {
                    run.join();
                    runOver = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Finished run!", Toast.LENGTH_LONG).show();
                return;
            }
            handler.postDelayed(this, 3000);
        }

    }

    private class Run extends Thread {

        private int i = 0;
        private int distanceToRun;
        private long totalDistance;
        private Location currentLocation;
        private GpsLocationListener gpsLocationListener;
        private boolean runOver;
        private Handler threadHandler;
        private RunTimeCounter runTimeCounter;

        public Run(int distanceToRun, GpsLocationListener gpsLocationListener, Handler threadHandler) {
    //        this.distanceToRun = distanceToRun;
            this.distanceToRun = 500;
            this.gpsLocationListener = gpsLocationListener;
            this.threadHandler = threadHandler;
            this.runTimeCounter = new RunTimeCounter();
        }

        @Override
        public void run() {
            // TODO pomyśleć nad wyrzuceniem tego stad zeby oszczedzic na ilosci operacji
            runTimeCounter.start();
            Location newLocation = gpsLocationListener.getCurrentLocation();
            if (newLocation != null) {
                if (currentLocation != null)
                    totalDistance += currentLocation.distanceTo(newLocation);
                currentLocation = newLocation;
                runTimeCounter.step();
                float edge = (distanceToRun - totalDistance) / (float) distanceToRun;
//                if (edge < 0.05) {
//                    gpsLocationListener.lowerLocationUpdateDistance(0.33f);
//                }
    //            totalDistance += new Random().nextInt(10) + 1;
                Log.d("Finish Counter", "Total distance: " + totalDistance);
                if (distanceToRun - totalDistance < 0) {
                    runOver = true;
                    gpsLocationListener.stop();
                    return;
                }
            }
            threadHandler.postDelayed(this, 1000);
        }

        public RunResult getRunResult() {
            long totalTime = runTimeCounter.totalTime();

            // testowo
            totalDistance += 100;
            totalTime += (i = i + 1000);

            return new RunResult(totalDistance, totalTime);
        }

        public boolean isRunOver() {
            return runOver;
        }

    }
}
