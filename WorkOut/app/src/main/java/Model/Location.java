package Model;

public class Location
{
    private long longitude;
    private long latitude;

    public Location(long longitude, long latitude)
    {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }
}
