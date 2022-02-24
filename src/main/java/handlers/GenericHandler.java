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
        this.doHandle(exchange);
    }

    /**
     * This function only exists to allow type templates without compiler errors
     * 
     * @param <RequestType> is the specific Request class used
     * @param <ResponseType> is the specific Response class used
     * @param exchange is the HttpExchange given to handle()
     * @throws IOException
     */
    public void doHandle(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String requestJSON = this.readFromInputStream(requestBody);
        Class<RequestType> requestClass = this.getBoundRequestClass();
        RequestType request = this.fromJSON(requestJSON, requestClass);
        
        String method = exchange.getRequestMethod();
        ServiceType service = this.createBoundService();
        ResponseType response = service.process(method, request);

        String responseJSON = this.toJSON(response);
        OutputStream responseBody = exchange.getResponseBody();
        this.writeToOutputStream(responseJSON, responseBody);
        responseBody.close();
    }

    /**
     * Returns the Response.class bound to the specific handler
     * 
     * @param <RequestType> is the specific Request type
     * @return the class reference for the Request type
     */
    protected abstract Class<RequestType> getBoundRequestClass();

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
     * Reads a string from some input stream, like a request body
     * 
     * @param str is the string to write
     * @param stream is the output stream to write to
     * @throws IOException whenever the output stream can't be written to
     */
    protected String readFromInputStream(InputStream stream) throws IOException {
        StringBuilder str = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        int numCharsAdded;
        while ((numCharsAdded = reader.read(buffer)) > 0) {
            str.append(buffer, 0, numCharsAdded);
        }
        return str.toString();
    }

    /**
     * Writes a string to some output stream, like a response body
     * 
     * @param str is the string to write
     * @param stream is the output stream to write to
     * @throws IOException whenever the output stream can't be written to
     */
    protected void writeToOutputStream(String str, OutputStream stream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(str);
        writer.flush();
    }

    protected <ObjType> String toJSON(ObjType obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    protected <ObjType> ObjType fromJSON(String objJson, Class<ObjType> objClass) {
        Gson gson = new Gson();
        return (ObjType) gson.fromJson(objJson, objClass);
    }
}
