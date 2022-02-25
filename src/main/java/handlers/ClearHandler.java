package handlers;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.ClearService;
import services.requests.ClearRequest;
import services.responses.ClearResponse;

public class ClearHandler extends GenericHandler<ClearRequest, ClearResponse, ClearService> {
    @Override
    protected ClearRequest parseRequest(HttpExchange exchange) {
        ClearRequest request = new ClearRequest();
        return request;
    }

    @Override
    protected ClearService createBoundService() {
        return new ClearService();
    }

    @Override
    protected int getStatusCode(ClearResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(ClearResponse response) {
        return this.toResponseJSON(response);
    }
}
