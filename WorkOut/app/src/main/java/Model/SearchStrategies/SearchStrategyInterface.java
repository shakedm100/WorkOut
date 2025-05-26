package Model.SearchStrategies;
import com.google.android.gms.tasks.Task;

import java.util.List;

import Model.Business;

public interface SearchStrategyInterface<T>
{
    abstract Task<List<Business>> search(T args);
}
