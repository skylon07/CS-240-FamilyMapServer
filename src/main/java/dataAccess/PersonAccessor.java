package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.Person;

/**
 * A collection of methods that give access to Person models in the database.
 * It can create, delete, update, and find Persons using a variety of methods.
 */
public class PersonAccessor extends Accessor<Person> {
    /**
     * Returns the Person matching a Person ID
     * 
     * @param personID is the Person ID to query by
     * @return the corresponding Person, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public Person getByID(String personID) throws DatabaseException {
        // TODO
        return null;
    }

    /**
     * Returns all Persons in the database
     * 
     * @return an array of all Persons in the database
     * @throws DatabaseException when a database error occurs
     */
    public Person[] getAll() throws DatabaseException {
        // TODO
        return null;
    }
    
    @Override
    public void create(Person[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(Person[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(Person[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] exists(Person[] models) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() throws DatabaseException {
        // TODO Auto-generated method stub   
    }

    @Override
    protected Person mapQueryResult(ResultSet result) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
