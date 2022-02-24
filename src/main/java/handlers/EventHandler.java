package handlers;

import com.sun.net.httpserver.*;

import services.EventService;
import services.requests.EventRequest;
import services.responses.EventResponse;

public class EventHandler extends GenericHandler<EventRequest, EventResponse, EventService> {
    @Override
    protected EventRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected EventService createBoundService() {
        return new EventService();
    }
}
