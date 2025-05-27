package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalTime;
import java.util.Objects;

public class Schedule extends Entity implements Parcelable
{
    private Day day;
    private LocalTime occurrence;

    public Schedule() {}
    public Schedule(String id, Day day, LocalTime occurrence)
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

    public void setDay(Day day) {
        this.day = day;
    }

    public void setOccurrence(LocalTime occurrence) {
        this.occurrence = occurrence;
    }

    protected Schedule(Parcel in) {
        super(in);
        day = Day.valueOf(in.readString());
        occurrence = LocalTime.parse(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(day.name());
        // write as ISO-8601 (e.g. "14:30:00")
        dest.writeString(occurrence.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return day == schedule.day && Objects.equals(occurrence, schedule.occurrence);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(day, occurrence);
    }
}
