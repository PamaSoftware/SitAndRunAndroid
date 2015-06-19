package software.pama.sitandrunandroid.integration.communication;

import com.appspot.formidable_code_826.sitAndRunApi.SitAndRunApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;

public class EndpointBuilder {

    /**
     * Class instance of the JSON factory.
     */
    public static final GsonFactory JSON_FACTORY = new GsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    /**
     * Retrieve a Helloworld api service handle to access the API.
     */
    public static SitAndRunApi getApiServiceHandle(GoogleAccountCredential credential) {
        // Use a builder to help formulate the API request.
        return new SitAndRunApi.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
//            .setRootUrl("http://localhost:8080/_ah/api/")
//        // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
//        // otherwise they can be skipped
////            .setRootUrl("http://localhost:8080/_ah/api/")
//            .setApplicationName("SitAndRun")
//            .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                @Override
//                public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
//                        throws IOException {
//                    abstractGoogleClientRequest.setDisableGZipContent(true);
//                }
//            })
//            .build();
    }

}