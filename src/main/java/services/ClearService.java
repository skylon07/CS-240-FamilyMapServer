package services;

import dataAccess.AuthTokenAccessor;
import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.EventAccessor;
import dataAccess.PersonAccessor;
import dataAccess.UserAccessor;
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
    public ClearResponse onPost(ClearRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // clear Users
        UserAccessor userAcc = new UserAccessor(database);
        userAcc.clear();
        
        // clear Persons
        PersonAccessor personAcc = new PersonAccessor(database);
        personAcc.clear();
        
        // clear Events
        EventAccessor eventAcc = new EventAccessor(database);
        eventAcc.clear();

        // clear AuthTokens
        AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(database);
        authTokenAcc.clear();

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
