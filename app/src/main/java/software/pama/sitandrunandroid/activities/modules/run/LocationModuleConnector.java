package software.pama.sitandrunandroid.activities.modules.run;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.modules.run.LocationModule;
import software.pama.sitandrunandroid.modules.run.extras.LocationConstants;

/**
 * Odpowiedzialna za połączenie i obsługę {@link software.pama.sitandrunandroid.modules.run.LocationModule}.
 * Udostępnia informacje o całkowitym przebytym dystansie przez użytkownika.
 */
public class LocationModuleConnector {

    private Context appContext;
    private LocationModule locationModule;
    private ServiceConnection locationServiceConnection;
    private boolean locationModuleBounded = false;
    private RunDistance distanceToRun;

    public LocationModuleConnector(Context appContext, RunDistance distanceToRun) {
        this.appContext = appContext;
        this.distanceToRun = distanceToRun;
    }

    public void prepareModule() {
        overrideServiceConnectionMethods();
        activateLocationService();
    }

    public void startRun() {
        if (locationModuleBounded)
            locationModule.startRun();
    }

    public boolean isRunOver() {
        return locationModuleBounded ? locationModule.isRunOver() : false;
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
                locationModule.startLocationListener();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                locationModule = null;
            }
        };

    }

    public int getFixedSattelitesNumber() {
        return locationModuleBounded ? locationModule.getFixedSattelitesNumber() : 0;
    }

    private void activateLocationService() {
        Intent locationServiceIntent = new Intent(appContext, LocationModule.class);
        locationServiceIntent.putExtra(LocationConstants.DISTANCE_TO_RUN_EXTRA, distanceToRun.getDistanceInMeters());
        appContext.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        appContext.startService(locationServiceIntent);
    }

    private void bindWithLocationService(IBinder iBinder) {
        if(locationModule == null) {
            LocationModule.LocalBinder binder = (LocationModule.LocalBinder) iBinder;
            locationModule = binder.getLocationService();
        }
    }

}
