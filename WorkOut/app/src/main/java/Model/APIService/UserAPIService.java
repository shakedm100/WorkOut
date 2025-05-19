package Model.APIService;
import Model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserAPIService {
   /* @GET("users/{id}")
    Call<User> fetchUser(@Path("id") String id);*/

    @POST("users.json")
    Call<FirebasePostResponse> createUser(@Body User user);
}

