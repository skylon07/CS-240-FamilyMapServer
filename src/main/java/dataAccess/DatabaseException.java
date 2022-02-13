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
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("SQL Error (");
        errorMsg.append(error.getErrorCode());
        errorMsg.append("): ");
        errorMsg.append(error.getMessage());
    }
    
    /**
     * Creates a new DatabaseException given a string
     * 
     * @param errorMsg is the string describing the non-SQL database error
     */
    public DatabaseException(String errorMsg) {
        super(errorMsg);
    }
}
