package Model.SearchStrategies;
import com.google.android.gms.tasks.Task;

import java.util.List;

import Model.Business;
import Model.Course;

public interface SearchStrategyInterface<T>
{
    abstract Task<List<Business>> searchBusinesses(T args);
    abstract Task<List<Course>> searchCourses(T args);
}
