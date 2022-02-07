package dataAccess;

import java.sql.SQLException;

public class DatabaseException extends Exception {
    public DatabaseException(SQLException error) {
        // TODO: generate message with helpful infos based on error type
    }
}
