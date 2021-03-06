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
        if (username == null) {
            return this.createIncompleteResponse("username");
        }
        String password = request.password;
        if (password == null) {
            return this.createIncompleteResponse("password");
        }
        String email = request.email;
        if (email == null) {
            return this.createIncompleteResponse("email");
        }
        String firstName = request.firstName;
        if (firstName == null) {
            return this.createIncompleteResponse("firstName");
        }
        String lastName = request.lastName;
        if (lastName == null) {
            return this.createIncompleteResponse("lastName");
        }
        String gender = request.gender;
        if (gender == null) {
            return this.createIncompleteResponse("gender");
        }

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
        try {
            famTreeUtils.generateFamilyTree(newUser);
        } catch (BadAccessException err) {
            throw new AssertionError("User Accessor did not catch duplicate create");
        }
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
