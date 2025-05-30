package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
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
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.CourseRepository;
import Model.Schedule;

@RunWith(AndroidJUnit4.class)
public class CourseRepositoryTest
{
    private static CourseRepository courseRepository;
    private static BusinessRepository businessRepository;
    private Course testSubject;
    private static Business testBusiness;
    private static Client testClient;

    @BeforeClass
    public static void setUp()
    {
        courseRepository = new CourseRepository();
        businessRepository = new BusinessRepository();

        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("2", "Rosh Ha'Ayin"), "Haim Hertzog");
        testClient = new Client("esdrg","shakedm100", "1234", phone,
                "shaked1mi@gmail.com", "Shaked","Michael", address, Gender.Male);

        try
        {
            testBusiness = await(businessRepository.insertBusiness("testEverything", "1234", testClient.getPhone(),
                    "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                    "Policy"), 10, TimeUnit.SECONDS);
        }catch (Exception e)
        {
            System.out.println("ERROR: Business probably exists");
        }

        try
        {
            if(testBusiness == null)
                testBusiness = await(businessRepository.getBusinessByUsername("testEverything"), 10, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println("ERROR: Business insertion failed on setUp");
        }


    }

    @AfterClass
    public static void setDown() throws ExecutionException, InterruptedException, TimeoutException
    {
        await(businessRepository.deleteBusiness(testBusiness), 30, TimeUnit.SECONDS);
    }

    @Test
    public void queryCourseTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        Schedule schedule = new Schedule(Day.Sunday, Timestamp.now());
        AgeRange ageRange = new AgeRange(23,50);

        // Test insert
        testSubject = await(courseRepository.insertCourse(testBusiness,"TRX", schedule, 50, CourseType.Dou,
                ageRange, Category.Archery, "Shoot to kill"), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject.getId());
        assertEquals("TRX", testSubject.getName());
        assertEquals(schedule, testSubject.getSchedule());
        assertEquals(50, testSubject.getCapacity());
        assertEquals(CourseType.Dou, testSubject.getType());
        assertEquals(ageRange, testSubject.getAgeRange());
        assertEquals(Category.Archery, testSubject.getCategory());
        assertEquals("Shoot to kill", testSubject.getDescription());

        // Test get course
        ArrayList<Course> courses = (ArrayList<Course>)
                await(courseRepository.getAllBusinessesCourses(testBusiness), 10, TimeUnit.SECONDS);

        // Should be one course
        assertEquals(1, courses.size());

        Course checkCourse = courses.get(0);
        assertNotNull(checkCourse.getId());
        assertEquals(testSubject.getName(), checkCourse.getName());
        assertEquals(testSubject.getSchedule(), checkCourse.getSchedule());
        assertEquals(testSubject.getCapacity(), checkCourse.getCapacity());
        assertEquals(testSubject.getType(), checkCourse.getType());
        assertEquals(testSubject.getAgeRange(), checkCourse.getAgeRange());
        assertEquals(testSubject.getCategory(), checkCourse.getCategory());
        assertEquals(testSubject.getDescription(), checkCourse.getDescription());

        // Test day of week
        ArrayList<Course> daysOfWeek = (ArrayList<Course>) await(courseRepository.getCoursesByDayOfWeek(testBusiness,
                Day.Sunday), 10, TimeUnit.SECONDS);

        assertNotNull(daysOfWeek);
        assertFalse(daysOfWeek.isEmpty());

        // Test update
        Course updateCourse = testSubject;
        updateCourse.setCapacity(45);
        boolean check = await(courseRepository.updateCourse(updateCourse, testBusiness), 10, TimeUnit.SECONDS);
        assertTrue(check);

        // Test delete
        boolean checkDelete = await(courseRepository.deleteCourse(updateCourse, testBusiness), 10, TimeUnit.SECONDS);
        assertTrue(checkDelete);
    }
}
