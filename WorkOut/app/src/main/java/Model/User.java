package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Core user model containing login credentials.
 * Extends {@link Entity} for ID support and implements Parcelable.
 */
public class User extends Entity implements Parcelable {
    private String username;
    private Phone phone;
    private String email;

    /** Default no-arg constructor (for Firestore/serialization). */
    public User() {}

    /**
     * Constructs a User with the given credentials.
     *
     * @param id       unique user ID
     * @param username login username
     * @param phone    phone contact info
     * @param email    email address
     */
    public User(String id, String username, Phone phone, String email)
    {
        super(id);
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    /** Reconstructs a User from a Parcel. */
    public User(Parcel in) {
        super(in);
        username = in.readString();
        phone = in.readParcelable(Phone.class.getClassLoader());
        email = in.readString();
    }

    /** @return the login username */
    public String getUsername() {
        return username;
    }

    /** @return the phone contact */
    public Phone getPhone() {
        return phone;
    }

    /** @return the email address */
    public String getEmail() {
        return email;
    }

    /** @param username the username to set */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @param phone the phone to set */
    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    /** @param email the email to set */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(username);
        parcel.writeParcelable(phone, i);
        parcel.writeString(email);
    }

    /** Parcelable.Creator that generates User instances from a Parcel. */
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Two Users are equal if they have the same username, phone, and email.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(phone, user.phone) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(username, phone, email);
    }
}
