package handlers;

import java.io.InputStream;

import com.sun.net.httpserver.*;

import services.LoginService;
import services.requests.LoginRequest;
import services.responses.LoginResponse;

public class LoginHandler extends GenericHandler<LoginRequest, LoginResponse, LoginService> {
    @Override
    protected LoginRequest parseRequest(HttpExchange exchange) {
        InputStream requestBody = exchange.getRequestBody();
        LoginRequest request = this.fromRequestJSON(requestBody, LoginRequest.class);
        return request;
    }

    @Override
    protected LoginService createBoundService() {
        return new LoginService();
    }
}
