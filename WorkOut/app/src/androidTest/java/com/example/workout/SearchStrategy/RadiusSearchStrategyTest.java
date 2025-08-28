package com.example.workout;

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

public class RadiusSearchStrategyTest {

    // ===== Firestore root and queries =====
    @Mock FirebaseFirestore db;

    @Mock CollectionReference businessesCol;  // db.collection("businesses")
    @Mock Query qLatMin;     // .whereGreaterThanOrEqualTo("location.latitude", minLat)
    @Mock Query qLatMax;     // .whereLessThanOrEqualTo("location.latitude", maxLat)
    @Mock Query qLonMin;     // .whereGreaterThanOrEqualTo("location.longitude", minLon)
    @Mock Query qLonMax;     // .whereLessThanOrEqualTo("location.longitude", maxLon)
    @Mock QuerySnapshot bizSnap; // result of qLonMax.get()

    // Business docs
    @Mock DocumentSnapshot inBizDoc;   // inside the circle
    @Mock DocumentSnapshot outBizDoc;  // outside the circle

    // Per-business: document + courses subcollection (for inside business)
    @Mock DocumentReference inBizRef;
    @Mock CollectionReference inCoursesCol;
    @Mock QuerySnapshot inCoursesSnap;

    // Course docs under inside business
    @Mock DocumentSnapshot c1Doc;
    @Mock DocumentSnapshot c2Doc;
    @Mock CourseRepository mockCourseRepo;

    // Strategy under test
    private SearchStrategyInterface<Location> radiusSearchStrategy;

    // ===== Test geometry =====
    // Center ~ Tel Aviv
    private static final double CENTER_LAT = 32.0853;
    private static final double CENTER_LON = 34.7818;
    private static final Location CENTER = new Location(CENTER_LAT, CENTER_LON);

    // One business inside ~5 km; one outside ~50+ km (Jerusalem)
    private static final Location IN_LOC  = new Location(32.12, 34.80);          // ~5 km
    private static final Location OUT_LOC = new Location(31.7683, 35.2137);      // ~54 km
    private static final int RADIUS_KM = 10;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCourseRepo = mock(CourseRepository.class);
        radiusSearchStrategy = new SearchRadiusStrategy(mockCourseRepo, db, RADIUS_KM);

        // db.collection("businesses")
        when(db.collection("businesses")).thenReturn(businessesCol);

        // Chain the four where-clauses (we'll allow any double bounds in the stubs)
        when(businessesCol.whereGreaterThanOrEqualTo(eq("location.latitude"), anyDouble()))
                .thenReturn(qLatMin);
        when(qLatMin.whereLessThanOrEqualTo(eq("location.latitude"), anyDouble()))
                .thenReturn(qLatMax);
        when(qLatMax.whereGreaterThanOrEqualTo(eq("location.longitude"), anyDouble()))
                .thenReturn(qLonMin);
        when(qLonMin.whereLessThanOrEqualTo(eq("location.longitude"), anyDouble()))
                .thenReturn(qLonMax);

        // Default: businesses query returns two docs (inside + outside).
        // IMPORTANT: your code iterates "for (DocumentSnapshot ds : bizSnap)", so we must stub iterator().
        List<DocumentSnapshot> bizDocs = Arrays.asList(inBizDoc, outBizDoc);
        when(bizSnap.getDocuments()).thenReturn(bizDocs);
        //when(bizSnap.getDocuments()).thenReturn(bizDocs);
        when(qLonMax.get()).thenReturn(Tasks.forResult(bizSnap));

        // Map business docs to objects/IDs
        when(inBizDoc.getId()).thenReturn("b_in");
        when(outBizDoc.getId()).thenReturn("b_out");

        Business inB = mkBusiness(IN_LOC);
        Business outB = mkBusiness(OUT_LOC);
        when(inBizDoc.toObject(Business.class)).thenReturn(inB);
        when(outBizDoc.toObject(Business.class)).thenReturn(outB);

        // For inside business: document("b_in").collection("courses").get()...
        when(businessesCol.document("b_in")).thenReturn(inBizRef);
        when(inBizRef.collection("courses")).thenReturn(inCoursesCol);

        // Two courses under b_in
        Course c1 = mkCourse("c1");
        Course c2 = mkCourse("c2");
        when(c1Doc.toObject(Course.class)).thenReturn(copyCourse(c1));
        when(c2Doc.toObject(Course.class)).thenReturn(copyCourse(c2));
        when(c1Doc.getId()).thenReturn("c1");
        when(c2Doc.getId()).thenReturn("c2");

        List<DocumentSnapshot> inCourseDocs = Arrays.asList(c1Doc, c2Doc);
        when(inCoursesSnap.getDocuments()).thenReturn(inCourseDocs);
        when(inCoursesCol.get()).thenReturn(Tasks.forResult(inCoursesSnap));

        // NOTE: We do NOT stub b_out courses path on purpose;
        // production should NOT call it because it's outside the circle.
    }

    @Test
    public void searchRadius_success_returnsCoursesFromInCircleBusinessesOnly() throws Exception {
        List<Course> result = Tasks.await(radiusSearchStrategy.searchCourses(CENTER));

        // Only inside business courses should appear
        assertEquals(2, result.size());
        Set<String> ids = new HashSet<>();
        for (Course c : result) ids.add(c.getId());
        assertTrue(ids.contains("c1"));
        assertTrue(ids.contains("c2"));

        // Parent business id is annotated
        for (Course c : result) {
            assertEquals("b_in", c.getBusinessId());
        }

        // Verify that we did NOT fetch courses for the outside business
        verify(businessesCol, never()).document("b_out");
    }

    @Test
    public void searchRadius_noBusinessesInBox_returnsEmpty() throws Exception
    {
        // Make the bounding-box query result empty
        QuerySnapshot emptyBizSnap = mock(QuerySnapshot.class);
        when(emptyBizSnap.getDocuments()).thenReturn(Collections.emptyList());
        when(qLonMax.get()).thenReturn(Tasks.forResult(emptyBizSnap));

        List<Course> result = Tasks.await(radiusSearchStrategy.searchCourses(CENTER));

        assertTrue(result.isEmpty());
        // no sub-collection fetches should happen
        verify(inBizRef, never()).collection(anyString());
    }

    @Test
    public void searchRadius_failure_coursesFetchPropagates()
    {
        // Make the inside business's courses fetch fail
        RuntimeException boom = new RuntimeException("courses get failed");
        when(inCoursesCol.get()).thenReturn(Tasks.forException(boom));

        Task<List<Course>> task = radiusSearchStrategy.searchCourses(CENTER);

        try {
            Tasks.await(task);
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertSame(boom, e.getCause());
        } catch (Exception e) {
            fail("Expected ExecutionException, got: " + e);
        }
    }

    // ===== helpers =====

    private static Business mkBusiness(Location loc)
    {
        Business b = new Business();
        b.setLocation(loc);
        return b;
    }

    private static Course mkCourse(String id)
    {
        Course c = new Course();
        c.setId(id);
        return c;
    }

    private static Course copyCourse(Course src)
    {
        Course c = new Course();
        if (src.getId() != null && !src.getId().isEmpty())
            c.setId(src.getId());

        return c;
    }
}
