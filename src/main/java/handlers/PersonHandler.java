package handlers;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.PersonService;
import services.requests.PersonRequest;
import services.responses.PersonResponse;

public class PersonHandler extends GenericHandler<PersonRequest, PersonResponse, PersonService> {
    @Override
    protected PersonRequest parseRequest(HttpExchange exchange) {
        // url parts: / person (req) / personID (opt)
        String url = exchange.getRequestURI().toString();
        String[] urlParts = url.split("/");
        String personID = null;
        for (int partIdx = 0; partIdx < urlParts.length; ++partIdx) {
            // partIdx == 0 -> ""; ignore it
            // partIdx == 1 -> "fill"; ignore it
            if (partIdx == 2) {
                personID = urlParts[partIdx];
            }
        }

        PersonRequest request = new PersonRequest();
        request.authtoken = exchange.getRequestHeaders().getFirst("Authorization");
        if (personID == null) {
            request.all = true;
            request.personID = null;
        } else {
            request.all = false;
            request.personID = personID;
        }
        return request;
    }

    @Override
    protected PersonService createBoundService() {
        return new PersonService();
    }

    @Override
    protected int getStatusCode(PersonResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        // NOPE!!! This breaks the tests...
        // } else if (response.message.matches(".*[Aa]uthorization.*")) {
            // return HttpURLConnection.HTTP_UNAUTHORIZED;
        } else {
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    @Override
    protected String convertResponse(PersonResponse response) {
        return this.toResponseJSON(response);
    }
}
