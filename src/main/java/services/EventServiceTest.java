package services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataAccess.*;
import models.*;

import services.requests.EventRequest;
import services.responses.EventResponse;

/**
 * Contains test cases to ensure the EventService works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventServiceTest {
    /**
     * Runs fail(), but shows a traceback to the call of this method, instead
     * of where the error was thrown
     * 
     * @param err is the error that was thrown
     */
    @SuppressWarnings("unused")
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
     * Ensures an empty, fresh database before each test
     * 
     * @throws DatabaseException whenever reset() does
     */
    @BeforeEach
    public void resetDatabase() throws DatabaseException {
        try (Database database = new Database()) {
            database.reset();
        }
    }

    /**
     * Fills the database with a bunch of (valid) data!
     */
    private void fillDatabase() {
        try (Database database = new Database()) {
            User sallyUser = new User("sillysally", "password", "sally@email.test", "Sally", "Black", "f", null);
            User markUser = new User("markimoo", "markabarkbark", "mark@email.test", "Mark", "Bark", "m", null);
            UserAccessor userAcc = new UserAccessor(database);
            User[] users = {sallyUser, markUser};
            userAcc.create(users);

            Person sallyPerson = new Person("sallypersonid", sallyUser.getUsername(), sallyUser.getFirstName(), sallyUser.getLastName(), "f");
            Person sallyFather = new Person("fatherpersonid", sallyUser.getUsername(), "Gregory", sallyUser.getLastName(), "m");
            Person sallyMother = new Person("motherpersonid", sallyUser.getUsername(), "Amy", "Thomson", "f");
            PersonAccessor personAcc = new PersonAccessor(database);
            Person markPerson = new Person("markpersonid", markUser.getUsername(), markUser.getFirstName(), markUser.getLastName(), "m");
            Person[] persons = {sallyPerson, sallyFather, sallyMother, markPerson};
            personAcc.create(persons);

            sallyUser.setPersonID(sallyPerson.getPersonID());
            markUser.setPersonID(markPerson.getPersonID());
            userAcc.update(users);

            Event sallyBirth = new Event("sallybirtheventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 1.1, 1.1, "USA", "Provo", "Birth", 2000);
            Event fatherBirth = new Event("fatherbirtheventid", sallyUser.getUsername(), sallyFather.getPersonID(), 2.1, 2.1, "USA", "Provo", "Birth", 1970);
            Event fatherMarriage = new Event("fathermarriageeventid", sallyUser.getUsername(), sallyFather.getPersonID(), 5.1, 5.1, "USA", "Provo", "Marriage", 1990);
            Event fatherDeath = new Event("fatherdeatheventid", sallyUser.getUsername(), sallyFather.getPersonID(), 2.2, 2.2, "USA", "Provo", "Death", 2023);
            Event motherBirth = new Event("motherbirtheventid", sallyUser.getUsername(), sallyMother.getPersonID(), 3.1, 3.1, "USA", "Provo", "Birth", 1971);
            Event motherMarriage = new Event("mothermarriageeventid", sallyUser.getUsername(), sallyMother.getPersonID(), 5.1, 5.1, "USA", "Provo", "Marriage", 1990);
            // I had to make one of the events not belong to Sally...
            // it ruins the "valid data" idea, but hey, the tests still work!
            Event motherDeath = new Event("motherdeatheventid", markUser.getUsername(), sallyMother.getPersonID(), 3.2, 3.2, "USA", "Provo", "Death", 2020);
            EventAccessor eventAcc = new EventAccessor(database);
            Event[] events = {sallyBirth, fatherBirth, fatherMarriage, fatherDeath, motherBirth, motherMarriage, motherDeath};
            eventAcc.create(events);

            AuthToken authToken = new AuthToken("sallyauth", sallyUser.getUsername());
            AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(database);
            AuthToken[] authTokens = {authToken};
            authTokenAcc.create(authTokens);
            
            database.commit();
        } catch (Throwable err) {
            System.out.println("An exception occurred in fillDatabase()");
        }
    }

    /**
     * Asserts that the number of each type of model is present in the database
     * 
     * @param expNumUsers is the expected number of Users to be in the database
     * @param expNumPersons is the expected number of Persons to be in the database
     * @param expNumEvents is the expected number of Events to be in the database
     * @param expNumAuthTokens is the expected number of AuthTokens to be in the database
     */
    private void assertNumModelsInDatabase(int expNumUsers, int expNumPersons, int expNumEvents, int expNumAuthTokens) throws AssertionError {
        try (Database database = new Database()) {
            int numUsers = database.query("select * from user", (result) -> null).size();
            assertEquals(expNumUsers, numUsers);
            
            int numPersons = database.query("select * from person", (result) -> null).size();
            assertEquals(expNumPersons, numPersons);
            
            int numEvents = database.query("select * from event", (result) -> null).size();
            assertEquals(expNumEvents, numEvents);
            
            int numAuthTokens = database.query("select * from authtoken", (result) -> null).size();
            assertEquals(expNumAuthTokens, numAuthTokens);
        } catch (AssertionError err) {
            throw err;
        } catch (Throwable err) {
            System.out.println("An exception occurred in getNumModelsInDatabase(): " + err.getClass().getName());
        }
    }

    /**
     * Ensures own birth event can be returned from the database
     */
    @Test
    @DisplayName("Getting own Event test -- filled database")
    public void testGetOwnEvent() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        EventService service = new EventService();
        EventRequest request = new EventRequest();
        request.eventID = "sallybirtheventid";
        request.authtoken = "sallyauth";
        EventResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("sallybirtheventid", response.eventID);
        assertEquals("sillysally", response.associatedUsername);
        assertEquals("sallypersonid", response.personID);
        assertEquals("Birth", response.eventType);
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Ensures a family member's Event can be returned from the database
     */
    @Test
    @DisplayName("Getting father Event test -- filled database")
    public void testGetFatherEvent() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        EventService service = new EventService();
        EventRequest request = new EventRequest();
        request.eventID = "fatherbirtheventid";
        request.authtoken = "sallyauth";
        EventResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("fatherbirtheventid", response.eventID);
        assertEquals("sillysally", response.associatedUsername);
        assertEquals("fatherpersonid", response.personID);
        assertEquals("Birth", response.eventType);
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Should fail when an invalid auth token is provided
     */
    @Test
    @DisplayName("Getting father Event test -- bad auth token")
    public void testInvalidAuthToken() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        EventService service = new EventService();
        EventRequest request = new EventRequest();
        request.eventID = "fatherbirtheventid";
        request.authtoken = "bad auth token";
        EventResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(.*A|.*a)uthorization failed.*"));
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Should fail when an invalid event id is requested
     */
    @Test
    @DisplayName("Getting bad Event test -- bad personID")
    public void testInvalidEventID() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        EventService service = new EventService();
        EventRequest request = new EventRequest();
        request.eventID = "bad eventID";
        request.authtoken = "sallyauth";
        EventResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches(".* not found.*"));
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Ensures only the correct events are returned when getting all events
     */
    @Test
    @DisplayName("Getting all Events test")
    public void testGetAllEvents() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        EventService service = new EventService();
        EventRequest request = new EventRequest();
        request.all = true;
        request.authtoken = "sallyauth";
        EventResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals(7 - 1, response.data.length);
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Ensures using POST fails
     */
    @Test
    @DisplayName("POST fails")
    public void testGetMethod() {
        EventService service = new EventService();
        EventRequest request = new EventRequest();
        EventResponse response = service.process("POST", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(I|.*i)nvalid (http |HTTP |Http )?method.*"));
    }
}