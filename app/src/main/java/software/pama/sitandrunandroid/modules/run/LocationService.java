package software.pama.sitandrunandroid.modules.run;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

/**
 * Serwis pobierający lokalizację użytkownika zarówno gdy aplikacja jest w trakcie działania
 * jak i wtedy gdy jest zatrzymana.
 */
// TODO w jednym z artykułów było opisane że GSP potrzbeuje bezpośredniego widoku z satelity, AGPS nie potrzebuje
public class LocationService extends Service implements LocationListener
{
    // TODO wrzucić to do pliku konfiguracyjnego
    private static final int LOCATION_UPDATE_TIME_MS = 1000;
    private static final int LOCATION_UPDATE_DIST_M = 15;
    // TODO 1. można przekazywać accuracy i ewentualnie uśredniać wynik jeżeli jest zbyt duże
    // TODO 2. przetestować na normalnym biegu jak to funkcjonuje przy zmniejszonych wartościach, ustawić zmienna do wpisania na GUI
    private static final int LOCATION_ACCURACY_M = 20;

    private Location currentLocation;
    /** Zapewnia dostęp do usług lokalizacji. */
    private LocationManager locationManager;
    /** Pozwala powiązać serwis z aktywnością. */
    private IBinder localBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setLocationManager();
        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    private void setLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // TODO sprawdzić czy GPS Provider prawidłowo działa podczas złej pogody, zachmurzenia
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME_MS, LOCATION_UPDATE_DIST_M, this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("*********************", "Location changed");
        if (isAccurate(location))
            currentLocation = location;
    }

    private boolean isAccurate(Location location) {
        if (location == null)
            return false;
        Log.d("D", "Location accuracy: " + location.getAccuracy());
        if(location.getAccuracy() > 0.0 && location.getAccuracy() < LOCATION_ACCURACY_M) {
            return true;
        }
        return false;
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    /**
     * Pozwala powiązać usługę z aktywnością.
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void onDestroy() {
        Log.i("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public LocationService getLocationService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

}
