package services;

/**
 * Thrown when a service is called with an invalid/unimplemented HTTP method.
 */
public class InvalidHTTPMethodException extends Exception {
    /**
     * Creates a new InvalidHTTPMethodException knowing the method of the
     * attempted request
     * 
     * @param serviceName is the name of the service that failed
     * @param method is the HTTP method ("GET", "POST", etc) that was used on the service
     */
    public InvalidHTTPMethodException(String serviceName, String method) {
        super("Tried to call service '" + serviceName + "' with an invalid method '" + method + "'");
    }
}
