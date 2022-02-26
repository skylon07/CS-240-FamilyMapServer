package handlers;

import java.io.InputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.LoadService;
import services.requests.LoadRequest;
import services.responses.LoadResponse;

public class LoadHandler extends GenericHandler<LoadRequest, LoadResponse, LoadService> {
    @Override
    protected LoadRequest parseRequest(HttpExchange exchange) {
        InputStream stream = exchange.getRequestBody();
        LoadRequest request = this.fromRequestJSON(stream, LoadRequest.class);
        return request;
    }

    @Override
    protected LoadService createBoundService() {
        return new LoadService();
    }

    @Override
    protected int getStatusCode(LoadResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(LoadResponse response) {
        return this.toResponseJSON(response);
    }
}
