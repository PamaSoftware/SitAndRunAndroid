package software.pama.sitandrunandroid.run.user.location.helpers;

public class RunTimeCounter {

    private long beginTimeMs;
    private long stepTimeMs;

    public void start() {
        beginTimeMs = System.currentTimeMillis();
        stepTimeMs = System.currentTimeMillis();
    }

    public void step() {
        stepTimeMs = System.currentTimeMillis();
    }

    public long totalTime() {
        return stepTimeMs - beginTimeMs;
    }

}
