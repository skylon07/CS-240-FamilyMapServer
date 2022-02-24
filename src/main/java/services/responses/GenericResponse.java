package services.responses;

/**
 * The generic class for all responses. Each response by default has "data",
 * "message", and "success" properties.
 */
public abstract class GenericResponse {
    /** The response's associated message. For errors, the error message. */
    public String message;
    /** The state of a request's success or failure */
    public boolean success;
}
