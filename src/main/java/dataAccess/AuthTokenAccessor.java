package dataAccess;

import models.AuthToken;

public class AuthTokenAccessor extends Accessor<AuthToken> {
    /**
     * Takes an AuthToken model and stores it in the database
     * 
     * @param model is the AuthToken model to insert into the database
     * @throws AccessException when the AuthToken model is already present
     */
    @Override
     public void create(AuthToken model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an AuthToken model and removes it from the database
     * 
     * @param model is the AuthToken model to delete from the database
     * @throws AccessException when the AuthToken model is not present in the database
     */
    @Override
    public void delete(AuthToken model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an AuthToken model (that exists in the database) and updates its corresponding row
     * 
     * @param model is the AuthToken model to update
     * @throws AccessException when the AuthToken model doesn't exist
     */
    @Override
    public void update(AuthToken model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an AuthToken model and checks if it is present in the database tables
     * 
     * @param model is the AuthToken model to check for
     * @return whether the AuthToken model exists or not
     */
    @Override
    public boolean exists(AuthToken model) {
        // TODO Auto-generated method stub
        return false;
    }
}
