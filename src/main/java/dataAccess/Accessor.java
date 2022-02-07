package dataAccess;

public abstract class Accessor<ModelType> {
    protected Database database;

    /**
     * Creation process for all Accessors
     */
    public Accessor() {
        this.database = new Database();
    }

    /**
     * Takes a model and stores it in the database
     * 
     * @param model is the model to insert into the database
     * @throws AccessException when the model is already a row in a table
     */
    public abstract void create(ModelType model) throws AccessException;
    
    /**
     * Takes a model and removes it from the database
     * 
     * @param model is the model to delete from the database
     * @throws AccessException when the model is not present in the database
     */
    public abstract void delete(ModelType model) throws AccessException;

    /**
     * Takes a model (that exists in the database) and updates its corresponding row
     * 
     * @param model is the model to update
     * @throws AccessException when the model doesn't exist
     */
    public abstract void update(ModelType model) throws AccessException;

    /**
     * Takes a model and checks if it is present in the database tables
     * 
     * @param model is the model to check for
     * @return whether the model exists or not
     */
    public abstract boolean exists(ModelType model);
}
