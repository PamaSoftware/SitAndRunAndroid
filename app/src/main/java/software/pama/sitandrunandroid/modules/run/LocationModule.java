package software.pama.sitandrunandroid.modules.run;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.modules.run.extras.LocationConstants;
import software.pama.sitandrunandroid.modules.run.managers.RunManager;
import software.pama.sitandrunandroid.modules.run.managers.LocationListenerManager;

/**
 * Serwis pobierający lokalizację użytkownika zarówno gdy aplikacja jest w trakcie działania
 * jak i wtedy gdy jest zatrzymana.
 */
// TODO poprawic zeby nie zatrzymywac aplikacji przy obroceniu ekranu
public class LocationModule extends Service {

    private LocationListenerManager locationListenerManager;
    private RunManager runManager;
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
        int distanceToRun = intent.getIntExtra(LocationConstants.DISTANCE_TO_RUN_EXTRA, -1);
        locationListenerManager = new LocationListenerManager(this);
        runManager = new RunManager(distanceToRun, locationListenerManager, handler);
        startManagingThreads();
        Toast.makeText(this, "Location Module started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    public boolean isRunOver() {
        return runOver;
    }

    public RunResult getCurrentRunResult() {
        return runManager.getRunResult();
    }

    @Override
    public void onDestroy() {
        Log.i("STOP_SERVICE", "DONE");
        locationListenerManager.stopListening();
        joinDistanceManager();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private void startManagingThreads() {
        locationListenerManager.startLocationManager();
        runManager.run();
        new ModuleManager().run();
    }

    private void joinDistanceManager() {
        try {
            runManager.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public LocationModule getLocationService() {
            return LocationModule.this;
        }
    }

    private class ModuleManager extends Thread {

        @Override
        public void run() {
            Log.d("Location Module", "Is run over? " + runManager.isRunOver());
            if (runManager.isRunOver()) {
                try {
                    runManager.join();
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
