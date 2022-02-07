package dataAccess;

import models.AuthToken;

/**
 * A collection of methods that give access to AuthToken models in the database.
 * It can create, delete, update, and find AuthTokens using a variety of methods.
 */
public class AuthTokenAccessor extends Accessor<AuthToken> {
    @Override
     public void create(AuthToken[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(AuthToken[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(AuthToken[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] exists(AuthToken[] model) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
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
}
