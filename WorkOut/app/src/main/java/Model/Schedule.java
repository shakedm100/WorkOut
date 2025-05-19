package Model;

import java.time.LocalTime;

public class Schedule extends Entity
{
    private Day day;
    private LocalTime occurrence;

    public Schedule(int id, Day day, LocalTime occurrence)
    {
        super(id);
        this.day = day;
        this.occurrence = occurrence;
    }

    public Day getDay() {
        return day;
    }

    public LocalTime getOccurrence() {
        return occurrence;
    }
}
