package software.pama.sitandrunandroid.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import software.pama.sitandrunandroid.R;
import software.pama.sitandrunandroid.model.RunDistance;
import software.pama.sitandrunandroid.modules.run.LocationServiceManager;

public class TmpRunActivity extends Activity {

    /** Czas określający co ile ms zaktualizować wyświetlane dane. */
    private final long THREAD_UPDATE_MS = 2000;

    /** Przechowuje wątki aktywności. */
    private Handler threadHandler;
    /** Zapewnia komunikację z LocationService i jego obsługę. */
    private LocationServiceManager userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userLocation = new LocationServiceManager(getApplicationContext(), RunDistance.FIVE);
        threadHandler = new Handler();
    }

    public void startRunService(View view) {
        userLocation.startLocationService();
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

    /**
     * Wątek wyświetlający całkowitą lokalizację.
     */
    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            updateUI();
            delayThreadPost();
        }
    };

    private void delayThreadPost() {
        threadHandler.postDelayed(showLocation, THREAD_UPDATE_MS);
    }

    private void updateUI() {
        userLocation.updateLocationDetails();
        float totalDistance = userLocation.getUserTotalDistance();
        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtDistance.setText(Float.toString(totalDistance) + " m");
        Log.d("D", "Total distance: " + totalDistance);
    }

    private void stopThreads() {
        threadHandler.removeCallbacksAndMessages(showLocation);
    }

    private void stopLocationService() {
        userLocation.stopLocationService();
    }
}
