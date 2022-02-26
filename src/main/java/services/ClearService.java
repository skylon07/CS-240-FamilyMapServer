package services;

import dataAccess.Database;
import dataAccess.DatabaseException;

import services.requests.ClearRequest;
import services.responses.ClearResponse;

import utils.BulkUtils;

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
    public ClearResponse onPost(ClearRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // clear data from the database
        BulkUtils bulkUtils = new BulkUtils(database);
        bulkUtils.clearDatabase();

        // generate the response
        return this.createSuccessfulResponse();
    }

    /**
     * Creates a successful ClearResponse
     * 
     * @return the successful ClearResponse
     */
    private ClearResponse createSuccessfulResponse() {
        ClearResponse response = new ClearResponse();
        response.success = true;
        response.message = "Clear succeeded.";
        return response;
    }

    @Override
    protected ClearResponse createSpecificErrorResponse(String errMsg) {
        return new ClearResponse();
    }
}
