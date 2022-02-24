package handlers;

import services.PersonService;
import services.requests.PersonRequest;
import services.responses.PersonResponse;

public class PersonHandler extends GenericHandler<PersonRequest, PersonResponse, PersonService> {
    @Override
    protected Class<PersonRequest> getBoundRequestClass() {
        return PersonRequest.class;
    }

    @Override
    protected PersonService createBoundService() {
        return new PersonService();
    }
}
