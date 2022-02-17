package dataAccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import models.User;

import java.sql.PreparedStatement;

/**
 * Contains the test cases that ensure the UserAccessor class runs correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAccessorTest {
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
     * Insert arbitrary users into the database, bypassing UserAccessor.
     * Person ID property defaults to null (no person to tie to).
     * 
     * @param username is the username of the new User
     * @param password is the password for the new User
     * @param email is the email for the new User
     * @param firstname is the first name of the new User
     * @param lastname is the last name of the new User
     * @param gender is the gender of the new User, 'm' or 'f'
     */
    private void insertUser(String username, String password, String email,
                            String firstname, String lastname, String gender) {
        try {
            String sqlStr =
                "insert into user\n" + 
                "   (username, password, email, firstname, lastname, gender)\n" + 
                "values (?, ?, ?, ?, ?, ?)";
            try (Database database = new Database()) {
                PreparedStatement statement = database.prepareStatement(sqlStr);
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, email);
                statement.setString(4, firstname);
                statement.setString(5, lastname);
                statement.setString(6, gender);
                database.update(statement);
                database.commit();
            }
        } catch (Exception err) {
            System.out.println("An error occured in insertUser()");
            throw new Error(err.getMessage());
        }                     
    }

    /**
     * Runs insertUser() with a bunch of filler data for get-testing
     */
    private void fillUsers() {
        try {
            this.insertUser(
                "testUsername", "testPassword", "testEmail",
                "testFirstname", "testLastname", "m"
            );
            this.insertUser(
                "testUsername2", "testPassword2", "testEmail2",
                "testFirstname2", "testLastname2", "m"
            );
            this.insertUser(
                "SillySally", "i am silly", "sally@email.test",
                "Sally", "Hendricks", "f"
            );
            this.insertUser(
                "BenHanson", "bens password", "ben@email.test",
                "Ben", "Hanson", "m"
            );
            this.insertUser(
                "BJBoi", "bens password", "ben@email.test",
                "Ben", "Jargon", "m"
            );
            this.insertUser(
                "ThatGuyChris", "im that guy", "chris@email.test",
                "Chris", "Lewis", "m"
            );
            this.insertUser(
                "AnnyWannyBoBanny", "bananafannafofanny", "anny@email.test",
                "Anny", "Wanny", "f"
            );
        } catch (Exception err) {
            System.out.println("An error occured in fillUsers()");
            throw new Error(err.getMessage());
        }
    }

    /**
     * Ensures a user can be acquired by just a username
     */
    @Test
    @DisplayName("Get existing users test -- by username")
    public void testGetExistingUserByUsername() {
        this.fillUsers();
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User benUser = accessor.getByUsername("BenHanson");
            assertEquals("BenHanson",       benUser.getUsername());
            assertEquals("bens password",   benUser.getPassword());
            assertEquals("ben@email.test",  benUser.getEmail());
            assertEquals("Ben",             benUser.getFirstName());
            assertEquals("Hanson",          benUser.getLastName());
            assertEquals("m",               benUser.getGender());
            assertEquals(null,              benUser.getPersonID());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures null is returned when a user does not exist
     */
    @Test
    @DisplayName("Get non-existing users test -- by username")
    public void testGetNonExistingUserByUsername() {
        this.fillUsers();
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User bensBrotherUser = accessor.getByUsername("BenHansonsBrotherWhoDoesntExist");
            assertNull(bensBrotherUser);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures all users with an email can be found 
     */
    @Test
    @DisplayName("Get existing users test -- by email")
    public void testGetExistingUsersByEmail() {
        this.fillUsers();
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] benUsers = accessor.getByEmail("ben@email.test");
            assertEquals(2, benUsers.length);
            // no order is (necessarily) defined to the results
            User benHUser = null, benJUser = null;
            for (int benIdx = 0; benIdx < benUsers.length; ++benIdx) {
                User benUser = benUsers[benIdx];
                if (benUser.getUsername().equals("BenHanson")) {
                    if (benHUser != null) {
                        fail("Duplicate entry found");
                    }
                    benHUser = benUser;
                } else if (benUser.getUsername().equals("BJBoi")) {
                    if (benJUser != null) {
                        fail("Duplicate entry found");
                    }
                    benJUser = benUser;
                }
            }
            // Ben Hanson's User
            assertEquals("BenHanson",       benHUser.getUsername());
            assertEquals("bens password",   benHUser.getPassword());
            assertEquals("ben@email.test",  benHUser.getEmail());
            assertEquals("Ben",             benHUser.getFirstName());
            assertEquals("Hanson",          benHUser.getLastName());
            assertEquals("m",               benHUser.getGender());
            assertEquals(null,              benHUser.getPersonID());
            // Ben Jargon's User
            assertEquals("BJBoi",           benJUser.getUsername());
            assertEquals("bens password",   benJUser.getPassword());
            assertEquals("ben@email.test",  benJUser.getEmail());
            assertEquals("Ben",             benJUser.getFirstName());
            assertEquals("Jargon",          benJUser.getLastName());
            assertEquals("m",               benJUser.getGender());
            assertEquals(null,              benJUser.getPersonID());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when looking up an email that no users have
     */
    @Test
    @DisplayName("Get non-existing users test -- by email")
    public void testGetNonExistingUsersByEmail() {
        this.fillUsers();
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] bensBrotherUsers = accessor.getByEmail("bensBrotherWhoDoesntExist@email.test");
            assertEquals(0, bensBrotherUsers.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that all existing users can be grabbed
     */
    @Test
    @DisplayName("Get all existing users test -- with filled data")
    public void testGetAllUsers() {
        this.fillUsers();
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] allUsers = accessor.getAll();
            assertNotEquals(0, allUsers.length);
            // no order is (necessarily) defined to the results
            User benHUser = null, benJUser = null, sallyUser = null;
            for (int benIdx = 0; benIdx < allUsers.length; ++benIdx) {
                User user = allUsers[benIdx];
                if (user.getUsername().equals("BenHanson")) {
                    if (benHUser != null) {
                        fail("Duplicate entry found");
                    }
                    benHUser = user;
                } else if (user.getUsername().equals("BJBoi")) {
                    if (benJUser != null) {
                        fail("Duplicate entry found");
                    }
                    benJUser = user;
                } else if (user.getUsername().equals("SillySally")) {
                    if (sallyUser != null) {
                        fail("Duplicate entry found");
                    }
                    sallyUser = user;
                }
            }
            // Ben Hanson's User
            assertEquals("BenHanson",           benHUser.getUsername());
            assertEquals("bens password",       benHUser.getPassword());
            assertEquals("ben@email.test",      benHUser.getEmail());
            assertEquals("Ben",                 benHUser.getFirstName());
            assertEquals("Hanson",              benHUser.getLastName());
            assertEquals("m",                   benHUser.getGender());
            assertEquals(null,                  benHUser.getPersonID());
            // Ben Jargon's User
            assertEquals("BJBoi",               benJUser.getUsername());
            assertEquals("bens password",       benJUser.getPassword());
            assertEquals("ben@email.test",      benJUser.getEmail());
            assertEquals("Ben",                 benJUser.getFirstName());
            assertEquals("Jargon",              benJUser.getLastName());
            assertEquals("m",                   benJUser.getGender());
            assertEquals(null,                  benJUser.getPersonID());
            // Sally Hendricks' User
            assertEquals("SillySally",          sallyUser.getUsername());
            assertEquals("i am silly",          sallyUser.getPassword());
            assertEquals("sally@email.test",    sallyUser.getEmail());
            assertEquals("Sally",               sallyUser.getFirstName());
            assertEquals("Hendricks",           sallyUser.getLastName());
            assertEquals("f",                   sallyUser.getGender());
            assertEquals(null,                  sallyUser.getPersonID());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when grabbing all users from an empty DB
     */
    @Test
    @DisplayName("Get all existing users test -- with no data")
    public void testGetAllUsersWhenEmpty() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] allUsers = accessor.getAll();
            assertEquals(0, allUsers.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that arbitrary users can be checked that they exist in the database
     */
    @Test
    @DisplayName("Check users exist test")
    public void testCheckExists() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m");
            this.insertUser("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f");
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User chaz = new User("chazzyboi", "password123", "chaz@email.test", "Chaz", "Chaz", "m", null);
            User[] users = {ben, sally, chaz};
            boolean[] exists = accessor.exists(users);

            boolean[] expectedExists = {true, true, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that an empty database contains no existing users
     */
    @Test
    @DisplayName("Check users exist test -- empty database")
    public void testCheckExistsWithEmptyDB() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User chaz = new User("chazzyboi", "password123", "chaz@email.test", "Chaz", "Chaz", "m", null);
            User[] users = {ben, sally, chaz};
            boolean[] exists = accessor.exists(users);

            boolean[] expectedExists = {false, false, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures new users can be created
     */
    @Test
    @DisplayName("Create new users test")
    public void testCreateNewUsers() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User[] users = {ben, sally};
            accessor.create(users);
            

            results = accessor.getAll();
            assertEquals(2, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that re-creating users errors
     */
    @Test
    @DisplayName("Create new users test -- error on re-create")
    public void testCreateNewUsersErrors() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            User[] results = accessor.getAll();
            assertEquals(0, results.length);
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User[] users = {ben, sally};
            accessor.create(users);

            results = accessor.getAll();
            assertEquals(2, results.length);

            User chaz = new User("chazzyboi", "password123", "chaz@email.test", "Chaz", "Chaz", "m", null);
            User[] usersAndChaz = {ben, chaz, sally};
            accessor.create(usersAndChaz);

            fail("create() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot create users.*"));
            assertTrue(err.getMessage().matches("(U|.*u)sernames (are|were)? already taken.*"));
            assertTrue(err.getMessage().contains("'benguy'"));
            assertTrue(err.getMessage().contains("'sallydudette'"));
            assertFalse(err.getMessage().contains("'chazzyboi'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that users can be deleted
     */
    @Test
    @DisplayName("Delete users test")
    public void testDeleteUsers() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m");
            this.insertUser("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f");
            this.insertUser("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m");
            
            User[] results = accessor.getAll();
            assertEquals(3, results.length);
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User[] users = {ben, sally};
            accessor.delete(users);

            results = accessor.getAll();
            assertEquals(1, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that deleting users that don't exist throws errors
     */
    @Test
    @DisplayName("Delete users test -- users don't exist")
    public void testDeleteUsersErrors() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m");
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User guy = new User("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m", null);
            User[] users = {ben, guy, sally};
            accessor.delete(users);

            fail("delete() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot delete users.*"));
            assertTrue(err.getMessage().matches("(U|.*u)sernames (didn'?t|did not|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'benguy'"));
            assertTrue(err.getMessage().contains("'sallydudette'"));
            assertFalse(err.getMessage().contains("'someotherguy'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that users can be updated
     */
    @Test
    @DisplayName("Update users test")
    public void testUpdateUsers() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m");
            this.insertUser("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f");
            this.insertUser("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m");
            
            User ben = accessor.getByUsername("benguy");
            User sally = accessor.getByUsername("sallydudette");
            User guy = accessor.getByUsername("someotherguy");
            assertEquals("bens password", ben.getPassword());
            assertEquals("ben@email.test", ben.getEmail());
            assertEquals("sallys password", sally.getPassword());
            assertEquals("sally@email.test", sally.getEmail());
            assertEquals("guy password", guy.getPassword());
            
            ben.setPassword("bens *new* password");
            sally.setPassword("sallys *new* password");
            sally.setEmail("sallyNEW@email.test");
            User[] users = {ben, sally, guy};
            accessor.update(users);
            
            User benNew = accessor.getByUsername("benguy");
            User sallyNew = accessor.getByUsername("sallydudette");
            User guyUnchanged = accessor.getByUsername("someotherguy");
            assertEquals("bens *new* password", benNew.getPassword());
            assertEquals("ben@email.test", benNew.getEmail());
            assertEquals("sallys *new* password", sallyNew.getPassword());
            assertEquals("sallyNEW@email.test", sallyNew.getEmail());
            assertEquals("guy password", guyUnchanged.getPassword());
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that updating users that don't exist throws errors
     */
    @Test
    @DisplayName("Update users test -- users don't exist")
    public void testUpdateUsersErrors() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m");
            
            User ben = new User("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m", null);
            User sally = new User("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f", null);
            User guy = new User("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m", null);
            
            ben.setPassword("bens *new* password");
            sally.setPassword("sallys *new* password");
            sally.setEmail("sallyNEW@email.test");
            User[] users = {ben, sally, guy};
            accessor.update(users);

            fail("update() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot update users.*"));
            assertTrue(err.getMessage().matches("(U|.*u)sernames (didn'?t|did not|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'benguy'"));
            assertTrue(err.getMessage().contains("'sallydudette'"));
            assertFalse(err.getMessage().contains("'someotherguy'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that the user table can be cleared from the database
     */
    @Test
    @DisplayName("Clear user table test")
    public void testClearUsers() {
        try (Database database = new Database()) {
            UserAccessor accessor = new UserAccessor(database);
            this.insertUser("benguy", "bens password", "ben@email.test", "Ben", "Guy", "m");
            this.insertUser("sallydudette", "sallys password", "sally@email.test", "Sally", "Gally", "f");
            this.insertUser("someotherguy", "guy password", "guy@email.test", "Guy", "Dude", "m");
            assertEquals(3, accessor.getAll().length);

            accessor.clear();

            assertEquals(0, accessor.getAll().length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
}
