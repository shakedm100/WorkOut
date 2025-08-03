package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a city, with names in the default locale and in English.
 * Implements Parcelable for inter-component passing.
 */
public class City implements Parcelable
{
    private String name;
    private String englishName;

    /**
     * Default no-argument constructor.
     */
    public City()
    {
    }

    /**
     * Constructs a City with the given name.
     * English name is initialized to empty.
     *
     * @param name the local name of the city
     */
    public City(String name)
    {
        this.name = name;
        englishName = "";
    }

    /**
     * Reconstructs a City from a Parcel.
     *
     * @param in the Parcel containing the serialized City
     */
    protected City(Parcel in)
    {
        name = in.readString();
        englishName = in.readString();
    }


    /**
     * Returns the local name of the city.
     *
     * @return the city name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the English name of the city.
     *
     * @return the English city name
     */
    public String getEnglishName()
    {
        return englishName;
    }

    /**
     * Sets the local name of the city.
     *
     * @param name the new local name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the English name of the city.
     *
     * @param englishName the new English name
     */
    public void setEnglishName(String englishName)
    {
        this.englishName = englishName;
    }

    /**
     * Two City objects are equal if both name and englishName match.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(name, city.name) && Objects.equals(englishName, city.englishName);
    }

    /**
     * Computes hash based on both name fields.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(name, englishName);
    }

    /**
     * Writes this City into a Parcel.
     *
     * @param dest  the Parcel to write into
     * @param flags additional flags (unused)
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(englishName);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Parcelable.Creator that generates City instances from a Parcel.
     */
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

    /**
     * Returns the English name for display purposes.
     *
     * @return englishName
     */
    @Override
    public String toString()
    {
        return englishName;
    }

}
