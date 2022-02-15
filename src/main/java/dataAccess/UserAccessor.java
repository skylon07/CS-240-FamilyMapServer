package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.User;

/**
 * A collection of methods that give access to User models in the database.
 * It can create, delete, update, and find Users using a variety of methods.
 */
public class UserAccessor extends Accessor<User> {
    /**
     * Returns the User matching a username
     * 
     * @param username is the username to query by
     * @return the corresponding User, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public User getByUsername(String username) throws DatabaseException {
        // TODO
        return null;
    }

    /**
     * Returns the User matching an email
     * 
     * @param email is the email to query by
     * @return the corresponding User, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public User getByEmail(String email) throws DatabaseException {
        // TODO
        return null;
    }

    /**
     * Returns all Users in the database
     * 
     * @return an array of all Users in the database
     * @throws DatabaseException when a database error occurs
     */
    public User[] getAll() throws DatabaseException {
        // TODO
        return null;
    }
    
    @Override
    public void create(User[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(User[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(User[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void clear() throws DatabaseException {
        // TODO Auto-generated method stub   
    }

    @Override
    public boolean[] exists(User[] model) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected User mapQueryResult(ResultSet result) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
