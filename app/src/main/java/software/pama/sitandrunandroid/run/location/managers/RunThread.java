package software.pama.sitandrunandroid.run.location.managers;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.location.helpers.RunTimeCounter;
import software.pama.sitandrunandroid.run.location.managers.GpsLocationListener;

public class RunThread extends Thread {

    private int i = 0;
    private int distanceToRun;
    private long totalDistance;
    private Location currentLocation;
    private GpsLocationListener gpsLocationListener;
    private boolean runOver;
    private Handler threadHandler;
    private RunTimeCounter runTimeCounter;

    public RunThread(int distanceToRun, GpsLocationListener gpsLocationListener, Handler threadHandler) {
//        this.distanceToRun = distanceToRun;
        this.distanceToRun = 500;
        this.gpsLocationListener = gpsLocationListener;
        this.threadHandler = threadHandler;
        this.runTimeCounter = new RunTimeCounter();
    }

    @Override
    public void run() {
        // TODO pomyśleć nad wyrzuceniem tego stad zeby oszczedzic na ilosci operacji
        runTimeCounter.start();
        Location newLocation = gpsLocationListener.getCurrentLocation();
        if (newLocation != null) {
            if (currentLocation != null)
                totalDistance += currentLocation.distanceTo(newLocation);
            currentLocation = newLocation;
            runTimeCounter.current();
            float edge = (distanceToRun - totalDistance) / (float) distanceToRun;
            if (edge < 0.05) {
                gpsLocationListener.lowerLocationUpdateDistance(0.33f);
            }
//            totalDistance += new Random().nextInt(10) + 1;
            Log.d("Finish Counter", "Total distance: " + totalDistance);
            if (distanceToRun - totalDistance < 0) {
                runOver = true;
                gpsLocationListener.stopListening();
                return;
            }
        }
        threadHandler.postDelayed(this, 1000);
    }

    public RunResult getRunResult() {
        long totalTime = runTimeCounter.countRunTimeMs();

        // testowo
        totalDistance += 100;
        totalTime += (i = i + 1000);

        return new RunResult(totalDistance, totalTime);
    }

    public boolean isRunOver() {
        return runOver;
    }

}
