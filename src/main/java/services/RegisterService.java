package services;

import dataAccess.Database;

import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

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
    public RegisterResponse onPost(RegisterRequest request, Database database) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RegisterResponse createSpecificErrorResponse(String errMsg) {
        // TODO Auto-generated method stub
        return null;
    }
}
