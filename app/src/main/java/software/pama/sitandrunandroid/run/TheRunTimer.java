package software.pama.sitandrunandroid.run;

public class TheRunTimer {

    private long runTimeStart;

    private TheRunTimer() {}

    private static class RunTimerHolder {
        private static final TheRunTimer RUN_TIMER_INSTANCE = new TheRunTimer();
    }

    public static TheRunTimer getInstance() {
        return RunTimerHolder.RUN_TIMER_INSTANCE;
    }

    public void start() {
        runTimeStart = System.currentTimeMillis();
    }

    public long getRunDuration() {
        return System.currentTimeMillis() - runTimeStart;
    }
}
