package com.example.workout.SearchStrategy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;

import Model.Category;
import Model.Course;
import Model.Day;
import Model.Repository.CourseRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

/**
 * Firestore-mocked tests for Category search strategy.
 * ----------------------------------------------------
 * Courses match if the Category is the same as a given Category.
 * course.getCategory.name == given Category.name.
 */
public class CategorySearchStrategyTest
{
    // root chain
    @Mock FirebaseFirestore db;
    @Mock Query cg; // for db.collectionGroup("courses")
    @Mock Query qCat; // for .whereEqualTo("category", wanted)

    // for snapshot with multiple docs
    @Mock QuerySnapshot cgSnapBoth;
    @Mock QuerySnapshot cgSnapOnlyWanted;
    @Mock QuerySnapshot cgSnapEmpty;

    // returned docs
    @Mock DocumentSnapshot docWanted1;
    @Mock DocumentSnapshot docWanted2;
    @Mock DocumentSnapshot docOther1;

    // get businessId from the doc path:
    @Mock DocumentReference w1Ref;
    @Mock DocumentReference w2Ref;
    @Mock DocumentReference o1Ref;

    // courses collections
    @Mock CollectionReference w1CoursesColRef;
    @Mock CollectionReference w2CoursesColRef;
    @Mock CollectionReference o1CoursesColRef;

    // business docs refs
    @Mock DocumentReference w1BizRef;
    @Mock DocumentReference w2BizRef;
    @Mock DocumentReference o1BizRef;
    @Mock CourseRepository mockCourseRepo;
    private SearchStrategyInterface<Category> categorySearchStrategy; // selected strategy test

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        mockCourseRepo = mock(CourseRepository.class);
        categorySearchStrategy = new SearchCategoryStrategy(mockCourseRepo, db);

        // mock & stub
        when(db.collectionGroup("courses")).thenReturn(cg);

        // category filter
        when(cg.whereEqualTo(eq("category"), any())).thenReturn(qCat);

        // Build fake courses with different categories
        // Two in BASKETBALL (wanted) and one in SOCCER (not wanted)
        Course wanted1 = mkCourse(Category.Basketball);
        Course wanted2 = mkCourse(Category.Basketball);
        Course other1  = mkCourse(Category.Soccer);

        // map toObject + IDs
        when(docWanted1.toObject(Course.class)).thenReturn(copy(wanted1));
        when(docWanted2.toObject(Course.class)).thenReturn(copy(wanted2));
        when(docOther1.toObject(Course.class)).thenReturn(copy(other1));

        when(docWanted1.getId()).thenReturn("cat_w1");
        when(docWanted2.getId()).thenReturn("cat_w2");
        when(docOther1.getId()).thenReturn("cat_o1");

        // snapshots with different values to check
        when(cgSnapOnlyWanted.getDocuments()).thenReturn(Arrays.asList(docWanted1, docWanted2));
        when(cgSnapBoth.getDocuments()).thenReturn(Arrays.asList(docWanted1, docOther1)); // mixed
        when(cgSnapEmpty.getDocuments()).thenReturn(Collections.emptyList());

        // get businessesIds via path parents
        when(docWanted1.getReference()).thenReturn(w1Ref);
        when(docWanted2.getReference()).thenReturn(w2Ref);
        when(docOther1.getReference()).thenReturn(o1Ref);

        when(w1Ref.getParent()).thenReturn(w1CoursesColRef);
        when(w2Ref.getParent()).thenReturn(w2CoursesColRef);
        when(o1Ref.getParent()).thenReturn(o1CoursesColRef);

        when(w1CoursesColRef.getParent()).thenReturn(w1BizRef);
        when(w2CoursesColRef.getParent()).thenReturn(w2BizRef);
        when(o1CoursesColRef.getParent()).thenReturn(o1BizRef);

        when(w1BizRef.getId()).thenReturn("biz_w1");
        when(w2BizRef.getId()).thenReturn("biz_w2");
        when(o1BizRef.getId()).thenReturn("biz_o1");
    }

    @Test
    public void searchCategory_success_twoMatches() throws Exception
    {
        Category wanted = Category.Basketball; // the wanted category

        // exact whereEqualTo filter
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        // return only docs that match the wanted category
        when(qCat.get()).thenReturn(Tasks.forResult(cgSnapOnlyWanted));

        List<Course> result = Tasks.await(categorySearchStrategy.searchCourses(wanted));

        assertEquals(2, result.size());
        Set<String> ids = new HashSet<>();

        // get the ids of the matched courses
        for (Course c : result)
            ids.add(c.getId());

        assertTrue(ids.contains("cat_w1"));
        assertTrue(ids.contains("cat_w2"));
        assertFalse(ids.contains("cat_o1"));

        // make sure the selected courses are Basketball category
        for (Course c : result)
        {
            assertNotNull(c.getBusinessId());
            assertTrue(c.getBusinessId().equals("biz_w1") || c.getBusinessId().equals("biz_w2"));
            assertEquals(Category.Basketball, c.getCategory());
        }
    }

    @Test
    public void searchCategory_success_empty() throws Exception
    {
        Category wanted = Category.Tennis; // no matched course

        // exact whereEqualTo with this given wanted course
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        // return empty results for this category
        when(qCat.get()).thenReturn(Tasks.forResult(cgSnapEmpty));

        List<Course> result = Tasks.await(categorySearchStrategy.searchCourses(wanted));
        assertTrue(result.isEmpty()); // no matches
    }

    @Test
    public void searchCategory_failure()
    {
        Category wanted = Category.Basketball;
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        RuntimeException fail = new RuntimeException("category search failed");
        when(qCat.get()).thenReturn(Tasks.forException(fail));

        Task<List<Course>> task = categorySearchStrategy.searchCourses(wanted);

        try {
            Tasks.await(task);
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertSame(fail, e.getCause());
        } catch (Exception e) {
            fail("Expected ExecutionException, got: " + e);
        }
    }

    // helper functions

    /**
     * Create a course with a given category.
     * @param category a given category.
     * @return a new course with the given category.
     */
    private static Course mkCourse(Category category)
    {
        Course c = new Course();
        c.setCategory(category);
        return c;
    }

    /**
     * Create a copy of a course.
     * @param src the source course that will be copied.
     * @return a new course with the same category as the source.
     */
    private static Course copy(Course src)
    {
        Course c = new Course();
        c.setCategory(src.getCategory());
        return c;
    }
}
