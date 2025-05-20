package Model.Repository;
import java.io.IOException;

import Model.APIService.AuthResponse;
import Model.APIService.FirebasePostResponse;
import Model.APIService.GoogleAuthRequest;
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

    public Call<User> getUserByID(String id) {
        return apiService.fetchUserByID(id);
    }

    public Call<User> updateUserByID(String id, User user)
    {
        return apiService.updateUserByID(id, user);
    }

    public Call<Void> deleteUserByID(String id)
    {
        return apiService.deleteUserByID(id);
    }

    /**
     * Sends the Google ID token to your backend,
     * returns your app’s JWT (SessionToken) + user on success.
     */
    public AuthResponse authenticateWithGoogle(String idToken) {
        Call<AuthResponse> call = apiService.authenticateWithGoogle(new GoogleAuthRequest(idToken));
        try
        {
            Response<AuthResponse> resp = call.execute();
            if (resp.isSuccessful() && resp.body() != null)
                return resp.body();
            else
                throw new RuntimeException("Auth failed: HTTP " + resp.code());
        } catch (IOException e) {
            throw new RuntimeException("Network error", e);
        }
    }

    public User findByGoogleId(String googleId)
    {
        try
        {
            Response<User> resp = apiService.fetchUserByGoogleId(googleId).execute();
            if (resp.isSuccessful())
                return resp.body();
        }
        catch (Exception e)
        {
            throw new RuntimeException("User not found");
        }

        return null;
    }

}