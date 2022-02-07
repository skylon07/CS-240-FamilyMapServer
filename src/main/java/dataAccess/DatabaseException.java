package dataAccess;

import java.sql.SQLException;

/**
 * A generic exception thrown when the Database encounters a SQLException;
 * DatabaseExceptions should be given the SQLException to store data on why the
 * exception occured.
 */
public class DatabaseException extends Exception {
    /**
     * Creates a new DatabaseException wrapping a given SQLException
     * 
     * @param error is the SQLException that occured
     */
    public DatabaseException(SQLException error) {
        // TODO: generate message with helpful infos based on error type
    }
}
