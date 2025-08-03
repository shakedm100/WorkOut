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

/**
 * Represents a weekly schedule entry with a day of week and a time-of-day occurrence.
 * Implements Parcelable for Android IPC.
 */
public class Schedule implements Parcelable
{
    private Day day;
    private Timestamp occurrence;

    /** Default no-arg constructor (required by serialization frameworks). */
    public Schedule()
    {
    }

    /**
     * Constructs a Schedule for a specific day and timestamp.
     * The timestamp is normalized to a time-of-day on the epoch date (Jan 1 1970 UTC).
     *
     * @param day        the day of the week
     * @param occurrence a full Timestamp; only its time component will be retained
     */
    public Schedule(Day day, Timestamp occurrence)
    {
        this.day = day;
        this.occurrence = normalizeToTimeOnly(occurrence);
    }

    /**
     * Strips the date portion of a Timestamp, retaining only the UTC time-of-day.
     * Resulting timestamp has seconds = seconds since midnight, date = 1970-01-01 UTC.
     *
     * @param raw the original Timestamp
     * @return a time-only Timestamp, or null if raw was null
     */
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

    /** @return the day of the week for this schedule */
    public Day getDay() {
        return day;
    }

    /** @return the normalized time-of-day occurrence (epoch date + time) */
    public Timestamp getOccurrence() {
        return occurrence;
    }

    /** @param day the day to set */
    public void setDay(Day day) {
        this.day = day;
    }

    /**
     * Sets the occurrence, normalizing to time-only.
     *
     * @param occurrence full Timestamp; only time-of-day is stored
     */
    public void setOccurrence(Timestamp occurrence) {
        this.occurrence = normalizeToTimeOnly(occurrence);
    }

    /** Recreates a Schedule from a Parcel. */
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

    /** Parcelable.Creator that generates Schedule instances from a Parcel. */
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

    /**
     * Two Schedules are equal if they share the same day and occurrence time.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
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
