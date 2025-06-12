package com.example.workout;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.google.android.gms.tasks.Tasks.forResult;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.runner.RunWith;

import java.util.ArrayList;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.CourseRepository;
import Model.Schedule;

@RunWith(AndroidJUnit4.class)
public class CourseRepositoryTest
{
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockBusinessCollection;
    @Mock
    DocumentReference mockBusinessDoc;
    @Mock
    CollectionReference mockCourseCollection;
    @Mock
    Task<DocumentReference> mockAddTask;
    @Mock
    DocumentReference mockCourseDocRef;
    private CourseRepository repo;
    private Business business;
    private Course sampleCourse;
    private Phone testPhone;

    @Before
    public void setUp()
    {
        testPhone = new Phone(PhonePrefix.PREFIX_052, "5265777");

        MockitoAnnotations.openMocks(this);
        // build a repo with overridden db
        repo = new CourseRepository(mockDb);

        // common mocks
        when(mockDb.collection("businesses")).thenReturn(mockBusinessCollection);
        when(mockBusinessCollection.document(anyString())).thenReturn(mockBusinessDoc);
        when(mockBusinessDoc.collection("courses")).thenReturn(mockCourseCollection);

        // sample business
        business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        Schedule schedule = new Schedule(Day.Wednesday, Timestamp.now());
        sampleCourse = new Course("cid", CourseType.Group, "YogaClass", new ArrayList<>(),
                10, new AgeRange(20, 40), schedule,
                Category.Archery, "Desc", business.getId(), 60);
    }

    @Test
    public void insertCourse_success() throws Exception
    {
        // stub add(...) to return a DocumentReference
        when(mockCourseCollection.add(anyMap()))
                .thenReturn(Tasks.forResult(mockCourseDocRef));
        when(mockAddTask.isSuccessful()).thenReturn(true);
        when(mockAddTask.getResult()).thenReturn(mockCourseDocRef);
        when(mockCourseDocRef.getId()).thenReturn("cid");

        CourseRepository spyRepo = spy(repo);

        doReturn(Tasks.forResult(false))
                .when(spyRepo)
                .checkIfCourseExist(business, sampleCourse);

        Course result = Tasks.await(spyRepo.insertCourse(
                business, "YogaClass", sampleCourse.getSchedule(),
                sampleCourse.getCapacity(), sampleCourse.getType(),
                sampleCourse.getAgeRange(), sampleCourse.getCategory(),
                sampleCourse.getDescription(), sampleCourse.getDuration()));

        assertNotNull(result);
        assertEquals("cid", result.getId());
        // verify business had the course added
        assertTrue(business.getCourses().stream()
                .anyMatch(c -> c.getId().equals("cid")));
    }

    @Test
    public void updateCourse_success() throws Exception
    {
        Business spyBusiness = spy(business);

        // spy repo to stub existence and helper
        CourseRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(true))
                .when(spyRepo)
                .checkIfCourseExist(spyBusiness, sampleCourse);

        // stub updateHelper to succeed
        doReturn(Tasks.forResult(true))
                .when(spyRepo)
                .updateHelper(sampleCourse, spyBusiness);

        // stub business.updateCourse() returns true
        when(spyBusiness.updateCourse(sampleCourse)).thenReturn(true);

        Boolean ok = Tasks.await(spyRepo.updateCourse(sampleCourse, spyBusiness));
        assertTrue(ok);
    }

    @Test
    public void updateCourse_notExists_returnsFalse() throws Exception
    {
        CourseRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(false))
                .when(spyRepo)
                .checkIfCourseExist(business, sampleCourse);
        Boolean ok = Tasks.await(spyRepo.updateCourse(sampleCourse, business));
        assertFalse(ok);
    }

    @Test
    public void deleteCourse_success() throws Exception
    {
        CourseRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(true))
                .when(spyRepo)
                .checkIfCourseExist(business, sampleCourse);
        // stub deleteHelper, which returns Task<Boolean>
        doReturn(forResult(true))
                .when(spyRepo).deleteHelper(sampleCourse, business);

        Boolean ok = Tasks.await(spyRepo.deleteCourse(sampleCourse, business));
        assertTrue(ok);
    }

    @Test
    public void deleteCourse_notExists_returnsFalse() throws Exception
    {
        CourseRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(false))
                .when(spyRepo)
                .checkIfCourseExist(business, sampleCourse);

        Boolean ok = Tasks.await(spyRepo.deleteCourse(sampleCourse, business));
        assertFalse(ok);
    }
}
