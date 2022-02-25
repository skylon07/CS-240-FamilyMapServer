package handlers;

import java.io.InputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.RegisterService;
import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

public class RegisterHandler extends GenericHandler<RegisterRequest, RegisterResponse, RegisterService> {
    @Override
    protected RegisterRequest parseRequest(HttpExchange exchange) {
        InputStream requestBody = exchange.getRequestBody();
        RegisterRequest request = this.fromRequestJSON(requestBody, RegisterRequest.class);
        return request;
    }

    @Override
    protected RegisterService createBoundService() {
        return new RegisterService();
    }

    @Override
    protected int getStatusCode(RegisterResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(RegisterResponse response) {
        return this.toResponseJSON(response);
    }
}
