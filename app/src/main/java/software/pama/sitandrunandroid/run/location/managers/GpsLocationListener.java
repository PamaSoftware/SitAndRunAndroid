package software.pama.sitandrunandroid.run.location.managers;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;

import static android.location.LocationManager.GPS_PROVIDER;

// TODO przeliczać wynik na czas uśredniając końcówkę biegu
public class GpsLocationListener implements LocationListener, GpsStatus.Listener {

    // zmienić i sprawdzić czy jest niedokładne
    private static final int LOCATION_UPDATE_TIME_MS = 0;
    // TODO 1. można przekazywać accuracy i ewentualnie uśredniać wynik jeżeli jest zbyt duże
    // TODO 2. przetestować na normalnym biegu jak to funkcjonuje przy zmniejszonych wartościach, ustawić zmienna do wpisania na GUI
    // TODO zrobić dopasowanie dokładności do ilości satelit z któymi jest połączenie
    private static final int LOCATION_ACCURACY_M = 12;
    public static final int INITIAL_LOCATION_UPDATE_M = 2 * LOCATION_ACCURACY_M;
    private float locationUpdateDistanceM = 0;
    private static final float BOARD_LINE_FOR_LOCATION = 5;

    private Context appContext;
    private Location currentLocation;
    private LocationManager locationManager;
    private int fixedSattelitesNumber;
    private boolean firstTime = true;

    public GpsLocationListener(Context appContext) {
        this.appContext = appContext;
    }

    public void startLocationListener() {
        locationManager = (android.location.LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        // TODO sprawdzić jak niedokładny jest GPS przy dużym zachmurzeniu, deszczu
        locationManager.requestLocationUpdates(GPS_PROVIDER, LOCATION_UPDATE_TIME_MS, 0, this);
        locationManager.addGpsStatusListener(this);
    }

    public int getFixedSatellitesNumber() {
        return fixedSattelitesNumber;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void stopListening() {
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(this);
    }

    public void lowerLocationUpdateDistance(float lowerPercentage) {
        float result = locationUpdateDistanceM - locationUpdateDistanceM * lowerPercentage;
        if (result >= BOARD_LINE_FOR_LOCATION) {
            locationUpdateDistanceM = result;
            Log.i("GpsLocationListener", "Location update distance changed to " + result);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("GpsLocationListener", "User location changed. New Location: " + location);
        if (isAccurate(location) && isFarEnough(location)) {
            if (firstTime) {
                // location is fixed and accurate so we can start running
                locationUpdateDistanceM = INITIAL_LOCATION_UPDATE_M;
                firstTime = false;
            }
            currentLocation = location;
        }
    }

    private boolean isFarEnough(Location location) {
        if (location == null)
            return false;
        if (currentLocation == null)
            return true;
        float distance = currentLocation.distanceTo(location);
        Log.d("GpsLocationListener", "Location far enough: " + distance);
        return distance > locationUpdateDistanceM;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("GpsLocationListener", "Gps Disabled. Provder: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("GpsLocationListener", "Gps Enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    // TODO wyłączyć ten element jeśli bieg się rozpoczął
    // TODO im później pobieram satelity tym większą niedokładność akceptuję (jeśli sprawdzę że użytkownik biegnie).
    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if (gpsStatus == null)
            return;
        fixedSattelitesNumber = 0;
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        for (GpsSatellite satellite : satellites)
            if (satellite.usedInFix())
                fixedSattelitesNumber++;
        Log.i("GpsLocationListener", "Sattelites number: " + fixedSattelitesNumber);
    }

    private boolean isAccurate(Location location) {
        if (location == null)
            return false;
        Log.d("GpsLocationListener", "Location accuracy: " + location.getAccuracy());
        return location.getAccuracy() > 0.0 && location.getAccuracy() <= LOCATION_ACCURACY_M;
    }

}
