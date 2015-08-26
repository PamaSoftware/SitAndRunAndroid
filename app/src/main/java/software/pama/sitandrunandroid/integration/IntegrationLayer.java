package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.OpponentPositionInfo;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunPreferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunStartInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

public interface IntegrationLayer {

    TheIntegrationLayer initialize(GoogleAccountCredential credential);

    Profile signIn() throws IOException;

    Profile signUp(String login) throws IOException;

    boolean deleteAccount() throws IOException;

    RunStartInfo startRunWithRandom(RunPreferences preferences) throws IOException;

    RunStartInfo joinFriend(RunPreferences preferences) throws IOException;

    boolean hostRunWithFriend(String login, RunPreferences preferences) throws IOException;

    RunStartInfo startRunWithFriend(RunPreferences preferences) throws IOException;

    boolean checkIfHostAlive() throws IOException;

    OpponentPositionInfo getEnemyResult(int forecast, RunResult myResult) throws IOException;

}
