package dataAccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import models.Person;

import java.sql.PreparedStatement;

/**
 * Contains the test cases that ensure the PersonAccessor class runs correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonAccessorTest {
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
     * Ensures an empty, fresh database before each test (with a base user to associate by)
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
            database.commit();
        }
    }

    /**
     * Insert arbitrary Persons into the database, bypassing PersonAccessor.
     * Associated Username property defaults to "baseUser".
     * Father, mother, and spouse IDs also default to null (for sake of ease).
     * 
     * @param personID is the person ID to insert
     * @param firstname is the first name of the new Person
     * @param lastname is the last name of the new Person
     * @param gender is the gender of the new person, 'm' or 'f'
     */
    private void insertPerson(String personID, String firstname, String lastname, String gender) {
        try {
            String sqlStr =
                "insert into person\n" + 
                "   (personID, firstname, lastname, gender, associatedUsername)\n" + 
                "values (?, ?, ?, ?, 'baseUser')";
            try (Database database = new Database()) {
                PreparedStatement statement = database.prepareStatement(sqlStr);
                statement.setString(1, personID);
                statement.setString(2, firstname);
                statement.setString(3, lastname);
                statement.setString(4, gender);
                database.update(statement);
                database.commit();
            }
        } catch (Exception err) {
            System.out.println("An error occured in insertPerson()");
            throw new Error(err.getMessage());
        }                     
    }

    /**
     * Runs insertPerson() with a bunch of filler data for get-testing
     */
    private void fillPersons() {
        try {
            this.insertPerson("testperson1", "testFirstname", "testLastname", "m");
            this.insertPerson("testperson2", "testFirstname2", "testLastname2", "m");
            this.insertPerson("benID1", "Ben", "Hanson", "m");
            this.insertPerson("sallyID", "Sally", "Hendricks", "f");
            this.insertPerson("benID2", "Ben", "Jargon", "m");
            this.insertPerson("chrisID", "Chris", "Lewis", "m");
            this.insertPerson("annyID", "Anny", "Wanny", "f");
        } catch (Exception err) {
            System.out.println("An error occured in fillPersons()");
            throw new Error(err.getMessage());
        }
    }

    /**
     * Ensures a person can be acquired by just a person ID
     */
    @Test
    @DisplayName("Get existing people test -- by personID")
    public void testGetExistingPersonByPersonID() {
        this.fillPersons();
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person benPerson = accessor.getByID("benID1");
            assertEquals("benID1",  benPerson.getPersonID());
            assertEquals("Ben",     benPerson.getFirstName());
            assertEquals("Hanson",  benPerson.getLastName());
            assertEquals("m",       benPerson.getGender());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures null is returned when a person does not exist
     */
    @Test
    @DisplayName("Get non-existing people test -- by personID")
    public void testGetNonExistingPersonByPersonID() {
        this.fillPersons();
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person bensBrotherPerson = accessor.getByID("BenHansonsBrotherWhoDoesntExist");
            assertNull(bensBrotherPerson);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that all existing people can be grabbed
     */
    @Test
    @DisplayName("Get all existing people test -- with filled data")
    public void testGetAllPeople() {
        this.fillPersons();
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person[] allPeople = accessor.getAll();
            assertNotEquals(0, allPeople.length);
            // no order is (necessarily) defined to the results
            Person benHPerson = null, benJPerson = null, sallyPerson = null;
            for (int benIdx = 0; benIdx < allPeople.length; ++benIdx) {
                Person person = allPeople[benIdx];
                if (person.getPersonID().equals("benID1")) {
                    if (benHPerson != null) {
                        fail("Duplicate entry found");
                    }
                    benHPerson = person;
                } else if (person.getPersonID().equals("benID2")) {
                    if (benJPerson != null) {
                        fail("Duplicate entry found");
                    }
                    benJPerson = person;
                } else if (person.getPersonID().equals("sallyID")) {
                    if (sallyPerson != null) {
                        fail("Duplicate entry found");
                    }
                    sallyPerson = person;
                }
            }
            // Ben Hanson's Person
            assertEquals("benID1",      benHPerson.getPersonID());
            assertEquals("Ben",         benHPerson.getFirstName());
            assertEquals("Hanson",      benHPerson.getLastName());
            assertEquals("m",           benHPerson.getGender());
            // Ben Jargon's Person
            assertEquals("benID2",      benJPerson.getPersonID());
            assertEquals("Ben",         benJPerson.getFirstName());
            assertEquals("Jargon",      benJPerson.getLastName());
            assertEquals("m",           benJPerson.getGender());
            // Sally Hendricks' Person
            assertEquals("sallyID",     sallyPerson.getPersonID());
            assertEquals("Sally",       sallyPerson.getFirstName());
            assertEquals("Hendricks",   sallyPerson.getLastName());
            assertEquals("f",           sallyPerson.getGender());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when grabbing all people from an empty DB
     */
    @Test
    @DisplayName("Get all existing people test -- with no data")
    public void testGetAllPeopleWhenEmpty() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person[] allPeople = accessor.getAll();
            assertEquals(0, allPeople.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that people can be grabbed that belong to a user
     */
    @Test
    @DisplayName("Get people from user test -- with filled data")
    public void testGetPeopleFromUser() {
        this.fillPersons();
        try (Database database = new Database()) {
            String sqlStr =
                "insert into person\n" + 
                "   (personID, firstname, lastname, gender, associatedUsername)\n" + 
                "values ('personIDThingy123', 'firstname', 'lastname', 'm', 'baseUser2')";
            database.update(sqlStr);
            String sqlStr2 =
                "insert into person\n" + 
                "   (personID, firstname, lastname, gender, associatedUsername)\n" + 
                "values ('personIDThingy123Again', 'firstname', 'lastname', 'm', 'baseUser2')";
            database.update(sqlStr2);

            PersonAccessor accessor = new PersonAccessor(database);
            Person[] people = accessor.getAllForUser("baseUser2");
            assertEquals(2, people.length);
            people = accessor.getAllForUser("baseUser");
            assertEquals(7, people.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when grabbing people from a user who doesn't exist
     */
    @Test
    @DisplayName("Get people from user test -- with no data")
    public void testGetPeopleFromUserWhenEmpty() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person[] allPeople = accessor.getAllForUser("userThatDoesntExist");
            assertEquals(0, allPeople.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that arbitrary people can be checked that they exist in the database
     */
    @Test
    @DisplayName("Check people exist test")
    public void testCheckExists() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("benID1", "Ben", "Hanson", "m");
            this.insertPerson("sallyID", "Sally", "Hendricks", "f");
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person chaz = new Person("chazID", "baseUser", "Chaz", "Shmaz", "m");
            Person[] people = {ben, sally, chaz};
            boolean[] exists = accessor.exists(people);

            boolean[] expectedExists = {true, true, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that an empty database contains no existing people
     */
    @Test
    @DisplayName("Check people exist test -- empty database")
    public void testCheckExistsWithEmptyDB() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person chaz = new Person("chazID", "baseUser", "Chaz", "Shmaz", "m");
            Person[] people = {ben, sally, chaz};
            boolean[] exists = accessor.exists(people);

            boolean[] expectedExists = {false, false, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures new people can be created
     */
    @Test
    @DisplayName("Create new people test")
    public void testCreateNewPeople() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person[] people = {ben, sally};
            accessor.create(people);

            results = accessor.getAll();
            assertEquals(2, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that re-creating people errors
     */
    @Test
    @DisplayName("Create new people test -- error on re-create")
    public void testCreateNewPeopleErrors() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            Person[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person[] people = {ben, sally};
            accessor.create(people);

            results = accessor.getAll();
            assertEquals(2, results.length);

            Person chaz = new Person("chazID", "baseUser", "Chaz", "Shmaz", "m");
            Person[] peopleAndChaz = {ben, chaz, sally};
            accessor.create(peopleAndChaz);

            fail("create() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot create people.*"));
            assertTrue(err.getMessage().matches("(P|.*p)erson[ ]?IDs (are|were)? already taken.*"));
            assertTrue(err.getMessage().contains("'benID1'"));
            assertTrue(err.getMessage().contains("'sallyID'"));
            assertFalse(err.getMessage().contains("'chazID'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that people can be deleted
     */
    @Test
    @DisplayName("Delete people test")
    public void testDeletePeople() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("benID1", "Ben", "Hanson", "m");
            this.insertPerson("sallyID", "Sally", "Hendricks", "f");
            this.insertPerson("guyID", "Guy", "Dude", "m");
            
            Person[] results = accessor.getAll();
            assertEquals(3, results.length);
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person[] people = {ben, sally};
            accessor.delete(people);

            results = accessor.getAll();
            assertEquals(1, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that deleting people that don't exist throws errors
     */
    @Test
    @DisplayName("Delete people test -- people don't exist")
    public void testDeletePeopleErrors() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("guyID", "Guy", "Dude", "m");
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person guy = new Person("guyID", "baseUser", "Guy", "Dude", "m");
            Person[] people = {ben, guy, sally};
            accessor.delete(people);

            fail("delete() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot delete people.*"));
            assertTrue(err.getMessage().matches("(P|.*p)erson[ ]?IDs (do not|did not|didn'?t|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'benID1'"));
            assertTrue(err.getMessage().contains("'sallyID'"));
            assertFalse(err.getMessage().contains("'chazID'"));
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
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("benID1", "Ben", "Hanson", "m");
            this.insertPerson("sallyID", "Sally", "Hendricks", "f");
            this.insertPerson("guyID", "Guy", "Dude", "m");
            
            Person ben = accessor.getByID("benID1");
            Person sally = accessor.getByID("sallyID");
            Person guy = accessor.getByID("guyID");
            assertEquals("Ben", ben.getFirstName());
            assertEquals("Hanson", ben.getLastName());
            assertEquals("Sally", sally.getFirstName());
            assertEquals("Hendricks", sally.getLastName());
            assertEquals("Guy", guy.getFirstName());
            
            ben.setFirstName("Benjamin");
            sally.setFirstName("Salenia");
            sally.setLastName("Hendrickson");
            Person[] people = {ben, sally, guy};
            accessor.update(people);
            
            Person benNew = accessor.getByID("benID1");
            Person sallyNew = accessor.getByID("sallyID");
            Person guyUnchanged = accessor.getByID("guyID");
            assertEquals("Benjamin", benNew.getFirstName());
            assertEquals("Hanson", benNew.getLastName());
            assertEquals("Salenia", sallyNew.getFirstName());
            assertEquals("Hendrickson", sallyNew.getLastName());
            assertEquals("Guy", guyUnchanged.getFirstName());
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
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("guyID", "Guy", "Dude", "m");
            
            Person ben = new Person("benID1", "baseUser", "Ben", "Hanson", "m");
            Person sally = new Person("sallyID", "baseUser", "Sally", "Hendricks", "f");
            Person guy = new Person("guyID", "baseUser", "Guy", "Dude", "m");
            
            ben.setFirstName("Benjamin");
            sally.setFirstName("Salenia");
            sally.setLastName("Hendrickson");
            Person[] people = {ben, sally, guy};
            accessor.update(people);

            fail("update() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot update people.*"));
            assertTrue(err.getMessage().matches("(P|.*p)erson[ ]?IDs (do not|did not|didn'?t|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'benID1'"));
            assertTrue(err.getMessage().contains("'sallyID'"));
            assertFalse(err.getMessage().contains("'chazID'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that the person table can be cleared from the database
     */
    @Test
    @DisplayName("Clear person table test")
    public void testClearPeople() {
        try (Database database = new Database()) {
            PersonAccessor accessor = new PersonAccessor(database);
            this.insertPerson("benID1", "Ben", "Hanson", "m");
            this.insertPerson("sallyID", "Sally", "Hendricks", "f");
            this.insertPerson("guyID", "Guy", "Dude", "m");
            assertEquals(3, accessor.getAll().length);

            accessor.clear();

            assertEquals(0, accessor.getAll().length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
}
