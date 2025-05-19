package Model.APIService;
import Model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    @POST("users.json")
    Call<FirebasePostResponse> createUser(@Body User user);
}

