package services;

import dataAccess.BadAccessException;
import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.UserAccessor;
import models.User;

import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

import utils.AuthUtils;
import utils.FamilyTreeUtils;

/**
 * This service provides functionality for the user registration endpoint.
 * It accepts calls through the HTTP POST method.
 */
public class RegisterService extends GenericService<RegisterRequest, RegisterResponse> {
    static final int NUM_GENERATIONS = 4;
    
    /**
     * Creates a new RegisterService by calling GenericService with this
     * service's name
     */
    public RegisterService() {
        super("RegisterService");
    }

    @Override
    public RegisterResponse onPost(RegisterRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        String username = request.username;
        // TODO: make new error (invalid request) for missing data
        assert username != null : "RegisterService expected a username";
        String password = request.password;
        assert password != null : "RegisterService expected a password";
        String email = request.email;
        assert email != null : "RegisterService expected an email";
        String firstName = request.firstName;
        assert firstName != null : "RegisterService expected a firstName";
        String lastName = request.lastName;
        assert lastName != null : "RegisterService expected a lastName";
        String gender = request.gender;
        assert gender != null : "RegisterService expected a gender";

        // create new user account
        User newUser = new User(username, password, email, firstName, lastName, gender, null);
        UserAccessor userAcc = new UserAccessor(database);
        User[] users = {newUser};
        try {
            userAcc.create(users);
        } catch (BadAccessException err) {
            return this.createUserExistsResponse(newUser);
        }

        // generate 4 generations of ancestor data
        FamilyTreeUtils famTreeUtils = new FamilyTreeUtils(database);
        famTreeUtils.generateFamilyTree(newUser, RegisterService.NUM_GENERATIONS);
        String personID = newUser.getPersonID();
        assert personID != null : "FamilyTreeUtils did not generate a personID";

        // log the user in/generate auth token
        AuthUtils authUtils = new AuthUtils(database);
        String authToken = authUtils.authenticateUser(username, password);
        assert authToken != null : "Newly created user's password doesn't match newly created password";
        
        // generate the response
        return this.createSuccessfulResponse(username, personID, authToken);
    }

    /**
     * Creates a successful RegisterResponse with required parameters
     * 
     * @param authToken is the newly created auth token
     * @param username is the username of the now-logged-in User
     * @param personID is the personID of that User
     * @return the successful RegisterResponse
     */
    private RegisterResponse createSuccessfulResponse(String username, String personID, String authToken) {
        RegisterResponse response = new RegisterResponse();
        response.success = true;
        response.username = username;
        response.personID = personID;
        response.authtoken = authToken;
        return response;
    }

    /**
     * Creates a failed RegisterResponse indicating a user already existed
     * 
     * @param user is the user that already existed
     * @return the failed response
     */
    private RegisterResponse createUserExistsResponse(User user) {
        RegisterResponse response = new RegisterResponse();
        response.success = false;
        response.message = "User '" + user.getUsername() + "' already exists";
        return response;
    }

    @Override
    protected RegisterResponse createSpecificErrorResponse(String errMsg) {
        return new RegisterResponse();
    }
}
