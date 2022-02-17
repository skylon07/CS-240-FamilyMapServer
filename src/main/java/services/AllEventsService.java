package services;

import dataAccess.Database;

import services.requests.AllEventsRequest;
import services.responses.AllEventsResponse;

/**
 * This service provides functionality for the events listing endpoint.
 * It accepts calls through the HTTP GET method.
 */
public class AllEventsService extends GenericService<AllEventsRequest, AllEventsResponse> {
    /**
     * Creates a new AllEventsService by calling GenericService with this
     * service's name
     */
    public AllEventsService() {
        super("AllEventsService");
    }
    
    @Override
    public AllEventsResponse onGet(AllEventsRequest request, Database database) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
