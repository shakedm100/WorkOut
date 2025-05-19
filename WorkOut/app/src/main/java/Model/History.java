package Model;
import java.time.LocalDateTime;

public class History extends Entity
{
    private Course course;
    private LocalDateTime date;
    private Client client;

    public History(String id, Course course, LocalDateTime date, Client client)
    {
        super(id);
        this.course = course;
        this.date = date;
        this.client = client;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Client getClient() {
        return client;
    }
}
