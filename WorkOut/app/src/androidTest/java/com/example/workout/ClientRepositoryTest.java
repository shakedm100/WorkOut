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
            Client testBusiness = await(repository.getClientByUsername("test"), 10, TimeUnit.SECONDS);
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
        testSubject = await(repository.insertClient("test", "1234", phone, "test@gmail.com", "Shaked",
                "Michael", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername())), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject);

        //TODO: Add many asserts
        //TODO: Check get functionality

        Client updateClient = testSubject;
        updateClient.setFirstName("Lagrange");
        repository.updateClientByID(updateClient);
        Client check = await(repository.getClientByUsername(testSubject.getUsername()), 10, TimeUnit.SECONDS);
        assertNotNull(check);
        assertEquals(check.getFirstName(), updateClient.getFirstName());
    }
}
