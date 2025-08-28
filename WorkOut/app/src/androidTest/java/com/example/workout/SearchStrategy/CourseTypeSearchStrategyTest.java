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

import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Schedule;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

/**
 * Firestore-mocked tests for CourseType search strategy.
 * ------------------------------------------------------
 * Courses match if the CourseType is the same as the given CourseType.
 * course.getType == given CourseType.
 */
public class CourseTypeSearchStrategyTest
{
    // root
    @Mock FirebaseFirestore db;
    @Mock Query cg; // for db.collectionGroup("courses")
    @Mock Query qType; // for .whereEqualTo("courseType", wanted)

    // wanted snaps
    @Mock QuerySnapshot snapOnlyWanted;
    @Mock QuerySnapshot snapOnlyNotWanted;
    @Mock QuerySnapshot snapEmpty;

    // returned docs
    @Mock DocumentSnapshot docWanted1;
    @Mock DocumentSnapshot docWanted2;
    @Mock DocumentSnapshot notWanted3;

    // get businessId from path (if your prod does that)
    @Mock DocumentReference w1Ref;
    @Mock DocumentReference w2Ref;
    @Mock DocumentReference w3Ref;
    @Mock CollectionReference w1CoursesColRef;
    @Mock CollectionReference w2CoursesColRef;
    @Mock CollectionReference w3CoursesColRef;
    @Mock DocumentReference w1BizRef;
    @Mock DocumentReference w2BizRef;
    @Mock DocumentReference w3BizRef;
    private SearchStrategyInterface<CourseType> courseTypeSearchStrategy; // test strategy

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        courseTypeSearchStrategy = new SearchCourseTypeStrategy(db);

        // mock & stub
        when(db.collectionGroup("courses")).thenReturn(cg);

        // whereEqualTo for courseType
        when(cg.whereEqualTo(eq("type"), any())).thenReturn(qType);

        // create fake courses
        Course wanted1 = mkCourse(CourseType.Solo);
        Course wanted2 = mkCourse(CourseType.Solo);
        Course not_wanted3 = mkCourse(CourseType.Group);

        // create fake docs
        when(docWanted1.toObject(Course.class)).thenReturn(copy(wanted1));
        when(docWanted2.toObject(Course.class)).thenReturn(copy(wanted2));
        when(notWanted3.toObject(Course.class)).thenReturn(copy(not_wanted3));

        when(docWanted1.getId()).thenReturn("type_w1");
        when(docWanted2.getId()).thenReturn("type_w2");

        when(snapOnlyWanted.getDocuments()).thenReturn(Arrays.asList(docWanted1, docWanted2));
        when(snapOnlyNotWanted.getDocuments()).thenReturn(Arrays.asList(notWanted3));
        when(snapEmpty.getDocuments()).thenReturn(Collections.emptyList());

        // businessId via path parents
        when(docWanted1.getReference()).thenReturn(w1Ref);
        when(docWanted2.getReference()).thenReturn(w2Ref);
        when(notWanted3.getReference()).thenReturn(w3Ref);
        when(w1Ref.getParent()).thenReturn(w1CoursesColRef);
        when(w2Ref.getParent()).thenReturn(w2CoursesColRef);
        when(w3Ref.getParent()).thenReturn(w3CoursesColRef);
        when(w1CoursesColRef.getParent()).thenReturn(w1BizRef);
        when(w2CoursesColRef.getParent()).thenReturn(w2BizRef);
        when(w1BizRef.getId()).thenReturn("biz_type_w1");
        when(w2BizRef.getId()).thenReturn("biz_type_w2");
        when(w3BizRef.getId()).thenReturn("biz_type_w3");
    }

    @Test
    public void searchCourseType_success_twoMatches() throws Exception
    {
        CourseType wanted = CourseType.Solo;

        // make the exact filter
        when(cg.whereEqualTo("type", wanted)).thenReturn(qType);
        when(qType.get()).thenReturn(Tasks.forResult(snapOnlyWanted));

        List<Course> result = Tasks.await(courseTypeSearchStrategy.searchCourses(wanted));

        assertEquals(2, result.size()); // expect to get two courses
        Set<String> ids = new HashSet<>();
        for (Course c : result)
            ids.add(c.getId());

        // assert
        assertTrue(ids.contains("type_w1"));
        assertTrue(ids.contains("type_w2"));
        assertFalse(ids.contains("type_w3")); // not wanted

        // make sure we got the solo types
        for (Course c : result)
        {
            assertEquals(CourseType.Solo, c.getType());
            assertNotNull(c.getBusinessId());
            assertTrue(c.getBusinessId().equals("biz_type_w1") || c.getBusinessId().equals("biz_type_w2"));
        }
    }

    @Test
    public void searchCourseType_success_empty() throws Exception
    {
        CourseType wanted = CourseType.Duo;

        when(cg.whereEqualTo("type", wanted)).thenReturn(qType);
        when(qType.get()).thenReturn(Tasks.forResult(snapEmpty));

        List<Course> result = Tasks.await(courseTypeSearchStrategy.searchCourses(wanted));
        assertTrue(result.isEmpty());
    }

    @Test
    public void searchCourseType_failure()
    {
        CourseType wanted = CourseType.Solo;
        when(cg.whereEqualTo("type", wanted)).thenReturn(qType);

        RuntimeException fail = new RuntimeException("courseType get failed");
        when(qType.get()).thenReturn(Tasks.forException(fail));

        Task<List<Course>> task = courseTypeSearchStrategy.searchCourses(wanted);

        try
        {
            Tasks.await(task);
            fail("Expected ExecutionException");
        } catch (ExecutionException e)
        {
            assertSame(fail, e.getCause());
        } catch (Exception e)
        {
            fail("Expected ExecutionException, got: " + e);
        }
    }

    // helpers

    /**
     * Create a course with a given CourseType.
     * @param type a given CourseType.
     * @return a new course of that type.
     */
    private static Course mkCourse(CourseType type) {
        Course c = new Course();
        c.setType(type);
        return c;
    }

    /**
     * Create a copy of a course.
     * @param src the source course that will be copied.
     * @return a new course with the same type as the source.
     */
    private static Course copy(Course src) {
        Course c = new Course();

        c.setType(src.getType());
        return c;
    }
}
