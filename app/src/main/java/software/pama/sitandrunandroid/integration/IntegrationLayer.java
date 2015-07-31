package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunStartInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

public interface IntegrationLayer {

    TheIntegrationLayer initialize(GoogleAccountCredential credential);

    Profile signIn() throws IOException;

    Profile signUp(String login) throws IOException;

    boolean deleteAccount() throws IOException;

    RunStartInfo startRunWithRandom(Preferences preferences) throws IOException;

    RunStartInfo joinFriend(Preferences preferences) throws IOException;

    boolean hostRunWithFriend(String login, Preferences preferences) throws IOException;

    RunStartInfo startRunWithFriend(Preferences preferences) throws IOException;

    boolean checkIfHostAlive() throws IOException;

    RunResultPiece getEnemyResult(int forecast, RunResult myResult) throws IOException;

}
