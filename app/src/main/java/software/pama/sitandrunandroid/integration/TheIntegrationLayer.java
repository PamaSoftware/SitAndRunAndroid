package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.SitAndRunApi;
import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.Collections;

import software.pama.sitandrunandroid.integration.communication.EndpointBuilder;
import software.pama.sitandrunandroid.model.RunResult;

public class TheIntegrationLayer implements IntegrationLayer {

    private static SitAndRunApi endpointAPI;

    private TheIntegrationLayer() {}

    private static class TheIntgerationLayerHolder {
        private final static TheIntegrationLayer instance = new TheIntegrationLayer();
    }

    public static TheIntegrationLayer getInstance() {
        return TheIntgerationLayerHolder.instance;
    }

    public TheIntegrationLayer initialize(GoogleAccountCredential credential) {
        endpointAPI = EndpointBuilder.getApiServiceHandle(credential);
        return TheIntgerationLayerHolder.instance;
    }

    @Override
    public Profile signIn() throws IOException {
        return endpointAPI.signIn().execute();
    }

    @Override
    public Profile signUp(String login) throws IOException {
        return endpointAPI.signUp(login).execute();
    }

    @Override
    public boolean deleteAccount() throws IOException {
        return !endpointAPI.deleteAccount().execute().getResult();
    }

    @Override
    public int startRunWithRandom(Preferences preferences) throws IOException {
         return endpointAPI.startRunWithRandom(preferences).execute().getResult();
    }

    @Override
    public int joinFriend(Preferences preferences) throws IOException {
        return endpointAPI.joinRunWithFriend(preferences).execute().getResult();
    }

    @Override
    public boolean hostRunWithFriend(String login, Preferences preferences) throws IOException {
        return endpointAPI.hostRunWithFriend(login, preferences).execute().getResult();
    }

    @Override
    public int startRunWithFriend(Preferences preferences) throws IOException {
        return endpointAPI.startRunWithFriend(preferences).execute().getResult();
    }

    @Override
    public boolean checkIfHostAlive() throws IOException {
        return endpointAPI.checkIfHostAlive().execute().getResult();
    }

    @Override
    public RunResultPiece getEnemyResult(int forecast, RunResult myResult) throws IOException {
        // zrobiæ w klasie RunResult metodê toRunResultPiece
        RunResultPiece runInfoPiece = new RunResultPiece();
        runInfoPiece.setDistance((int) myResult.getTotalDistance());
        runInfoPiece.setTime((int)myResult.getTotalTime()/1000);
        com.appspot.formidable_code_826.sitAndRunApi.model.RunResult runResult =
                new com.appspot.formidable_code_826.sitAndRunApi.model.RunResult().setResults(
                Collections.singletonList(runInfoPiece));
        return endpointAPI.currentRunState(forecast, runResult).execute();
    }

}
