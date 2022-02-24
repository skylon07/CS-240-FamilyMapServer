package handlers;

import services.RegisterService;
import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

public class RegisterHandler extends GenericHandler<RegisterRequest, RegisterResponse, RegisterService> {
    @Override
    protected Class<RegisterRequest> getBoundRequestClass() {
        return RegisterRequest.class;
    }

    @Override
    protected RegisterService createBoundService() {
        return new RegisterService();
    }
}
