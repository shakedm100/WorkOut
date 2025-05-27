package Model;
import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        History history = (History) o;
        return Objects.equals(course, history.course) && Objects.equals(date, history.date) && Objects.equals(client, history.client);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(course, date, client);
    }
}
