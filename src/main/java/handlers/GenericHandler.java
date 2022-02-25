package handlers;

import java.io.*;
import java.net.HttpURLConnection;

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
        System.out.println(
            "Handling endpoint " + exchange.getRequestURI().toString() + "\n" + 
            "    with " + this.getClass().getName()
        );
        RequestType request = this.parseRequest(exchange);
        
        String method = exchange.getRequestMethod();
        ServiceType service = this.createBoundService();
        ResponseType response = service.process(method, request);

        int statusCode;
        String responseBodyStr;
        if (response == null) {
            statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
            responseBodyStr = this.generateInternalErrorResponse("Service " + service.getClass().getName() + " returned null response");
        } else {
            statusCode = this.getStatusCode(response);
            responseBodyStr = this.convertResponse(response);
            if (responseBodyStr == null) {
                if (statusCode == 0) {
                    statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
                    responseBodyStr = this.generateInternalErrorResponse("Handler " + this.getClass().getName() + " returned a null converted response (it seems unimplemented...)");
                } else {
                    responseBodyStr = this.generateInternalErrorResponse("Handler " + this.getClass().getName() + " returned a null converted response");
                }
            }
        }
        exchange.sendResponseHeaders(statusCode, 0);
        OutputStream responseBody = exchange.getResponseBody();
        OutputStreamWriter responseBodyWriter = new OutputStreamWriter(responseBody);
        responseBodyWriter.write(responseBodyStr);
        responseBodyWriter.close();
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

    /**
     * Gets the status code from a particular response state
     * 
     * @param response is the response returned by the service
     * @return the status code to tie to the response
     */
    protected abstract int getStatusCode(ResponseType response);

    /**
     * Converts the response into a string to send back to the client
     * 
     * @param response is the response to convert
     * @return a response string, usually just the JSON representation of the response
     */
    protected abstract String convertResponse(ResponseType response);

    protected void toResponseJSON(ResponseType obj, OutputStream stream) {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        Gson gson = new Gson();
        gson.toJson(writer);
    }

    protected String toResponseJSON(ResponseType response) {
        Gson gson = new Gson();
        return gson.toJson(response);
    }

    protected RequestType fromRequestJSON(InputStream stream, Class<RequestType> requestClass) {
        InputStreamReader reader = new InputStreamReader(stream);
        Gson gson = new Gson();
        return (RequestType) gson.fromJson(reader, requestClass);
    }

    private String generateInternalErrorResponse(String message) {
        return "{\"message\":\"" + message + "\",\"success\":false}";
    }
}
