package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.UserAccessor;

import models.User;

import services.requests.LoginRequest;
import services.responses.LoginResponse;

import utils.AuthUtils;

/**
 * This service provides functionality for the login endpoint.
 * It accepts calls through the HTTP POST method.
 */
public class LoginService extends GenericService<LoginRequest, LoginResponse> {
    /**
     * Creates a new LoginService by calling GenericService with this
     * service's name
     */
    public LoginService() {
        super("LoginService");
    }

    @Override
    public LoginResponse onPost(LoginRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        String username = request.username;
        String password = request.password;
        assert username != null : "LoginService expected a username";
        assert password != null : "LoginService expected a password";
        
        AuthUtils authUtils = new AuthUtils(database);
        UserAccessor userAcc = new UserAccessor(database);
        String authToken = authUtils.authenticateUser(username, password);
        User user = userAcc.getByUsername(username);
        String personID = user.getPersonID();
        return createSuccessfulResponse(authToken, username, personID);
    }

    /**
     * Creates a successful LoginResponse with required parameters
     * 
     * @param authToken is the newly created auth token
     * @param username is the username of the now-logged-in User
     * @param personID is the personID of that User
     * @return the successful LoginResponse
     */
    private LoginResponse createSuccessfulResponse(String authToken, String username, String personID) {
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.authtoken = authToken;
        response.username = username;
        response.personID = personID;
        return response;
    }

    @Override
    protected LoginResponse createSpecificErrorResponse(String errMsg) {
        return new LoginResponse();
    }
}
