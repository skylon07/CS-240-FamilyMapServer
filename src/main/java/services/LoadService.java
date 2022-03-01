package services;

import dataAccess.BadAccessException;
import dataAccess.Database;
import dataAccess.DatabaseException;

import models.*;

import services.requests.LoadRequest;
import services.responses.LoadResponse;
import utils.BulkUtils;

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
    public LoadResponse onPost(LoadRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        User[] users = request.users;
        Person[] persons = request.persons;
        Event[] events = request.events;

        // clear all data from the database
        BulkUtils bulkUtils = new BulkUtils(database);
        bulkUtils.clearDatabase();

        // load data into the database
        try {
            AuthToken[] authTokens = {};
            bulkUtils.loadIntoDatabase(users, persons, events, authTokens);
        } catch (BadAccessException err) {
            throw new AssertionError("BulkUtils did not clear the database before loading");
        }

        // generate a response
        return this.createSuccessfulResponse(users.length, persons.length, events.length);
    }

    /**
     * Creates a successful LoadResponse with required parameters
     * 
     * @param numUsersCreated is the number of Users created
     * @param numPeopleCreated is the number of Persons created
     * @param numEventsCreated is the number of Events created
     * @return the successful LoadResponse
     */
    private LoadResponse createSuccessfulResponse(int numUsersCreated, int numPeopleCreated, int numEventsCreated) {
        LoadResponse response = new LoadResponse();
        response.success = true;
        response.message = String.format(
            "Successfully added %d users, %d persons, and %d events to the database.",
            numUsersCreated, numPeopleCreated, numEventsCreated
        );
        return response;
    }

    @Override
    protected LoadResponse createSpecificErrorResponse(String errMsg) {
        return new LoadResponse();
    }
}
