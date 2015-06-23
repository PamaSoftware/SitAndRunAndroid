package software.pama.sitandrunandroid.run;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.user.ResultManager;
import software.pama.sitandrunandroid.run.user.location.intent.IntentParams;

public class RunConnector {

    private Context context;
    private RunDistance runDistance;
    private ResultManager resultManager;
    private boolean userResultManagerBounded = false;
    private ServiceConnection userResultManagerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(resultManager == null) {
                ResultManager.LocalBinder binder = (ResultManager.LocalBinder) iBinder;
                resultManager = binder.getUserResultManager();
            }
            userResultManagerBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            resultManager = null;
        }
    };

    public RunConnector(Context context, RunDistance distanceToRun) {
        this.context = context;
        this.runDistance = distanceToRun;
        activateLocationService();
    }
    public void startRun() {
        if (userResultManagerBounded)
            resultManager.startRun();
    }

    public RunResult getUserResultForNow() {
        if (userResultManagerBounded)
            return resultManager.getUserResult();
        return null;
    }

    public RunResult getEnemyResultForNow() {
        if (userResultManagerBounded)
            return resultManager.getEnemyResult();
        return null;
    }

    public boolean isRunOver() {
        if (userResultManagerBounded)
            return resultManager.isRunOver();
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
        Intent locationServiceIntent = new Intent(context, ResultManager.class);
        locationServiceIntent.putExtra(IntentParams.DISTANCE_TO_RUN_PARAM, runDistance.getDistanceInMeters());
        context.bindService(locationServiceIntent, userResultManagerConnection, Context.BIND_AUTO_CREATE);
        context.startService(locationServiceIntent);
    }

}
