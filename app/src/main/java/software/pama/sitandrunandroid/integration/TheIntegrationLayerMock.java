package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

public class TheIntegrationLayerMock implements IntegrationLayer {

    public static final String TEST = "TEST";
    public static final int COUNTDOWN = 5;
    private int time = 0;
    private int distance = 10;

    private TheIntegrationLayerMock() {}

    private static class TheIntgerationLayerHolder {
        private final static TheIntegrationLayerMock instance = new TheIntegrationLayerMock();
    }

    public static TheIntegrationLayerMock getInstance() {
        return TheIntgerationLayerHolder.instance;
    }

    @Override
    public Profile signIn() throws IOException {
        return new Profile().setLogin(TEST);
    }

    @Override
    public Profile signUp(String login) throws IOException {
        return new Profile().setLogin(TEST);
    }

    @Override
    public boolean deleteAccount() throws IOException {
        return true;
    }

    @Override
    public int startRunWithRandom(Preferences preferences) throws IOException {
        return COUNTDOWN;
    }

    @Override
    public int joinFriend(Preferences preferences) throws IOException {
        return COUNTDOWN;
    }

    @Override
    public boolean hostRunWithFriend(String login, Preferences preferences) throws IOException {
        return true;
    }

    @Override
    public int startRunWithFriend(Preferences preferences) throws IOException {
        return COUNTDOWN;
    }

    @Override
    public boolean checkIfHostAlive() throws IOException {
        return true;
    }

    @Override
    public RunResultPiece getEnemyResult(int forecast, RunResult myResult) throws IOException {
        distance += 10;
        time += 1;
        return new RunResultPiece().setDistance(distance).setTime(time);
    }

}
