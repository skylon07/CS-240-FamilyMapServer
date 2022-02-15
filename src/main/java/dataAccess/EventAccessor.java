package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.Event;

/**
 * A collection of methods that give access to Event models in the database.
 * It can create, delete, update, and find Events using a variety of methods.
 */
public class EventAccessor extends Accessor<Event> {
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

    @Override
    public void create(Event[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(Event[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(Event[] models) throws BadAccessException, DatabaseException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] exists(Event[] models) throws DatabaseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() throws DatabaseException {
        // TODO Auto-generated method stub   
    }

    @Override
    protected Event mapQueryResult(ResultSet result) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
