package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
import services.requests.FillRequest;
import services.responses.FillResponse;

/**
 * This service provides functionality for the data fill endpoint.
 * It accepts calls through the HTTP POST method.
 */
public class FillService extends GenericService<FillRequest, FillResponse> {
    /**
     * Creates a new FillService by calling GenericService with this
     * service's name
     */
    public FillService() {
        super("FillService");
    }

    @Override
    public FillResponse onPost(FillRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FillResponse createSpecificErrorResponse(String errMsg) {
        // TODO Auto-generated method stub
        return null;
    }
}
