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

/**
 * A {@link SearchStrategyInterface} implementation that finds
 * {@link Business} or {@link Course} entities whose scheduled times
 * fall between two provided {@link Timestamp} values.
 * <p>
 * The two-element {@code Timestamp[]} represents an arbitrary interval;
 * the strategy normalizes to “time-of-day” on January 1, 1970, and
 * matches courses whose {@code schedule.occurrence} lies within that range
 * on the same “day” of week.
 * </p>
 */
public class SearchDateStrategy implements SearchStrategyInterface<Timestamp[]>
{
    private FirebaseFirestore db;

    /**
     * Constructs a new SearchDateStrategy using the Firestore singleton.
     */
    public SearchDateStrategy()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Searches for {@link Business} entities that offer courses whose
     * scheduled occurrence falls between the two provided timestamps.
     * <ol>
     *   <li>Validates exactly two timestamps are supplied.</li>
     *   <li>Sorts them into min/max.</li>
     *   <li>Extracts the {@link Day} of week from the earlier timestamp.</li>
     *   <li>Normalizes both to time-only {@code Timestamp} at 1970-01-01 UTC.</li>
     *   <li>Performs a collectionGroup query on “courses” where day matches
     *       and occurrence ∈ [normMin, normMax].</li>
     *   <li>Groups resulting courses by their parent business document reference.</li>
     *   <li>Fetches each business’s document and attaches its list of matching courses.</li>
     * </ol>
     *
     * @param times two-element array of Timestamps defining the search interval
     * @return a Task completing with a List of matching {@link Business} objects,
     *         each populated with the relevant {@link Course} subcollection;
     *         or an empty list if none match.
     * @throws IllegalArgumentException if {@code times.length != 2}.
     */
    @Override
    public Task<List<Business>> searchBusinesses(Timestamp[] times)
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

    /**
     * Searches for {@link Course} entities whose scheduled occurrence
     * falls between two provided timestamps.
     * <ol>
     *   <li>Validates two timestamps.</li>
     *   <li>Sorts into min/max.</li>
     *   <li>Extracts Day of week.</li>
     *   <li>Normalizes timestamps to time-only at 1970-01-01.</li>
     *   <li>Performs a collectionGroup query on “courses” matching day
     *       and occurrence within the time window.</li>
     *   <li>Maps each result to a Course and populates its businessId.</li>
     * </ol>
     *
     * @param times two-element array of Timestamps for the time window
     * @return a Task completing with a List of matching {@link Course} objects
     * @throws IllegalArgumentException if {@code times.length != 2}.
     */
    @Override
    public Task<List<Course>> searchCourses(Timestamp[] times) {
        if (times.length != 2) {
            throw new IllegalArgumentException("There aren't exactly two timestamps in the array");
        }

        // 1) Sort the two timestamps into minTime / maxTime
        Timestamp t0 = times[0], t1 = times[1];
        Timestamp minTime = (t0.compareTo(t1) <= 0) ? t0 : t1;
        Timestamp maxTime = (t0.compareTo(t1) <= 0) ? t1 : t0;

        // 2) Extract the Day enum from one of them (they share the same day)
        Day day = extractDayFromTimestamp(minTime);

        // 3) Normalize both endpoints to “time‐only” (Jan 1 1970 + clock time UTC)
        Timestamp normMin = normalizeToTimeOnly(minTime);
        Timestamp normMax = normalizeToTimeOnly(maxTime);

        // 4) Query all courses on that day whose occurrence is between normMin and normMax
        return db.collectionGroup("courses")
                .whereEqualTo("schedule.day",    day.toString())
                .whereGreaterThanOrEqualTo("schedule.occurrence", normMin)
                .whereLessThanOrEqualTo(   "schedule.occurrence", normMax)
                .get()

                // 5) Map each matching DocumentSnapshot → Course (with businessId set)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    List<Course> results = new ArrayList<>();
                    for (DocumentSnapshot cs : task.getResult()) {
                        // a) Convert to Course object
                        Course c = cs.toObject(Course.class);
                        if (c == null) continue;

                        // b) Set the Course’s Firestore document ID
                        c.setId(cs.getId());

                        // c) Find the parent business reference and save its ID on the course
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // “courses” collection
                                .getParent();  // “businesses/{bizId}” doc
                        if (bizRef != null) {
                            c.setBusinessId(bizRef.getId());
                        }

                        results.add(c);
                    }
                    return results;
                });
    }

    /**
     * Converts a full {@link Timestamp} into a “time‐only” {@code Timestamp}
     * anchored at 1970-01-01 00:00:00 UTC plus the same hour/min/sec/nanos.
     *
     * @param raw the original Timestamp
     * @return normalized Timestamp or {@code null} if {@code raw} is null
     */
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

    /**
     * Extracts the {@link Day} of week from a {@code Timestamp} (UTC).
     *
     * @param timestamp the input Timestamp
     * @return corresponding Day enum
     */
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