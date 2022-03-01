package handlers;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.EventService;
import services.requests.EventRequest;
import services.responses.EventResponse;

public class EventHandler extends GenericHandler<EventRequest, EventResponse, EventService> {
    @Override
    protected EventRequest parseRequest(HttpExchange exchange) {
        // url parts: / event (req) / eventID (opt)
        String url = exchange.getRequestURI().toString();
        String[] urlParts = url.split("/");
        String eventID = null;
        for (int partIdx = 0; partIdx < urlParts.length; ++partIdx) {
            // partIdx == 0 -> ""; ignore it
            // partIdx == 1 -> "fill"; ignore it
            if (partIdx == 2) {
                eventID = urlParts[partIdx];
            }
        }

        EventRequest request = new EventRequest();
        request.authtoken = exchange.getRequestHeaders().getFirst("Authorization");
        if (eventID == null) {
            request.all = true;
            request.eventID = null;
        } else {
            request.all = false;
            request.eventID = eventID;
        }
        return request;
    }

    @Override
    protected EventService createBoundService() {
        return new EventService();
    }

    @Override
    protected int getStatusCode(EventResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        // NOPE!!! This breaks the tests...
        // } else if (response.message.matches(".*[Aa]uthorization.*")) {
            // return HttpURLConnection.HTTP_UNAUTHORIZED;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(EventResponse response) {
        return this.toResponseJSON(response);
    }
}
