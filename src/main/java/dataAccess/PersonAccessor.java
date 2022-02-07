package dataAccess;

import models.Person;

public class PersonAccessor extends Accessor<Person> {
    /**
     * Takes a Person model and stores it in the database
     * 
     * @param model is the Person model to insert into the database
     * @throws AccessException when the Person model is already a row in a table
     */
    @Override
    public void create(Person model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a Person model and removes it from the database
     * 
     * @param model is the Person model to delete from the database
     * @throws AccessException when the Person model is not present in the database
     */
    @Override
    public void delete(Person model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a Person model (that exists in the database) and updates its corresponding row
     * 
     * @param model is the Person model to update
     * @throws AccessException when the Person model doesn't exist
     */
    @Override
    public void update(Person model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes a Person model and checks if it is present in the database tables
     * 
     * @param model is the Person model to check for
     * @return whether the Person model exists or not
     */
    @Override
    public boolean exists(Person model) {
        // TODO Auto-generated method stub
        return false;
    }
}
