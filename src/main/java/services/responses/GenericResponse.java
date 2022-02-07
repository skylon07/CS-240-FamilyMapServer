package services.responses;

/**
 * The generic class for all responses. Each response by default has "data",
 * "message", and "success" properties.
 */
abstract class GenericResponse {
    /** The response's associated message. For errors, the error message. */
    private String message;
    /** The state of a request's success or failure */
    private boolean success;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
