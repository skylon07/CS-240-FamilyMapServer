package services;

import dataAccess.BadAccessException;
import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.UserAccessor;

import models.User;

import services.requests.FillRequest;
import services.responses.FillResponse;

import utils.BulkUtils;
import utils.FamilyTreeUtils;

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
        String username = request.username;
        int generations = request.generations;

        // clear data for the user
        UserAccessor userAcc = new UserAccessor(database); 
        User user = userAcc.getByUsername(username);
        BulkUtils bulkUtils = new BulkUtils(database);
        try {
            bulkUtils.deleteUsersAssociatedData(user);
        } catch (BadAccessException err) {
            throw new AssertionError("User Accessor returned a user that doesn't exist (which was supposed to)");
        }

        // generate family history data for the user
        FamilyTreeUtils famTreeUtils = new FamilyTreeUtils(database);
        FamilyTreeUtils.GenerationAttempt attempt;
        try {
            if (generations > 0) {
                attempt = famTreeUtils.generateFamilyTree(user, generations);
            } else {
                attempt = famTreeUtils.generateFamilyTree(user);
            }
        } catch (BadAccessException err) {
            throw new AssertionError("BulkUtils did not clear user data properly")  ;
        }
        String personID = user.getPersonID();
        assert personID != null : "FamilyTreeUtils did not generate a personID";

        // generate the response
        return this.createSuccessfulResponse(attempt.getNumPersonsCreated(), attempt.getNumEventsCreated());
    }

    private FillResponse createSuccessfulResponse(int numPersonsCreated, int numEventsCreated) {
        FillResponse response = new FillResponse();
        response.success = true;
        response.message = String.format("Successfully added %d persons and %d events to the database.", numPersonsCreated, numEventsCreated);
        return response;
    }

    @Override
    protected FillResponse createSpecificErrorResponse(String errMsg) {
        return new FillResponse();
    }
}
