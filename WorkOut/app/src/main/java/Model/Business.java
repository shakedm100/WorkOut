package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a business account, which extends a User.
 * Holds business-specific data: courses offered, location, followers, ratings, policy, and address.
 * Implements Parcelable so Business instances can be passed between Android components.
 */
public class Business extends User implements Parcelable
{
    private String businessName;
    private ArrayList<Course> courses;
    private Location location;
    private ArrayList<Client> followers;
    private ArrayList<Rating> ratings;
    private String policy;
    private Address address;

    /**
     * Default no-argument constructor.
     */
    public Business()
    {
    }

    /**
     * Full constructor.
     *
     * @param id           the unique ID inherited from User
     * @param username     the username inherited from User
     * @param phone        the phone number inherited from User
     * @param email        the email inherited from User
     * @param businessName the display name of the business
     * @param courses      list of courses offered
     * @param location     geographic location of the business
     * @param followers    list of clients following this business
     * @param ratings      list of ratings left by clients
     * @param policy       business policy text
     * @param address      physical address of the business
     */
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

    /**
     * Simplified constructor initializing empty collections.
     *
     * @param id           the unique ID inherited from User
     * @param username     the username
     * @param phone        the phone number
     * @param email        the email
     * @param businessName the display name
     * @param location     geographic location
     * @param policy       business policy text
     * @param address      physical address
     */
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

    /**
     * @return the business display name
     */
    public String getBusinessName()
    {
        return businessName;
    }

    /**
     * @return list of courses offered by this business
     */
    public List<Course> getCourses()
    {
        return courses;
    }

    /**
     * @return geographic location of this business
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * @return clients following this business
     */
    public ArrayList<Client> getFollowers()
    {
        return followers;
    }

    /**
     * @return ratings left by clients
     */
    public ArrayList<Rating> getRatings()
    {
        return ratings;
    }

    /**
     * @return the textual policy of the business
     */
    public String getPolicy()
    {
        return policy;
    }

    /**
     * @return the physical address of this business
     */
    public Address getAddress()
    {
        return address;
    }

    /**
     * @param businessName the name to set for this business
     */
    public void setBusinessName(String businessName)
    {
        this.businessName = businessName;
    }

    /**
     * @param courses the list of courses to set
     */
    public void setCourses(ArrayList<Course> courses)
    {
        this.courses = courses;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location)
    {
        this.location = location;
    }

    /**
     * @param followers the list of followers to set
     */
    public void setFollowers(ArrayList<Client> followers)
    {
        this.followers = followers;
    }

    /**
     * @param ratings the list of ratings to set
     */
    public void setRatings(ArrayList<Rating> ratings)
    {
        this.ratings = ratings;
    }

    /**
     * @param policy the policy text to set
     */
    public void setPolicy(String policy)
    {
        this.policy = policy;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address)
    {
        this.address = address;
    }

    /**
     * Reconstructs a Business from a Parcel.
     *
     * @param in the Parcel containing the serialized Business
     */
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

    /**
     * Serializes this Business into a Parcel.
     *
     * @param dest the Parcel in which the object should be written
     * @param flags additional flags about how the object should be written
     */
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

    /** Parcelable.Creator that generates instances of Business from a Parcel. */
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

    /**
     * Adds a course to this business’s course list.
     *
     * @param course the Course to add
     * @return true if added successfully, false if null
     */
    public boolean addCourse(Course course)
    {
        if (courses == null)
            courses = new ArrayList<>();
        if (course != null)
        {
            courses.add(course);
            return true;
        }

        return false;
    }

    /**
     * Removes a course from this business.
     *
     * @param course the Course to remove
     * @return true if removed, false otherwise
     */
    public boolean deleteCourse(Course course)
    {
        return courses.remove(course);
    }

    /**
     * Replaces an existing course with an updated instance.
     *
     * @param course the updated Course (matched by ID)
     * @return true if an existing course was updated, false otherwise
     */
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

    /**
     * Adds a rating to this business.
     *
     * @param rating the Rating to add
     * @return true if added, false otherwise
     */
    public boolean addRating(Rating rating)
    {
        if (ratings == null)
            ratings = new ArrayList<>();
        return ratings.add(rating);
    }

    /**
     * Removes a rating.
     *
     * @param rating the Rating to remove
     * @return true if removed, false otherwise
     */
    public boolean deleteRating(Rating rating)
    {
        return ratings.remove(rating);
    }

    /**
     * Updates an existing rating by the same client.
     *
     * @param rating the new Rating object
     * @return true if updated, false otherwise
     */
    public boolean updateRating(Rating rating)
    {
        for (int i = 0; i < ratings.size(); i++)
        {
            if (ratings.get(i).getClient().equals(rating.getClient()))
            {
                ratings.set(i, rating);
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the average number of stars across all ratings.
     *
     * @return the average stars, or 0 if no ratings exist
     */
    public float averageRating()
    {
        if (ratings == null || ratings.isEmpty())
            return 0;

        float sum = 0;

        for (Rating rating : ratings)
        {
            sum += rating.getStars();
        }

        return sum / ratings.size();
    }

    /**
     * Adds a follower to this business.
     *
     * @param follower the Client to add as a follower
     * @return true if added, false otherwise
     */
    public boolean addFollower(Client follower)
    {
        if (followers == null)
            followers = new ArrayList<>();
        return followers.add(follower);
    }

    /**
     * Removes a follower.
     *
     * @param follower the Client to remove
     * @return true if removed, false otherwise
     */
    public boolean deleteFollower(Client follower)
    {
        return followers.remove(follower);
    }

    /**
     * Updates an existing follower’s data.
     *
     * @param follower the updated Client
     * @return true if updated, false otherwise
     */
    public boolean updateFollower(Client follower)
    {
        for (int i = 0; i < followers.size(); i++)
        {
            if (followers.get(i).getUsername().equals(follower.getUsername()))
            {
                followers.set(i, follower);
                return true;
            }
        }

        return false;
    }

    /**
     * Two Business objects are equal if their core fields match.
     *
     * @param o the object to compare to
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return Objects.equals(businessName, business.businessName) && Objects.equals(courses, business.courses) && Objects.equals(location, business.location) && Objects.equals(followers, business.followers) && Objects.equals(ratings, business.ratings) && Objects.equals(policy, business.policy);
    }

    /**
     * Computes the hash code based on key fields.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(businessName, courses, location, followers, ratings, policy);
    }
}
