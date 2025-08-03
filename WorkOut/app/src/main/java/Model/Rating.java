package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * A client’s rating of a business or course, with stars and an optional comment.
 * Implements {@link Parcelable} for Android IPC.
 */
public class Rating implements Parcelable
{
    private float stars;
    private String comment;
    private Client client;

    /**
     * Default no-arg constructor for Firestore.
     */
    public Rating()
    {
    }

    /**
     * Constructs a Rating.
     *
     * @param stars   number of stars (e.g. 4.5)
     * @param comment optional textual feedback
     * @param client  the client who left the rating
     */
    public Rating(float stars, String comment, Client client)
    {
        this.stars = stars;
        this.comment = comment;
        this.client = client;
    }

    /**
     * @return the star count
     */
    public float getStars()
    {
        return stars;
    }

    /**
     * @return the comment text
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @return the client who rated
     */
    public Client getClient()
    {
        return client;
    }

    /**
     * @param stars the star count to set
     */
    public void setStars(float stars)
    {
        this.stars = stars;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Client client)
    {
        this.client = client;
    }

    /** Recreates a Rating from a Parcel. */
    protected Rating(Parcel in)
    {
        stars = in.readFloat();
        comment = in.readString();
        client = in.readParcelable(Client.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeFloat(stars);
        dest.writeString(comment);
        dest.writeParcelable(client, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    /** Parcelable.Creator that generates Rating instances from a Parcel. */
    public static final Creator<Rating> CREATOR = new Creator<Rating>()
    {
        @Override
        public Rating createFromParcel(Parcel in)
        {
            return new Rating(in);
        }

        @Override
        public Rating[] newArray(int size)
        {
            return new Rating[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Float.compare(stars, rating.stars) == 0 && Objects.equals(comment, rating.comment) && Objects.equals(client, rating.client);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(stars, comment, client);
    }
}
