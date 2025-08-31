package com.example.workout.SearchStrategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

import Model.AgeRange;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Schedule;
import Model.Location;
import Model.SearchStrategies.SearchDayOfWeekStrategy;
import Model.SearchStrategies.SearchStrategyInterface;


/**
 * Firestore-mocked tests for day search strategy.
 * -----------------------------------------------
 * Courses match if the course occurs at a given day.
 * course.schedule.day == given day.
 */
public class DayOfWeekSearchStrategyTest
{
    // root
    @Mock FirebaseFirestore db;

    // "businesses" collection & snapshot
    @Mock CollectionReference businessesCol;
    @Mock QuerySnapshot businessesSnap;

    // business docs
    @Mock DocumentSnapshot bizDoc1;
    @Mock DocumentSnapshot bizDoc2;

    // business doc refs & nested "courses"
    @Mock DocumentReference b1DocRef;
    @Mock DocumentReference b2DocRef;
    @Mock CollectionReference b1CoursesCol;
    @Mock CollectionReference b2CoursesCol;

    // business courses snapshots
    @Mock QuerySnapshot coursesSnapB1;
    @Mock QuerySnapshot coursesSnapB2;

    // course docs
    @Mock DocumentSnapshot b1c1Doc;
    @Mock DocumentSnapshot b1c2Doc;
    @Mock DocumentSnapshot b2c1Doc;
    SearchStrategyInterface<Day> dayOfWeekSearchStrategy; // test strategy

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dayOfWeekSearchStrategy = new SearchDayOfWeekStrategy(db);


        // mock & stub relevant methods
        when(db.collection("businesses")).thenReturn(businessesCol);
        when(businessesCol.get()).thenReturn(Tasks.forResult(businessesSnap));

        // create two businesses: b1, b2
        when(bizDoc1.getId()).thenReturn("b1");
        when(bizDoc2.getId()).thenReturn("b2");
        when(businessesSnap.getDocuments()).thenReturn(Arrays.asList(bizDoc1, bizDoc2));

        // document(id) per business
        when(businessesCol.document("b1")).thenReturn(b1DocRef);
        when(businessesCol.document("b2")).thenReturn(b2DocRef);

        // nested "courses" collections
        when(b1DocRef.collection("courses")).thenReturn(b1CoursesCol);
        when(b2DocRef.collection("courses")).thenReturn(b2CoursesCol);

        // each coursesCol.get() returns a successful Task
        when(b1CoursesCol.get()).thenReturn(Tasks.forResult(coursesSnapB1));
        when(b2CoursesCol.get()).thenReturn(Tasks.forResult(coursesSnapB2));

        // create fake courses
        // b1 has 2 courses: Sunday (match), Monday (not matched)
        Course b1Sun = mkCourse(Day.Sunday);
        Course b1Mon = mkCourse(Day.Monday);
        when(b1c1Doc.toObject(Course.class)).thenReturn(copy(b1Sun));
        when(b1c2Doc.toObject(Course.class)).thenReturn(copy(b1Mon));
        when(b1c1Doc.getId()).thenReturn("b1c1");
        when(b1c2Doc.getId()).thenReturn("b1c2");
        when(coursesSnapB1.getDocuments()).thenReturn(Arrays.asList(b1c1Doc, b1c2Doc));

        // b2 has 1 course: Sunday (match)
        Course b2Sun = mkCourse(Day.Sunday);
        when(b2c1Doc.toObject(Course.class)).thenReturn(copy(b2Sun));
        when(b2c1Doc.getId()).thenReturn("b2c1");
        when(coursesSnapB2.getDocuments()).thenReturn(Collections.singletonList(b2c1Doc));
    }

    @Test
    public void searchDayOfWeek_success() throws Exception
    {
        List<Course> result = Tasks.await(dayOfWeekSearchStrategy.searchCourses(Day.Sunday));

        // size check
        assertEquals(2, result.size()); // expected 2 courses

        // contains both ids
        List<String> ids = result.stream().map(Course::getId).toList();
        assertTrue(ids.contains("b1c1")); // sunday
        assertTrue(ids.contains("b2c1")); // sunday
        assertFalse(ids.contains("b1c2")); // monday

        // make sure they occur at Sunday
        for (Course c : result)
        {
            assertNotNull(c.getSchedule());
            assertEquals(Day.Sunday, c.getSchedule().getDay());
        }
    }

    @Test
    public void searchDayOfWeek_success_empty() throws Exception
    {
        // stub businesses to be empty
        when(businessesSnap.getDocuments()).thenReturn(Collections.emptyList());

        Task<List<Course>> task = dayOfWeekSearchStrategy.searchCourses(Day.Sunday);
        List<Course> result = Tasks.await(task);

        assertTrue(result.isEmpty());
    }

    @Test
    public void searchDayOfWeek_failure()
    {
        // force b1 courses fail
        RuntimeException fail = new RuntimeException("fail");
        when(b1CoursesCol.get()).thenReturn(Tasks.forException(fail));

        Task<List<Course>> task = dayOfWeekSearchStrategy.searchCourses(Day.Sunday);

        try
        {
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
     * Create a course with a given day.
     * @param day a given day.
     * @return a new course occurs at that day.
     */
    private static Course mkCourse(Day day) {
        Course c = new Course();
        Schedule s = new Schedule();
        s.setDay(day);
        c.setSchedule(s);
        return c;
    }

    /**
     * Create a copy of a course.
     * @param src the source course that will be copied.
     * @return a new course with the same day as the source.
     */
    private static Course copy(Course src) {
        Course c = new Course();
        if (src.getSchedule() != null) {
            Schedule s = new Schedule();
            s.setDay(src.getSchedule().getDay());
            c.setSchedule(s);
        }
        return c;
    }
}
