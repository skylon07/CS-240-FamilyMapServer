package handlers;

import com.sun.net.httpserver.*;

import services.PersonService;
import services.requests.PersonRequest;
import services.responses.PersonResponse;

public class PersonHandler extends GenericHandler<PersonRequest, PersonResponse, PersonService> {
    @Override
    protected PersonRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected PersonService createBoundService() {
        return new PersonService();
    }
}
