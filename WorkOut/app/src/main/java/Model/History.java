package Model;
import android.annotation.SuppressLint;

import java.time.LocalDateTime;

@SuppressLint("ParcelCreator")
public class History extends Entity
{
    private Course course;
    private LocalDateTime date;
    private Client client;

    public History() {}
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

    public void setCourse(Course course)
    {
        this.course = course;
    }

    public void setDate(LocalDateTime date)
    {
        this.date = date;
    }

    public void setClient(Client client)
    {
        this.client = client;
    }
}
