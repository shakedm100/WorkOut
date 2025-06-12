package com.example.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Model.Business;
import Model.Client;
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;

import static com.google.android.gms.tasks.Tasks.forException;
import static com.google.android.gms.tasks.Tasks.forResult;
import static org.junit.Assert.*;

import com.google.android.gms.tasks.Tasks;
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

@RunWith(AndroidJUnit4.class)
public class BusinessRepositoryTest
{
    @Mock
    FirebaseAuth mockAuth;
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockCollection;
    @Mock
    DocumentReference mockDocument;
    @Mock
    Query mockQuery;
    @Mock
    QuerySnapshot mockQuerySnapshot;
    @Mock
    GeneralRepository mockGeneralRepo;
    @Mock
    FirebaseUser mockUser;
    private Phone testPhone;
    private Location testLocation;
    private BusinessRepository repo;

    @Before
    public void setUp()
    {
        testPhone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        testLocation = new Location(12345, 2145435);
        MockitoAnnotations.openMocks(this);
        // common stubs
        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        // init repository
        repo = new BusinessRepository(mockDb, mockAuth, mockGeneralRepo);
    }

    // --- INSERT BUSINESS ---
    @Test
    public void insertBusiness_success() throws Exception
    {
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        // Auth stub
        when(mockGeneralRepo.canRegisterUser(anyString(), anyString(), anyString())).thenReturn(Tasks.forResult(false));
        AuthResult authRes = mock(AuthResult.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(mockAuth.createUserWithEmailAndPassword(eq(business.getEmail()), anyString()))
                .thenReturn(forResult(authRes));
        when(authRes.getUser()).thenReturn(user);
        when(user.getUid()).thenReturn("bid");
        // Firestore stub
        when(mockDocument.set(anyMap())).thenReturn(forResult(null));

        Business result = Tasks.await(
                repo.insertBusiness(
                        business.getUsername(), "123456", business.getPhone(),
                        business.getEmail(), "EasyBusy", business.getLocation(),
                        business.getPolicy()), 5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals("bid", result.getId());
        assertEquals("testEverything", result.getUsername()); //TODO: Add more asserts
        assertEquals("EasyBusy", result.getBusinessName());
    }

    @Test
    public void insertBusiness_authMismatch_throws()
    {
        // stub query to simulate existing email
        when(mockCollection.whereEqualTo(eq("email"), anyString()))
                .thenReturn(mockQuery);
        when(mockQuery.limit(1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(forResult(mockQuerySnapshot));
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockGeneralRepo.canRegisterUser(anyString(), anyString(), anyString())).thenReturn(Tasks.forResult(true));

        try
        {
            Tasks.await(repo.insertBusiness(
                    "testEverything", "123456", testPhone,
                    "dup@mail.com", "EasyBusy", testLocation,
                    "Policy"), 5, TimeUnit.SECONDS);
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Username already exists", cause.getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
        catch (TimeoutException e)
        {
            Thread.currentThread().interrupt();
            fail("Insertion Timeout");
        }
    }

    // --- GET BUSINESS ---
    @Test
    public void getBusinessByID_success() throws Exception
    {
        DocumentSnapshot snap = mock(DocumentSnapshot.class);
        Business stored = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        when(snap.toObject(Business.class)).thenReturn(stored);
        when(snap.getId()).thenReturn("bid");
        when(mockDocument.get()).thenReturn(forResult(snap));
        when(snap.exists()).thenReturn(true);

        Business result = Tasks.await(repo.getBusinessesById("bid"), 5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("bid", result.getId());
        assertEquals("testEverything", result.getUsername());
    }

    @Test
    public void getBusinessByID_notFound_throws()
    {
        // simulate get() returning null or unsuccessful
        when(mockDocument.get()).thenReturn(forException(new RuntimeException("not found")));

        try
        {
            Tasks.await(repo.getBusinessesById("bid"));
            fail("Expected ExecutionException");
        }
        catch (ExecutionException ee)
        {
            assertTrue(ee.getCause() instanceof RuntimeException);
            assertEquals("not found", ee.getCause().getMessage());
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    // --- UPDATE BUSINESS ---
    @Test
    public void updateBusinessByID_success() throws Exception
    {
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        when(mockDocument.set(business))
                .thenReturn(forResult(null));

        Boolean ok = Tasks.await(repo.updateBusiness(business), 5, TimeUnit.SECONDS);
        assertTrue(ok);
    }

    @Test
    public void updateBusinessByID_failure() throws Exception
    {
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        when(mockDocument.set(business))
                .thenReturn(forException(new RuntimeException("update failed")));

        Boolean ok = Tasks.await(repo.updateBusiness(business), 5, TimeUnit.SECONDS);
        assertFalse(ok);
    }

    // --- DELETE BUSINESS ---
    @Test
    public void deleteBusinessByID_wrongUser_throws()
    {
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        FirebaseUser user = mock(FirebaseUser.class);
        when(mockAuth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("otherBid");

        try
        {
            Tasks.await(repo.deleteBusiness(business));
            fail("Expected ExecutionException or IAE");
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
        }
        catch (IllegalArgumentException iae)
        {
            // direct throw
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
    }

    @Test
    public void deleteBusinessByID_success() throws Exception
    {
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        FirebaseUser user = mock(FirebaseUser.class);
        when(mockAuth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("bid");

        when(mockDocument.delete()).thenReturn(forResult(null));
        when(user.delete()).thenReturn(forResult(null));

        Boolean ok = Tasks.await(repo.deleteBusiness(business), 5, TimeUnit.SECONDS);
        assertTrue(ok);
    }

    // --- LOGIN BUSINESS ---
    @Test
    public void checkBusinessLogin_success() throws Exception
    {
        BusinessRepository spyRepo = spy(repo);
        Business business = new Business("bid", "testEverything", testPhone,
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy");
        doReturn(Tasks.forResult(business))
                .when(spyRepo)
                .getBusinessByUsername(eq("testEverything"));

        AuthResult authRes = mock(AuthResult.class);
        when(mockAuth.signInWithEmailAndPassword(business.getEmail(), "pw"))
                .thenReturn(Tasks.forResult(authRes));
        when(authRes.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("bid");

        Business result = Tasks.await(spyRepo.checkLogin("testEverything", "pw"), 5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals("bid", result.getId());
    }
}
