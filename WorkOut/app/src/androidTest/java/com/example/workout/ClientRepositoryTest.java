package com.example.workout;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;

import static com.google.android.gms.tasks.Tasks.await;
import static com.google.android.gms.tasks.Tasks.forException;
import static com.google.android.gms.tasks.Tasks.forResult;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class ClientRepositoryTest
{
    @Mock
    FirebaseAuth mockAuth;
    @Mock
    FirebaseUser mockUser;
    @Mock
    AuthResult mockAuthResult;
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockCollection;
    @Mock
    DocumentReference mockDocument;
    @Mock
    Task<Void> mockSetTask;
    @Mock
    GeneralRepository mockGeneralRepo;
    @Mock
    Query mockQuery;
    private ClientRepository repo;
    private Phone phone;
    private Address address;

    /**
     * What is mock?
     *  A mock is a stand-in object that simulates behavior of real dependencies,
     *  allowing you to test a specific unit of code in isolation.
     *  --------------------------------------------------------------------------------------
     * For example in this code we're mocking all database related dependencies and test
     * only the code we wrote.
     * Pros:
     * - The code doesn't need to account for database maintenance while testing
     * - You can easily simulate both success and failure scenarios without
     *   polluting or depending on production data.
     *   we don't enter synthetic bad data that can impact negatively on the data
     * - Tests run faster because we only simulate the dependencies and don't actually
     *   run them
     *   --------------------------------------------------------------------------------------
     * Cons:
     * - You do not verify the actual integration with the database (or other
     *   external systems), so mocks can mask real-world issues.
     * - Over-mocking can make tests brittle and reduce confidence. You must
     *   strike a balance between isolation and end-to-end coverage. So think
     *   carefully 'To mock or not to mock? That is the question!'
     *   --------------------------------------------------------------------------------------
     * Mock tools that we used:
     * - mock(Class<T>)
     *   Creates a bare‐bones fake of a class or interface. All methods return defaults (null, 0, false).
     * - stubbing - is the act of pre-programming a mock (or spy) to
     * return a specific value (or throw an exception) when a particular method is called.
     * In other words, you’re “stubbing out” the real implementation and telling Mockito, for example,
     * “When someone calls foo.bar(...), don’t run the real code—just give me this canned response.”
     * - spy(Object real) Wraps a real instance so you can selectively override (stub)
     *   some methods, while leaving others to execute real logic.
     * - Argument matchers (any(), anyString(), eq(...), etc.)
     *   Let you stub or verify calls without hard-coding exact arguments
     * - when(…) / thenReturn(…)
     *   Stubs a call on a mock to return a given value.
     * - doReturn(…) / when(spy).method(…)
     *   Stubs a call on a spy without invoking the real method at stub-time
     *   --------------------------------------------------------------------------------------
     *   A little deeper dive into mock:
     * - when(…) / thenReturn(…) VS. doReturn(…) / when(spy).method(…)
     *   when(...).thenReturn(...):
     *   How it works: Mockito evaluates the argument to when(...) up front,
     *   so it actually calls the real method on your mock or spy to figure out
     *   “what method is being stubbed.”
     * - doReturn(...).when(...).method(...)
     *   How it works: Mockito does not invoke the method when you stub. It simply records
     *   “for that target object and method signature, return this value.”
     */


    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        mockGeneralRepo = mock(GeneralRepository.class);

        // Stub with ALL matchers
        when(mockGeneralRepo.canRegisterUser(
                anyString(), anyString(), anyString()))
                .thenReturn(Tasks.forResult(false));

        repo = new ClientRepository(mockAuth, mockDb, mockGeneralRepo);
        phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        address = new Address(new City("Rosh Ha'Ayin"), "Haim Hertzog");

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString()))
                .thenReturn(mockDocument);
    }

    @Test
    public void insertClient_successFlow_returnsClient() throws Exception
    {
        // canRegisterUser -> false (i.e. username free)
        when(mockGeneralRepo.canRegisterUser(any(), any(), any()))
                .thenReturn(Tasks.forResult(false));

        // createUserWithEmailAndPassword -> a successful AuthResult with UID
        when(mockAuth.createUserWithEmailAndPassword("testEverything@gmail.com", "123456"))
                .thenReturn(forResult(mockAuthResult));
        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("new-uid-123");

        // Firestore set() -> successful
        when(mockDocument.set(anyMap()))
                .thenReturn(forResult(null));

        // Act
        Task<Client> task = repo.insertClient("testEverything", "123456", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        Client client = await(task);

        // Assert
        assertNotNull(client);
        assertEquals("testEverything", client.getUsername());
        assertEquals(phone, client.getPhone());
        assertEquals("testEverything@gmail.com", client.getEmail());
        assertEquals("Shaked", client.getFirstName());
        assertEquals("Michael", client.getLastName());
        assertEquals(address, client.getAddress());
        assertEquals(Gender.Male, client.getGender());
    }

    @Test
    public void insertClient_usernameTaken_throws()
    {
        // canRegisterUser -> true (already exists)
        when(mockGeneralRepo.canRegisterUser(any(), any(), any()))
                .thenReturn(forResult(true));

        // Act: this should immediately throw
        try
        {
            await(repo.insertClient("testEverything", "123456", phone, "testEverything@gmail.com", "Shaked",
                    "Michael", address, Gender.Male));
        }
        catch (ExecutionException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Username already exists", cause.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    @Test
    public void insertClient_authFails_throws()
    {
        // canRegisterUser -> false (username free)
        when(mockGeneralRepo.canRegisterUser(
                anyString(), anyString(), anyString()))
                .thenReturn(Tasks.forResult(false));

        // createUserWithEmailAndPassword -> failure
        FirebaseAuthException authEx =
                new FirebaseAuthException("ERROR_CODE", "bad credentials");
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(Tasks.forException(authEx));

        try
        {
            // Act: this will throw ExecutionException wrapping FirebaseAuthException
            Tasks.await(repo.insertClient("testEverything", "123456", phone,
                    "testEverything@gmail.com", "Shaked", "Michael",
                    address, Gender.Male));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue("Cause should be FirebaseAuthException",
                    cause instanceof FirebaseAuthException);
            assertEquals("bad credentials", cause.getMessage());
            // optionally assert the error code too:
            assertEquals("ERROR_CODE",
                    ((FirebaseAuthException) cause).getErrorCode());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    @Test
    public void insertClient_firestoreFails_throws()
    {
        // canRegisterUser -> false
        when(mockGeneralRepo.canRegisterUser(
                anyString(), anyString(), anyString()))
                .thenReturn(Tasks.forResult(false));

        // auth succeeds
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(Tasks.forResult(mockAuthResult));
        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid42");

        // Firestore .set(...) -> failure
        RuntimeException fsEx = new RuntimeException("oops");
        when(mockDocument.set(anyMap()))
                .thenReturn(Tasks.forException(fsEx));

        try
        {
            // Act: this will throw ExecutionException wrapping our RuntimeException
            Tasks.await(repo.insertClient("testEverything", "123456", phone,
                    "testEverything@gmail.com", "Shaked", "Michael",
                    address, Gender.Male));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue("Cause should be RuntimeException",
                    cause instanceof RuntimeException);
            assertEquals("oops", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    // UPDATE
    @Test
    public void updateClientByID_success() throws Exception
    {
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        // stub the document ref
        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(eq("cid"))).thenReturn(mockDocument);

        // make .set(...) succeed
        when(mockDocument.set(client))
                .thenReturn(Tasks.forResult(null));

        // Act
        Boolean result = Tasks.await(repo.updateClientByID(client), 5, TimeUnit.SECONDS);

        // Assert
        assertTrue("updateClientByID should return true on success", result);
    }

    @Test
    public void updateClientByID_failure() throws Exception
    {
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(eq("cid"))).thenReturn(mockDocument);

        // make .set(...) fail
        RuntimeException ex = new RuntimeException("write error");
        when(mockDocument.set(client))
                .thenReturn(Tasks.forException(ex));

        // Act
        Boolean result = Tasks.await(repo.updateClientByID(client), 5, TimeUnit.SECONDS);

        // Assert
        // on a failing task, continueWith returns task.isSuccessful() → false
        assertFalse("updateClientByID should return false when set() fails", result);
    }


    // DELETE
    @Test
    public void deleteClientByID_noUser_signedOut()
    {
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        // no user signed in
        when(mockAuth.getCurrentUser()).thenReturn(null);

        try
        {
            Tasks.await(repo.deleteClientByID(client));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("No user is currently signed in", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void deleteClientByID_wrongUser_throwsIAE()
    {
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        FirebaseUser current = mock(FirebaseUser.class);
        when(current.getUid()).thenReturn("activeUid");
        when(mockAuth.getCurrentUser()).thenReturn(current);

        // should throw immediately, not wrapped in ExecutionException
        try
        {
            Tasks.await(repo.deleteClientByID(client));
            fail("Expected IllegalArgumentException");
        }
        catch (ExecutionException ee)
        {
            // if it came back wrapped
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
        }
        catch (IllegalArgumentException iae)
        {
            // or direct
            assertEquals("Error trying to delete a user that is not the current active user",
                    iae.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void deleteClientByID_success() throws Exception
    {
        Client client = new Client("uid42", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);

        FirebaseUser current = mock(FirebaseUser.class);
        when(current.getUid()).thenReturn("uid42");
        when(mockAuth.getCurrentUser()).thenReturn(current);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(eq("uid42"))).thenReturn(mockDocument);

        // stub document.delete()
        when(mockDocument.delete())
                .thenReturn(Tasks.forResult(null));
        // stub user.delete()
        when(current.delete())
                .thenReturn(Tasks.forResult(null));

        Boolean ok = Tasks.await(repo.deleteClientByID(client), 5, TimeUnit.SECONDS);
        assertTrue("deleteClientByID should return true on full success", ok);
    }


    // CHECK LOGIN
    @Test
    public void checkLogin_noSuchUser_throws()
    {
        ClientRepository spyRepo = spy(repo);

        // stub ONLY getClientByUsername, bypassing the real method
        doReturn(Tasks.forResult(null))
                .when(spyRepo)
                .getClientByUsername(anyString());

        try
        {
            Tasks.await(spyRepo.checkLogin("bob", "pw"));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof NoSuchElementException);
            assertEquals("No such user: bob", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void checkLogin_authMismatch_throws()
    {
        ClientRepository spyRepo = spy(repo);
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);
        doReturn(Tasks.forResult(client))
                .when(spyRepo)
                .getClientByUsername(eq("bob"));

        // stub signIn to succeed but return a user with wrong UID
        AuthResult mockAuthRes2 = mock(AuthResult.class);
        FirebaseUser wrongUser = mock(FirebaseUser.class);
        when(mockAuth.signInWithEmailAndPassword(client.getEmail(), "pw"))
                .thenReturn(Tasks.forResult(mockAuthRes2));
        when(mockAuthRes2.getUser()).thenReturn(wrongUser);
        when(wrongUser.getUid()).thenReturn("otherUid");

        try
        {
            Tasks.await(spyRepo.checkLogin("bob", "pw"));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof SecurityException);
            assertEquals("Authenticated UID mismatch", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void checkLogin_success() throws Exception
    {
        ClientRepository spyRepo = spy(repo);
        Client client = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);
        doReturn(Tasks.forResult(client))
                .when(spyRepo)
                .getClientByUsername(eq("bob"));

        AuthResult authRes = mock(AuthResult.class);
        when(mockAuth.signInWithEmailAndPassword(client.getEmail(), "pw"))
                .thenReturn(Tasks.forResult(authRes));
        when(authRes.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("cid");

        Client result = Tasks.await(spyRepo.checkLogin("bob", "pw"), 5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("cid", result.getId());
    }


    // GET BY USERNAME
    @Test
    public void getClientByUsername_notFound_throws()
    {
        // stub empty QuerySnapshot
        QuerySnapshot emptySnap = mock(QuerySnapshot.class);
        when(emptySnap.isEmpty()).thenReturn(true);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo(eq("username"), eq("nobody")))
                .thenReturn(mockQuery);
        when(mockQuery.limit(1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(emptySnap));

        try
        {
            Tasks.await(repo.getClientByUsername("nobody"));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("No client with username: nobody", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void getClientByUsername_success() throws Exception
    {
        // prepare one DocumentSnapshot
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        Client stored = new Client("cid", "testEverything", phone, "testEverything@gmail.com", "Shaked",
                "Michael", address, Gender.Male);
        when(doc.toObject(Client.class)).thenReturn(stored);
        when(doc.getId()).thenReturn("cid");

        List<DocumentSnapshot> docs = Collections.singletonList(doc);
        QuerySnapshot snap = mock(QuerySnapshot.class);
        when(snap.isEmpty()).thenReturn(false);
        when(snap.getDocuments()).thenReturn(docs);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo("username", "alice"))
                .thenReturn(mockQuery);
        when(mockQuery.limit(1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snap));

        Client result = Tasks.await(repo.getClientByUsername("alice"), 5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("cid", result.getId());
        assertEquals("testEverything", result.getUsername());
    }
}
