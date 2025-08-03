package Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Base class for all domain entities, providing an ID and Parcelable support.
 */
public abstract class Entity implements Parcelable {
    private String id;

    /** Default no-arg constructor. */
    public Entity() {}

    /**
     * Constructs an Entity with the given ID.
     *
     * @param id unique identifier
     */
    public Entity(String id)
    {
        this.id = id;
    }

    /**
     * Deserializes the ID from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected Entity(Parcel in)
    {
        id = in.readString();
    }

    /** @return the entity’s unique ID */
    public String getId() {
        return id;
    }

    /** @param id the unique ID to set */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes the ID into a Parcel.
     *
     * @param parcel the Parcel to write into
     * @param i additional flags (unused)
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeString(id);
    }
}
