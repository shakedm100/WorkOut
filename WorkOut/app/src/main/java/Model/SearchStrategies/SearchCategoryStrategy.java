package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Business;
import Model.Category;

public class SearchCategoryStrategy implements SearchStrategyInterface<Category>
{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Find all the businesses that have a matching category
     * @param category the category the user wants
     * @return list of businesses that have that category
     */
    @Override
    public Task<List<Business>> search(Category category)
    {
        return db.collection("business").document().collection("courses")
                .whereEqualTo("category", category).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    QuerySnapshot reference = task.getResult();
                    List<Business> results = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : reference)
                    {
                        Business business = documentSnapshot.toObject(Business.class);
                        if (business != null)
                            results.add(business);
                    }

                    return results;
                });
    }
}
