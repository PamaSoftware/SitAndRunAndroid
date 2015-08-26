package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.OpponentPositionInfo;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunPreferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunStartInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

public class TheIntegrationLayerMock implements IntegrationLayer {

    public static final String TEST = "TEST";
    public static final int COUNTDOWN = 3;
    public static final int DISTANCE = 100;
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
    public TheIntegrationLayer initialize(GoogleAccountCredential credential) {
        return null;
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
    public RunStartInfo startRunWithRandom(RunPreferences preferences) throws IOException {
        return new RunStartInfo().setDistance(DISTANCE).setTime(COUNTDOWN);
    }

    @Override
    public RunStartInfo joinFriend(RunPreferences preferences) throws IOException {
        return new RunStartInfo().setDistance(DISTANCE).setTime(COUNTDOWN);
    }

    @Override
    public boolean hostRunWithFriend(String login, RunPreferences preferences) throws IOException {
        return true;
    }

    @Override
    public RunStartInfo startRunWithFriend(RunPreferences preferences) throws IOException {
        return new RunStartInfo().setDistance(DISTANCE).setTime(COUNTDOWN);
    }

    @Override
    public boolean checkIfHostAlive() throws IOException {
        return true;
    }

    @Override
    public OpponentPositionInfo getEnemyResult(int forecast, RunResult myResult) throws IOException {
        distance += 1;
        time += 1;
        return new OpponentPositionInfo().setDistance(distance).setTime(time);
    }

}
