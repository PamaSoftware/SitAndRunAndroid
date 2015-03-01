package software.pama.sitandrunandroid.modules.run.helpers;

public class RunTimeCounter {

    private boolean firstTime = true;
    private long beginTimeMs;
    private long stepTimeMs;

    public void start() {
        if (firstTime) {
            beginTimeMs = System.currentTimeMillis();
            stepTimeMs = System.currentTimeMillis();
            firstTime = false;
        }
    }

    public void current() {
        stepTimeMs = System.currentTimeMillis();
    }

    public long countRunTimeMs() {
        return stepTimeMs - beginTimeMs;
    }

}
