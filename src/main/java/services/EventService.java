package services;

import dataAccess.Database;

import services.requests.EventRequest;
import services.responses.EventResponse;

/**
 * This service provides functionality for the event getter endpoint.
 * It accepts calls through the HTTP GET method.
 */
public class EventService extends GenericService<EventRequest, EventResponse> {
    /**
     * Creates a new EventService by calling GenericService with this
     * service's name
     */
    public EventService() {
        super("EventService");
    }

    @Override
    public EventResponse onGet(EventRequest request, Database database) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
