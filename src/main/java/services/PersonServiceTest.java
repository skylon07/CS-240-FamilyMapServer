package services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataAccess.*;
import models.*;

import services.requests.PersonRequest;
import services.responses.PersonResponse;

/**
 * Contains test cases to ensure the ClearService works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonServiceTest {
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
            Event fatherBirth = new Event("fatherbirtheventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 2.1, 2.1, "USA", "Provo", "Birth", 1970);
            Event fatherMarriage = new Event("fathermarriageeventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 5.1, 5.1, "USA", "Provo", "Marriage", 1990);
            Event fatherDeath = new Event("fatherdeatheventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 2.2, 2.2, "USA", "Provo", "Death", 2023);
            Event motherBirth = new Event("motherbirtheventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 3.1, 3.1, "USA", "Provo", "Birth", 1971);
            Event motherMarriage = new Event("mothermarriageeventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 5.1, 5.1, "USA", "Provo", "Marriage", 1990);
            Event motherDeath = new Event("motherdeatheventid", sallyUser.getUsername(), sallyPerson.getPersonID(), 3.2, 3.2, "USA", "Provo", "Death", 2020);
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
     * Ensures own Person can be returned from the database
     */
    @Test
    @DisplayName("Getting own Person test -- filled database")
    public void testGetOwnPerson() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        PersonService service = new PersonService();
        PersonRequest request = new PersonRequest();
        request.personID = "sallypersonid";
        request.authtoken = "sallyauth";
        PersonResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("sallypersonid", response.personID);
        assertEquals("sillysally", response.associatedUsername);
        assertEquals("Sally", response.firstName);
        assertEquals("Black", response.lastName);
        assertEquals("f", response.gender);
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Ensures a family Person can be returned from the database
     */
    @Test
    @DisplayName("Getting father Person test -- filled database")
    public void testGetFatherPerson() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        PersonService service = new PersonService();
        PersonRequest request = new PersonRequest();
        request.personID = "fatherpersonid";
        request.authtoken = "sallyauth";
        PersonResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("fatherpersonid", response.personID);
        assertEquals("sillysally", response.associatedUsername);
        assertEquals("Gregory", response.firstName);
        assertEquals("Black", response.lastName);
        assertEquals("m", response.gender);
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Should fail when an invalid auth token is provided
     */
    @Test
    @DisplayName("Getting father Person test -- bad auth token")
    public void testInvalidAuthToken() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        PersonService service = new PersonService();
        PersonRequest request = new PersonRequest();
        request.personID = "fatherpersonid";
        request.authtoken = "bad auth token";
        PersonResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(.*A|.*a)uthorization failed.*"));
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Should fail when an invalid person id is requested
     */
    @Test
    @DisplayName("Getting bad Person test -- bad personID")
    public void testInvalidPersonID() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(2, 4, 7, 1);

        PersonService service = new PersonService();
        PersonRequest request = new PersonRequest();
        request.personID = "invalid person ID";
        request.authtoken = "sallyauth";
        PersonResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches(".* not found.*"));
        // GET should not change database
        this.assertNumModelsInDatabase(2, 4, 7, 1);
    }

    /**
     * Ensures using POST fails
     */
    @Test
    @DisplayName("POST fails")
    public void testGetMethod() {
        PersonService service = new PersonService();
        PersonRequest request = new PersonRequest();
        PersonResponse response = service.process("POST", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(I|.*i)nvalid (http |HTTP |Http )?method.*"));
    }
}