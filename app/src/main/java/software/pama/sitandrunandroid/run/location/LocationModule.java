package software.pama.sitandrunandroid.run.location;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.location.intent.IntentParams;
import software.pama.sitandrunandroid.run.location.managers.GpsLocationListener;
import software.pama.sitandrunandroid.run.location.managers.RunThread;

/**
 * Serwis pobierający lokalizację użytkownika zarówno gdy aplikacja jest w trakcie działania
 * jak i wtedy gdy jest zatrzymana.
 */
// TODO poprawic zeby nie zatrzymywac aplikacji przy obroceniu ekranu
public class LocationModule extends Service {

    private GpsLocationListener gpsLocationListener;
    private RunThread runThread;
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
        runThread = new RunThread(distanceToRun, gpsLocationListener, handler);
        Toast.makeText(this, "Location Module started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    public void startLocationModule() {
        gpsLocationListener.startLocationListener();
    }

    public void startRun() {
        runThread.run();
        new ModuleManager().run();
    }

    public boolean isRunOver() {
        return runOver;
    }

    public int getFixedSatellitesNumber() {
        return gpsLocationListener.getFixedSatellitesNumber();
    }

    public RunResult getCurrentRunResult() {
        return runThread.getRunResult();
    }

    @Override
    public void onDestroy() {
        Log.i("STOP_SERVICE", "DONE");
        gpsLocationListener.stopListening();
        joinDistanceManager();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private void joinDistanceManager() {
        try {
            runThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public LocationModule getLocationModule() {
            return LocationModule.this;
        }
    }

    private class ModuleManager extends Thread {

        @Override
        public void run() {
            Log.d("Location Module", "Is run over? " + runThread.isRunOver());
            if (runThread.isRunOver()) {
                try {
                    runThread.join();
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

}
