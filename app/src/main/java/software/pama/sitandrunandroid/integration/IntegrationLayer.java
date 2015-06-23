package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

/**
 * Created by marek_000 on 2015-06-16.
 */
public interface IntegrationLayer {

    Profile signIn() throws IOException;

    Profile signUp(String login) throws IOException;

    boolean deleteAccount() throws IOException;

    int startRunWithRandom(Preferences preferences) throws IOException;

    int joinFriend(Preferences preferences) throws IOException;

    boolean hostRunWithFriend(String login, Preferences preferences) throws IOException;

    int startRunWithFriend(Preferences preferences) throws IOException;

    boolean checkIfHostAlive() throws IOException;

    RunResultPiece getEnemyResult(int forecast, RunResult myResult) throws IOException;

}
