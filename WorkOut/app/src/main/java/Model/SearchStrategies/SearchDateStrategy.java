package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import Model.Business;
import Model.Course;
import Model.Day;

public class SearchDateStrategy implements SearchStrategyInterface<Timestamp[]>
{
    private FirebaseFirestore db;

    public SearchDateStrategy()
    {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public Task<List<Business>> search(Timestamp[] times)
    {
        if (times.length != 2)
        {
            throw new IllegalArgumentException("There aren't exactly two time stamps in the array");
        }

        Timestamp minTime, maxTime;
        if (times[0].compareTo(times[1]) > 0)
        {
            maxTime = times[0];
            minTime = times[1];
        }
        else
        {
            maxTime = times[1];
            minTime = times[0];
        }

        Day day = extractDayFromTimestamp(minTime);

        Timestamp normMin = normalizeToTimeOnly(minTime);
        Timestamp normMax = normalizeToTimeOnly(maxTime);

        return db.collectionGroup("courses")
                .whereEqualTo("schedule.day", day.toString())
                .whereGreaterThanOrEqualTo("schedule.occurrence", normMin)
                .whereLessThanOrEqualTo("schedule.occurrence", normMax).get()
                .onSuccessTask(querySnap ->
                {
                    // group matching courses by their parent business ref
                    Map<DocumentReference, List<Course>> coursesByBiz = new HashMap<>();
                    for (DocumentSnapshot cs : querySnap)
                    {
                        Course c = cs.toObject(Course.class);
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // "courses"
                                .getParent();  // the business
                        if (c != null && bizRef != null)
                        {
                            coursesByBiz
                                    .computeIfAbsent(bizRef, __ -> new ArrayList<>())
                                    .add(c);
                        }
                    }

                    if (coursesByBiz.isEmpty())
                    {
                        return Tasks.forResult(Collections.<Business>emptyList());
                    }

                    // for each business, fetch its doc and attach its courses
                    List<Task<Business>> bizTasks = coursesByBiz.entrySet().stream()
                            .map(entry ->
                            {
                                DocumentReference bizRef = entry.getKey();
                                List<Course> courseList = entry.getValue();

                                return bizRef.get().continueWith(bizSnapTask ->
                                {
                                    if (!bizSnapTask.isSuccessful())
                                    {
                                        throw Objects.requireNonNull(bizSnapTask.getException());
                                    }

                                    DocumentSnapshot bsnap = bizSnapTask.getResult();
                                    Business b = bsnap.toObject(Business.class);
                                    if (b != null)
                                    {
                                        b.setId(bsnap.getId());
                                        b.setCourses((ArrayList<Course>) courseList);
                                    }
                                    return b;
                                });
                            })
                            .collect(Collectors.toList());

                    // merge all into one Task<List<Business>>
                    return Tasks.<Business>whenAllSuccess(bizTasks);
                })

                // 5) cast the raw List<Object> into List<Business>
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(finalTask.getException());
                    }

                    @SuppressWarnings("unchecked")
                    List<Business> result = (List<Business>) finalTask.getResult();
                    return result;
                });
    }

    /*private static Timestamp normalizeToTimeOnly(Timestamp raw)
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
    }*/

    private static Timestamp normalizeToTimeOnly(Timestamp raw)
    {
        if (raw == null)
        {
            return null;
        }

        // Get an Instant from the raw Timestamp
        Instant inst = Instant.ofEpochSecond(raw.getSeconds(), raw.getNanoseconds());

        // Pull out the LocalTime in your local zone (or UTC if you prefer)
        LocalTime time = inst
                .atZone(ZoneOffset.UTC)
                .toLocalTime();

        // Compute seconds since midnight (0..86399) and the nanos
        int twoHours = 7200;
        long secondsOfDay = time.toSecondOfDay() - twoHours;
        int nanoOfSecond = time.getNano();

        // Build a Timestamp at Jan 1 1970 00:00:00Z + secondsOfDay
        // This automatically makes the date “1970-01-01”
        return new Timestamp(secondsOfDay, nanoOfSecond);
    }


    private Day extractDayFromTimestamp(Timestamp timestamp)
    {
        // Convert to java.util.Date
        Date date = timestamp.toDate();

        // Use Calendar to pull out DAY_OF_WEEK
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);  // 1=Sunday, 2=Monday, …, 7=Saturday

        // Map to your Day enum
        switch (dayOfWeek)
        {
            case Calendar.SUNDAY:
                return Day.Sunday;
            case Calendar.MONDAY:
                return Day.Monday;
            case Calendar.TUESDAY:
                return Day.Tuesday;
            case Calendar.WEDNESDAY:
                return Day.Wednesday;
            case Calendar.THURSDAY:
                return Day.Thursday;
            case Calendar.FRIDAY:
                return Day.Friday;
            case Calendar.SATURDAY:
                return Day.Saturday;
            default:
                throw new IllegalStateException("Impossible day: " + dayOfWeek);
        }
    }
}