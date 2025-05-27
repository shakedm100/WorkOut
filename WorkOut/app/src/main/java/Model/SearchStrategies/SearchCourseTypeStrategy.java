package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Business;
import Model.CourseType;

public class SearchCourseTypeStrategy implements SearchStrategyInterface<CourseType>
{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public Task<List<Business>> search(CourseType type)
    {
        return db.collection("business").document().collection("courses")
                .whereEqualTo("type", type).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                   List<Business> businesses = new ArrayList<>();
                   for(DocumentSnapshot documentSnapshot : task.getResult())
                   {
                       Business business = documentSnapshot.toObject(Business.class);
                       if(business != null)
                           businesses.add(business);
                   }

                   return businesses;
                });
    }
}
