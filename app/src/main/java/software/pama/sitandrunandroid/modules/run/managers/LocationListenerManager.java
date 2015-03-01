package software.pama.sitandrunandroid.modules.run.managers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;

// TODO przeliczać wynik na czas uśredniając końcówkę biegu
// TODO w jednym z artykułów było opisane że GSP potrzbeuje bezpośredniego widoku z satelity, AGPS nie potrzebuje
public class LocationListenerManager implements LocationListener {

    // TODO wrzucić to do pliku konfiguracyjnego
    private static final int LOCATION_UPDATE_TIME_MS = 0;
    private static final int LOCATION_UPDATE_DIST_M = 15;
    // TODO 1. można przekazywać accuracy i ewentualnie uśredniać wynik jeżeli jest zbyt duże
    // TODO 2. przetestować na normalnym biegu jak to funkcjonuje przy zmniejszonych wartościach, ustawić zmienna do wpisania na GUI
    // TODO zrobić dopasowanie dokładności do ilości satelit z któymi jest połączenie
    private static final int LOCATION_ACCURACY_M = 13;

    private Context appContext;
    private Location currentLocation;
    private LocationManager locationManager;

    public LocationListenerManager(Context appContext) {
        this.appContext = appContext;
    }

    public void startLocationManager() {
        locationManager = (android.location.LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        // TODO sprawdzić czy GPS Provider prawidłowo działa podczas złej pogody, zachmurzenia
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME_MS, LOCATION_UPDATE_DIST_M, this);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void stopListening() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("LocationListenerManager", "User location changed. New Location: " + location);
        if (isAccurate(location)) {
            Log.d("LocationListenerManager", "accurate location");
            currentLocation = location;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LocationListenerManager", "Gps Disabled. Provder: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LocationListenerManager", "Gps Enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    private boolean isAccurate(Location location) {
        if (location == null)
            return false;
        Log.d("LocationListenerManager", "Location accuracy: " + location.getAccuracy());
        return location.getAccuracy() > 0.0 && location.getAccuracy() < LOCATION_ACCURACY_M;
    }

}
