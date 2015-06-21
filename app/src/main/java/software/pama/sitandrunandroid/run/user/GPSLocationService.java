package software.pama.sitandrunandroid.run.user;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import software.pama.sitandrunandroid.run.user.location.gps.GpsLocationListener;

public class GPSLocationService extends Service {

    private GpsLocationListener gpsLocationListener;
    private IBinder localBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsLocationListener = new GpsLocationListener(this);
        Toast.makeText(this, "GPSLocationService started", Toast.LENGTH_SHORT).show();
        Log.d("SERVICE", "GPSLocationService started");
        return START_STICKY;
    }

    public void start() {
        gpsLocationListener.startLocationListener();
    }

    public int getSatellitesNumber() {
        return gpsLocationListener.getFixedSatellitesNumber();
    }

    public Location getCurrentLocation() {
        return gpsLocationListener.getCurrentLocation();
    }

    @Override
    public void onDestroy() {
        Log.d("SERVICE", "GPSLocationService stopped");
        gpsLocationListener.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public GPSLocationService getLocationModule() {
            return GPSLocationService.this;
        }
    }


}
