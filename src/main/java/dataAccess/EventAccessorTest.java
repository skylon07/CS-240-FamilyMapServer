package dataAccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import models.Event;

import java.sql.PreparedStatement;

/**
 * Contains the test cases that ensure the EventAccessor class runs correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventAccessorTest {
    /**
     * Runs fail(), but shows a traceback to the call of this method, instead
     * of where the error was thrown
     * 
     * @param err is the error that was thrown
     */
    private void failNoTraceback(Exception err) {
        fail(err.getMessage());
    }

    /**
     * Signals the Database class to use the testing database for testing
     */
    @BeforeAll
    static public void useTestDB() {
        Database.useTestDB();
    }

    /**
     * Ensures an empty, fresh database before each test (with a base user/person to associate by)
     * 
     * @throws DatabaseException whenever reset() does
     */
    @BeforeEach
    public void resetDatabase() throws DatabaseException {
        try (Database database = new Database()) {
            database.reset();
            String sqlStr =
                "insert into user (username, password, email, firstname, lastname, gender)\n" + 
                "values ('baseUser', 'password', 'base@email.test', 'Base', 'User', 'm'),\n" + 
                "       ('baseUser2', 'password', 'base2@email.test', 'Base2', 'User2', 'm')";
            database.update(sqlStr);
            String sqlStr2 =
                "insert into person (personID, associatedUsername, firstname, lastname, gender)\n" + 
                "values ('basePerson', 'baseUser', 'Base', 'User', 'm'),\n" + 
                "       ('basePerson2', 'baseUser2', 'Base2', 'User2', 'm')";
            database.update(sqlStr2);
            database.commit();
        }
    }

    private boolean floatEquals(double a, float b) {
        return (int) (a * 100) == (int) (b * 100);
    }

    /**
     * Insert arbitrary Events into the database, bypassing EventAccessor.
     * Associated Username property defaults to "baseUser",
     * and personID defaults to basePerson.
     * 
     * @param eventID is the event ID to insert
     * @param latitude is the latitude of the event
     * @param longitude is the longitude of the event
     * @param country is the country the event happened in
     * @param city is the city the event happened in
     * @param eventType is the type of event that happened
     * @param year is the year the event occured
     */
    private void insertEvent(String eventID, double latitude, double longitude,
                              String country, String city, String eventType, int year) {
        try {
            String sqlStr =
                "insert into event\n" + 
                "   (eventID, associatedUsername, personID, latitude, longitude, country, city, eventType, year)\n" + 
                "values (?, 'baseUser', 'basePerson', ?, ?, ?, ?, ?, ?)";
            try (Database database = new Database()) {
                PreparedStatement statement = database.prepareStatement(sqlStr);
                statement.setString(1, eventID);
                statement.setDouble(2, latitude);
                statement.setDouble(3, longitude);
                statement.setString(4, country);
                statement.setString(5, city);
                statement.setString(6, eventType);
                statement.setInt(7, year);
                database.update(statement);
                database.commit();
            }
        } catch (Exception err) {
            System.out.println("An error occured in insertEvent()");
            throw new Error(err.getMessage());
        }                     
    }

    /**
     * Runs insertEvent() with a bunch of filler data for get-testing
     */
    private void fillEvents() {
        try {
            this.insertEvent("event1", 10.312, 11.511, "USA", "New York", "Marriage", 2010);
            this.insertEvent("event2", 51.612, 12.123, "USA", "New York", "Vacation", 2015);
            this.insertEvent("event3", -19.12, -8.517, "USA", "Salt Lake City", "Near-Death Experience", 2013);
            this.insertEvent("event4", -12.14, 9.5531, "USA", "Salt Lake City", "Vacation", 2009);
            this.insertEvent("event5", -5.151, 14.156, "Japan", "Tokyo", "Vacation", 2013);
            this.insertEvent("event6", 61.552, 17.182, "Japan", "Tokyo", "Business Trip", 2015);
        } catch (Exception err) {
            System.out.println("An error occured in fillUsers()");
            throw new Error(err.getMessage());
        }
    }

    /**
     * Ensures an event can be acquired by just an event ID
     */
    @Test
    @DisplayName("Get existing events test -- by eventID")
    public void testGetExistingEventByEventID() {
        this.fillEvents();
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event event = accessor.getByID("event2");
            assertEquals("event2", event.getEventID());
            assertEquals("baseUser", event.getAssociatedUsername());
            assertEquals("basePerson", event.getPersonID());
            assertTrue(this.floatEquals(51.612, event.getLatitude()));
            assertTrue(this.floatEquals(12.123, event.getLongitude()));
            assertEquals("USA", event.getCountry());
            assertEquals("New York", event.getCity());
            assertEquals("Vacation", event.getEventType());
            assertEquals(2015, event.getYear());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures null is returned when a person does not exist
     */
    @Test
    @DisplayName("Get non-existing events test -- by eventID")
    public void testGetNonExistingEventByEventID() {
        this.fillEvents();
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event nonExistEvent = accessor.getByID("someEventIDThatDoesntExist");
            assertNull(nonExistEvent);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that all existing events can be grabbed
     */
    @Test
    @DisplayName("Get all existing events test -- with filled data")
    public void testGetAllEvents() {
        this.fillEvents();
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event[] allPeople = accessor.getAll();
            assertNotEquals(0, allPeople.length);
            // no order is (necessarily) defined to the results
            Event event1 = null, event3 = null, event6 = null;
            for (int eventIdx = 0; eventIdx < allPeople.length; ++eventIdx) {
                Event person = allPeople[eventIdx];
                if (person.getEventID().equals("event1")) {
                    if (event1 != null) {
                        fail("Duplicate entry found");
                    }
                    event1 = person;
                } else if (person.getEventID().equals("event3")) {
                    if (event3 != null) {
                        fail("Duplicate entry found");
                    }
                    event3 = person;
                } else if (person.getEventID().equals("event6")) {
                    if (event6 != null) {
                        fail("Duplicate entry found");
                    }
                    event6 = person;
                }
            }
            // event1
            assertEquals("event1",                  event1.getEventID());
            assertEquals("baseUser",                event1.getAssociatedUsername());
            assertEquals("basePerson",              event1.getPersonID());
            assertTrue(this.floatEquals(10.312,     event1.getLatitude()));
            assertTrue(this.floatEquals(11.511,     event1.getLongitude()));
            assertEquals("USA",                     event1.getCountry());
            assertEquals("New York",                event1.getCity());
            assertEquals("Marriage",                event1.getEventType());
            assertEquals(2010,                      event1.getYear());
            // event3
            assertEquals("event3",                  event3.getEventID());
            assertEquals("baseUser",                event3.getAssociatedUsername());
            assertEquals("basePerson",              event3.getPersonID());
            assertTrue(this.floatEquals(-19.12,     event3.getLatitude()));
            assertTrue(this.floatEquals(-8.517,     event3.getLongitude()));
            assertEquals("USA",                     event3.getCountry());
            assertEquals("Salt Lake City",          event3.getCity());
            assertEquals("Near-Death Experience",   event3.getEventType());
            assertEquals(2013,                      event3.getYear());
            // event6
            assertEquals("event6",                  event6.getEventID());
            assertEquals("baseUser",                event6.getAssociatedUsername());
            assertEquals("basePerson",              event6.getPersonID());
            assertTrue(this.floatEquals(61.552,     event6.getLatitude()));
            assertTrue(this.floatEquals(17.182,     event6.getLongitude()));
            assertEquals("Japan",                   event6.getCountry());
            assertEquals("Tokyo",                   event6.getCity());
            assertEquals("Business Trip",           event6.getEventType());
            assertEquals(2015,                      event6.getYear());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when grabbing all people from an empty DB
     */
    @Test
    @DisplayName("Get all existing events test -- with no data")
    public void testGetAllEventsWhenEmpty() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event[] allPeople = accessor.getAll();
            assertEquals(0, allPeople.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that events can be grabbed that belong to a user
     */
    @Test
    @DisplayName("Get events from user test -- with filled data")
    public void testGetEventsFromUser() {
        this.fillEvents();
        try (Database database = new Database()) {
            String sqlStr =
                "insert into event\n" + 
                "   (eventID, associatedUsername, personID, latitude, longitude, country, city, eventType, year)\n" + 
                "values ('eventIDThingy', 'baseUser2', 'basePerson2', 1234.567, 8910.11, 'USA', 'Provo', 'Special Event', 2015)";
            database.update(sqlStr);
            String sqlStr2 =
                "insert into event\n" + 
                "   (eventID, associatedUsername, personID, latitude, longitude, country, city, eventType, year)\n" + 
                "values ('eventIDThingyAgain', 'baseUser2', 'basePerson2', 1234.567, 8910.11, 'USA', 'Provo', 'Special Event', 2015)";
            database.update(sqlStr2);

            EventAccessor accessor = new EventAccessor(database);
            Event[] events = accessor.getAllForUser("baseUser2");
            assertEquals(2, events.length);
            events = accessor.getAllForUser("baseUser");
            assertEquals(6, events.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when grabbing events from a user who doesn't exist
     */
    @Test
    @DisplayName("Get events from user test -- with no data")
    public void testGetEventsFromUserWhenEmpty() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event[] events = accessor.getAllForUser("userThatDoesntExist");
            assertEquals(0, events.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that arbitrary events can be checked that they exist in the database
     */
    @Test
    @DisplayName("Check events exist test")
    public void testCheckExists() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someEvent", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            this.insertEvent("someOtherEvent", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event someThirdEvent = new Event("someThirdEvent", "baseUser", "basePerson", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            Event[] events = {someEvent, someOtherEvent, someThirdEvent};
            boolean[] exists = accessor.exists(events);

            boolean[] expectedExists = {true, true, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that an empty database contains no existing events
     */
    @Test
    @DisplayName("Check events exist test -- empty database")
    public void testCheckExistsWithEmptyDB() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event someThirdEvent = new Event("someThirdEvent", "baseUser", "basePerson", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            Event[] events = {someEvent, someOtherEvent, someThirdEvent};
            boolean[] exists = accessor.exists(events);

            boolean[] expectedExists = {false, false, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures new events can be created
     */
    @Test
    @DisplayName("Create new events test")
    public void testCreateNewEvents() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event[] events = {someEvent, someOtherEvent};
            accessor.create(events);

            results = accessor.getAll();
            assertEquals(2, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that re-creating events errors
     */
    @Test
    @DisplayName("Create new events test -- error on re-create")
    public void testCreateNewEventsErrors() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            Event[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event[] events = {someEvent, someOtherEvent};
            accessor.create(events);

            results = accessor.getAll();
            assertEquals(2, results.length);

            Event someThirdEvent = new Event("someThirdEvent", "baseUser", "basePerson", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            Event[] eventsWithThird = {someEvent, someOtherEvent, someThirdEvent};
            accessor.create(eventsWithThird);

            fail("create() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot create events.*"));
            assertTrue(err.getMessage().matches("(E|.*e)vent[ ]?IDs (are|were)? already (taken|used).*"));
            assertTrue(err.getMessage().contains("'someEvent'"));
            assertTrue(err.getMessage().contains("'someOtherEvent'"));
            assertFalse(err.getMessage().contains("'someThirdEvent'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that events can be deleted
     */
    @Test
    @DisplayName("Delete events test")
    public void testDeleteEvents() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someEvent", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            this.insertEvent("someOtherEvent", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            this.insertEvent("someThirdEvent", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            
            Event[] results = accessor.getAll();
            assertEquals(3, results.length);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event[] events = {someEvent, someOtherEvent};
            accessor.delete(events);

            results = accessor.getAll();
            assertEquals(1, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that deleting events that don't exist throws errors
     */
    @Test
    @DisplayName("Delete events test -- events don't exist")
    public void testDeleteEventsErrors() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someThirdEvent", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event someThirdEvent = new Event("someThirdEvent", "baseUser", "basePerson", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            Event[] events = {someEvent, someOtherEvent, someThirdEvent};
            accessor.delete(events);

            fail("delete() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot delete events.*"));
            assertTrue(err.getMessage().matches("(E|.*e)vent[ ]?IDs (didn'?t|did not|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'someEvent'"));
            assertTrue(err.getMessage().contains("'someOtherEvent'"));
            assertFalse(err.getMessage().contains("'someThirdEvent'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that people can be updated
     */
    @Test
    @DisplayName("Update people test")
    public void testUpdatePeople() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someEvent", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            this.insertEvent("someOtherEvent", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            this.insertEvent("someThirdEvent", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            
            Event someEvent = accessor.getByID("someEvent");
            Event someOtherEvent = accessor.getByID("someOtherEvent");
            Event someThirdEvent = accessor.getByID("someThirdEvent");
            assertTrue(this.floatEquals(12.345, someEvent.getLatitude()));
            assertEquals("Provo", someEvent.getCity());
            assertEquals("USA", someOtherEvent.getCountry());
            assertEquals("Special Event", someOtherEvent.getEventType());
            assertEquals("USA", someThirdEvent.getCountry());
            
            someEvent.setLatitude(543.21);
            someEvent.setCity("Las Vegas");
            someOtherEvent.setEventType("REALLY SPECIAL EVENT");
            Event[] events = {someEvent, someOtherEvent, someThirdEvent};
            accessor.update(events);
            
            Event someEventNew = accessor.getByID("someEvent");
            Event someOtherEventNew = accessor.getByID("someOtherEvent");
            Event someThirdEventNew = accessor.getByID("someThirdEvent");
            assertTrue(this.floatEquals(543.21, someEventNew.getLatitude()));
            assertEquals("Las Vegas", someEventNew.getCity());
            assertEquals("USA", someOtherEventNew.getCountry());
            assertEquals("REALLY SPECIAL EVENT", someOtherEventNew.getEventType());
            assertEquals("USA", someThirdEventNew.getCountry());
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that updating people that don't exist throws errors
     */
    @Test
    @DisplayName("Update people test -- people don't exist")
    public void testUpdatePeopleErrors() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someThirdEvent", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            
            Event someEvent = new Event("someEvent", "baseUser", "basePerson", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            Event someOtherEvent = new Event("someOtherEvent", "baseUser", "basePerson", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            Event someThirdEvent = new Event("someThirdEvent", "baseUser", "basePerson", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            
            someEvent.setLatitude(543.21);
            someEvent.setCity("Las Vegas");
            someOtherEvent.setEventType("REALLY SPECIAL EVENT");
            Event[] events = {someEvent, someOtherEvent, someThirdEvent};
            accessor.update(events);

            fail("update() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot update events.*"));
            assertTrue(err.getMessage().matches("(E|.*e)vent[ ]?IDs (didn'?t|did not|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'someEvent'"));
            assertTrue(err.getMessage().contains("'someOtherEvent'"));
            assertFalse(err.getMessage().contains("'someThirdEvent'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that the user table can be cleared from the database
     */
    @Test
    @DisplayName("Clear person table test")
    public void testClearPeople() {
        try (Database database = new Database()) {
            EventAccessor accessor = new EventAccessor(database);
            this.insertEvent("someEvent", 12.345, -54.321, "USA", "Provo", "Special Event", 2020);
            this.insertEvent("someOtherEvent", 32.145, 43.512, "USA", "Provo", "Special Event", 2021);
            this.insertEvent("someThirdEvent", 23.415, 41.523, "USA", "Provo", "Special Event", 2024);
            assertEquals(3, accessor.getAll().length);

            accessor.clear();

            assertEquals(0, accessor.getAll().length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
}
