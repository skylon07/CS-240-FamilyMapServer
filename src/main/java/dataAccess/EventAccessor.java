package dataAccess;

import models.Event;

/**
 * A collection of methods that give access to Event models in the database.
 * It can create, delete, update, and find Events using a variety of methods.
 */
public class EventAccessor extends Accessor<Event> {
    @Override
    public void create(Event[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(Event[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(Event[] model) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] exists(Event[] model) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the Event matching an Event ID
     * 
     * @param eventID is the Event ID to query by
     * @return the corresponding Event, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public Event getById(String eventID) throws DatabaseException {
        // TODO
        return null;
    }

    /**
     * Returns all Events in the database
     * 
     * @return an array of all Events in the database
     * @throws DatabaseException when a database error occurs
     */
    public Event[] getAll() throws DatabaseException {
        // TODO
        return null;
    }
}
