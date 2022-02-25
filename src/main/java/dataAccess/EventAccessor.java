package dataAccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import models.Event;

/**
 * A collection of methods that give access to Event models in the database.
 * It can create, delete, update, and find Events using a variety of methods.
 */
public class EventAccessor extends Accessor<Event> {
    /**
     * Creates an EventAccessor with a given database
     * 
     * @param database is the database to use
     */
    public EventAccessor(Database database) {
        super(database);
    }

    /**
     * Returns the Event matching an Event ID
     * 
     * @param eventID is the Event ID to query by
     * @return the corresponding Event, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public Event getByID(String eventID) throws DatabaseException {
        String sqlStr = "select * from event where eventID == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, eventID);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<Event> events = this.database.query(statement, (result) -> this.mapQueryResult(result));
        if (events.size() == 0) {
            return null;
        } else if (events.size() == 1) {
            return events.get(0);
        } else {
            // should never happen...
            throw new DatabaseException("Database returned multiple events for one event ID");
        }
    }

    /**
     * Returns all Events in the database
     * 
     * @return an array of all Events in the database
     * @throws DatabaseException when a database error occurs
     */
    public Event[] getAll() throws DatabaseException {
        String sqlStr = "select * from event";
        ArrayList<Event> events = this.database.query(sqlStr, (result) -> this.mapQueryResult(result));
        return events.toArray(new Event[events.size()]);
    }

    /**
     * Returns all Events in the database that belong to a user
     * 
     * @param username is the username of the user whose events should be returned
     * @return an array of events belonging to the user
     * @throws DatabaseException when a database error occurs
     */
    public Event[] getAllForUser(String username) throws DatabaseException {
        String sqlStr = "select * from event where associatedUsername == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, username);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<Event> events = this.database.query(statement, (result) -> this.mapQueryResult(result));
        return events.toArray(new Event[events.size()]);
    }

