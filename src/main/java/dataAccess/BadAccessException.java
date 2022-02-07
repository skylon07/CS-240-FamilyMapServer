package dataAccess;

/**
 * Provides an exception type for when Accessors are given bad input. Not to be
 * confused with DatabaseExceptions, which are thrown for all SQLExceptions
 */
public class BadAccessException extends Exception {
    /**
     * Creates a new BadAccessException with an error message
     * 
     * @param errMsg is the message to use for the error
     */
    public BadAccessException(String errMsg) {
        super(errMsg);
    }
}
