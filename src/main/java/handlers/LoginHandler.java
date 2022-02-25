package handlers;

import java.io.InputStream;
import java.net.HttpURLConnection;

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

    @Override
    protected int getStatusCode(LoginResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(LoginResponse response) {
        return this.toResponseJSON(response);
    }
}
