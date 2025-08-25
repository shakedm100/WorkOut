package com.example.workout.espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.workout.Business.BusinessHomeActivity;
import com.example.workout.LoginActivity;
import com.example.workout.MainActivity;
import com.example.workout.R;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.hamcrest.Matchers.allOf;

import Model.Address;
import Model.Business;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;
import ViewModel.LoginViewModel;
import ViewModel.ViewModelFactoryProvider;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest
{

    @Rule
    public IntentsTestRule<LoginActivity> intentsRule =
            new IntentsTestRule<>(LoginActivity.class, /* initialTouchMode */ true, /* launchActivity */ false);

    private ClientRepository clientRepository;
    private BusinessRepository businessRepository;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        clientRepository = Mockito.mock(ClientRepository.class);
        businessRepository = Mockito.mock(BusinessRepository.class);

        // Swap the factory so the Activity receives our injected ViewModel.
        ViewModelFactoryProvider.factory = new ViewModelProvider.Factory()
        {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
            {
                if (modelClass.equals(LoginViewModel.class))
                {
                    return (T) new LoginViewModel(clientRepository, businessRepository);
                }
                throw new IllegalArgumentException("Unknown model: " + modelClass);
            }
        };
    }

    @After
    public void tearDown()
    {
        // Restore default factory so other tests aren’t affected
        ViewModelFactoryProvider.factory = new ViewModelProvider.NewInstanceFactory();
    }

    @Test
    public void login_without_password()
    {
        intentsRule.launchActivity(new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class));

        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Perform UI actions
        onView(withId(R.id.usernameTextLogin)).perform(typeText("shakedm10"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginStatusTextView))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(withText(containsString("Password can not be empty"))));
    }

    @Test
    public void login_without_username()
    {
        intentsRule.launchActivity(new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class));

        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Perform UI actions
        onView(withId(R.id.passwordTextLogin)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginStatusTextView))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(withText(containsString("Username can not be empty"))));
    }

    @Test
    public void login_asClient_success_navigatesToMain()
    {
        Client client = new Client("sdfdsf", "shakedm10", new Phone(PhonePrefix.PREFIX_052, "846456"),
                "bla@gmail.com", "Shaked", "Michael", new Address(new City("Rosh Haayin"), "Ofra Haza 4"),
                Gender.Male);

        // Make the login succeed synchronously
        when(clientRepository.checkLogin("shakedm10", "123456"))
                .thenReturn(Tasks.forResult(client));
        when(businessRepository.checkLogin(anyString(), anyString()))
                .thenReturn(Tasks.forException(new Exception("not business")));

        // Launch the activity (Intents already initialized by the rule)
        intentsRule.launchActivity(new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class));

        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Perform UI actions
        onView(withId(R.id.usernameTextLogin)).perform(typeText("shakedm10"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextLogin)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Make sure all posted work completed
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Verify an intent to MainActivity was sent
        intended(allOf(
                hasComponent(MainActivity.class.getName()),
                toPackage(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName())
        ));
    }

    @Test
    public void login_asBusiness_success_navigatesToMain()
    {
        Business business = new Business("sdfdsf", "shakedm10", new Phone(PhonePrefix.PREFIX_052, "846456"),
                "bla@gmail.com", "Shaked", new Location(32.0853, 34.7818), "Standard T&C",
                new Address(new City("Rosh Haayin"), "Ofra Haza 4"));

        // Make the login succeed synchronously
        when(clientRepository.checkLogin("shakedm10", "123456"))
                .thenReturn(Tasks.forException(new Exception("not client")));
        when(businessRepository.checkLogin(anyString(), anyString()))
                .thenReturn(Tasks.forResult(business));

        // Launch the activity (Intents already initialized by the rule)
        intentsRule.launchActivity(new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class));

        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Perform UI actions
        onView(withId(R.id.usernameTextLogin)).perform(typeText("shakedm10"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextLogin)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Make sure all posted work completed
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Verify an intent to MainActivity was sent
        intended(allOf(
                hasComponent(BusinessHomeActivity.class.getName()),
                toPackage(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName())
        ));
    }

    @Test
    public void login_failure_showsErrorOnScreen()
    {
        // Given: both repos fail -> "No user was found" (as per your ViewModel error path)
        when(clientRepository.checkLogin(anyString(), anyString()))
                .thenReturn(Tasks.forException(new Exception("bad creds")));
        when(businessRepository.checkLogin(anyString(), anyString()))
                .thenReturn(Tasks.forException(new Exception("bad creds")));

        intentsRule.launchActivity(new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class));

        onView(withId(R.id.usernameTextLogin)).perform(typeText("unknown"), closeSoftKeyboard());
        onView(withId(R.id.passwordTextLogin)).perform(typeText("nope"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // ProgressBar should hide and status should show the error
        onView(withId(R.id.loginProgressBar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.loginStatusTextView))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(withText(containsString("Authentication failed"))));
    }
}
