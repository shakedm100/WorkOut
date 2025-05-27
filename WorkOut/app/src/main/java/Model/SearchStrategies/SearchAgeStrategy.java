package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import Model.AgeRange;
import Model.Business;

public class SearchAgeStrategy implements SearchStrategyInterface<AgeRange>
{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * This method find all the businesses that have courses with a compatible age range,
     * above the min and below the max
     * @param ageRange the minimum and maximum ages
     * @return all businesses that have a course between the desired ages
     */
    @Override
    public Task<List<Business>> search(AgeRange ageRange)
    {
        return db.collection("businesses").document().collection("courses")
                .whereGreaterThanOrEqualTo("minAge", ageRange.getMaxAge()) // minAge >=
                .whereLessThanOrEqualTo("maxAge", ageRange.getMinAge())// maxAge <=
                .get().onSuccessTask(task ->
                {
                    Set<DocumentReference> referenceSet = new HashSet<>();
                    for(DocumentSnapshot documentSnapshot : task)
                    {
                        // If we want to change it to return Courses, all that needs to be done
                        // is to delete currentBusiness and add to the referenceSet currentCourse instead
                        DocumentReference currentCourse = documentSnapshot.getReference();
                        DocumentReference currentBusiness = currentCourse.getParent().getParent();
                        if(currentBusiness != null)
                            referenceSet.add(currentBusiness);
                    }

                    if(referenceSet.isEmpty())
                        return Tasks.forResult(Collections.emptyList());

                    List<Task<DocumentSnapshot>> fetchTasks = referenceSet.stream()
                            .map(DocumentReference::get).collect(Collectors.toList());

                    @SuppressWarnings("Not redundant") // if we don't specify Task<List<DocumentSnapshot>> the next task
                            // won't know the documentSnapshots can get the results
                    Task<List<DocumentSnapshot>> allSnapshots = Tasks.whenAllSuccess(fetchTasks);
                    return allSnapshots;
                })
                .continueWith(task -> {
                    if(!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    List<DocumentSnapshot> documentSnapshots = task.getResult();
                    List<Business> businessList = new ArrayList<>();
                    for(DocumentSnapshot documentSnapshot : documentSnapshots)
                    {
                        businessList.add(documentSnapshot.toObject(Business.class));
                    }

                    return businessList;
                });
    }
}
