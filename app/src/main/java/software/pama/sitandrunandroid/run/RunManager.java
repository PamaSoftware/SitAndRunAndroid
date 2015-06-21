package software.pama.sitandrunandroid.run;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.user.UserResultManager;
import software.pama.sitandrunandroid.run.user.location.intent.IntentParams;

public class RunManager {

    private Context context;
    private RunDistance runDistance;
    private UserResultManager userResultManager;
    private boolean userResultManagerBounded = false;
    private ServiceConnection userResultManagerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(userResultManager == null) {
                UserResultManager.LocalBinder binder = (UserResultManager.LocalBinder) iBinder;
                userResultManager = binder.getUserResultManager();
            }
            userResultManagerBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            userResultManager = null;
        }
    };

    public RunManager(Context context, RunDistance distanceToRun) {
        this.context = context;
        this.runDistance = distanceToRun;
        activateLocationService();
    }
    public void startRun() {
        if (userResultManagerBounded)
            userResultManager.startRun();
    }

    public RunResult getUserResult() {
        if (userResultManagerBounded)
            return userResultManager.getCurrentResult();
        return null;
    }

    public RunResult getEnemyResult() {

        return null;
    }

    public boolean isRunOver() {
        if (userResultManagerBounded)
            return userResultManager.isRunOver();
        return false;
    }

    public int getSatellitesNumber() {
        // TODO
        return 0;
    }

    public void stopModule() {
        if (userResultManagerBounded)
            context.unbindService(userResultManagerConnection);
    }

    private void activateLocationService() {
        Intent locationServiceIntent = new Intent(context, UserResultManager.class);
        locationServiceIntent.putExtra(IntentParams.DISTANCE_TO_RUN_PARAM, runDistance.getDistanceInMeters());
        context.bindService(locationServiceIntent, userResultManagerConnection, Context.BIND_AUTO_CREATE);
        context.startService(locationServiceIntent);
    }

}
