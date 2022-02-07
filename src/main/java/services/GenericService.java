package services;

/**
 * A definition of "required" methods for all services. It also contains helpful
 * utility functions that are shared across many types of services.
 */
abstract class GenericService<RequestType, ResponseType> {
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
     * Runs when a GET request is sent to the service
     * 
     * @param request is the request object from the GET request
     * @return the response object to send back to the user
     * @throws InvalidHTTPMethodException if the subclassed service does not implement this method type
     */
    public ResponseType onGet(RequestType request) throws InvalidHTTPMethodException {
        this.throwInvalidHTTPMethod("GET");
        // never reached, but needed to avoid errors
        return null;
    }

    /**
     * Runs when a POST request is sent to the service
     * 
     * @param request is the request object from the POST request
     * @return the response object to send back to the user
     * @throws InvalidHTTPMethodException if the subclassed service does not implement this method type
     */
    public ResponseType onPost(RequestType request) throws InvalidHTTPMethodException {
        this.throwInvalidHTTPMethod("POST");
        // never reached, but needed to avoid errors
        return null;
    }

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
