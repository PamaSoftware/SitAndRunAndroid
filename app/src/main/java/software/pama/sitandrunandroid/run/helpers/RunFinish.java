package software.pama.sitandrunandroid.run.helpers;

import software.pama.sitandrunandroid.model.RunResult;

// napisaæ ¿e na pocz¹tku ta klasam ia³¹ byæ enumem, jednak podejrzewa³em ¿e
// mo¿e potrzebowaæ wiêcej ni¿ tylko dwa booleany, wiêc zrobi³em z tego klasê
public class RunFinish {

    private boolean runOver = false;
    private boolean userWon = false;

    public boolean isRunOver() {
        return runOver;
    }

    public void setRunOver(boolean runOver) {
        this.runOver = runOver;
    }

    public boolean userWon() {
        return userWon;
    }

    public void setUserWon(boolean userWon) {
        this.userWon = userWon;
    }
}
