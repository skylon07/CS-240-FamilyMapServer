package dataAccess;

public class DatabaseException extends Exception {
    public DatabaseException(String errMsg) {
        super(errMsg);
    }
}
