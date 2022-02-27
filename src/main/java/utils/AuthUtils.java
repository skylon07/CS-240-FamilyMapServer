package utils;

import java.security.SecureRandom;
import java.util.Base64;

import dataAccess.*;

import models.AuthToken;
import models.User;

public class AuthUtils extends GenericUtility {
    public AuthUtils(Database database) {
        super(database);
    }

    /**
     * Gets a user currently logged in by their auth token
     * 
     * @param authToken is the auth token the user logged in with
     * @return the corresponding logged-in User object, or null if the auth token is invalid
     */
    public User getAuthenticatedUser(String authTokenStr) throws DatabaseException {
        if (authTokenStr == null) {
            return null;
        }

        AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(this.database);
        AuthToken authToken = authTokenAcc.getByAuthToken(authTokenStr);
        
        User authedUser;
        if (authToken == null) {
            authedUser = null;
        } else {
            String username = authToken.getUsername();
            UserAccessor userAcc = new UserAccessor(this.database);
            authedUser = userAcc.getByUsername(username);
        }
        return authedUser;
    }

    /**
     * Logs in a user and returns their new auth token, or null if unsuccessful.
     * A "login attempt" is successful if the given password matches the password
     * in the database for the given username.
     *  
     * @param username is the username of the User attempting to log in
     * @param password is the password the user gave
     * @return the new auth token if login was successful, otherwise null
     * @throws DatabaseException when the database fails to perform an operation
     */
    public String authenticateUser(String username, String password) throws DatabaseException {
        UserAccessor userAcc = new UserAccessor(this.database);
        User user = userAcc.getByUsername(username);
        
        boolean successfulLogin = user != null && password.equals(user.getPassword());
        if (successfulLogin) {
            AuthTokenAccessor authAcc = new AuthTokenAccessor(this.database);
            String tokenStr = null;
            while (tokenStr == null) {
                tokenStr = this.makeAuthTokenString();
                AuthToken[] tokensToCreate = {new AuthToken(tokenStr, username)};
                try {
                    authAcc.create(tokensToCreate);
                } catch (BadAccessException err) {
                    // a collision happened! make a new one
                    tokenStr = null;
                }
            }
            return tokenStr;
        } else {
            return null;
        }
    }

    /**
     * Generates a unique auth token string
     * 
     * @return the auth token
     */
    private String makeAuthTokenString() {
        SecureRandom rand = new SecureRandom();
        byte[] randomBytes = new byte[8];
        rand.nextBytes(randomBytes);
        
        Base64.Encoder encoder = Base64.getUrlEncoder();
        String strWithEquals = encoder.encodeToString(randomBytes);
        return strWithEquals.split("=")[0];
    }
}
