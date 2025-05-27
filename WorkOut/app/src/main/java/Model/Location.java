package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Location implements Parcelable
{
    private double longitude;
    private double latitude;

    public Location() {}
    public Location(double longitude, double latitude)
    {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    protected Location(Parcel in)
    {
        longitude = in.readLong();
        latitude = in.readLong();
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
