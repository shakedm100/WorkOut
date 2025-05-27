package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Phone implements Parcelable {
    private PhonePrefix prefix;
    private String number;

    /***
     * No-arg constructor required by Firestore’s POJO mapper
     * POJO convention is:
     * 1. Give each class a public no-argument constructor
     * 2. Expose each field via a public getter (or make the fields public)
     * 3. Avoid transient or mark fields you don’t want stored with @Exclude
     */
    // Firestore will use setters to populate fields
    public Phone() {}

    public Phone(PhonePrefix phonePrefix, String number)
    {
        this.prefix = phonePrefix;
        this.number = number;
    }

    protected Phone(Parcel in) {
        number = in.readString();
        String code = in.readString();
        prefix = PhonePrefix.fromString(code);
    }

    // Getter and setter for prefix
    public PhonePrefix getPrefix() {
        return prefix;
    }
    public void setPrefix(PhonePrefix prefix) {
        this.prefix = prefix;
    }

    // Getter and setter for number
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(number);
        parcel.writeString(prefix.getCode());
    }

    public static final Creator<Phone> CREATOR = new Creator<Phone>() {
        @Override
        public Phone createFromParcel(Parcel in) {
            return new Phone(in);
        }

        @Override
        public Phone[] newArray(int size) {
            return new Phone[size];
        }
    };
}
