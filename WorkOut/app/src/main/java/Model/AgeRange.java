package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Defines an inclusive range of ages.
 * Implements Parcelable so it can be passed between Android components.
 */
public class AgeRange implements Parcelable
{
    private int minAge;
    private int maxAge;

    /**
     * Default no-argument constructor.
     */
    public AgeRange()
    {
    }

    /**
     * Constructs an AgeRange with the specified minimum and maximum.
     *
     * @param minAge the minimum age (inclusive)
     * @param maxAge the maximum age (inclusive)
     */
    public AgeRange(int minAge, int maxAge)
    {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Returns the minimum age.
     *
     * @return the minimum age
     */
    public int getMinAge()
    {
        return minAge;
    }

    /**
     * Returns the maximum age.
     *
     * @return the maximum age
     */
    public int getMaxAge()
    {
        return maxAge;
    }

    /**
     * Sets the minimum age.
     *
     * @param minAge the minimum age to set
     */
    public void setMinAge(int minAge)
    {
        this.minAge = minAge;
    }

    /**
     * Sets the maximum age.
     *
     * @param maxAge the maximum age to set
     */
    public void setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
    }

    /**
     * Reconstructs an AgeRange from a Parcel.
     *
     * @param in the Parcel containing the serialized AgeRange
     */
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

    /**
     * Serializes this AgeRange into a Parcel.
     *
     * @param parcel the Parcel in which the object should be written
     * @param i additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeInt(minAge);
        parcel.writeInt(maxAge);
    }

    /**
     * Parcelable.Creator that generates instances of AgeRange from a Parcel.
     */
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

    /**
     * Two AgeRange objects are equal if both minAge and maxAge match.
     *
     * @param o the object to compare to
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        AgeRange ageRange = (AgeRange) o;
        return minAge == ageRange.minAge && maxAge == ageRange.maxAge;
    }

    /**
     * Computes the hash code based on minAge and maxAge.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(minAge, maxAge);
    }
}
