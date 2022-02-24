package utils;

import dataAccess.Database;

public abstract class GenericUtility {
    /** The database wrapper, allowing Utilities to create Accessors */
    protected Database database;

    /**
     * Creation process for all Utility classes
     * 
     * @param database is the database to use with Accessors
     */
    public GenericUtility(Database database) {
        this.database = database;
    }
}
