package services;

import services.requests.LoadRequest;
import services.responses.LoadResponse;

/**
 * This service provides functionality for the loading endpoint.
 * It accepts calls through the HTTP POST method.
 */
public class LoadService extends GenericService<LoadRequest, LoadResponse> {
    /**
     * Creates a new LoadService by calling GenericService with this
     * service's name
     */
    public LoadService() {
        super("LoadService");
    }

    @Override
    public LoadResponse onPost(LoadRequest request) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
