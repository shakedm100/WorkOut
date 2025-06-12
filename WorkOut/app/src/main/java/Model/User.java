package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class User extends Entity implements Parcelable {
    private String username;
    private Phone phone;
    private String email;

    public User() {}
    public User(String id, String username, Phone phone, String email)
    {
        super(id);
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    public User(Parcel in) {
        super(in);
        username = in.readString();
        phone = in.readParcelable(Phone.class.getClassLoader());
        email = in.readString();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
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
