package models;

/**
 * A representation of an authentication token which authorizes users to interact
 * with the app after obtaining one through a login request.
 */
public class AuthToken {
    /** The authorization token that identifies the user's validated session */
    private String authtoken;
    /** The username of the user this token belongs to */
    private String username;

    /**
     * Creates an AuthToken by defining each property directly
     * 
     * @param authtoken is the token string to use
     * @param username is the associated username
     */
    public AuthToken(String authtoken, String username) {
        this.setAuthtoken(authtoken);
        this.setUsername(username);
    }

    public String getAuthtoken() {
        return this.authtoken;
    }

    public void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = authtoken;
    }
}
