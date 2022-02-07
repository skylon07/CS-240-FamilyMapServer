package dataAccess;

public class BadAccessException extends Exception {
    public BadAccessException(String errMsg) {
        super(errMsg);
    }
}
