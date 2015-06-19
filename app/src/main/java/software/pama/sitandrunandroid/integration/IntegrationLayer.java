package software.pama.sitandrunandroid.integration;

import com.appspot.formidable_code_826.sitAndRunApi.model.Preferences;
import com.appspot.formidable_code_826.sitAndRunApi.model.Profile;
import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;
import com.appspot.formidable_code_826.sitAndRunApi.model.WrappedInteger;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

import software.pama.sitandrunandroid.model.RunResult;

/**
 * Created by marek_000 on 2015-06-16.
 */
public interface IntegrationLayer {

    public Profile signIn() throws IOException;

    public Profile signUp(String login) throws IOException;

    public boolean deleteAccount() throws IOException;

    public int startRunWithRandom(Preferences preferences) throws IOException;

    public int joinFriend(Preferences preferences) throws IOException;

    public boolean hostRunWithFriend(String login, Preferences preferences) throws IOException;

    public int startRunWithFriend(Preferences preferences) throws IOException;

    public boolean checkIfHostAlive() throws IOException;

    public RunResultPiece getEnemyResult(int forecast, RunResult myResult) throws IOException;

}
