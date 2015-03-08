package software.pama.sitandrunandroid.activities.modules.run;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.model.RunResult;

public class TmpRunActivity extends Activity {

    private final long THREAD_UPDATE_MS = 2000;

    private Handler threadHandler;
    private LocationModuleConnector locationModuleConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationModuleConnector = new LocationModuleConnector(getApplicationContext(), RunDistance.FIVE);
        locationModuleConnector.prepareModule();
        threadHandler = new Handler();
        showSattelites.run();
    }

    public void startRunService(View view) {
        locationModuleConnector.startRun();
        showLocation.run();
    }

    @Override
    protected void onDestroy() {
        stopLocationService();
        stopThreads();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void delayThreadPost(Runnable runnable) {
        threadHandler.postDelayed(runnable, THREAD_UPDATE_MS);
    }

    private void updateUI() {
        RunResult result = locationModuleConnector.getRunResult();
        if (result == null)
            return;
        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        TextView txtTime = (TextView) findViewById(R.id.txtTime);
        txtDistance.setText(Float.toString(result.getTotalDistance()) + " m");
        long totalTime = result.getTotalTime();
        DateFormat formatter = new SimpleDateFormat("mm:ss:SSS");
        txtTime.setText(formatter.format(new Date(totalTime)) + " s");
        if (locationModuleConnector.isRunOver()) {
            TextView txtRunOver = (TextView) findViewById(R.id.txtRunOver);
            txtRunOver.setText("Run over!");
        }
    }

    private void stopThreads() {
        threadHandler.removeCallbacksAndMessages(showLocation);
    }

    private void stopLocationService() {
        locationModuleConnector.stopModule();
    }

    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            updateUI();
            delayThreadPost(this);
        }
    };

    private Runnable showSattelites = new Runnable() {
        @Override
        public void run() {
            TextView txtSattelites = (TextView) findViewById(R.id.txtSattelites);
            txtSattelites.setText("Sattelites: " + locationModuleConnector.getFixedSattelitesNumber());
            delayThreadPost(this);
        }
    };


}
