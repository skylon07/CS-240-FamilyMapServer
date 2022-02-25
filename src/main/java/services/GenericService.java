package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
import services.requests.GenericRequest;
import services.responses.GenericResponse;

/**
 * A definition of "required" methods for all services. It also contains helpful
 * utility functions that are shared across many types of services.
 */
public abstract class GenericService<
    RequestType extends GenericRequest,
    ResponseType extends GenericResponse
> {
    /** A string to help identify the service (primarily used for error messages) */
    private String serviceName;

    /**
     * Runs for all generic service types. This sets the service name
     * for the service instance.
     * 
     * @param serviceName is the name of the service (ideally, the name of the class)
     */
    public GenericService(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Main entry point for anything wanting to use the service. This will
     * 
     * @param method is the HTTP method being used
     * @param request is the HTTP request, parsed for the service
     * @return the HTTP response object to send back to the client
     */
    public ResponseType process(String method, RequestType request) {
        assert request != null : "Services cannot have null requests";
        try (Database database = new Database()) {
            ResponseType response;
            if (method.equals("GET")) {
                response = this.onGet(request, database);
            } else if (method.equals("POST")) {
                response = this.onPost(request, database);
            } else {
                this.throwInvalidHTTPMethod(method);
                response = null; // needed to avoid compiler errors
            }
            if (database.getActiveConnection() != null) {
                database.commit();
            }
            return response;
        } catch (DatabaseException err) {
            return this.processError(err);
        } catch (InvalidHTTPMethodException err) {
            return this.processError(err);
        } catch (Exception err) {
            return this.processError(err);
        }
    }

    /**
     * This is the functionality for handling DatabaseExceptions during processing
     * 
     * @param err is the DatabaseException that occured
     * @return the response indicating a failure
     */
    private ResponseType processError(DatabaseException err) {
        return this.createErrorResponse(err.getMessage());
    }

    /**
     * This is the functionality for handling InvalidHTTPMethodException during processing
     * 
     * @param err is the InvalidHTTPMethodException that occured
     * @return the response indicating a failure
     */
    private ResponseType processError(InvalidHTTPMethodException err) {
        return this.createErrorResponse(err.getMessage());
    }

    /**
     * This is the functionality for handling generic Exception during processing
     * 
     * @param err is the generic Exception that occured
     * @return the response indicating a failure
     */
    private ResponseType processError(Exception err) {
        return this.createErrorResponse(err.getMessage());
    }

    private ResponseType createErrorResponse(String errMsg) {
        ResponseType response = this.createSpecificErrorResponse(errMsg);
        // success field is boolean; it will either initialize to false,
        // or be explicitly set false
        // either way, we don't need to set it false here (and we don't want
        // to override it if it is true), so don't do anything
        if (response.message == null || response.message == "") {
            response.message = errMsg;
        }
        return response;
    }

    /**
     * Overridden function that is called when a GET request is sent to the service
     * 
     * @param request is the request object from the GET request
     * @param database is the active database to get/insert data with
     * @return the response object to send back to the user
     * @throws InvalidHTTPMethodException if the subclassed service does not implement this method type
     */
    public ResponseType onGet(RequestType request, Database database) throws InvalidHTTPMethodException {
        this.throwInvalidHTTPMethod("GET");
        // never reached, but needed to avoid errors
        return null;
    }

    /**
     * Overridden function that is called when a POST request is sent to the service
     * 
     * @param request is the request object from the POST request
     * @param database is the active database to get/insert data with
     * @return the response object to send back to the user
     * @throws InvalidHTTPMethodException if the subclassed service does not implement this method type
     */
    public ResponseType onPost(RequestType request, Database database) throws InvalidHTTPMethodException {
        this.throwInvalidHTTPMethod("POST");
        // never reached, but needed to avoid errors
        return null;
    }

    /**
     * Overridden function that converts an error message into the specific response type.
     * Setting the message and success fields is not required.
     * 
     * @param errMsg is the error message from the error that occurred
     * @return the response
     */
    protected abstract ResponseType createSpecificErrorResponse(String errMsg);

    /**
     * Helper function that throws an InvalidHTTPMethodException for invalid/unimplemented HTTP method
     * 
     * @param method is the HTTP method that the service failed with
     * @throws InvalidHTTPMethodException always
     */
    protected void throwInvalidHTTPMethod(String method) throws InvalidHTTPMethodException {
        throw new InvalidHTTPMethodException(this.serviceName, method);
    }
}
