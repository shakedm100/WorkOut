package Model.Repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.City;

public class GeneralRepository {

    FirebaseFirestore db;

    public GeneralRepository() { db = FirebaseFirestore.getInstance(); }

    /**
     * Returns up to 3 cities whose names start with the given partialName.
     * @param namePrefix the name prefix to search for
     * @return a Task that completes with a List<City>
     */
    public Task<List<City>> getCityByNamePartially(String namePrefix)
    {
        // Firestore strings sort lexicographically, so we can do:
        // orderBy("name").startAt(partialName).endAt(partialName + "\uf8ff")
        // startAt and endAt tell the db what range to look at
        // So basically the code states for the db to look at all the range starting at namePrefix
        // And namePrefix + one unicode char
        Query q = db.collection("cities")
                .orderBy("name")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff") // Signal an ending with UTF-8 encoding
                .limit(3);

        return q.get().continueWith(task ->
        {
            if(!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            List<City> cities = new ArrayList<>();
            for(DocumentSnapshot snap : task.getResult())
            {
                City city = snap.toObject(City.class);
                if(city != null)
                    cities.add(city);
            }

            return cities;
        });
    }
}
