package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a postal address within a city.
 * Implements Parcelable so Address instances can be passed between Android components.
 */
public class Address implements Parcelable
{
    private City city;
    private String name;

    /**
     * Default no-argument constructor.
     */
    public Address()
    {
    }

    /**
     * Constructs an Address with the specified city and street/name.
     *
     * @param city the city of this address
     * @param name the street name or detailed address
     */
    public Address(City city, String name)
    {
        this.city = city;
        this.name = name;
    }

    /**
     * Reconstructs an Address from a Parcel.
     *
     * @param in the Parcel containing the serialized Address
     */
    protected Address(Parcel in)
    {
        city = in.readParcelable(City.class.getClassLoader());
        name = in.readString();
    }

    /**
     * Returns the city of this address.
     *
     * @return the city
     */
    public City getCity()
    {
        return city;
    }

    /**
     * Returns the name/detail of this address.
     *
     * @return the address name (e.g., street)
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the city of this address.
     *
     * @param city the city to set
     */
    public void setCity(City city)
    {
        this.city = city;
    }

    /**
     * Sets the name/detail of this address.
     *
     * @param name the street name or detailed address
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Serializes this Address into a Parcel.
     *
     * @param parcel the Parcel in which the object should be written
     * @param i      additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeParcelable(city, i);
        parcel.writeString(name);
    }

    /**
     * Two Address objects are equal if both their city and name fields are equal.
     *
     * @param o the object to compare to
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(name, address.name);
    }

    /**
     * Computes the hash code based on city and name.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(city, name);
    }

    /**
     * Parcelable.Creator that generates instances of Address from a Parcel.
     */
    public static final Creator<Address> CREATOR = new Creator<Address>()
    {
        @Override
        public Address createFromParcel(Parcel in)
        {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size)
        {
            return new Address[size];
        }
    };

    /**
     * Returns a human-readable string for debugging.
     *
     * @return string representation of this Address
     */
    @Override
    public String toString()
    {
        return "Address: " + "city: " + city.getName() + ", address: '" + name;
    }
}
