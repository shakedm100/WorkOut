package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Business;
import Model.Location;

public class SearchRadiusStrategy implements SearchStrategyInterface<Location>
{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final double radius;

    public SearchRadiusStrategy(double radius)
    {
        this.radius = radius;
    }

    // Conversion source: https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-and-km-distance
    private double convertLatitudeToKM(double latitude)
    {
        return latitude * 110.574;
    }

    private double convertLongitudeToKM(double longitude, double latitude)
    {
        double rad = Math.toRadians(latitude);
        return 111.320*longitude*Math.cos(rad);
    }

    /**
     * Helper function that calculates the distance between two locations
     * @param currentLocation the current user's location
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
        Query q =  db.collection("businesses").document().collection("courses")
                .whereGreaterThanOrEqualTo("location.latitude", minLat)
                .whereLessThanOrEqualTo("location.latitude", maxLat)
                .whereGreaterThanOrEqualTo("location.longitude", minLon)
                .whereLessThanOrEqualTo("location.longitude", maxLon);

        return q.get().continueWith(task ->
        {
           if(!task.isSuccessful())
               throw Objects.requireNonNull(task.getException());

           List<Business> results = new ArrayList<>();
           for(DocumentSnapshot documentSnapshot : task.getResult())
           {
               Business business = documentSnapshot.toObject(Business.class);
               if(business == null || business.getLocation() == null)
                    continue;

               // After the first filtering make sure it's really is inside the radius
               // of our search
               double dis = distance(current, business.getLocation());
               if(dis <= radius)
                   results.add(business);
           }

           return results;
        });
    }
}
