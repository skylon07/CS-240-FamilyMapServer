package services;

import services.requests.AllPersonsRequest;
import services.responses.AllPersonsResponse;

/**
 * This service provides functionality for the Persons listing endpoint.
 * It accepts calls through the HTTP GET method.
 */
public class AllPersonsService extends GenericService<AllPersonsRequest, AllPersonsResponse> {
    /**
     * Creates a new AllPersonsService by calling GenericService with this
     * service's name
     */
    public AllPersonsService() {
        super("AllPersonsService");
    }

    @Override
    public AllPersonsResponse onGet(AllPersonsRequest request) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
