package Model.Repository;
import java.io.IOException;

import Model.APIService.FirebasePostResponse;
import Model.User;
import Model.APIService.UserAPIService;
import retrofit2.Call;
import retrofit2.Response;
public class UserRepository {
    private final UserAPIService apiService;

    public UserRepository(UserAPIService apiService) {
        this.apiService = apiService;
    }

    /**
     * Sends a POST /users and returns the created User.
     * Throws RuntimeException on HTTP or network error.
     */
    public User createUser(User user) {
        try {
            Call<FirebasePostResponse> call = apiService.createUser(user);
            Response<FirebasePostResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                user.setId(response.body().key);
                return user;
            } else {
                throw new RuntimeException("API error: HTTP " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Network error", e);
        }
    }

    public Call<User> getUser(String id) {
        return apiService.fetchUserByID(id);
    }
}