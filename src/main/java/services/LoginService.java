package services;

import services.requests.LoginRequest;
import services.responses.LoginResponse;

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
    public LoginResponse onPost(LoginRequest request) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
