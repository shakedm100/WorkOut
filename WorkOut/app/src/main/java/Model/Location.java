package Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable
{
    private long longitude;
    private long latitude;

    public Location() {}
    public Location(long longitude, long latitude)
    {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getLatitude()
    {
        return latitude;
    }

    public long getLongitude()
    {
        return longitude;
    }

    public void setLatitude(long latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(long longitude)
    {
        this.longitude = longitude;
    }

    protected Location(Parcel in)
    {
        longitude = in.readLong();
        latitude = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(longitude);
        dest.writeLong(latitude);
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
