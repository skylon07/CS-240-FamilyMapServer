package services.responses;

public class ClearResponse extends GenericResponse {
    private String message;
    private boolean success;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
