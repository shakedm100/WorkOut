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

import Model.AgeRange;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Location;
import Model.Repository.CourseRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

/**
 * Firestore-mocked tests for AgeRange search strategy.
 * ----------------------------------------------------
 * A course matches if its AgeRange fully covers the requested AgeRange:
 * course.min <= wanted.min  AND  course.max >= wanted.max
 */
public class AgeSearchStrategyTest {

    // root
    @Mock FirebaseFirestore db;
    @Mock CourseRepository mockCourseRepo;

    // businesses collection & snapshot
    @Mock CollectionReference businessesCol;
    @Mock QuerySnapshot businessesSnap;

    // business docs
    @Mock DocumentSnapshot bizDoc1;
    @Mock DocumentSnapshot bizDoc2;

    // businesses doc refs & nested "courses"
    @Mock DocumentReference b1DocRef;
    @Mock DocumentReference b2DocRef;
    @Mock CollectionReference b1CoursesCol;
    @Mock CollectionReference b2CoursesCol;

    // businesses courses snapshots
    @Mock QuerySnapshot coursesSnapB1;
    @Mock QuerySnapshot coursesSnapB2;

    // course docs
    @Mock DocumentSnapshot b1c1Doc;
    @Mock DocumentSnapshot b1c2Doc;
    @Mock DocumentSnapshot b1c3Doc;
    @Mock DocumentSnapshot b2c1Doc;


    // collectionGroup("courses") chain
    @Mock Query cg; // for db.collectionGroup("courses")
    @Mock Query q1; // for cg.whereLessThanOrEqualTo("ageRange.minAge", wanted.max)
    @Mock Query q2; // for q1.whereGreaterThanOrEqualTo("ageRange.maxAge", wanted.min)
    @Mock QuerySnapshot cgSnap; // for empty snap
    @Mock DocumentSnapshot cDoc1; // for empty success test
    @Mock DocumentSnapshot cDoc2; // for empty success test
    @Mock DocumentReference c1Ref; // c1 ref
    @Mock DocumentReference c2Ref; // c2 ref

    // course & businesses collections refs
    @Mock CollectionReference c1CoursesColRef;
    @Mock CollectionReference c2CoursesColRef;
    @Mock DocumentReference c1BusinessDocRef;
    @Mock DocumentReference c2BusinessDocRef;
    @Mock QuerySnapshot cgSnapEmpty; // for empty success test
    @Mock QuerySnapshot cgSnapOnlyC1; // for only 1 valid course
    private SearchStrategyInterface<AgeRange> ageRangeSearchStrategy; // selected strategy

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        // stub repository & necessary methods
        mockCourseRepo = mock(CourseRepository.class);
        ageRangeSearchStrategy = new SearchAgeStrategy(mockCourseRepo, db);

        // root
        when(db.collection("businesses")).thenReturn(businessesCol);
        when(businessesCol.get()).thenReturn(Tasks.forResult(businessesSnap));

        // "create" two businesses: b1, b2
        when(bizDoc1.getId()).thenReturn("b1");
        when(bizDoc2.getId()).thenReturn("b2");
        when(businessesSnap.getDocuments()).thenReturn(Arrays.asList(bizDoc1, bizDoc2));

        // document(id) per business
        when(businessesCol.document("b1")).thenReturn(b1DocRef);
        when(businessesCol.document("b2")).thenReturn(b2DocRef);

        // set the nested "courses" collections to each business
        when(b1DocRef.collection("courses")).thenReturn(b1CoursesCol);
        when(b2DocRef.collection("courses")).thenReturn(b2CoursesCol);

        // each coursesCol.get() returns a successful Task
        when(b1CoursesCol.get()).thenReturn(Tasks.forResult(coursesSnapB1));
        when(b2CoursesCol.get()).thenReturn(Tasks.forResult(coursesSnapB2));

        // create fake courses
        Course b1c1 = mkCourse(10, 16);
        Course b1c2 = mkCourse(12, 14);
        Course b1c3 = mkCourse(15, 17);

        // stub
        when(b1c1Doc.toObject(Course.class)).thenReturn(copy(b1c1));
        when(b1c2Doc.toObject(Course.class)).thenReturn(copy(b1c2));
        when(b1c3Doc.toObject(Course.class)).thenReturn(copy(b1c3));
        when(b1c1Doc.getId()).thenReturn("b1c1");
        when(b1c2Doc.getId()).thenReturn("b1c2");
        when(b1c3Doc.getId()).thenReturn("b1c3");
        when(coursesSnapB1.getDocuments()).thenReturn(Arrays.asList(b1c1Doc, b1c2Doc, b1c3Doc));

        // b2 has:
        // c1: 8-11 -> should NOT match
        Course b2c1 = mkCourse(8, 11);
        when(b2c1Doc.toObject(Course.class)).thenReturn(copy(b2c1));
        when(b2c1Doc.getId()).thenReturn("b2c1");
        when(coursesSnapB2.getDocuments()).thenReturn(Collections.singletonList(b2c1Doc));

