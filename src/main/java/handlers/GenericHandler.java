package handlers;

import java.io.*;

import com.google.gson.Gson;
import com.sun.net.httpserver.*;

import services.GenericService;
import services.requests.GenericRequest;
import services.responses.GenericResponse;

public abstract class GenericHandler<
    RequestType extends GenericRequest,
    ResponseType extends GenericResponse,
    ServiceType extends GenericService<RequestType, ResponseType>
> implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestType request = this.parseRequest(exchange);
        
        String method = exchange.getRequestMethod();
        ServiceType service = this.createBoundService();
        ResponseType response = service.process(method, request);

        OutputStream responseBody = exchange.getResponseBody();
        this.toResponseJSON(response, responseBody);
        responseBody.close();
    }

    /**
     * Parses the specific request from the HttpExchange
     * 
     * @param exchange is the HttpExchange the request is stored in
     * @return the parsed Request object
     */
    protected abstract RequestType parseRequest(HttpExchange exchange);

    /**
     * Calls the Service bound to the specific handler
     * 
     * @param <RequestType> is the specific Request type
     * @param <ResponseType> is the specific Response type
     * @param request is the request generated from getBoundRequestClass()
     * @return the Response from the Service after being invoked
     */
    protected abstract ServiceType createBoundService();

    protected void toResponseJSON(ResponseType obj, OutputStream stream) {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        Gson gson = new Gson();
        gson.toJson(writer);
    }

    protected RequestType fromRequestJSON(InputStream stream, Class<RequestType> objClass) {
        InputStreamReader reader = new InputStreamReader(stream);
        Gson gson = new Gson();
        return (RequestType) gson.fromJson(reader, objClass);
    }
}
