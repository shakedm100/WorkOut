package Model;

import java.util.ArrayList;

public class Business extends User
{
    private String name;
    private Location location;
    private ArrayList<Client> followers;
    private ArrayList<Rating> ratings;
    private String policy;

    public Business(String id, String username, String password, Phone phone, String email, String name,
                    Location location, ArrayList<Client> followers, ArrayList<Rating> ratings, String policy)
    {
        super(id, username, password, phone, email);
        this.name = name;
        this.location = location;
        this.followers = followers;
        this.ratings = ratings;
        this.policy = policy;
    }

    public String getName() {
        return name;
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
}