        // Start of collectionGroup chain
        when(db.collectionGroup("courses")).thenReturn(cg);

        // stub chain filters
        when(cg.whereLessThanOrEqualTo(eq("ageRange.minAge"), anyInt())).thenReturn(q1);
        when(q1.whereGreaterThanOrEqualTo(eq("ageRange.maxAge"), anyInt())).thenReturn(q2);

        // stub final get()
        when(q2.get()).thenReturn(Tasks.forResult(cgSnap));

        // Documents returned by the collectionGroup query
        // c1: 10-16  (overlaps 12-14) -> match
        // c2: 15-17  (does NOT overlap 12-14) -> no match
        Course cgC1 = mkCourse(10, 16);
        Course cgC2 = mkCourse(15, 17);

        when(cDoc1.toObject(Course.class)).thenReturn(copy(cgC1));
        when(cDoc2.toObject(Course.class)).thenReturn(copy(cgC2));
        when(cDoc1.getId()).thenReturn("cg_c1");
        when(cDoc2.getId()).thenReturn("cg_c2");
        when(cgSnap.getDocuments()).thenReturn(Arrays.asList(cDoc1, cDoc2));

        // extracts businessId from the doc path
        when(cDoc1.getReference()).thenReturn(c1Ref);
        when(cDoc2.getReference()).thenReturn(c2Ref);

        when(c1Ref.getParent()).thenReturn(c1CoursesColRef);
        when(c2Ref.getParent()).thenReturn(c2CoursesColRef);

        when(c1CoursesColRef.getParent()).thenReturn(c1BusinessDocRef);
        when(c2CoursesColRef.getParent()).thenReturn(c2BusinessDocRef);

        when(c1BusinessDocRef.getId()).thenReturn("biz_from_cg_1");
        when(c2BusinessDocRef.getId()).thenReturn("biz_from_cg_2");

        // empty list
        when(cgSnapEmpty.getDocuments()).thenReturn(Collections.emptyList());
    }

    @Test
    public void searchAgeRange_success_overlapLogic() throws Exception
    {
        AgeRange wanted = new AgeRange(12, 14); // ages between 12 to 14 are "valid"

        // build the exact where-chain for this case
        when(cg.whereLessThanOrEqualTo("ageRange.minAge", 14)).thenReturn(q1);
        when(q1.whereGreaterThanOrEqualTo("ageRange.maxAge", 12)).thenReturn(q2);

        // stub - return cDoc1 for this query
        when(cgSnapOnlyC1.getDocuments()).thenReturn(Collections.singletonList(cDoc1));
        when(q2.get()).thenReturn(Tasks.forResult(cgSnapOnlyC1));

        List<Course> result = Tasks.await(ageRangeSearchStrategy.searchCourses(wanted));

        assertEquals(1, result.size());
        assertEquals("cg_c1", result.get(0).getId());
    }

    @Test
    public void searchAgeRange_success_noMatches() throws Exception
    {
        AgeRange wanted = new AgeRange(12, 14); // wanted age range

        // make the exact where filter chain return the empty snapshot for this case
        when(cg.whereLessThanOrEqualTo("ageRange.minAge", 14)).thenReturn(q1);
        when(q1.whereGreaterThanOrEqualTo("ageRange.maxAge", 12)).thenReturn(q2);
        when(q2.get()).thenReturn(Tasks.forResult(cgSnapEmpty)); // returns an empty list

        List<Course> result = Tasks.await(ageRangeSearchStrategy.searchCourses(wanted));
        assertTrue(result.isEmpty());
    }

    @Test
    public void searchAgeRange_failure()
    {
        // force to fail
        RuntimeException fail = new RuntimeException("fail");
        when(q2.get()).thenReturn(Tasks.forException(fail));

        Task<List<Course>> task = ageRangeSearchStrategy.searchCourses(new AgeRange(12, 14));

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

    // helper functions

    /**
     * Create a course with a given age range.
     * @param minAge minimum age for the course.
     * @param maxAge maximum age for the course.
     * @return a new course with that age range.
     */
    private static Course mkCourse(int minAge, int maxAge) {
        Course c = new Course();
        AgeRange ar = new AgeRange(minAge, maxAge);
        c.setAgeRange(ar);
        return c;
    }

    /**
     * Create a copy of a course.
     * @param src the source course that will be copied.
     * @return a new course with the same age range as the source.
     */
    private static Course copy(Course src) {
        Course c = new Course();
        if (src.getAgeRange() != null) {
            AgeRange ar = new AgeRange(src.getAgeRange().getMinAge(), src.getAgeRange().getMaxAge());
            c.setAgeRange(ar);
        }
        return c;
    }
}
