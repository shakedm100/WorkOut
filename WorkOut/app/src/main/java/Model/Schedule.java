package Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

public class Schedule implements Parcelable
{
    private Day day;
    private Timestamp occurrence;

    public Schedule()
    {
    }

    public Schedule(Day day, Timestamp occurrence)
    {
        this.day = day;
        this.occurrence = normalizeToTimeOnly(occurrence);
    }

    private static Timestamp normalizeToTimeOnly(Timestamp raw)
    {
        if (raw == null) return null;

        // Raw seconds/nanos are always in UTC
        Instant inst = Instant.ofEpochSecond(raw.getSeconds(), raw.getNanoseconds());

        // Extract the UTC time‐of‐day
        LocalTime time = inst.atZone(ZoneOffset.UTC).toLocalTime();

        // Compute seconds‐of‐day (0..86399) and nanos
        long secondsOfDay = time.toSecondOfDay();  // e.g. 0h00=0, 1h00=3600, 12h23=443*60+? etc.
        int nanoOfSecond = time.getNano();

        // Build a Timestamp at Jan 1 1970 00:00:00 UTC + secondsOfDay
        //    (so seconds==secondsOfDay, date==1970-01-01)
        return new Timestamp(secondsOfDay, nanoOfSecond);
    }

    public Day getDay()
    {
        return day;
    }

    public Timestamp getOccurrence()
    {
        return occurrence;
    }

    public void setDay(Day day)
    {
        this.day = day;
    }

    public void setOccurrence(Timestamp occurrence)
    {
        this.occurrence = normalizeToTimeOnly(occurrence);
    }

    protected Schedule(Parcel in)
    {
        day = Day.valueOf(in.readString());
        occurrence = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(day.name());
        // write as ISO-8601 (e.g. "14:30:00")
        dest.writeParcelable(occurrence, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>()
    {
        @Override
        public Schedule createFromParcel(Parcel in)
        {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size)
        {
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
