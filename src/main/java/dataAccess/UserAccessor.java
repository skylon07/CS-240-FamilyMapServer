package dataAccess;

import models.User;

public class UserAccessor extends Accessor<User> {
    /**
     * Takes a User model and stores it in the database
     * 
     * @param model is the User model to insert into the database
     * @throws AccessException when the User model is already a row in a table
     */
    @Override
    public void create(User model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a User model and removes it from the database
     * 
     * @param model is the User model to delete from the database
     * @throws AccessException when the User model is not present in the database
     */
    @Override
    public void delete(User model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a User model (that exists in the database) and updates its corresponding row
     * 
     * @param model is the User model to update
     * @throws AccessException when the User model doesn't exist
     */
    @Override
    public void update(User model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a User model and checks if it is present in the database tables
     * 
     * @param model is the User model to check for
     * @return whether the User model exists or not
     */
    @Override
    public boolean exists(User model) {
        // TODO Auto-generated method stub
        return false;
    }
}
