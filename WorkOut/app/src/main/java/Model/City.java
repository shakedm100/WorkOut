package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class City implements Parcelable
{
    private String name;
    private String englishName;

    public City()
    {
    }

    public City(String name)
    {
        this.name = name;
        englishName = "";
    }

    protected City(Parcel in)
    {
        name = in.readString();
    }


    public String getName()
    {
        return name;
    }

    public String getEnglishName()
    {
        return englishName;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(name, city.name) && Objects.equals(englishName, city.englishName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, englishName);
    }

    public void setEnglishName(String englishName)
    {
        this.englishName = englishName;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags)
    {
        dest.writeString(name);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<City> CREATOR = new Creator<City>()
    {
        @Override
        public City createFromParcel(Parcel in)
        {
            return new City(in);
        }

        @Override
        public City[] newArray(int size)
        {
            return new City[size];
        }
    };


}
