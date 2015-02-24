package software.pama.sitandrunandroid.modules.run;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;

import software.pama.sitandrunandroid.model.RunDistance;

/**
 * Odpowiedzialna za połączenie i obsługę {@link LocationService}.
 * Udostępnia informacje o przebytym dystansie przez użytkownika.
 */
// TODO przerobić na serwis który ogarnia bieganie ze wszystkimi aspektami
public class LocationServiceManager extends Service {

    /** Kontekst aplikacji, umożliwia uruchomienie serwisu. */
    private Context appContext;
    /** Określa dystans po którym serwis mierzący bieg ma być zamknięty. */
    private RunDistance runDistance;
    /** Serwis otrzymujący uaktualnienia lokalizacji. */
    private LocationService locationService;
    /** Połączenie z LocationService. */
    private ServiceConnection locationServiceConnection;
    /** Bieżąca lokalizacja użytkownika. */
    private Location currentLocation;
    /** Całkowity przebyty dystans. */
    private float userTotalDistance = 0f;
    /** Wartość definiująca czy istnieje połączenie z serwisem. */
    private boolean locationServiceBounded = false;


    /**
     * Tworzy obiekt LocationServiceManager z dodatkową inicjalizacją metod
     * odpowiedzialnych za połączenia z {@link LocationService}.
     *
     * @param appContext kontekst aplikacji pozwalający uruchomić serwis.
     */
    public LocationServiceManager(Context appContext, RunDistance runDistance) {
        this.appContext = appContext;
        this.runDistance = runDistance;
        overrideServiceConnectionMethods();
    }

    /**
     * Uruchamia {@link LocationService}.
     *
     * @see #overrideServiceConnectionMethods()
     * @see #stopLocationService()
     */
    public void startLocationService() {
        Intent locationServiceIntent = new Intent(appContext, LocationService.class);
        appContext.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        appContext.startService(locationServiceIntent);
    }

    /**
     * Zatrzymuje {@link LocationService}.
     * Konieczne wywołanie w przypadku niszczenia aktywności korzystającej z serwisu
     * aby serwis nie działał bez końca.
     */
    public void stopLocationService() {
        if (isLocationServiceBounded()) {
            appContext.unbindService(locationServiceConnection);
            setLocationServiceBounded(false);
            locationService.stopSelf();
        }
    }

    public void updateLocationDetails() {
        if(isLocationServiceBounded()) {
            Location location = locationService.getCurrentLocation();
            countTotalDistance(location);
            setCurrentLocation(location);
        }
    }

    /**
     * Pobiera całkowity dystans przebyty przez użytkownika.
     * Aby dane były aktualne wymagane jest użycie metody updateLocationDetails().
     *
     * @see #updateLocationDetails()
     */
    public float getUserTotalDistance() {
        return userTotalDistance;
    }

    /**
     * Nadpisuje metody onServiceConnected i onServiceDisconnected
     * z interfejsu {@link ServiceConnection}.
     * Metody te zapewniają poprawne funkcjonowanie komunikacji z serwisem.
     */
    private void overrideServiceConnectionMethods() {
        locationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                setupLocationService(iBinder);
                setLocationServiceBounded(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                locationService = null;
            }
        };
    }

    /**
     * Tworzy połączenie z {@link LocationService}.
     *
     * @param iBinder obiekt przekazywany przez serwis służący do pobrania instancji serwisu.
     */
    private void setupLocationService(IBinder iBinder) {
        if(locationService == null) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) iBinder;
            locationService = binder.getLocationService();
        }
    }

    /**
     * Aktualizuje całkowity dystans korzystając z informacji o nowej lokalizacji.
     */
    private void countTotalDistance(Location newLocation) {
        if(currentLocation != null) {
            float dist = currentLocation.distanceTo(newLocation);
            userTotalDistance += dist;
        }
    }

    private void setLocationServiceBounded(boolean locationServiceBounded) {
        this.locationServiceBounded = locationServiceBounded;
    }

    private void setCurrentLocation(Location newLocation) {
        currentLocation = newLocation;
    }

    /**
     * Informuje czy jest połączenie z {@link LocationService}.
     */
    private boolean isLocationServiceBounded() {
        return locationServiceBounded;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
