package handlers;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.FillService;
import services.requests.FillRequest;
import services.responses.FillResponse;

public class FillHandler extends GenericHandler<FillRequest, FillResponse, FillService> {
    @Override
    protected FillRequest parseRequest(HttpExchange exchange) {
        // url parts: / fill (req) / username (req) / generations (opt)
        String url = exchange.getRequestURI().toString();
        String[] urlParts = url.split("/");
        String username = null, generations = null;
        for (int partIdx = 0; partIdx < urlParts.length; ++partIdx) {
            // partIdx == 0 -> ""; ignore it
            // partIdx == 1 -> "fill"; ignore it
            if (partIdx == 2) {
                username = urlParts[partIdx];
            } else if (partIdx == 3) {
                generations = urlParts[partIdx];
            }
        }

        FillRequest request = new FillRequest();
        request.username = username;
        request.generations = Integer.parseInt(generations);
        return request;
    }

    @Override
    protected FillService createBoundService() {
        return new FillService();
    }

    @Override
    protected int getStatusCode(FillResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(FillResponse response) {
        return this.toResponseJSON(response);
    }
}
