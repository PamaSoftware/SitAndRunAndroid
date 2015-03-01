package software.pama.sitandrunandroid.model;

public class RunResult {

    private long totalDistance;
    private long totalTime;

    public RunResult(long totalDistance, long totalTime) {
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
