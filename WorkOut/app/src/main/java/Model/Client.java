package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * A client user, extending User, with personal details and login state.
 * Implements Parcelable for inter-component transfers.
 */
public class Client extends User implements Parcelable{
    private String firstName;
    private String lastName;
    private Address address;
    private Gender gender;
    private boolean isFirstLogin;

    /** Default no-argument constructor. */
    public Client() {}

    /**
     * Constructs a Client with the given personal details.
     * First-login flag defaults to false.
     *
     * @param id        unique ID from User
     * @param username  login username
     * @param phone     phone number
     * @param email     email address
     * @param firstName given name
     * @param lastName  family name
     * @param address   postal address
     * @param gender    gender identity
     */
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

    /** @return the first name of this client */
    public String getFirstName() {
        return firstName;
    }

    /** @return the last name of this client */
    public String getLastName() {
        return lastName;
    }

    /** @return the postal address of this client */
    public Address getAddress() {
        return address;
    }

    /** @return the gender of this client */
    public Gender getGender() {
        return gender;
    }

    /**
     * Indicates whether this is the client's first login.
     *
     * @return true if first login, false otherwise
     */
    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    /** @param firstName the first name to set */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** @param lastName the last name to set */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** @param address the address to set */
    public void setAddress(Address address) {
        this.address = address;
    }

    /** @param gender the gender to set */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /** @param firstLogin the first-login flag to set */
    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    /*
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

    /**
     * Reconstructs a Client from a Parcel.
     * Read/write order must match writeToParcel exactly.
     *
     * @param in the Parcel to read from
     */
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

    /**
     * Writes this Client's fields into a Parcel.
     * Call super to serialize User fields first.
     *
     * @param dest  the Parcel to write into
     * @param flags additional flags (unused)
     */
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

    /*
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

    /**
     * Parcelable.Creator that generates Client instances from a Parcel.
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

    /**
     * Two Client objects are equal if their personal fields match.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(firstName, client.firstName) && Objects.equals(lastName, client.lastName) && Objects.equals(address, client.address) && gender == client.gender;
    }

    /**
     * Computes hash code based on personal and login-state fields.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(firstName, lastName, address, gender, isFirstLogin);
    }
}
