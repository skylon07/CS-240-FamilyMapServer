package handlers;

import services.LoginService;
import services.requests.LoginRequest;
import services.responses.LoginResponse;

public class LoginHandler extends GenericHandler<LoginRequest, LoginResponse, LoginService> {
    @Override
    protected Class<LoginRequest> getBoundRequestClass() {
        return LoginRequest.class;
    }

    @Override
    protected LoginService createBoundService() {
        return new LoginService();
    }
}
