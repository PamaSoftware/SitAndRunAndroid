package software.pama.sitandrunandroid.run.helpers;

import software.pama.sitandrunandroid.model.RunResult;

// napisa� �e na pocz�tku ta klasam ia�� by� enumem, jednak podejrzewa�em �e
// mo�e potrzebowa� wi�cej ni� tylko dwa booleany, wi�c zrobi�em z tego klas�
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
