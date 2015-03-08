package software.pama.sitandrunandroid.modules.run.managers;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import java.util.Random;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.modules.run.helpers.RunTimeCounter;

public class RunManager extends Thread {

    private int distanceToRun;
    private long totalDistance;
    private Location currentLocation;
    private LocationListenerManager locationListenerManager;
    private boolean runOver;
    private Handler threadHandler;
    private RunTimeCounter runTimeCounter;

    public RunManager(int distanceToRun, LocationListenerManager locationListenerManager, Handler threadHandler) {
//        this.distanceToRun = distanceToRun;
        this.distanceToRun = 200;
        this.locationListenerManager = locationListenerManager;
        this.threadHandler = threadHandler;
        this.runTimeCounter = new RunTimeCounter();
    }

    @Override
    public void run() {
        // TODO pomyśleć nad wyrzuceniem tego stad zeby oszczedzic na ilosci operacji
        runTimeCounter.start();
        Location newLocation = locationListenerManager.getCurrentLocation();
        if (newLocation != null) {
            if (currentLocation != null)
                totalDistance += currentLocation.distanceTo(newLocation);
            currentLocation = newLocation;
            runTimeCounter.current();
            float edge = (distanceToRun - totalDistance) / (float) distanceToRun;
            if (edge < 0.05) {
                locationListenerManager.lowerLocationUpdateDistance(0.33f);
            }
//            totalDistance += new Random().nextInt(10) + 1;
            Log.d("Finish Counter", "Total distance: " + totalDistance);
            if (distanceToRun - totalDistance < 0) {
                runOver = true;
                locationListenerManager.stopListening();
                return;
            }
        }
        threadHandler.postDelayed(this, 1000);
    }

    public RunResult getRunResult() {
        return new RunResult(totalDistance, runTimeCounter.countRunTimeMs());
    }

    public boolean isRunOver() {
        return runOver;
    }

}
