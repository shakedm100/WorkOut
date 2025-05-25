package Model;

import java.util.ArrayList;
import java.util.List;

public class Business extends User
{
    private String name;
    private List<Course> courses;
    private Location location;
    private ArrayList<Client> followers;
    private ArrayList<Rating> ratings;
    private String policy;

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

    public String getName() {
        return name;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public Location getLocation() {
        return location;
    }

    public ArrayList<Client> getFollowers() {
        return followers;
    }

    public ArrayList<Rating> getRatings() {
        return ratings;
    }

    public String getPolicy() {
        return policy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setFollowers(ArrayList<Client> followers) {
        this.followers = followers;
    }

    public void setRatings(ArrayList<Rating> ratings) {
        this.ratings = ratings;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
