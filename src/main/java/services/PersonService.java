package services;

import dataAccess.Database;

import services.requests.PersonRequest;
import services.responses.PersonResponse;

/**
 * This service provides functionality for the person getter endpoint.
 * It accepts calls through the HTTP GET method.
 */
public class PersonService extends GenericService<PersonRequest, PersonResponse> {
    /**
     * Creates a new PersonService by calling GenericService with this
     * service's name
     */
    public PersonService() {
        super("PersonService");
    }

    @Override
    public PersonResponse onGet(PersonRequest request, Database database) throws InvalidHTTPMethodException {
        // TODO Auto-generated method stub
        return null;
    }
}
