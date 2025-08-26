package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;
import static com.google.android.gms.tasks.Tasks.forResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Model.Address;
import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.City;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;
import Model.Repository.CourseRepository;
import Model.Repository.GeneralRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchDayOfWeekStrategy;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

@RunWith(JUnit4.class)
public class SearchStrategyTestMock
{
    SearchStrategyInterface<AgeRange> ageRangeSearchStrategy;
    SearchStrategyInterface<Category> categorySearchStrategy;
    SearchStrategyInterface<CourseType> courseTypeSearchStrategy;
    SearchStrategyInterface<Day> dayOfWeekSearchStrategy;
    SearchStrategyInterface<Location> radiusSearchStrategy;
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockCollection, mockCollection1, mockCollection2;
    @Mock
    DocumentReference mockDocument, mockDocument1, mockDocument2;
    @Mock
    CourseRepository mockCourseRepo;;
    @Mock
    Business business1, business2;
    @Mock
    Query mockQuery;
    @Mock
    Course validCourse, invalidCourse;
    @Mock
    QuerySnapshot mockQuerySnapshot;
    @Mock
    DocumentSnapshot mockDoc1, mockDoc2, mockSnapshot1, mockSnapshot2;

    /*
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // ==== Core mocks ====
        mockDb = mock(FirebaseFirestore.class);
        mockCourseRepo = mock(CourseRepository.class);

        // Strategies under test
        ageRangeSearchStrategy = new SearchAgeStrategy(mockCourseRepo, mockDb);
        categorySearchStrategy = new SearchCategoryStrategy(mockCourseRepo, mockDb);
        courseTypeSearchStrategy = new SearchCourseTypeStrategy(mockDb);
        dayOfWeekSearchStrategy = new SearchDayOfWeekStrategy(mockDb);
        radiusSearchStrategy = new SearchRadiusStrategy(mockCourseRepo, mockDb, 10);

        // ==== Test data ====
        validCourse = new Course(
                "valid",              // id (will be overwritten from snapshot id if you set it)
                CourseType.Solo,
                "valid_course",
                10,
                new AgeRange(12, 14),
                30,
                new Schedule(Day.Sunday, new Timestamp(1000, 1000)),
                Category.Basketball,
                "Basketball course to children between 12 to 14 years old."
        );

        invalidCourse = new Course(
                "invalid",
                CourseType.Duo,
                "invalid_course",
                15,
                new AgeRange(30, 50),
                30,
                new Schedule(Day.Monday, new Timestamp(1000, 1000)),
                Category.Yoga,
                "Yoga class to adults."
        );

        Business business1 = new Business(
                "biz1", "testEverything1",
                new Phone(PhonePrefix.PREFIX_052, "5265777"),
                "testEverything@mail", "Busi1",
                new Location(12345, 2145435),
                "Policy",
                new Address(new City("Tel Aviv"), "Even Gvirol")
        );

        Business business2 = new Business(
                "biz2", "testEverything2",
                new Phone(PhonePrefix.PREFIX_050, "1234567"),
                "testEverything2@mail", "Busi2",
                new Location(12345, 2145435),
                "Policy",
                new Address(new City("Tel Aviv"), "Even Gvirol")
        );

        // ==== Firestore: collectionGroup("courses") path ====
        Query mockCoursesQuery = mock(Query.class);
        QuerySnapshot mockCoursesQuerySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockCourseDocSnap1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockCourseDocSnap2 = mock(DocumentSnapshot.class);

        // db.collectionGroup("courses") → query
        when(mockDb.collectionGroup(eq("courses"))).thenReturn(mockCoursesQuery);
        // chain the filters
        when(mockCoursesQuery.whereLessThanOrEqualTo(eq("ageRange.minAge"), anyInt())).thenReturn(mockCoursesQuery);
        when(mockCoursesQuery.whereGreaterThanOrEqualTo(eq("ageRange.maxAge"), anyInt())).thenReturn(mockCoursesQuery);
        // get() returns snapshot
        when(mockCoursesQuery.get()).thenReturn(Tasks.forResult(mockCoursesQuerySnapshot));
        // snapshot has 2 course docs
        when(mockCoursesQuerySnapshot.getDocuments()).thenReturn(Arrays.asList(mockCourseDocSnap1, mockCourseDocSnap2));

        // Each course doc maps to a Course and has an id
        when(mockCourseDocSnap1.toObject(eq(Course.class))).thenReturn(validCourse);
        when(mockCourseDocSnap2.toObject(eq(Course.class))).thenReturn(invalidCourse);
        when(mockCourseDocSnap1.getId()).thenReturn("course1");
        when(mockCourseDocSnap2.getId()).thenReturn("course2");

        // Course snapshot → DocumentReference → "courses" collection → parent business doc ref
        DocumentReference mockCourseRef1 = mock(DocumentReference.class);
        DocumentReference mockCourseRef2 = mock(DocumentReference.class);
        when(mockCourseDocSnap1.getReference()).thenReturn(mockCourseRef1);
        when(mockCourseDocSnap2.getReference()).thenReturn(mockCourseRef2);

        CollectionReference mockCoursesColl1 = mock(CollectionReference.class);
        CollectionReference mockCoursesColl2 = mock(CollectionReference.class);
        when(mockCourseRef1.getParent()).thenReturn(mockCoursesColl1);
        when(mockCourseRef2.getParent()).thenReturn(mockCoursesColl2);

        DocumentReference mockBizRef1 = mock(DocumentReference.class);
        DocumentReference mockBizRef2 = mock(DocumentReference.class);
        when(mockCoursesColl1.getParent()).thenReturn(mockBizRef1);
        when(mockCoursesColl2.getParent()).thenReturn(mockBizRef2);

        // Business refs fetch → business doc snapshots
        DocumentSnapshot mockBizDocSnap1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockBizDocSnap2 = mock(DocumentSnapshot.class);
        when(mockBizRef1.get()).thenReturn(Tasks.forResult(mockBizDocSnap1));
        when(mockBizRef2.get()).thenReturn(Tasks.forResult(mockBizDocSnap2));

        // Business snapshots map to Business + ids
        when(mockBizDocSnap1.toObject(eq(Business.class))).thenReturn(business1);
        when(mockBizDocSnap2.toObject(eq(Business.class))).thenReturn(business2);
        when(mockBizDocSnap1.getId()).thenReturn("biz1");
        when(mockBizDocSnap2.getId()).thenReturn("biz2");

        // ==== Firestore: collection("businesses") path (if any strategy uses it) ====
        CollectionReference mockBizCollection = mock(CollectionReference.class);
        QuerySnapshot mockBizListSnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockBizListDoc1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockBizListDoc2 = mock(DocumentSnapshot.class);

        when(mockDb.collection(eq("businesses"))).thenReturn(mockBizCollection);
        when(mockBizCollection.get()).thenReturn(Tasks.forResult(mockBizListSnapshot));
        when(mockBizListSnapshot.getDocuments()).thenReturn(Arrays.asList(mockBizListDoc1, mockBizListDoc2));

        // Map those business docs too (use the same Business objects for simplicity)
        when(mockBizListDoc1.toObject(eq(Business.class))).thenReturn(business1);
        when(mockBizListDoc2.toObject(eq(Business.class))).thenReturn(business2);
        when(mockBizListDoc1.getId()).thenReturn("biz1");
        when(mockBizListDoc2.getId()).thenReturn("biz2");

        // ==== Repository: return a REAL ArrayList (avoid ClassCastException) ====
        when(mockCourseRepo.getAllBusinessesCourses(any(Business.class)))
                .thenReturn(Tasks.forResult(new ArrayList<>(Arrays.asList(validCourse, invalidCourse))));
    }

     */

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        mockCourseRepo = mock(CourseRepository.class);
        ageRangeSearchStrategy = new SearchAgeStrategy(mockCourseRepo, mockDb);
        categorySearchStrategy = new SearchCategoryStrategy(mockCourseRepo, mockDb);
        courseTypeSearchStrategy = new SearchCourseTypeStrategy(mockDb);
        dayOfWeekSearchStrategy = new SearchDayOfWeekStrategy(mockDb);
        radiusSearchStrategy = new SearchRadiusStrategy(mockCourseRepo, mockDb, 10);

