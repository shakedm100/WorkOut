package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class City extends Entity implements Parcelable
{
    private String name;

    public City() {}

    public City(String id, String name)
    {
        super(id);
        this.name = name;
    }

    protected City(Parcel in) {
        super(in);
        name = in.readString();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }


    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

}
