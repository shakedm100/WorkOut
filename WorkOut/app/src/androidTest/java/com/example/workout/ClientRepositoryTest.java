package com.example.workout;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import static com.google.android.gms.tasks.Tasks.await;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class ClientRepositoryTest
{
    private static ClientRepository repository;
    private static Client testSubject;

    @BeforeClass
    public static void setUp()
    {
        repository = new ClientRepository();

        try
        {
            Client testBusiness = await(repository.getClientByUsername("testEverything"), 10, TimeUnit.SECONDS);
            await(repository.deleteClientByID(testBusiness), 10, TimeUnit.SECONDS);
        }
        catch (Exception e) { /*User doesn't exist in db, good!*/ }
    }

    @AfterClass
    public static void setDown() throws ExecutionException, InterruptedException, TimeoutException
    {
        await(repository.deleteClientByID(testSubject), 30, TimeUnit.SECONDS);
    }

    @Test
    public void queryClientTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("2","Rosh Ha'Ayin"), "Haim Hertzog");

        // Test insert functionality
        testSubject = await(repository.insertClient("testEverything", "1234", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject);

        assertEquals("testEverything", testSubject.getUsername());
        assertEquals("1234", testSubject.getPassword());
        assertEquals(phone, testSubject.getPhone());
        assertEquals("testEverything@gmail.com", testSubject.getEmail());
        assertEquals("Shaked", testSubject.getFirstName());
        assertEquals("Michael", testSubject.getLastName());
        assertEquals(address, testSubject.getAddress());
        assertEquals(Gender.Male, testSubject.getGender());

        // Fail by username
        assertThrows(Exception.class,()-> await(repository.insertClient("testEverything", "1234", phone, "bla@gmail.com", "Shaked",
                "Michael", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername())), 10, TimeUnit.SECONDS));

        // Fail by email
        assertThrows(Exception.class, () -> await(repository.insertClient("bla", "1234", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername())), 10, TimeUnit.SECONDS));

        // Test Get functionality
        Client checkGet = await(repository.getClientByUsername(testSubject.getUsername()), 10, TimeUnit.SECONDS);

        assertEquals("testEverything", checkGet.getUsername());
        assertEquals("1234", checkGet.getPassword());
        assertEquals(phone, checkGet.getPhone());
        assertEquals("testEverything@gmail.com", checkGet.getEmail());
        assertEquals("Shaked", checkGet.getFirstName());
        assertEquals("Michael", checkGet.getLastName());
        assertEquals(address, checkGet.getAddress());
        assertEquals(Gender.Male, checkGet.getGender());

        // Test update functionality
        Client updateClient = testSubject;
        updateClient.setFirstName("Lagrange");
        repository.updateClientByID(updateClient);
        Client check = await(repository.getClientByUsername(testSubject.getUsername()), 10, TimeUnit.SECONDS);
        assertNotNull(check);
        assertEquals(check.getFirstName(), updateClient.getFirstName());
    }
}
