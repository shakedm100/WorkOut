package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Client extends User implements Parcelable{
    private String firstName;
    private String lastName;
    private Address address;
    private Gender gender;
    private boolean isFirstLogin;

    public Client() {}
    public Client(String id, String username, Phone phone, String email,
                  String firstName, String lastName, Address address, Gender gender)
    {
        super(id, username, phone, email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.gender = gender;
        this.isFirstLogin = false;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isFirstLogin()
    {
        return isFirstLogin;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setFirstLogin(boolean firstLogin)
    {
        isFirstLogin = firstLogin;
    }
    /**
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////
     * This part implements Parcelable interface. Parcelable is better then Serialization in Android because:
     * 1. Better performance
     * 2. Small memory usage
     * 3. Android frameworks are designed around Parcelable (e.g Intent, Bundle, etc..)
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////
     * Note: The Read and Write orders are important! Otherwise it would write the wrong values
     * to the wrong fields!!
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    /*** Parcel-constructor ***/
    protected Client(Parcel in) {
        super(in); // let User read its own fields
        firstName = in.readString();
        lastName = in.readString();
        address = in.readParcelable(Address.class.getClassLoader());
        gender = Gender.valueOf(in.readString());
        isFirstLogin = in.readByte() != 0; // Read byte and convert to boolean
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags); // let User write its own fields
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeParcelable(address, flags);
        dest.writeString(gender.name());
        dest.writeByte((byte) (isFirstLogin ? 1 : 0)); // Convert boolean to byte
    }

    /**
     * CREATOR usage is for:
     * 1. Factory for unparceling -
     * When Android needs to recreate your object from a Parcel
     * (for example, when your Activity is recreated and you called intent.putExtra("foo", myObj))
     * it doesn’t know how to call your constructor directly. Instead it looks for this CREATOR
     *  field and calls its createFromParcel(...) method.
     * 2. Array allocation
     * It also needs a way to make arrays of your object (e.g. if you do intent.getParcelableArrayExtra("foo")).
     * That’s what newArray(int size) is for.
     */
    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(firstName, client.firstName) && Objects.equals(lastName, client.lastName) && Objects.equals(address, client.address) && gender == client.gender;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(firstName, lastName, address, gender, isFirstLogin);
    }
}
