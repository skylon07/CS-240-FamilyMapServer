package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A generic Accessor class that defines the base for all Accessors. It guarantees
 * basic database manupulation operations for a given model type, however subclasses
 * should implement more functions as needed (ex. database query functions)
 */
public abstract class Accessor<ModelType> {
    /** The database wrapper, allowing Accessors to make calls to the Database */
    protected Database database;

    /**
     * Creation process for all Accessors
     * 
     * @param database is the database the Accessor should use
     */
    public Accessor(Database database) {
        this.database = database;
    }

    /**
     * Takes a list of models and stores them in the database.
     * Changes must be committed or rolled back after calling this function.
     * 
     * @param models is the list of models to insert into the database
     * @throws BadAccessException when a model is already a row in a table
     * @throws DatabaseException when another database error occurs
     */
    public abstract void create(ModelType[] models) throws BadAccessException, DatabaseException;
    
    /**
     * Takes a list of models and removes them from the database
     * * Changes must be committed or rolled back after calling this function.
     * 
     * @param models is the list of models to delete from the database
     * @throws BadAccessException when a model is not present in the database
     * @throws DatabaseException when another database error occurs
     */
    public abstract void delete(ModelType[] models) throws BadAccessException, DatabaseException;

    /**
     * Takes a list of models (that exist in the database) and updates their
     * corresponding rows, according to their primary keys (ie primary keys
     * cannot be changed using this method).
     * * Changes must be committed or rolled back after calling this function.
     * 
     * @param models is the list of models to update
     * @throws BadAccessException when a model doesn't exist
     * @throws DatabaseException when another database error occurs
     */
    public abstract void update(ModelType[] models) throws BadAccessException, DatabaseException;

    /**
     * Takes a list of models and checks if they are present in the database tables
     * 
     * @param models is the list of models to check for
     * @return whether the models each exist or not
     * @throws DatabaseException when another database error occurs
     */
    public abstract boolean[] exists(ModelType[] models) throws DatabaseException;

    /**
     * Clears the associated table in the database
     * 
     * @throws DatabaseException when a database error occurs
     */
    public abstract void clear() throws DatabaseException;

    /**
     * Mapping function to use for Database.query() calls
     * 
     * @param result is the ResultSet passed by query()
     * @return the User given by the result
     * @throws SQLException when the result fails to get data
     */
    protected abstract ModelType mapQueryResult(ResultSet result) throws SQLException;
}
