package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class AgeRange implements Parcelable
{
    private int minAge;
    private int maxAge;

    public AgeRange() {}
    public AgeRange(int minAge, int maxAge)
    {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    protected AgeRange(Parcel in)
    {
        minAge = in.readInt();
        maxAge = in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeInt(minAge);
        parcel.writeInt(maxAge);
    }

    public static final Creator<AgeRange> CREATOR = new Creator<AgeRange>()
    {
        @Override
        public AgeRange createFromParcel(Parcel in)
        {
            return new AgeRange(in);
        }

        @Override
        public AgeRange[] newArray(int size)
        {
            return new AgeRange[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        AgeRange ageRange = (AgeRange) o;
        return minAge == ageRange.minAge && maxAge == ageRange.maxAge;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(minAge, maxAge);
    }
}
