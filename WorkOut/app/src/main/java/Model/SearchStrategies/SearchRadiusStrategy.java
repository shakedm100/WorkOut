package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import Model.Business;
import Model.Course;
import Model.Location;
import Model.Repository.CourseRepository;

/**
 * A {@link SearchStrategyInterface} implementation that finds
 * {@link Business} and {@link Course} entities within a given
 * geographic radius from a specified {@link Location}.
 */
public class SearchRadiusStrategy implements SearchStrategyInterface<Location>
{
    // Conversion source: https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-and-km-distance
    private final FirebaseFirestore db;
    private final CourseRepository courseRepository;
    private final double radius;

    /**
     * Constructs a new SearchRadiusStrategy.
     *
     * @param radius the search radius in kilometers
     */
    public SearchRadiusStrategy(double radius)
    {
        this.radius = radius * 1000;
        db = FirebaseFirestore.getInstance();
        courseRepository = new CourseRepository();
    }

    /**
     * Injection constructor for testing.
     *
     * @param courseRepository the CourseRepository to use
     * @param db               FirebaseFirestore instance to use
     */
    public SearchRadiusStrategy(CourseRepository courseRepository, FirebaseFirestore db, double radius)
    {
        this.courseRepository = courseRepository;
        this.db = db;
        this.radius = radius * 1000;
    }

    /**
     * Converts degrees of latitude into approximate kilometers.
     *
     * @param latitude degrees of latitude
     * @return kilometers corresponding to the latitude difference
     */
    private double convertLatitudeToKM(double latitude)
    {
        return latitude * 110.574;
    }

    /**
     * Converts degrees of longitude into approximate kilometers at a given latitude.
     *
     * @param longitude degrees of longitude
     * @param latitude  latitude at which to compute the conversion
     * @return kilometers corresponding to the longitude difference
     */
    private double convertLongitudeToKM(double longitude, double latitude)
    {
        double rad = Math.toRadians(latitude);
        return 111.320 * longitude * Math.cos(rad);
    }

    /**
     * Helper function that calculates the distance between two locations
     *
     * @param a  the reference location
     * @param b the target location
     * @return distance in kilometers between the two points
     */
    private double distance(Location a, Location b) {
        double meanLat = (a.getLatitude() + b.getLatitude()) / 2.0;

        double aLatKm  = convertLatitudeToKM(a.getLatitude());
        double bLatKm  = convertLatitudeToKM(b.getLatitude());

        double aLonKm  = convertLongitudeToKM(a.getLongitude(), meanLat);
        double bLonKm  = convertLongitudeToKM(b.getLongitude(), meanLat);

        double dx = aLonKm - bLonKm;
        double dy = aLatKm - bLatKm;
        return Math.hypot(dx, dy) * 1000; // meter
    }


