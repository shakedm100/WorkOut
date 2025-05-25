package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Address implements Parcelable
{
    private City city;
    private String name;

    public Address(){}
    public Address(City city, String name)
    {
        this.city = city;
        this.name = name;
    }

    protected Address(Parcel in)
    {
        city = in.readParcelable(City.class.getClassLoader());
        name = in.readString();
    }

    public City getCity()
    {
        return city;
    }

    public String getName()
    {
        return name;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeParcelable(city, i);
        parcel.writeString(name);
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
