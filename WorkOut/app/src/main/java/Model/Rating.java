package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Rating implements Parcelable
{
    private float stars;
    private String comment;
    private Client client;

    public Rating()
    {
    }

    public Rating(float stars, String comment, Client client)
    {
        this.stars = stars;
        this.comment = comment;
        this.client = client;
    }

    public float getStars()
    {
        return stars;
    }

    public String getComment()
    {
        return comment;
    }

    public Client getClient()
    {
        return client;
    }

    public void setStars(float stars)
    {
        this.stars = stars;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setClient(Client client)
    {
        this.client = client;
    }

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
