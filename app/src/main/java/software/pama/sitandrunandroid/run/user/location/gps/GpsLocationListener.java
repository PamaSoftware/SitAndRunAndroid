package software.pama.sitandrunandroid.run.user.location.gps;

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
    private static final int LOCATION_UPDATE_TIME_MS = 1000;
    public static final int LOCATION_UPDATE_DIST = 0;
    // TODO 1. można przekazywać accuracy i ewentualnie uśredniać wynik jeżeli jest zbyt duże
    // TODO 2. przetestować na normalnym biegu jak to funkcjonuje przy zmniejszonych wartościach, ustawić zmienna do wpisania na GUI
    // TODO zrobić dopasowanie dokładności do ilości satelit z któymi jest połączenie
    private Context appContext;
    private Location currentLocation;
    private LocationManager locationManager;
    private int fixedSatellitesNumber;

    public GpsLocationListener(Context appContext) {
        this.appContext = appContext;
        this.locationManager = (android.location.LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationListener() {
        locationManager.requestLocationUpdates(GPS_PROVIDER, LOCATION_UPDATE_TIME_MS, LOCATION_UPDATE_DIST, this);
        locationManager.addGpsStatusListener(this);
    }

    public int getFixedSatellitesNumber() {
        return fixedSatellitesNumber;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void stop() {
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("GpsLocationListener", "User location changed. New Location: " + location);
        currentLocation = location;
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
    // TODO im później pobieram satelity tym większą niedokładność akceptuję (jeśli sprawdzę że użytkownik nie? biegnie).
    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if (gpsStatus == null)
            return;
        fixedSatellitesNumber = 0;
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        for (GpsSatellite satellite : satellites)
            if (satellite.usedInFix())
                fixedSatellitesNumber++;
        Log.i("GpsLocationListener", "Sattelites number: " + fixedSatellitesNumber);
    }

}
