package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;

import java.time.LocalDateTime;
import java.util.List;

import Model.Business;

public class SearchDateStrategy implements SearchStrategyInterface<LocalDateTime>
{
    @Override
    public Task<List<Business>> search(LocalDateTime args)
    {
        return null;
    }
}
