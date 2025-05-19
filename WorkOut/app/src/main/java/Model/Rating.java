package Model;

public class Rating extends Entity
{
    private float stars;
    private String comment;
    private Client client;

    public Rating(int id, float stars, String comment, Client client)
    {
        super(id);
        this.stars = stars;
        this.comment = comment;
        this.client = client;
    }

    public float getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public Client getClient() {
        return client;
    }
}
