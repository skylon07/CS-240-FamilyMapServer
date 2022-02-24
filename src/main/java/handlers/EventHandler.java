package handlers;

import services.EventService;
import services.requests.EventRequest;
import services.responses.EventResponse;

public class EventHandler extends GenericHandler<EventRequest, EventResponse, EventService> {
    @Override
    protected Class<EventRequest> getBoundRequestClass() {
        return EventRequest.class;
    }

    @Override
    protected EventService createBoundService() {
        return new EventService();
    }
}
