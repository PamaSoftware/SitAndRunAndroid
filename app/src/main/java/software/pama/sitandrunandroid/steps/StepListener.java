package software.pama.sitandrunandroid.steps;

public interface StepListener {

    /**
     * Called when the StepDetector detects a step. Based on the sensitivity
     * setting.
     */
    void onStep();
}
