package services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataAccess.*;
import models.*;

import services.requests.RegisterRequest;
import services.responses.RegisterResponse;

/**
 * Contains test cases to ensure the RegisterService works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterServiceTest {
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
            UserAccessor userAcc = new UserAccessor(database);
            User[] users = {sallyUser};
            userAcc.create(users);

            Person sallyPerson = new Person("sallypersonid", sallyUser.getUsername(), sallyUser.getFirstName(), sallyUser.getLastName(), "f");
            Person sallyFather = new Person("fatherpersonid", sallyUser.getUsername(), "Gregory", sallyUser.getLastName(), "m");
            Person sallyMother = new Person("motherpersonid", sallyUser.getUsername(), "Amy", "Thomson", "f");
            PersonAccessor personAcc = new PersonAccessor(database);
            Person[] persons = {sallyPerson, sallyFather, sallyMother};
            personAcc.create(persons);

            sallyUser.setPersonID(sallyPerson.getPersonID());
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
     * Ensures an empty database can be filled with new registered data
     */
    @Test
    @DisplayName("Register test with empty database")
    public void testRegisterOnEmptyDatabase() {
        this.assertNumModelsInDatabase(0, 0, 0, 0);

        RegisterService service = new RegisterService();
        RegisterRequest request = new RegisterRequest();
        request.username = "newuser";
        request.password = "newpassword";
        request.email = "new@email.test";
        request.firstName = "NewFirstName";
        request.lastName = "NewLastName";
        request.gender = "m";
        RegisterResponse response = service.process("POST", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("newuser", response.username);
        assertNotNull(response.personID);
        assertNotNull(response.authtoken);

        // 1 user, 4 generations of people, 3 events per person per generation, and 1 new auth token
        this.assertNumModelsInDatabase(1, 1 + 2 + 4 + 8 + 16, 1 + 2*3 + 4*3 + 8*3 + 16*3, 1);
    }

    /**
     * Ensures a database with information can be given a new user
     */
    @Test
    @DisplayName("Register test with non-empty database")
    public void testRegisterWhenDatabaseHasInfo() {
        this.fillDatabase();
        this.assertNumModelsInDatabase(1, 3, 7, 1);

        RegisterService service = new RegisterService();
        RegisterRequest request = new RegisterRequest();
        request.username = "newuser";
        request.password = "newpassword";
        request.email = "new@email.test";
        request.firstName = "NewFirstName";
        request.lastName = "NewLastName";
        request.gender = "m";
        RegisterResponse response = service.process("POST", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("newuser", response.username);
        assertNotNull(response.personID);
        assertNotNull(response.authtoken);

        // 1 new user, 4 new generations of people, 3 new events per person per generation, and 1 new auth token
        this.assertNumModelsInDatabase(1 + 1, 3 + (1 + 2 + 4 + 8 + 16), 7 + (1 + 2*3 + 4*3 + 8*3 + 16*3), 2);
    }

    /**
     * Ensures using GET fails
     */
    @Test
    @DisplayName("GET fails")
    public void testGetMethod() {
        RegisterService service = new RegisterService();
        RegisterRequest request = new RegisterRequest();
        RegisterResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(I|.*i)nvalid (http |HTTP |Http )?method.*"));
    }
}