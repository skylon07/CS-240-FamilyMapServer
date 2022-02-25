package services;

import dataAccess.Database;

import services.requests.ClearRequest;
import services.responses.ClearResponse;

/**
 * This service provides functionality for the database clearing endpoint.
 * It accepts calls through the HTTP POST method.
 */
public class ClearService extends GenericService<ClearRequest, ClearResponse> {
    /**
     * Creates a new ClearService by calling GenericService with this
     * service's name
     */
    public ClearService() {
        super("ClearService");
    }

    @Override
    public ClearResponse onPost(ClearRequest request, Database database) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ClearResponse createSpecificErrorResponse(String errMsg) {
        // TODO Auto-generated method stub
        return null;
    }
}
