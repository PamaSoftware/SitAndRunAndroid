package software.pama.sitandrunandroid.model;

public class RunResult {

    private float totalDistance;
    // zmieniæ na int
    private long totalTimeMillis;

    public RunResult(float totalDistance, long totalTimeMillis) {
        this.totalDistance = totalDistance;
        this.totalTimeMillis = totalTimeMillis;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    public long getTotalTimeSeconds() {
        return totalTimeMillis/1000;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        RunResult other = (RunResult) obj;
        return totalDistance == other.totalDistance && totalTimeMillis == other.totalTimeMillis;
    }

    public boolean greaterThan(RunResult other) {
        if (other == null)
            return true;
        return totalDistance > other.getTotalDistance() || totalTimeMillis > other.totalTimeMillis;
    }

}
