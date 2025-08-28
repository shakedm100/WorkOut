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
 * This test suite fully stubs the Firestore chain for:
 * db.collection("businesses").get()  -> QuerySnapshot(businesses)
 * for each business: document(bizId).collection("courses").get() -> QuerySnapshot(courses)
 * and each course doc: toObject(Course.class) and getId()
 *
 * Replace SearchDayOfWeekStrategy and fetchByDay with your class/method names.
 */
public class DayOfWeekSearchStrategyTest
{

    // === Firestore root ===
    @Mock FirebaseFirestore db;

    // top-level "businesses" collection & snapshot
    @Mock CollectionReference businessesCol;
    @Mock QuerySnapshot businessesSnap;

    // business docs
    @Mock DocumentSnapshot bizDoc1;
    @Mock DocumentSnapshot bizDoc2;

    // per-business doc refs & nested "courses"
    @Mock DocumentReference b1DocRef;
    @Mock DocumentReference b2DocRef;
    @Mock CollectionReference b1CoursesCol;
    @Mock CollectionReference b2CoursesCol;

    // per-business courses snapshots
    @Mock QuerySnapshot coursesSnapB1;
    @Mock QuerySnapshot coursesSnapB2;

    // course docs
    @Mock DocumentSnapshot b1c1Doc;
    @Mock DocumentSnapshot b1c2Doc;
    @Mock DocumentSnapshot b2c1Doc;

    // class under test
    SearchStrategyInterface<Day> dayOfWeekSearchStrategy;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // ===== CLASS UNDER TEST =====
        dayOfWeekSearchStrategy = new SearchDayOfWeekStrategy(db);


        // ===== ROOT =====
        when(db.collection("businesses")).thenReturn(businessesCol);
        when(businessesCol.get()).thenReturn(Tasks.forResult(businessesSnap));

        // let's say we have two businesses: b1, b2
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

        // ===== COURSES DATA =====
        // b1 has 2 courses: Sunday (kept), Monday (filtered out)
        Course b1Sun = mkCourse(Day.Sunday);
        Course b1Mon = mkCourse(Day.Monday);
        when(b1c1Doc.toObject(Course.class)).thenReturn(copy(b1Sun));
        when(b1c2Doc.toObject(Course.class)).thenReturn(copy(b1Mon));
        when(b1c1Doc.getId()).thenReturn("b1c1");
        when(b1c2Doc.getId()).thenReturn("b1c2");
        when(coursesSnapB1.getDocuments()).thenReturn(Arrays.asList(b1c1Doc, b1c2Doc));

        // b2 has 1 course: Sunday (kept)
        Course b2Sun = mkCourse(Day.Sunday);
        when(b2c1Doc.toObject(Course.class)).thenReturn(copy(b2Sun));
        when(b2c1Doc.getId()).thenReturn("b2c1");
        when(coursesSnapB2.getDocuments()).thenReturn(Collections.singletonList(b2c1Doc));
    }

    @Test
    public void searchDayOfWeek_success() throws Exception {
        List<Course> result = Tasks.await(dayOfWeekSearchStrategy.searchCourses(Day.Sunday));

        // size check
        assertEquals(2, result.size());

        // contains both ids
        List<String> ids = result.stream().map(Course::getId).toList();
        assertTrue(ids.contains("b1c1")); // sunday
        assertTrue(ids.contains("b2c1")); // sunday
        assertFalse(ids.contains("b1c2")); // monday

        // all are Sunday
        for (Course c : result) {
            assertNotNull(c.getSchedule());
            assertEquals(Day.Sunday, c.getSchedule().getDay());
        }
    }

    @Test
    public void searchDayOfWeek_failure_handlesNoBusinesses() throws Exception {
        // Re-stub businesses to be empty
        when(businessesSnap.getDocuments()).thenReturn(Collections.emptyList());

        Task<List<Course>> task = dayOfWeekSearchStrategy.searchCourses(Day.Sunday);
        List<Course> result = Tasks.await(task);

        assertTrue(result.isEmpty());
    }

    @Test
    public void searchDayOfWeek_failure()
    {
        // Make b1 courses fail
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

    // ======= helpers =======

    private static Course mkCourse(Day day) {
        Course c = new Course();
        Schedule s = new Schedule();
        s.setDay(day);
        c.setSchedule(s);
        return c;
    }
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
