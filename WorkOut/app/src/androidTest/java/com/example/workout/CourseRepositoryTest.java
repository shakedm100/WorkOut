package com.example.workout;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.google.android.gms.tasks.Tasks.await;
import static com.google.android.gms.tasks.Tasks.forResult;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import Model.Address;
import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.City;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Enrollment;
import Model.Gender;
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
    @Mock
    DocumentSnapshot mockDocSnap;
    @Mock
    QuerySnapshot mockQuerySnap;
    @Mock
    Enrollment mockEnrollment;
    @Mock
    Client mockClient;
    @Mock
    DocumentReference mockEnrollDocRef;
    @Mock
    CollectionReference mockEnrollCollection;
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
                "Policy", new Address(new City("Rosh Haayin"), "Ofra Haza 4"));
        Schedule schedule = new Schedule(Day.Wednesday, Timestamp.now());
        sampleCourse = new Course("cid", CourseType.Group, "YogaClass", new ArrayList<>(),
                10, new AgeRange(20, 40), schedule,
                Category.Archery, "Desc", business.getId(), 60);

        when(mockDb.collection("enrollment")).thenReturn(mockEnrollCollection);
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
    public void deleteCourse_notExists_returnsFalse() throws Exception
    {
        CourseRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(false))
                .when(spyRepo)
                .checkIfCourseExist(business, sampleCourse);

        Boolean ok = Tasks.await(spyRepo.deleteCourse(sampleCourse, business));
        assertFalse(ok);
    }

    @Test
    public void getCoursesById_found() throws Exception
    {
        // stub the DocumentReference.get()
        DocumentReference courseRef = mock(DocumentReference.class);
        when(mockDb.collection("businesses"))
                .thenReturn(mockBusinessCollection);
        when(mockBusinessCollection.document(sampleCourse.getBusinessId()))
                .thenReturn(mockBusinessDoc);
        when(mockBusinessDoc.collection("courses"))
                .thenReturn(mockCourseCollection);
        when(mockCourseCollection.document(sampleCourse.getId()))
                .thenReturn(courseRef);

        when(courseRef.get()).thenReturn(forResult(mockDocSnap));
        when(mockDocSnap.exists()).thenReturn(true);
        when(mockDocSnap.toObject(Course.class)).thenReturn(sampleCourse);
        when(mockDocSnap.getId()).thenReturn(sampleCourse.getId());

        List<Course> result = Tasks.await(repo.getCoursesById(sampleCourse));
        assertEquals(1, result.size());
        assertEquals(sampleCourse.getId(), result.get(0).getId());
    }

    // getCoursesById – not exists
    @Test
    public void getCoursesById_notFound() throws Exception
    {
        DocumentReference courseRef = mock(DocumentReference.class);
        when(mockCourseCollection.document(sampleCourse.getId()))
                .thenReturn(courseRef);
        when(courseRef.get()).thenReturn(forResult(mockDocSnap));
        when(mockDocSnap.exists()).thenReturn(false);

        List<Course> result = Tasks.await(repo.getCoursesById(sampleCourse));
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllBusinessesCourses_success() throws Exception
    {
        // prepare two mock docs
        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        Course course1 = new Course("c1", CourseType.Group, "A", new ArrayList<>(),
                5, new AgeRange(1, 2),
                new Schedule(Day.Monday, Timestamp.now()),
                Category.Archery, "d", business.getId(), 30);
        when(doc1.toObject(Course.class)).thenReturn(course1);
        when(doc1.getId()).thenReturn("c1");

        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        Course course2 = new Course("c2", CourseType.Solo, "B", new ArrayList<>(),
                3, new AgeRange(10, 20),
                new Schedule(Day.Tuesday, Timestamp.now()),
                Category.Archery, "e", business.getId(), 45);
        when(doc2.toObject(Course.class)).thenReturn(course2);
        when(doc2.getId()).thenReturn("c2");

        // 1) stub the Task<QuerySnapshot> to return our mockQuerySnap
        when(mockCourseCollection.get())
                .thenReturn(Tasks.forResult(mockQuerySnap));

        // 2) stub only getDocuments(), not getResult()
        when(mockQuerySnap.getDocuments())
                .thenReturn(Arrays.asList(doc1, doc2));

        List<Course> courses = Tasks.await(repo.getAllBusinessesCourses(business));
        assertEquals(2, courses.size());
        assertTrue(business.getCourses().stream()
                .anyMatch(c -> c.getId().equals("c1")));
        assertTrue(business.getCourses().stream()
                .anyMatch(c -> c.getId().equals("c2")));
    }

    @Test
    public void signupClientToCourse_success() throws Exception
    {
        // course has empty participants so insertParticipant returns true
        when(mockEnrollCollection.add(anyMap()))
                .thenReturn(forResult(mockEnrollDocRef));

        Boolean ok = Tasks.await(
                repo.signupClientToCourse(mockClient, sampleCourse, Timestamp.now()));
        assertTrue(ok);
    }

    @Test(expected = IllegalArgumentException.class)
    public void signupClientToCourse_full_throws()
    {
        // force insertParticipant to fail
        Course full = spy(sampleCourse);
        // fill capacity
        for (int i = 0; i < full.getCapacity(); i++)
        {
            full.insertParticipant(mockClient);
        }
        // now add one more
        repo.signupClientToCourse(mockClient, full, Timestamp.now());
    }

    @Test
    public void cancelSignupClientToCourse_success() throws Exception
    {
        Phone phone = new Phone(PhonePrefix.PREFIX_053, "1234567");
        Address addr = new Address(new City("Tel Aviv"), "Dizengoff 100");
        Client client = new Client("uid", "bob", phone, "bob@example.com",
                "Bob", "Cohen", addr, Gender.Male);
        // prepare an enrollment with a course that contains the client
        Course course = new Course("c1", CourseType.Group, "A", new ArrayList<>(),
                5, new AgeRange(1, 2), new Schedule(Day.Monday, Timestamp.now()),
                Category.Archery, "d", business.getId(), 30);
        course.insertParticipant(client);
        Enrollment enrollment = new Enrollment("e1", client, course, Timestamp.now());

        when(mockEnrollCollection.document("e1"))
                .thenReturn(mockEnrollDocRef);
        when(mockEnrollDocRef.delete()).thenReturn(forResult(null));

        Boolean ok = Tasks.await(repo.cancelSignupClientToCourse(enrollment));
        assertTrue(ok);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cancelSignupClientToCourse_notSignedUp_throws()
    {
        Phone phone = new Phone(PhonePrefix.PREFIX_053, "1234567");
        Address addr = new Address(new City("Tel Aviv"), "Dizengoff 100");
        Client client = new Client("uid", "bob", phone, "bob@example.com",
                "Bob", "Cohen", addr, Gender.Male);
        // prepare an enrollment with a course that contains the client
        Course course = new Course("c1", CourseType.Group, "A", new ArrayList<>(),
                5, new AgeRange(1, 2), new Schedule(Day.Monday, Timestamp.now()),
                Category.Archery, "d", business.getId(), 30);
        // no insertParticipant called
        Enrollment enrollment = new Enrollment("e1", client, course, Timestamp.now());

        repo.cancelSignupClientToCourse(enrollment);
    }

    @Test
    public void getEnrollmentByDate_found() throws Exception
    {
        when(mockEnrollCollection
                .whereGreaterThanOrEqualTo(eq("time"), any()))
                .thenReturn(mockEnrollCollection);
        when(mockEnrollCollection
                .whereLessThanOrEqualTo(eq("time"), any()))
                .thenReturn(mockEnrollCollection);
        when(mockEnrollCollection
                .whereEqualTo(eq("client"), any()))
                .thenReturn(mockEnrollCollection);
        when(mockEnrollCollection
                .whereEqualTo(eq("course"), any()))
                .thenReturn(mockEnrollCollection);
        when(mockEnrollCollection.limit(1)).thenReturn(mockEnrollCollection);

        // build a fake QuerySnapshot with one doc
        QuerySnapshot qs = mock(QuerySnapshot.class);
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        Enrollment expected = mock(Enrollment.class);
        when(doc.toObject(Enrollment.class)).thenReturn(expected);
        when(qs.getDocuments()).thenReturn(Collections.singletonList(doc));
        when(mockEnrollCollection.get()).thenReturn(forResult(qs));

        Enrollment result = Tasks.await(
                repo.getEnrollmentByDate(mockClient, sampleCourse,
                        Timestamp.now(), Timestamp.now()));
        assertSame(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void getEnrollmentByDate_notFound_throws()
    {
        when(mockEnrollCollection.get())
                .thenReturn(forResult(mock(QuerySnapshot.class)));
        when(mockEnrollCollection.limit(1)).thenReturn(mockEnrollCollection);

        repo.getEnrollmentByDate(mockClient, sampleCourse,
                Timestamp.now(), Timestamp.now());
    }

    @Test
    public void getAllEnrollmentsByClient_success() throws Exception
    {
        when(mockDb.collection("enrollment"))
                .thenReturn(mockEnrollCollection);
        when(mockEnrollCollection
                .whereEqualTo(eq("client.id"), any()))
                .thenReturn(mockEnrollCollection);

        // prepare two docs
        DocumentSnapshot documentSnapshot1 = mock(DocumentSnapshot.class);
        DocumentSnapshot documentSnapshot2 = mock(DocumentSnapshot.class);
        Enrollment enrollment = mock(Enrollment.class), e2 = mock(Enrollment.class);
        when(documentSnapshot1.toObject(Enrollment.class)).thenReturn(enrollment);
        when(documentSnapshot2.toObject(Enrollment.class)).thenReturn(e2);

        // mock the QuerySnapshot
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(mockEnrollCollection.get())
                .thenReturn(Tasks.forResult(querySnapshot));

        // stub getDocuments() *and* iterator()
        when(querySnapshot.getDocuments())
                .thenReturn(Arrays.asList(documentSnapshot1, documentSnapshot2));
        doReturn(Arrays.asList(documentSnapshot1, documentSnapshot2).iterator())
                .when(querySnapshot).iterator();

        List<Enrollment> list = Tasks.await(
                repo.getAllEnrollmentsByClient(mockClient));

        assertEquals(2, list.size());
        assertTrue(list.contains(enrollment));
        assertTrue(list.contains(e2));
    }

}
