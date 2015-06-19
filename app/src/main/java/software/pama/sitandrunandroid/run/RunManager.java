package software.pama.sitandrunandroid.run;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.location.LocationModule;
import software.pama.sitandrunandroid.run.location.intent.IntentParams;
import software.pama.sitandrunandroid.run.steps.StepCounterModule;

/**
 * Odpowiedzialna za połączenie i obsługę {@link LocationModule}.
 * Udostępnia informacje o całkowitym przebytym dystansie przez użytkownika.
 */
public class RunManager {

    private Context appContext;
    private LocationModule locationModule;
    private StepCounterModule stepCounterModule;
    private ServiceConnection locationServiceConnection;
    private boolean locationModuleBounded = false;
    private RunDistance distanceToRun;

    public RunManager(Context appContext, RunDistance distanceToRun) {
        this.appContext = appContext;
        this.distanceToRun = distanceToRun;
    }

    public RunManager prepareModule() {
        overrideServiceConnectionMethods();
        activateLocationService();
        return this;
    }

    public void startRun() {
        if (locationModuleBounded)
            locationModule.startRun();
    }

    public boolean isRunOver() {
        return locationModuleBounded && locationModule.isRunOver();
    }

    public RunResult getRunResult() {
        return locationModuleBounded ? locationModule.getCurrentRunResult() : null;
    }

    public void stopModule() {
        if (locationModuleBounded) {
            appContext.unbindService(locationServiceConnection);
            locationModuleBounded = false;
            locationModule.stopSelf();
        }
    }

    private void overrideServiceConnectionMethods() {
        locationServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                bindWithLocationService(iBinder);
                locationModuleBounded = true;
                locationModule.startLocationModule();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                locationModule = null;
            }
        };

    }

    public int getFixedSatellitesNumber() {
        return locationModuleBounded ? locationModule.getFixedSatellitesNumber() : 0;
    }

    private void activateLocationService() {
        Intent locationServiceIntent = new Intent(appContext, LocationModule.class);
        locationServiceIntent.putExtra(IntentParams.DISTANCE_TO_RUN_PARAM, distanceToRun.getDistanceInMeters());
        appContext.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        appContext.startService(locationServiceIntent);
    }

    private void bindWithLocationService(IBinder iBinder) {
        if(locationModule == null) {
            LocationModule.LocalBinder binder = (LocationModule.LocalBinder) iBinder;
            locationModule = binder.getLocationModule();
        }
    }

}
