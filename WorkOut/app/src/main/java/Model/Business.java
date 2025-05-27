package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Business extends User implements Parcelable
{
    private String name;
    private List<Course> courses;
    private Location location;
    private ArrayList<Client> followers;
    private ArrayList<Rating> ratings;
    private String policy;

    public Business() {}
    public Business(String id, String username, String password, Phone phone, String email, String name,
                    List<Course> courses, Location location, ArrayList<Client> followers, ArrayList<Rating> ratings, String policy)
    {
        super(id, username, password, phone, email);
        this.name = name;
        this.courses = courses;
        this.location = location;
        this.followers = followers;
        this.ratings = ratings;
        this.policy = policy;
    }

    public String getName()
    {
        return name;
    }

    public List<Course> getCourses()
    {
        return courses;
    }

    public Location getLocation()
    {
        return location;
    }

    public ArrayList<Client> getFollowers()
    {
        return followers;
    }

    public ArrayList<Rating> getRatings()
    {
        return ratings;
    }

    public String getPolicy()
    {
        return policy;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setCourses(List<Course> courses)
    {
        this.courses = courses;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public void setFollowers(ArrayList<Client> followers)
    {
        this.followers = followers;
    }

    public void setRatings(ArrayList<Rating> ratings)
    {
        this.ratings = ratings;
    }

    public void setPolicy(String policy)
    {
        this.policy = policy;
    }

    protected Business(Parcel in)
    {
        super(in);
        name = in.readString();
        courses = in.createTypedArrayList(Course.CREATOR);
        followers = in.createTypedArrayList(Client.CREATOR);
        ratings = in.createTypedArrayList(Rating.CREATOR);
        policy = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeTypedList(courses);
        dest.writeTypedList(followers);
        dest.writeTypedList(ratings);
        dest.writeString(policy);
        dest.writeParcelable(location, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Business> CREATOR = new Creator<Business>()
    {
        @Override
        public Business createFromParcel(Parcel in)
        {
            return new Business(in);
        }

        @Override
        public Business[] newArray(int size)
        {
            return new Business[size];
        }
    };
}