        // Reusable fake data
        validCourse = new Course(
                "valid",
                CourseType.Solo,
                "valid_course",
                10,
                new AgeRange(12,14),
                30,
                new Schedule(Day.Sunday, new Timestamp(1000, 1000)),
                Category.Basketball,
                "Basketball course to children between 12 to 14 years old.");

        invalidCourse = new Course(
                "invalid",
                CourseType.Duo,
                "invalid_course",
                15,
                new AgeRange(30, 50),
                30,
                new Schedule(Day.Monday, new Timestamp(1000, 1000)),
                Category.Yoga,
                "Yoga class to adults.");

        List<Course> allCourses = Arrays.asList(validCourse, invalidCourse);
        when(mockCourseRepo.getAllBusinessesCourses(any(Business.class)))
                .thenReturn(Tasks.forResult(allCourses));

        mockQuerySnapshot = mock(QuerySnapshot.class);
        mockDoc1 = mock(DocumentSnapshot.class);
        mockDoc2 = mock(DocumentSnapshot.class);

        when(mockDoc1.toObject(Course.class)).thenReturn(validCourse);
        when(mockDoc2.toObject(Course.class)).thenReturn(invalidCourse);

        when(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot));

        // Chain: course document -> courses collection -> business document
        when(mockDoc1.getReference()).thenReturn(mockDocument);
        when(mockDoc2.getReference()).thenReturn(mockDocument);

        when(mockDocument.getParent()).thenReturn(mockCollection);
        //when(mockCollection.getParent()).thenReturn(mockDocument);

        // And give it some fake ID
        when(mockDocument.getId()).thenReturn("fakeBusinessId");

        // Firestore mocks
        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDb.collectionGroup(anyString())).thenReturn(mockQuery);

        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), anyInt())).thenReturn(mockQuery);
        when(mockQuery.whereLessThanOrEqualTo(anyString(), anyInt())).thenReturn(mockQuery);

        DocumentSnapshot mockBizDoc = mock(DocumentSnapshot.class);
        when(mockDocument.get()).thenReturn(Tasks.forResult(mockBizDoc));

        mockSnapshot1 = mock(DocumentSnapshot.class);
        mockSnapshot2 = mock(DocumentSnapshot.class);

        business1 = new Business(
                "id1",
                "testEverything1",
                new Phone(PhonePrefix.PREFIX_052, "5265777"),
                "testEverything@mail", "Busi1",
                new Location(12345, 2145435),
                "Policy",
                new Address(new City("Tel Aviv"), "Even Gvirol"));

        business2 = new Business(
                "id2",
                "testEverything2",
                new Phone(PhonePrefix.PREFIX_050, "1234567"),
                "testEverything2@mail", "Busi2",
                new Location(12345, 2145435),
                "Policy",
                new Address(new City("Tel Aviv"), "Even Gvirol"));

        when(mockSnapshot1.toObject(Business.class)).thenReturn(business1);
        when(mockSnapshot2.toObject(Business.class)).thenReturn(business2);


        when(mockSnapshot1.getId()).thenReturn("id1");
        when(mockSnapshot2.getId()).thenReturn("id2");

        when(mockDoc1.getId()).thenReturn("course1");
        when(mockDoc2.getId()).thenReturn("course2");

        mockDocument1 = mock(DocumentReference.class);
        mockDocument2 = mock(DocumentReference.class);
        // each course snapshot has a reference
        when(mockDoc1.getReference()).thenReturn(mockDocument1);
        when(mockDoc2.getReference()).thenReturn(mockDocument2);

        // --- Collection and parent business doc chain ---
        mockCollection1 = mock(CollectionReference.class);
        mockCollection2 = mock(CollectionReference.class);
        when(mockDocument1.getParent()).thenReturn(mockCollection1);
        when(mockDocument2.getParent()).thenReturn(mockCollection2);

        // go up one more to the business doc
        DocumentReference mockBizRef1 = mock(DocumentReference.class);
        DocumentReference mockBizRef2 = mock(DocumentReference.class);
        when(mockCollection1.getParent()).thenReturn(mockBizRef1);
        when(mockCollection2.getParent()).thenReturn(mockBizRef2);

        // fetching those business docs should yield mocked biz snapshots
        when(mockBizRef1.get()).thenReturn(Tasks.forResult(mockSnapshot1));
        when(mockBizRef2.get()).thenReturn(Tasks.forResult(mockSnapshot2));

        // --- Business snapshots ---
        when(mockSnapshot1.toObject(Business.class)).thenReturn(business1);
        when(mockSnapshot2.toObject(Business.class)).thenReturn(business2);
        when(mockSnapshot1.getId()).thenReturn("biz1");
        when(mockSnapshot2.getId()).thenReturn("biz2");
    }

    @Test
    public void searchAgeRange_success() throws Exception
    {
        // Arrange
        AgeRange validAge = new AgeRange(11, 15);

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(mockDoc1); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(ageRangeSearchStrategy.searchCourses(validAge), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(courses_search_results.isEmpty());
        assertEquals(1, courses_search_results.size());
        assertEquals("valid_course", courses_search_results.get(0).getName());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(ageRangeSearchStrategy.searchBusinesses(validAge), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(businesses_search_results.isEmpty());
        assertEquals(1, businesses_search_results.size());
        assertEquals("Busi1", businesses_search_results.get(0).getBusinessName());
    }

    @Test
    public void searchAgeRange_failure() throws Exception
    {
        AgeRange validAge = new AgeRange(0, 5); // no courses

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);

        // Search
        List<Course> search_results = Tasks.await(ageRangeSearchStrategy.searchCourses(validAge), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(search_results.isEmpty());
        assertEquals(0, search_results.size());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(ageRangeSearchStrategy.searchBusinesses(validAge), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(businesses_search_results.isEmpty());
        assertEquals(0, businesses_search_results.size());
    }

    @Test
    public void searchCategory_success() throws Exception
    {
        // Arrange
        Category validCategory = Category.Basketball;

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(mockDoc1); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(validCategory))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(categorySearchStrategy.searchCourses(validCategory), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(courses_search_results.isEmpty());
        assertEquals(1, courses_search_results.size());
        assertEquals("valid_course", courses_search_results.get(0).getName());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(categorySearchStrategy.searchBusinesses(validCategory), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(businesses_search_results.isEmpty());
        assertEquals(1, businesses_search_results.size());
        assertEquals("Busi1", businesses_search_results.get(0).getBusinessName());
        assertEquals(Category.Basketball.name(), businesses_search_results.get(0).getCourses().get(0).getCategory().name());
    }

    @Test
    public void searchCategory_failure() throws Exception
    {
        // Arrange
        Category validCategory = Category.Archery; // no courses

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(validCategory))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(categorySearchStrategy.searchCourses(validCategory), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(courses_search_results.isEmpty());
        assertEquals(0, courses_search_results.size());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(categorySearchStrategy.searchBusinesses(validCategory), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(businesses_search_results.isEmpty());
        assertEquals(0, businesses_search_results.size());
    }

    @Test
    public void searchCourseType_success() throws Exception
    {
        // Arrange
        CourseType valid_courseType = CourseType.Solo;

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(mockDoc1); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(valid_courseType))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(courseTypeSearchStrategy.searchCourses(valid_courseType), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(courses_search_results.isEmpty());
        assertEquals(1, courses_search_results.size());
        assertEquals("valid_course", courses_search_results.get(0).getName());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(courseTypeSearchStrategy.searchBusinesses(valid_courseType), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(businesses_search_results.isEmpty());
        assertEquals(1, businesses_search_results.size());
        assertEquals("Busi1", businesses_search_results.get(0).getBusinessName());
        assertEquals(Category.Basketball.name(), businesses_search_results.get(0).getCourses().get(0).getCategory().name());
    }

    @Test
    public void searchCourseType_failure() throws Exception
    {
        // Arrange
        CourseType invalid_courseType = CourseType.Group; // no courses

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(invalid_courseType))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(courseTypeSearchStrategy.searchCourses(invalid_courseType), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(courses_search_results.isEmpty());
        assertEquals(0, courses_search_results.size());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(courseTypeSearchStrategy.searchBusinesses(invalid_courseType), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(businesses_search_results.isEmpty());
        assertEquals(0, businesses_search_results.size());
    }

    @Test
    public void searchDayOfWeek_success() throws Exception
    {
        // Arrange
        Day valid_day = Day.Sunday; // validCourse

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(mockDoc1); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(valid_day))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(dayOfWeekSearchStrategy.searchCourses(valid_day), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(courses_search_results.isEmpty());
        assertEquals(1, courses_search_results.size());
        assertEquals("valid_course", courses_search_results.get(0).getName());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(dayOfWeekSearchStrategy.searchBusinesses(valid_day), 10, TimeUnit.SECONDS);

        // Assert
        assertFalse(businesses_search_results.isEmpty());
        assertEquals(1, businesses_search_results.size());
        assertEquals("Busi1", businesses_search_results.get(0).getBusinessName());
        assertEquals(Category.Basketball.name(), businesses_search_results.get(0).getCourses().get(0).getCategory().name());
    }

    @Test
    public void searchDayOfWeek_failure() throws Exception
    {
        // Arrange
        Day invalid_day = Day.Saturday; // validCourse; // no courses

        List<DocumentSnapshot> onlyMatchingDocs = Arrays.asList(); // only validCourse
        when(mockQuerySnapshot.getDocuments()).thenReturn(onlyMatchingDocs);
        when(mockDb.collectionGroup(anyString()).whereEqualTo(anyString(), eq(invalid_day))).thenReturn(mockQuery);

        // Act for courses
        List<Course> courses_search_results = Tasks.await(dayOfWeekSearchStrategy.searchCourses(invalid_day), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(courses_search_results.isEmpty());
        assertEquals(0, courses_search_results.size());

        // Act for businesses
        List<Business> businesses_search_results = Tasks.await(dayOfWeekSearchStrategy.searchBusinesses(invalid_day), 10, TimeUnit.SECONDS);

        // Assert
        assertTrue(businesses_search_results.isEmpty());
        assertEquals(0, businesses_search_results.size());
    }
}
