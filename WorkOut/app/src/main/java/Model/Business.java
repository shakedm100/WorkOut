package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Business extends User implements Parcelable
{
    private String businessName;
    private ArrayList<Course> courses;
    private Location location;
    private ArrayList<Client> followers;
    private ArrayList<Rating> ratings;
    private String policy;
    private Address address;

    public Business()
    {
    }

    public Business(String id, String username, Phone phone, String email, String businessName,
                    ArrayList<Course> courses, Location location, ArrayList<Client> followers,
                    ArrayList<Rating> ratings, String policy, Address address)
    {
        super(id, username, phone, email);
        this.businessName = businessName;
        this.courses = courses;
        this.location = location;
        this.followers = followers;
        this.ratings = ratings;
        this.policy = policy;
        this.address = address;
    }

    public Business(String id, String username, Phone phone, String email, String businessName,
                    Location location, String policy, Address address)
    {
        super(id, username, phone, email);
        this.businessName = businessName;
        this.courses = new ArrayList<>();
        this.location = location;
        this.followers = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.policy = policy;
        this.address = address;
    }

    public String getBusinessName()
    {
        return businessName;
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

    public Address getAddress()
    {
        return address;
    }

    public void setBusinessName(String businessName)
    {
        this.businessName = businessName;
    }

    public void setCourses(ArrayList<Course> courses)
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

    public void setAddress(Address address)
    {
        this.address = address;
    }

    protected Business(Parcel in)
    {
        super(in);
        businessName = in.readString();
        courses = in.createTypedArrayList(Course.CREATOR);
        followers = in.createTypedArrayList(Client.CREATOR);
        ratings = in.createTypedArrayList(Rating.CREATOR);
        policy = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        address = in.readParcelable(Address.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(businessName);
        dest.writeTypedList(courses);
        dest.writeTypedList(followers);
        dest.writeTypedList(ratings);
        dest.writeString(policy);
        dest.writeParcelable(location, flags);
        dest.writeParcelable(address, flags);
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

    public boolean addCourse(Course course)
    {
        if(courses == null)
            courses = new ArrayList<>();
        if (course != null)
        {
            courses.add(course);
            return true;
        }

        return false;
    }

    public boolean deleteCourse(Course course)
    {
        return courses.remove(course);
    }

    public boolean updateCourse(Course course)
    {
        for (int i = 0; i < courses.size(); i++)
        {
            if (courses.get(i).getId().equals(course.getId()))
            {
                courses.set(i, course);
                return true;
            }
        }

        return false;
    }

    public boolean addRating(Rating rating)
    {
        if(ratings == null)
            ratings = new ArrayList<>();
        return ratings.add(rating);
    }

    public boolean deleteRating(Rating rating)
    {
        return ratings.remove(rating);
    }

    public boolean updateRating(Rating rating)
    {
        for (int i = 0; i < ratings.size(); i++)
        {
            if(ratings.get(i).getClient().equals(rating.getClient()))
            {
                ratings.set(i, rating);
                return true;
            }
        }

        return false;
    }

    public float averageRating()
    {
        if(ratings == null || ratings.isEmpty())
            return 0;

        float sum = 0;

        for(Rating rating : ratings)
        {
            sum += rating.getStars();
        }

        return sum / ratings.size();
    }

    public boolean addFollower(Client follower)
    {
        if(followers == null)
            followers = new ArrayList<>();
        return followers.add(follower);
    }

    public boolean deleteFollower(Client follower)
    {
        return followers.remove(follower);
    }

    public boolean updateFollower(Client follower)
    {
        for (int i = 0; i < followers.size(); i++)
        {
            if(followers.get(i).getUsername().equals(follower.getUsername()))
            {
                followers.set(i, follower);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return Objects.equals(businessName, business.businessName) && Objects.equals(courses, business.courses) && Objects.equals(location, business.location) && Objects.equals(followers, business.followers) && Objects.equals(ratings, business.ratings) && Objects.equals(policy, business.policy);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(businessName, courses, location, followers, ratings, policy);
    }
}
