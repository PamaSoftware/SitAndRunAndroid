package software.pama.sitandrunandroid.run.enemy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunResult;

public class EnemyResultManager extends Service {

    private boolean runOver = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startRun() {

    }


    public RunResult getCurrentResult() {

        return null;
    }


    public void stopRun() {
        runOver = true;
    }

    public boolean isRunOver() {
        return runOver;
    }

}

