package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public abstract class Entity implements Parcelable {
    private String id;

    public Entity() {}
    public Entity(String id)
    {
        this.id = id;
    }

    protected Entity(Parcel in)
    {
        id = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeString(id);
    }
}
