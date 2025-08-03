package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Geographic coordinates with latitude and longitude.
 * Implements {@link Parcelable} for Android IPC.
 */
public class Location implements Parcelable
{
    private double longitude;
    private double latitude;

    /**
     * Default no-arg constructor for Firestore.
     */
    public Location()
    {
    }

    /**
     * Constructs a Location with the given coordinates.
     *
     * @param longitude longitudinal coordinate
     * @param latitude  latitudinal coordinate
     */
    public Location(double longitude, double latitude)
    {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * @return the latitude
     */
    public double getLatitude()
    {
        return latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude()
    {
        return longitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    /** Recreates a Location from a Parcel. */
    protected Location(Parcel in)
    {
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        Location location = (Location) o;
        return longitude == location.longitude && latitude == location.latitude;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(longitude, latitude);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    /** Parcelable.Creator that generates Location instances from a Parcel. */
    public static final Creator<Location> CREATOR = new Creator<Location>()
    {
        @Override
        public Location createFromParcel(Parcel in)
        {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size)
        {
            return new Location[size];
        }
    };
}
