package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.FirebaseApp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalTime;
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
import Model.Rating;
import Model.Repository.BusinessRepository;
import Model.Schedule;

public class BusinessRepositoryTest
{
    private static BusinessRepository repository;
    private Context appContext;
    private static Business testSubject;
    private static Client testHelper;

    @BeforeClass
    public static void setUp()
    {
        repository = new BusinessRepository();
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("2","Rosh Ha'Ayin"), "Haim Hertzog");
        testHelper = new Client("esdrg","shakedm100", "1234", phone,
                "shaked1mi@gmail.com", "Shaked","Michael", address, Gender.Male);

        try
        {
            // Just to add more robustness to testing, even if something failed it
            // still attempts to delete the previous test subject
            Business testBusiness = await(repository.getBusinessByUsername("test"), 10, TimeUnit.SECONDS);
            await(repository.deleteBusinessByID(testBusiness), 10, TimeUnit.SECONDS);
        }
        catch (Exception e) { /*User doesn't exist in db, good!*/ }
    }

    @AfterClass
    public static void setDown() throws ExecutionException, InterruptedException, TimeoutException
    {
        // All database calls need to await because if we don't it just exits the test
        // without finishing executing the function
        await(repository.deleteBusinessByID(testSubject), 30, TimeUnit.SECONDS);
    }

    @Test
    public void insertBusinessTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        ArrayList<Client> participants = new ArrayList<>();
        participants.add(testHelper);
        ArrayList<Client> followers = new ArrayList<>();
        Rating rating = new Rating("1234", 4.9F,"Terribly amazing", testHelper);
        ArrayList<Rating> ratings = new ArrayList<>();
        ratings.add(rating);
        followers.add(testHelper);
        Schedule schedule = new Schedule("bla", Day.Sunday, LocalTime.now());
        Course course = new Course("bla", CourseType.Dou, "TRX", participants, 50, new AgeRange(23,50),
                schedule, Category.Archery, "Shoot to kill");
        ArrayList<Course> courses = new ArrayList<>();
        courses.add(course);
        testSubject = await(repository.insertBusiness("test", "1234", testHelper.getPhone(),
                "test@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy"), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject);
        assertEquals("test", testSubject.getUsername());
        assertEquals("1234", testSubject.getPassword());
        assertEquals(testHelper.getPhone(), testSubject.getPhone());
        assertEquals("test@mail", testSubject.getEmail());
        assertEquals(new Location(12345, 2145435), testSubject.getLocation());
        assertEquals("EasyBusy", testSubject.getName());
        assertEquals("Policy", testSubject.getPolicy());
    }

    @Test
    public void getBusinessByUsernameTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        Business check = await(repository.getBusinessByUsername(testSubject.getUsername()), 10, TimeUnit.SECONDS);
        assertNotNull(check);
        assertEquals(check.getId(), testSubject.getId());
    }

    @Test
    public void updateBusinessTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        Boolean check = await(repository.updateBusiness(testSubject), 10, TimeUnit.SECONDS);
        assertNotNull(check);
        assertTrue(check);
    }

}
