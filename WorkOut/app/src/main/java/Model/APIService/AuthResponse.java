package Model.APIService;

import Model.User;

public class AuthResponse
{
    private String sessionToken; // Server’s session token
    private User user; // User profile returned

    public AuthResponse(String sessionToken, User user)
    {
        this.sessionToken = sessionToken;
        this.user = user;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public User getUser()
    {
        return user;
    }

    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}
