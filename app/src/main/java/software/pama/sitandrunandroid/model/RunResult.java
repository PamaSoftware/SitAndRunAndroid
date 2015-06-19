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

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        RunResult other = (RunResult) obj;
        return totalDistance == other.totalDistance && totalTime == other.totalTime;
    }

    public boolean greaterThan(RunResult other) {
        if (other == null)
            return true;
        return totalDistance > other.getTotalDistance() && totalTime > other.getTotalTime();
    }

}
