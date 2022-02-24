package handlers;

import com.sun.net.httpserver.*;

import services.RegisterService;
import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

public class RegisterHandler extends GenericHandler<RegisterRequest, RegisterResponse, RegisterService> {
    @Override
    protected RegisterRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected RegisterService createBoundService() {
        return new RegisterService();
    }
}
