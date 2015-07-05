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

    public static final RunResult EMPTY_RESULT = new RunResult(0, 0);
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
        return EMPTY_RESULT;
    }

    public RunResult getEnemyResultForNow() {
        if (userResultManagerBounded)
            return resultManager.getEnemyResult();
        return EMPTY_RESULT;
    }

    public long getRunTime() {
        if (userResultManagerBounded)
            return resultManager.getRunTime();
        return 0;
    }

    public RunFinish getRunFinish() {
        if (userResultManagerBounded)
            return resultManager.getRunFinish();
        return new RunFinish();
    }

    public int getSatellitesNumber() {
        // TODO
        return 0;
    }

    public void stopModule() {
        if (userResultManagerBounded) {
            resultManager.stopRun();
            context.unbindService(userResultManagerConnection);
        }
    }

    private void activateLocationService() {
        Intent locationServiceIntent = new Intent(context, ResultManager.class);
        locationServiceIntent.putExtra(IntentParams.DISTANCE_TO_RUN_PARAM, runDistance.getDistanceInMeters());
        context.bindService(locationServiceIntent, userResultManagerConnection, Context.BIND_AUTO_CREATE);
        context.startService(locationServiceIntent);
    }

}
