package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
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
    public EventResponse onGet(EventRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected EventResponse createSpecificErrorResponse(String errMsg) {
        // TODO Auto-generated method stub
        return null;
    }
}
