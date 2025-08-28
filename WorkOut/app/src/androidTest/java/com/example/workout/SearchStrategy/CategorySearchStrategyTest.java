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

public class CategorySearchStrategyTest {

    // ===== Firestore: collectionGroup chain =====
    @Mock FirebaseFirestore db;

    @Mock Query cg;        // db.collectionGroup("courses")
    @Mock Query qCat;      // .whereEqualTo("category", wanted)
    @Mock QuerySnapshot cgSnapBoth;   // snapshot with multiple docs
    @Mock QuerySnapshot cgSnapOnlyWanted;
    @Mock QuerySnapshot cgSnapEmpty;

    // Returned docs
    @Mock DocumentSnapshot docWanted1;
    @Mock DocumentSnapshot docWanted2;
    @Mock DocumentSnapshot docOther1;

    // If production derives businessId from the doc path:
    @Mock DocumentReference w1Ref;
    @Mock DocumentReference w2Ref;
    @Mock DocumentReference o1Ref;
    @Mock CollectionReference w1CoursesColRef;
    @Mock CollectionReference w2CoursesColRef;
    @Mock CollectionReference o1CoursesColRef;
    @Mock DocumentReference w1BizRef;
    @Mock DocumentReference w2BizRef;
    @Mock DocumentReference o1BizRef;
    @Mock CourseRepository mockCourseRepo;

    private SearchStrategyInterface<Category> categorySearchStrategy;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        mockCourseRepo = mock(CourseRepository.class);

        // Class under test
        categorySearchStrategy = new SearchCategoryStrategy(mockCourseRepo, db);

        // Start of collectionGroup
        when(db.collectionGroup("courses")).thenReturn(cg);

        // Category filter (if your code uses category.name(), adjust here)
        when(cg.whereEqualTo(eq("category"), any())).thenReturn(qCat);

        // Build some Course objects
        // Two in BASKETBALL (wanted) and one in SOCCER (not wanted)
        Course wanted1 = mkCourse(Category.Basketball);
        Course wanted2 = mkCourse(Category.Basketball);
        Course other1  = mkCourse(Category.Soccer);

        // Map toObject + IDs
        when(docWanted1.toObject(Course.class)).thenReturn(copy(wanted1));
        when(docWanted2.toObject(Course.class)).thenReturn(copy(wanted2));
        when(docOther1.toObject(Course.class)).thenReturn(copy(other1));

        when(docWanted1.getId()).thenReturn("cat_w1");
        when(docWanted2.getId()).thenReturn("cat_w2");
        when(docOther1.getId()).thenReturn("cat_o1");

        // Snapshots with different compositions
        when(cgSnapOnlyWanted.getDocuments()).thenReturn(Arrays.asList(docWanted1, docWanted2));
        when(cgSnapBoth.getDocuments()).thenReturn(Arrays.asList(docWanted1, docOther1)); // mixed
        when(cgSnapEmpty.getDocuments()).thenReturn(Collections.emptyList());

        // If businessId is derived via path parents:
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
        Category wanted = Category.Basketball;

        // Exact whereEqualTo for determinism
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        // Return only docs that match the wanted category
        when(qCat.get()).thenReturn(Tasks.forResult(cgSnapOnlyWanted));

        List<Course> result = Tasks.await(categorySearchStrategy.searchCourses(wanted));

        assertEquals(2, result.size());
        Set<String> ids = new HashSet<>();
        for (Course c : result) ids.add(c.getId());
        assertTrue(ids.contains("cat_w1"));
        assertTrue(ids.contains("cat_w2"));

        for (Course c : result) {
            assertNotNull(c.getBusinessId());
            assertTrue(c.getBusinessId().equals("biz_w1") || c.getBusinessId().equals("biz_w2"));
            assertEquals(Category.Basketball, c.getCategory());
        }

        // Verify query built correctly
        verify(cg).whereEqualTo("category", wanted);
        verify(qCat).get();
    }

    @Test
    public void searchCategory_success_noMatches() throws Exception
    {
        Category wanted = Category.Tennis; // choose any not present in stubs

        // exact whereEqualTo with this wanted
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        // Return empty results for this category
        when(qCat.get()).thenReturn(Tasks.forResult(cgSnapEmpty));

        List<Course> result = Tasks.await(categorySearchStrategy.searchCourses(wanted));
        assertTrue(result.isEmpty());
    }

    @Test
    public void searchCategory_failure()
    {
        Category wanted = Category.Basketball;
        when(cg.whereEqualTo("category", wanted)).thenReturn(qCat);

        RuntimeException fail = new RuntimeException("category get failed");
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

    // ===== helpers =====

    private static Course mkCourse(Category category) {
        Course c = new Course();
        c.setCategory(category);
        return c;
    }

    private static Course copy(Course src) {
        Course c = new Course();
        c.setCategory(src.getCategory());
        return c;
    }
}
