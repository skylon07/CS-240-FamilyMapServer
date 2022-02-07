package dataAccess;

import models.Event;

public class EventAccessor extends Accessor<Event> {
    /**
     * Takes an Event model and stores it in the database
     * 
     * @param model is the Event model to insert into the database
     * @throws AccessException when the Event model is already a row in a table
     */
    @Override
    public void create(Event model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an Event model and removes it from the database
     * 
     * @param model is the Event model to delete from the database
     * @throws AccessException when the Event model is not present in the database
     */
    @Override
    public void delete(Event model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an Event model (that exists in the database) and updates its corresponding row
     * 
     * @param model is the Event model to update
     * @throws AccessException when the Event model doesn't exist
     */
    @Override
    public void update(Event model) throws AccessException {
        // TODO Auto-generated method stub
    }

    /**
     * Takes an Event model and checks if it is present in the database tables
     * 
     * @param model is the Event model to check for
     * @return whether the Event model exists or not
     */
    @Override
    public boolean exists(Event model) {
        // TODO Auto-generated method stub
        return false;
    }
}
