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

public class SearchRadiusStrategy implements SearchStrategyInterface<Location>
{
    private final FirebaseFirestore db;
    private final CourseRepository courseRepository;
    private final double radius;

    public SearchRadiusStrategy(double radius)
    {
        this.radius = radius;
        db = FirebaseFirestore.getInstance();
        courseRepository = new CourseRepository();
    }

    // Conversion source: https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-and-km-distance
    private double convertLatitudeToKM(double latitude)
    {
        return latitude * 110.574;
    }

    private double convertLongitudeToKM(double longitude, double latitude)
    {
        double rad = Math.toRadians(latitude);
        return 111.320 * longitude * Math.cos(rad);
    }

    /**
     * Helper function that calculates the distance between two locations
     *
     * @param currentLocation  the current user's location
     * @param businessLocation the business's location
     * @return the distance between them
     */
    private double distance(Location currentLocation, Location businessLocation)
    {
        double currentLat = convertLatitudeToKM(currentLocation.getLatitude());
        double currentLong = convertLongitudeToKM(currentLocation.getLongitude(), currentLocation.getLatitude());

        double businessLat = convertLatitudeToKM(businessLocation.getLatitude());
        double businessLong = convertLongitudeToKM(businessLocation.getLongitude(), currentLocation.getLatitude());

        double powX = Math.pow((currentLat - businessLat), 2);
        double powY = Math.pow((currentLong - businessLong), 2);

        return Math.sqrt(powX + powY);
    }

    @Override
    public Task<List<Business>> search(Location current)
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
}
