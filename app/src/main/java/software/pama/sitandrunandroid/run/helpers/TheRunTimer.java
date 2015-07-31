package software.pama.sitandrunandroid.run.helpers;

public class TheRunTimer {

    private long runTimeStartSystemMillis;

    private TheRunTimer() {}

    private static class RunTimerHolder {
        private static final TheRunTimer RUN_TIMER_INSTANCE = new TheRunTimer();
    }

    public static TheRunTimer getInstance() {
        return RunTimerHolder.RUN_TIMER_INSTANCE;
    }

    public void start() {
        runTimeStartSystemMillis = System.currentTimeMillis();
    }

    public long getRunDurationMillis() {
        return System.currentTimeMillis() - runTimeStartSystemMillis;
    }

}
