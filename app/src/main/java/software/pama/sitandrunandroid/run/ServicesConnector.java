package software.pama.sitandrunandroid.run;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.user.location.RunService;
import software.pama.sitandrunandroid.run.user.location.intent.IntentParams;
import software.pama.sitandrunandroid.run.user.steps.StepCounterService;

/**
 * Odpowiedzialna za połączenie i obsługę {@link RunService}.
 * Udostępnia informacje o całkowitym przebytym dystansie przez użytkownika.
 */
public class ServicesConnector {

    private Context appContext;
    private RunService runService;
    private StepCounterService stepCounterService;
    private ServiceConnection locationServiceConnection;
    private boolean locationModuleBounded = false;
    private RunDistance distanceToRun;

    public ServicesConnector(Context appContext, RunDistance distanceToRun) {
        this.appContext = appContext;
        this.distanceToRun = distanceToRun;
    }

    public ServicesConnector prepareModule() {
        overrideServiceConnectionMethods();
        activateLocationService();
        return this;
    }

    public void startRun() {
        if (locationModuleBounded)
            runService.startRun();
    }

    public boolean isRunOver() {
        return locationModuleBounded && runService.isRunOver();
    }

    public RunResult getRunResult() {
        return locationModuleBounded ? runService.getCurrentRunResult() : null;
    }

    public void stopModule() {
        if (locationModuleBounded) {
            appContext.unbindService(locationServiceConnection);
            locationModuleBounded = false;
            runService.stopSelf();
        }
    }

    private void overrideServiceConnectionMethods() {
        locationServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                bindWithLocationService(iBinder);
                locationModuleBounded = true;
                runService.startLocationModule();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                runService = null;
            }
        };

    }

    public int getFixedSatellitesNumber() {
        return locationModuleBounded ? runService.getFixedSatellitesNumber() : 0;
    }

    private void activateLocationService() {
        Intent locationServiceIntent = new Intent(appContext, RunService.class);
        locationServiceIntent.putExtra(IntentParams.DISTANCE_TO_RUN_PARAM, distanceToRun.getDistanceInMeters());
        appContext.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        appContext.startService(locationServiceIntent);
    }

    private void bindWithLocationService(IBinder iBinder) {
        if(runService == null) {
            RunService.LocalBinder binder = (RunService.LocalBinder) iBinder;
            runService = binder.getLocationModule();
        }
    }

}