    /**
     * Searches for {@link Business} documents whose locations fall within the
     * specified radius of the {@code current} location. First applies a bounding‐box
     * filter for performance, then refines by true distance, and finally loads
     * each business’s courses before returning the results.
     *
     * @param current the center {@link Location} for the search
     * @return a Task completing with a List of matching {@link Business} objects,
     * each populated with its subcollection of courses.
     */
    @Override
    public Task<List<Business>> searchBusinesses(Location current)
    {
        double lat = current.getLatitude();
        double lon = current.getLongitude();

        // Compute a 'square' (slightly distorted by earth's curve)
        // around the point that covers radius and more
        // mean for first filtering
        double latDelta = radius / 110.574;
        double lonDelta = radius / (111.320 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lon - lonDelta;
        double maxLon = lon + lonDelta;

        // Prepare the query
        return db.collection("businesses")
                .whereGreaterThanOrEqualTo("location.latitude", minLat)
                .whereLessThanOrEqualTo("location.latitude", maxLat)
                .whereGreaterThanOrEqualTo("location.longitude", minLon)
                .whereLessThanOrEqualTo("location.longitude", maxLon)
                .get()

                // 3) Group matching Course objects by their parent Business ref, but only keep
                //    the courses whose true distance ≤ radius
                .onSuccessTask(bizSnap ->
                {
                    List<Business> inBox = new ArrayList<>();
                    for (DocumentSnapshot ds : bizSnap)
                    {
                        Business b = ds.toObject(Business.class);
                        if (b == null || b.getLocation() == null) continue;
                        b.setId(ds.getId());

                        double d = distance(current, b.getLocation());
                        if (d <= radius)
                        {
                            inBox.add(b);
                        }
                    }
                    if (inBox.isEmpty())
                    {
                        return Tasks.forResult(Collections.emptyList());
                    }

                    // 4) For each business, fetch its “courses” subcollection
                    List<Task<Business>> bizWithCourses = inBox.stream()
                            .map(business ->
                                    courseRepository
                                            .getAllBusinessesCourses(business)   // Task<List<Course>>
                                            .continueWith(courseTask ->
                                            {
                                                if (!courseTask.isSuccessful())
                                                {
                                                    throw Objects.requireNonNull(courseTask.getException());
                                                }
                                                business.setCourses((ArrayList<Course>) courseTask.getResult());
                                                return business;                  // now Task<Business>
                                            })
                            )
                            .collect(Collectors.toList());

                    // 5) Combine into a single Task<List<Business>>
                    return Tasks.<Business>whenAllSuccess(bizWithCourses);
                })

                // 6) Cast the final list
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
     * Searches for {@link Course} documents whose parent businesses fall within
     * the specified radius of {@code current}. Uses a bounding‐box filter, then
     * refines by true distance, and finally aggregates courses from each matching business.
     *
     * @param current the center {@link Location} for the search
     * @return a Task completing with a List of matching {@link Course} objects,
     * each annotated with its parent businessId.
     */
    @Override
    public Task<List<Course>> searchCourses(Location current)
    {
        double lat = current.getLatitude();
        double lon = current.getLongitude();

        // Compute bounding‐box deltas for radius (in kilometers)
        double latDelta = radius / 110.574;
        double lonDelta = radius / (111.320 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lon - lonDelta;
        double maxLon = lon + lonDelta;

        // Query the “businesses” collection for all businesses in the bounding box
        return db.collection("businesses")
                .whereGreaterThanOrEqualTo("location.latitude", minLat)
                .whereLessThanOrEqualTo("location.latitude", maxLat)
                .whereGreaterThanOrEqualTo("location.longitude", minLon)
                .whereLessThanOrEqualTo("location.longitude", maxLon)
                .get()

                // Filter out any businesses that lie outside the true circle (radius),
                //    then collect their IDs for the next step
                .onSuccessTask(bizSnap ->
                {
                    List<String> inBoxBizIds = new ArrayList<>();
                    for (DocumentSnapshot ds : bizSnap)
                    {
                        Business b = ds.toObject(Business.class);
                        if (b == null || b.getLocation() == null) continue;
                        b.setId(ds.getId());

                        double d = distance(current, b.getLocation());
                        if (d <= radius)
                        {
                            inBoxBizIds.add(ds.getId());
                        }
                    }

                    if (inBoxBizIds.isEmpty())
                    {
                        return Tasks.forResult(Collections.emptyList());
                    }

                    // 4) For each business ID, fetch its “courses” subcollection.
                    //    This produces a Task<List<Course>> per business.
                    List<Task<List<Course>>> courseTasks = inBoxBizIds.stream()
                            .map(bizId -> db.collection("businesses")
                                    .document(bizId)
                                    .collection("courses")
                                    .get()
                                    .continueWith(courseSnapTask ->
                                    {
                                        if (!courseSnapTask.isSuccessful())
                                        {
                                            throw Objects.requireNonNull(courseSnapTask.getException());
                                        }

                                        List<Course> bizCourses = new ArrayList<>();
                                        for (DocumentSnapshot cs : courseSnapTask.getResult())
                                        {
                                            Course c = cs.toObject(Course.class);
                                            if (c == null) continue;

                                            // a) Set this Course’s own Firestore ID
                                            c.setId(cs.getId());
                                            // b) Annotate with parent business ID
                                            c.setBusinessId(bizId);

                                            bizCourses.add(c);
                                        }
                                        return bizCourses;
                                    }))
                            .collect(Collectors.toList());

                    // 5) Combine all those Task<List<Course>> into one Task<List<Course>>
                    //    using whenAllSuccess. The result will be a List<Object> where each
                    //    Object is a List<Course>. We will flatten in the next step.
                    return Tasks.<List<Course>>whenAllSuccess(courseTasks);
                })

                // 6) Flatten List<List<Course>> → List<Course>
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(finalTask.getException());
                    }

                    @SuppressWarnings("unchecked")
                    List<List<Course>> listOfCourseLists = (List<List<Course>>) finalTask.getResult();
                    List<Course> allCourses = new ArrayList<>();
                    for (List<Course> sublist : listOfCourseLists)
                    {
                        allCourses.addAll(sublist);
                    }
                    return allCourses;
                });
    }
}
