package Model.APIService;
import Model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;

/**
 * Retrofit2 implements this interface at Runtime.
 * Way easier then handling the calls and creating
 * the queries ourselves
 */
public interface UserAPIService {

    // The annotation is to announce the kind of operation
    // GET is for getting information (Like SELECT in SQL)
    // POST is for inserting information (Like INSERT)
    // PUT is for updating
    // DELETE for deletion
    // Then you specify where to get/put the information
    // For example users/{id} means under 'users' 'Table'
    // (Not really a table because it's in JSON)
    // And access field called id
    @GET("users/{id}")
    Call<User> fetchUserByID(@Path("id") String id);

    // @Body Serializes the annotated method parameter as the HTTP request body, using
    // a converter we add to Retrofit.StringBuilder
    // our converter is Gson and the implementation can
    // be found in the UserRepositoryTest.java
    @POST("users.json")
    Call<FirebasePostResponse> createUser(@Body User user);

    // @Path Substitutes the value of a method parameter into a placeholder in your URL
    // Basically it replaces the field in the URL, in this example {id}
    // with the @Path the correlates to it, in this example @Path("id")
    // so basically it replaces {id} with the given id String
    @PUT("users/{id}.json")
    Call<User> updateUserByID(@Path("id") String id, @Body User user);

    @DELETE("user/{id}.json")
    Call<Void> deleteUserByID(@Path("id") String id);

    @GET("users/username.json")
    Call<User> getUserByUsername(@Path("username") String username);

    // POST the Google ID token
    // returns your auth token
    @POST("auth/google")
    Call<AuthResponse> authenticateWithGoogle(@Body GoogleAuthRequest body);

    // After login fetch the app‐user by googleId
    @GET("users/google/{googleId}")
    Call<User> fetchUserByGoogleId(@Path("googleId") String googleId);

}

