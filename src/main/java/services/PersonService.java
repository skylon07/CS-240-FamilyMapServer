package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.PersonAccessor;
import models.Person;
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
    public PersonResponse onGet(PersonRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // determine branch
        if (request.all) {
            // get all persons
            PersonAccessor personAcc = new PersonAccessor(database);
            Person[] allPersons = personAcc.getAll();
            
            // generate response
            return this.createSuccessfulAllResponse(allPersons);
        } else {
            // get specific person
            PersonAccessor personAcc = new PersonAccessor(database);
            Person matchingPerson = personAcc.getByID(request.personID);

            // generate response
            return this.createSuccessfulSingleResponse(matchingPerson);
        }
    }

    private PersonResponse createSuccessfulAllResponse(Person[] allPersons) {
        PersonResponse response = new PersonResponse();
        response.success = true;
        response.data = allPersons;
        return response;
    }

    private PersonResponse createSuccessfulSingleResponse(Person matchingPerson) {
        PersonResponse response = new PersonResponse();
        response.success = true;
        // I feel like there's gotta be a better way to do this...
        // Like serializing the Person() object itself...
        // But whatever! Specs are specs
        response.personID =             matchingPerson.getPersonID();
        response.associatedUsername =   matchingPerson.getAssociatedUsername();
        response.firstName =            matchingPerson.getFirstName();
        response.lastName =             matchingPerson.getLastName();
        response.gender =               matchingPerson.getFirstName();
        response.fatherID =             matchingPerson.getFatherID();
        response.motherID =             matchingPerson.getMotherID();
        response.spouseID =             matchingPerson.getSpouseID();
        return response;
    }

    @Override
    protected PersonResponse createSpecificErrorResponse(String errMsg) {
        return new PersonResponse();
    }
}
