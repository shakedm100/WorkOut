package Model.SearchStrategies;
import com.google.android.gms.tasks.Task;

import java.util.List;

import Model.Business;
import Model.Course;

/**
 * Generic interface defining a search strategy for both {@link Business}
 * and {@link Course} entities.
 *
 * @param <T> the type of argument object used to parameterize the search.
 */
public interface SearchStrategyInterface<T>
{
    /**
     * Searches for businesses matching the given criteria.
     *
     * @param args an instance of {@code T} encapsulating business search parameters
     * @return a {@link Task} that completes with a {@link List} of matching {@link Business} objects,
     *         or fails with an appropriate exception if the query cannot be executed.
     */
    abstract Task<List<Business>> searchBusinesses(T args);

    /**
     * Searches for courses matching the given criteria.
     *
     * @param args an instance of {@code T} encapsulating course search parameters
     * @return a {@link Task} that completes with a {@link List} of matching {@link Course} objects,
     *         or fails with an appropriate exception if the query cannot be executed.
     */
    abstract Task<List<Course>> searchCourses(T args);
}
