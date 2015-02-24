package software.pama.sitandrunandroid.model;

// mozliwe ze to bedzie bardziej obfita klasa przechowujaca wiecej danych z zupelnie inna nazwa, np. RunMetadata
/**
 * Defines enum with distance
 */
public enum RunDistance {

    TWO (2000),
    FIVE (5000),
    TEN (10000);

    private int distanceInMeters;

    private RunDistance(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public int getDistanceInMeters() {
        return distanceInMeters;
    }
}
