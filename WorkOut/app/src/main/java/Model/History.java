package Model;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historical record of a client’s participation in a past course.
 * Extends {@link Entity} to include a unique ID.
 */
@SuppressLint("ParcelCreator")
public class History extends Entity
{
    private Course course;
    private LocalDateTime date;
    private Client client;

    /**
     * Default no-arg constructor for Firestore.
     */
    public History()
    {
    }

    /**
     * Constructs a History record.
     *
     * @param id     unique history ID
     * @param course the course taken
     * @param date   date/time of participation
     * @param client the client who participated
     */
    public History(String id, Course course, LocalDateTime date, Client client)
    {
        super(id);
        this.course = course;
        this.date = date;
        this.client = client;
    }

    /**
     * @return the associated course
     */
    public Course getCourse()
    {
        return course;
    }

    /**
     * @return the date of participation
     */
    public LocalDateTime getDate()
    {
        return date;
    }

    /**
     * @return the client who participated
     */
    public Client getClient()
    {
        return client;
    }

    /**
     * @param course the course to set
     */
    public void setCourse(Course course)
    {
        this.course = course;
    }

    /**
     * @param date the date to set
     */
    public void setDate(LocalDateTime date)
    {
        this.date = date;
    }

    /**
     * @param client the client to set
     */
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
