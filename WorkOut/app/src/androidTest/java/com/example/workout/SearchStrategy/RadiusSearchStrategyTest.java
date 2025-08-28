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

import Model.Business;
import Model.Course;
import Model.Day;
import Model.Location;
import Model.Repository.CourseRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

public class RadiusSearchStrategyTest
{
    @Mock FirebaseFirestore db;
    @Mock CollectionReference businessesCol;  // for db.collection("businesses")
    @Mock Query qLatMin;     // for .whereGreaterThanOrEqualTo("location.latitude", minLat)
    @Mock Query qLatMax;     // for .whereLessThanOrEqualTo("location.latitude", maxLat)
    @Mock Query qLonMin;     // for .whereGreaterThanOrEqualTo("location.longitude", minLon)
    @Mock Query qLonMax;     // for .whereLessThanOrEqualTo("location.longitude", maxLon)
    @Mock QuerySnapshot bizSnap; // result of qLonMax.get()

    // Business docs
    @Mock DocumentSnapshot inBizDoc;   // in range
    @Mock DocumentSnapshot outBizDoc;  // not in range

    // for inside business: document + courses subcollection
    @Mock DocumentReference inBizRef;
    @Mock CollectionReference inCoursesCol;
    @Mock QuerySnapshot inCoursesSnap;

    // Course docs under inside business
    @Mock DocumentSnapshot c1Doc;
    @Mock DocumentSnapshot c2Doc;
    @Mock CourseRepository mockCourseRepo; // fake repository

    // Strategy under test
    private SearchStrategyInterface<Location> radiusSearchStrategy;

    // test on the center of Tel Aviv
    private static final double CENTER_LAT = 32.0853;
    private static final double CENTER_LON = 34.7818;
    private static final Location CENTER = new Location(CENTER_LAT, CENTER_LON); // center of tel aviv

    // two businesses: one inside ~5 km and one outside ~50+ km (center of Jerusalem)
    private static final Location IN_LOC  = new Location(32.12, 34.80); // ~5 km from center of tel aviv
    private static final Location OUT_LOC = new Location(31.7683, 35.2137); // ~54 km from center of tel aviv
    private static final int RADIUS_KM = 10; // the given radius to the constructor

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCourseRepo = mock(CourseRepository.class);
        radiusSearchStrategy = new SearchRadiusStrategy(mockCourseRepo, db, RADIUS_KM); // 10km from center

        // mock & stub everything needed

        // first collection of query
        when(db.collection("businesses")).thenReturn(businessesCol);

        // stub chain the four where-clauses in the filter
        when(businessesCol.whereGreaterThanOrEqualTo(eq("location.latitude"), anyDouble()))
                .thenReturn(qLatMin);
        when(qLatMin.whereLessThanOrEqualTo(eq("location.latitude"), anyDouble()))
                .thenReturn(qLatMax);
        when(qLatMax.whereGreaterThanOrEqualTo(eq("location.longitude"), anyDouble()))
                .thenReturn(qLonMin);
        when(qLonMin.whereLessThanOrEqualTo(eq("location.longitude"), anyDouble()))
                .thenReturn(qLonMax);

        // businesses query returns two docs (inside + outside).
        List<DocumentSnapshot> bizDocs = Arrays.asList(inBizDoc, outBizDoc);
        when(bizSnap.getDocuments()).thenReturn(bizDocs);
        when(qLonMax.get()).thenReturn(Tasks.forResult(bizSnap));

        // map business docs to objects/IDs
        when(inBizDoc.getId()).thenReturn("b_in");
        when(outBizDoc.getId()).thenReturn("b_out");

        Business inB = mkBusiness(IN_LOC);
        Business outB = mkBusiness(OUT_LOC);
        when(inBizDoc.toObject(Business.class)).thenReturn(inB);
        when(outBizDoc.toObject(Business.class)).thenReturn(outB);

        // for inside business: document("b_in").collection("courses").get()...
        when(businessesCol.document("b_in")).thenReturn(inBizRef);
        when(inBizRef.collection("courses")).thenReturn(inCoursesCol);

        // create two fake courses under b_in & stub
        Course c1 = mkCourse("c1");
        Course c2 = mkCourse("c2");
        when(c1Doc.toObject(Course.class)).thenReturn(copyCourse(c1));
        when(c2Doc.toObject(Course.class)).thenReturn(copyCourse(c2));
        when(c1Doc.getId()).thenReturn("c1");
        when(c2Doc.getId()).thenReturn("c2");

        List<DocumentSnapshot> inCourseDocs = Arrays.asList(c1Doc, c2Doc);
        when(inCoursesSnap.getDocuments()).thenReturn(inCourseDocs);
        when(inCoursesCol.get()).thenReturn(Tasks.forResult(inCoursesSnap));
    }

    @Test
    public void searchRadius_success() throws Exception
    {
        List<Course> result = Tasks.await(radiusSearchStrategy.searchCourses(CENTER));

        // only inside business courses should appear
        assertEquals(2, result.size());
        Set<String> ids = new HashSet<>();

        // get all the ids of the courses within 10km from center of tel aviv
        for (Course c : result)
            ids.add(c.getId()); // insert the id

        // we expect to get c1 and c2 as ids
        assertTrue(ids.contains("c1"));
        assertTrue(ids.contains("c2"));

        ids.clear(); // init

        // get all the ids of the businesses within 10km from center of tel aviv
        for (Course c : result)
            ids.add((c.getBusinessId()));

        // we expect to get only b_in as an id
        assertEquals(1, ids.size());
        assertTrue(ids.contains("b_in"));
        assertFalse(ids.contains("b_out"));
    }

    @Test
    public void searchRadius_success_empty() throws Exception
    {
        // stub and get an empty list
        QuerySnapshot emptyBizSnap = mock(QuerySnapshot.class);
        when(emptyBizSnap.getDocuments()).thenReturn(Collections.emptyList());
        when(qLonMax.get()).thenReturn(Tasks.forResult(emptyBizSnap));

        List<Course> result = Tasks.await(radiusSearchStrategy.searchCourses(CENTER)); // should get an empty list

        assertTrue(result.isEmpty());
    }

    @Test
    public void searchRadius_failure()
    {
        // force the inside business's courses fetch fail
        RuntimeException fail = new RuntimeException("failed");
        when(inCoursesCol.get()).thenReturn(Tasks.forException(fail));

        Task<List<Course>> task = radiusSearchStrategy.searchCourses(CENTER);

        try {
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
     * Create a business with a given location.
     * @param loc a given location.
     * @return a new business at that location.
     */
    private static Business mkBusiness(Location loc)
    {
        Business b = new Business();
        b.setLocation(loc);
        return b;
    }

    /**
     * Create a course with a given ID.
     * @param id a given id to a course.
     * @return a new course with that id.
     */
    private static Course mkCourse(String id)
    {
        Course c = new Course();
        c.setId(id);
        return c;
    }

    /**
     * Create a copy of a course.
     * @param src the source course that will be copied.
     * @return a new course with the same id as the source.
     */
    private static Course copyCourse(Course src)
    {
        Course c = new Course();
        if (src.getId() != null && !src.getId().isEmpty())
            c.setId(src.getId());

        return c;
    }
}
