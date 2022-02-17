package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.AuthToken;

/**
 * A collection of methods that give access to AuthToken models in the database.
 * It can create, delete, update, and find AuthTokens using a variety of methods.
 */
public class AuthTokenAccessor extends Accessor<AuthToken> {
    /**
     * Creates an AuthTokenAccessor with a given database
     * 
     * @param database is the database to use
     */
    public AuthTokenAccessor(Database database) {
        super(database);
    }

    /**
     * Gets the authentication token tied to a User
     * 
     * @param username is the username of the User to query by
     * @return the AuthToken associated to the user, or null if there is none
     * @throws DatabaseException when a database error occurs
     */
    public AuthToken getByUsername(String username) throws DatabaseException {
        // TODO
        return null;
    }
    
    @Override
     public void create(AuthToken[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(AuthToken[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(AuthToken[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] exists(AuthToken[] models) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() throws DatabaseException {
        // TODO Auto-generated method stub   
    }

    @Override
    protected AuthToken mapQueryResult(ResultSet result) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