    @Override
    public void create(Event[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> usedEventIDs = new ArrayList<>();
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            boolean exists = existingModels[eventIdx];
            if (exists) {
                Event event = models[eventIdx];
                usedEventIDs.add(event.getEventID());
            }
        }
        if (usedEventIDs.size() > 0) {
            String errMsg = "Cannot create events; some event IDs were already used: ";
            for (String eventID : usedEventIDs) {
                errMsg += "'" + eventID + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("insert into event\n");
        sqlStr.append("   (eventID, associatedUsername, personID, latitude, longitude, country, city, eventType, year)\n");
        sqlStr.append("values\n");
        boolean firstEvent = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstEvent) {
                sqlStr.append(", ");
            }
            sqlStr.append("(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            firstEvent = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            Event event = models[eventIdx];
            
            int numFields = 9;
            int eventIDIdx              = eventIdx * numFields + 1;
            int associatedUsernameIdx   = eventIdx * numFields + 2;
            int personIDIdx             = eventIdx * numFields + 3;
            int latitudeIdx             = eventIdx * numFields + 4;
            int longitudeIdx            = eventIdx * numFields + 5;
            int countryIdx              = eventIdx * numFields + 6;
            int cityIdx                 = eventIdx * numFields + 7;
            int eventTypeIdx            = eventIdx * numFields + 8;
            int yearIdx                 = eventIdx * numFields + 9;

            try {
                statement.setString(eventIDIdx,             event.getEventID());
                statement.setString(associatedUsernameIdx,  event.getAssociatedUsername());
                statement.setString(personIDIdx,            event.getPersonID());
                statement.setFloat(latitudeIdx,             event.getLatitude());
                statement.setFloat(longitudeIdx,            event.getLongitude());
                statement.setString(countryIdx,             event.getCountry());
                statement.setString(cityIdx,                event.getCity());
                statement.setString(eventTypeIdx,           event.getEventType());
                statement.setInt(yearIdx,                   event.getYear());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public void delete(Event[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistEventIDs = new ArrayList<>();
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            boolean exists = existingModels[eventIdx];
            if (!exists) {
                Event event = models[eventIdx];
                nonExistEventIDs.add(event.getEventID());
            }
        }
        if (nonExistEventIDs.size() > 0) {
            String errMsg = "Cannot delete events; some event IDs did not exist: ";
            for (String eventID : nonExistEventIDs) {
                errMsg += "'" + eventID + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("delete from event where\n");
        boolean firstEvent = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstEvent) {
                sqlStr.append(" or ");
            }
            sqlStr.append("eventID == ?");
            firstEvent = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            Event event = models[eventIdx];
            
            int numFields = 1;
            int eventIDIdx = eventIdx * numFields + 1;

            try {
                statement.setString(eventIDIdx, event.getEventID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public void update(Event[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistEventIDs = new ArrayList<>();
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            boolean exists = existingModels[eventIdx];
            if (!exists) {
                Event event = models[eventIdx];
                nonExistEventIDs.add(event.getEventID());
            }
        }
        if (nonExistEventIDs.size() > 0) {
            String errMsg = "Cannot update events; some event IDs did not exist: ";
            for (String eventID : nonExistEventIDs) {
                errMsg += "'" + eventID + "' ";
            }
            throw new BadAccessException(errMsg);
        }
        
        StringBuilder associatedUsernameStr = new StringBuilder();
        associatedUsernameStr.append("associatedUsername = case\n");
        StringBuilder personIDStr = new StringBuilder();
        personIDStr.append("personID = case\n");
        StringBuilder latitudeStr = new StringBuilder();
        latitudeStr.append("latitude = case\n");
        StringBuilder longitudeStr = new StringBuilder();
        longitudeStr.append("longitude = case\n");
        StringBuilder countryStr = new StringBuilder();
        countryStr.append("country = case\n");
        StringBuilder cityStr = new StringBuilder();
        cityStr.append("city = case\n");
        StringBuilder eventTypeStr = new StringBuilder();
        eventTypeStr.append("eventType = case\n");
        StringBuilder yearStr = new StringBuilder();
        yearStr.append("year = case\n");
        StringBuilder whereClauseStr = new StringBuilder();
        whereClauseStr.append("where eventID in (");
        boolean firstEvent = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstEvent) {
                associatedUsernameStr.append("\n");
                personIDStr.append("\n");
                latitudeStr.append("\n");
                longitudeStr.append("\n");
                countryStr.append("\n");
                cityStr.append("\n");
                eventTypeStr.append("\n");
                yearStr.append("\n");
                whereClauseStr.append(", ");
            }
            associatedUsernameStr.append("when eventID == ? then ?");
            personIDStr.append("when eventID == ? then ?");
            latitudeStr.append("when eventID == ? then ?");
            longitudeStr.append("when eventID == ? then ?");
            countryStr.append("when eventID == ? then ?");
            cityStr.append("when eventID == ? then ?");
            eventTypeStr.append("when eventID == ? then ?");
            yearStr.append("when eventID == ? then ?");
            whereClauseStr.append("?");
            firstEvent = false;
        }
        associatedUsernameStr.append("else associatedUsername end,\n");
        personIDStr.append("else personID end,\n");
        latitudeStr.append("else latitude end,\n");
        longitudeStr.append("else longitude end,\n");
        countryStr.append("else country end,\n");
        cityStr.append("else city end,\n");
        eventTypeStr.append("else eventType end,\n");
        yearStr.append("else year end\n");
        whereClauseStr.append(")\n");

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("update event set\n");
        sqlStr.append(associatedUsernameStr.toString());
        sqlStr.append(personIDStr.toString());
        sqlStr.append(latitudeStr.toString());
        sqlStr.append(longitudeStr.toString());
        sqlStr.append(countryStr.toString());
        sqlStr.append(cityStr.toString());
        sqlStr.append(eventTypeStr.toString());
        sqlStr.append(yearStr.toString());
        sqlStr.append(whereClauseStr.toString());

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            Event event = models[eventIdx];

            int numFieldsPerWhen = 2; // when eventID == ? then ?
            int numFieldsPerProp            = models.length * numFieldsPerWhen;
            int whenAssociatedUsernameIdx   = 0 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenPersonIDIdx             = 1 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenLatitudeIdx             = 2 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenLongitudeIdx            = 3 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenCountryIdx              = 4 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenCityIdx                 = 5 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenEventTypeIdx            = 6 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whenYearIdx                 = 7 * numFieldsPerProp + eventIdx * numFieldsPerWhen + 1;
            int whereClauseIdx              = 8 * numFieldsPerProp + eventIdx + 1;

            try {
                statement.setString(whenAssociatedUsernameIdx,      event.getEventID());
                statement.setString(whenAssociatedUsernameIdx + 1,  event.getAssociatedUsername());
                statement.setString(whenPersonIDIdx,                event.getEventID());
                statement.setString(whenPersonIDIdx + 1,            event.getPersonID());
                statement.setString(whenLatitudeIdx,                event.getEventID());
                statement.setFloat(whenLatitudeIdx + 1,             event.getLatitude());
                statement.setString(whenLongitudeIdx,               event.getEventID());
                statement.setFloat(whenLongitudeIdx + 1,            event.getLongitude());
                statement.setString(whenCountryIdx,                 event.getEventID());
                statement.setString(whenCountryIdx + 1,             event.getCountry());
                statement.setString(whenCityIdx,                    event.getEventID());
                statement.setString(whenCityIdx + 1,                event.getCity());
                statement.setString(whenEventTypeIdx,               event.getEventID());
                statement.setString(whenEventTypeIdx + 1,           event.getEventType());
                statement.setString(whenYearIdx,                    event.getEventID());
                statement.setInt(whenYearIdx + 1,                   event.getYear());
                statement.setString(whereClauseIdx,                 event.getEventID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public boolean[] exists(Event[] models) throws DatabaseException {
        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select eventID from event where\n");
        boolean firstEvent = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstEvent) {
                sqlStr.append(" or ");
            }
            sqlStr.append("eventID == ?");
            firstEvent = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            Event event = models[eventIdx];
            
            int numFields = 1;
            int eventIDIdx = eventIdx * numFields + 1;

            try {
                statement.setString(eventIDIdx, event.getEventID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        boolean[] exists = new boolean[models.length];
        for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
            exists[eventIdx] = false;
        }
        ArrayList<String> existingeventIDs = this.database.query(statement, (result) -> result.getString(1));
        for (String eventID : existingeventIDs) {
            int userExistsIdx = -1;
            for (int eventIdx = 0; eventIdx < models.length; ++eventIdx) {
                Event event = models[eventIdx];
                if (event.getEventID().equals(eventID)) {
                    userExistsIdx = eventIdx;
                    break;
                }
            }
            if (userExistsIdx != -1) {
                exists[userExistsIdx] = true;
            }
        }
        return exists;
    }

    @Override
    public void clear() throws DatabaseException {
        String sqlStr = "delete from event";
        this.database.update(sqlStr);
    }

    @Override
    protected Event mapQueryResult(ResultSet result) throws SQLException {
        String eventID = result.getString(1);
        String associatedUsername = result.getString(2);
        String personID = result.getString(3);
        float latitude = result.getFloat(4);
        float longitude = result.getFloat(5);
        String country = result.getString(6);
        String city = result.getString(7);
        String eventType = result.getString(8);
        int year = result.getInt(9);
        Event event = new Event(eventID, associatedUsername, personID, latitude, longitude, country, city, eventType, year);
        return event;
    }
}
