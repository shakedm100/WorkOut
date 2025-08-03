package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * A phone number with a country-specific prefix.
 * Implements {@link Parcelable} for Android IPC and follows Firestore POJO conventions.
 */
public class Phone implements Parcelable
{
    private PhonePrefix prefix;
    private String number;

    /*
     * No-arg constructor required by Firestore’s POJO mapper
     * POJO convention is:
     * 1. Give each class a public no-argument constructor
     * 2. Expose each field via a public getter (or make the fields public)
     * 3. Avoid transient or mark fields you don’t want stored with @Exclude
     */

    /**
     * Default no-arg constructor for Firestore.
     */
    public Phone()
    {
    }

    /**
     * Constructs a Phone.
     *
     * @param phonePrefix the numeric prefix (e.g. PREFIX_052)
     * @param number      the remaining digits
     */
    public Phone(PhonePrefix phonePrefix, String number)
    {
        this.prefix = phonePrefix;
        this.number = number;
    }

    /**
     * Recreates a Phone from a Parcel.
     */
    protected Phone(Parcel in)
    {
        number = in.readString();
        String code = in.readString();
        prefix = PhonePrefix.fromString(code);
    }

    /**
     * @return the phone prefix enum
     */
    public PhonePrefix getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(PhonePrefix prefix)
    {
        this.prefix = prefix;
    }

    /**
     * @return the phone number digits
     */
    public String getNumber()
    {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number)
    {
        this.number = number;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeString(number);
        parcel.writeString(prefix.getCode());
    }

    /** Parcelable.Creator that generates Phone instances from a Parcel. */
    public static final Creator<Phone> CREATOR = new Creator<Phone>()
    {
        @Override
        public Phone createFromParcel(Parcel in)
        {
            return new Phone(in);
        }

        @Override
        public Phone[] newArray(int size)
        {
            return new Phone[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return prefix == phone.prefix && Objects.equals(number, phone.number);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(prefix, number);
    }

    @Override
    public String toString()
    {
        return "Phone: " + prefix.getCode() + "-" + number;
    }
}
