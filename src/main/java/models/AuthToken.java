package models;

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

    String getAuthtoken() {
        return this.authtoken;
    }

    void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }

    String getUsername() {
        return this.username;
    }

    void setUsername(String username) {
        this.username = authtoken;
    }
}
