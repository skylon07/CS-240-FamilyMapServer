package dataAccess;

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
     */
    public Accessor() {
        this.database = new Database();
    }

    /**
     * Takes a list of models and stores them in the database
     * 
     * @param models is the list of models to insert into the database
     * @throws BadAccessException when a model is already a row in a table
     * @throws DatabaseException when another database error occurs
     */
    public abstract void create(ModelType[] models) throws BadAccessException, DatabaseException;
    
    /**
     * Takes a list of models and removes them from the database
     * 
     * @param models is the list of models to delete from the database
     * @throws BadAccessException when a model is not present in the database
     * @throws DatabaseException when another database error occurs
     */
    public abstract void delete(ModelType[] models) throws BadAccessException, DatabaseException;

    /**
     * Takes a list of models (that exist in the database) and updates their corresponding rows
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
     * @return whether the models exists or not
     * @throws DatabaseException when another database error occurs
     */
    public abstract boolean[] exists(ModelType[] models) throws DatabaseException;
}
