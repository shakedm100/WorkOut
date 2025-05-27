package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User extends Entity implements Parcelable {
    private String username;
    private String password;
    private Phone phone;
    private String email;

    public User() {}
    public User(String id, String username, String password, Phone phone, String email)
    {
        super(id);
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }

    public User(Parcel in) {
        super(in);
        username = in.readString();
        password = in.readString();
        phone = in.readParcelable(Phone.class.getClassLoader());
        email = in.readString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public void setPassword(String password) {
        this.password = password;
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
        parcel.writeString(password);
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
}
