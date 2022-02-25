package dataAccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import models.AuthToken;

import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * Contains the test cases that ensure the PersonAccessor class runs correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthTokenAccessorTest {
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
                "values ('baseUser', 'password', 'base@email.test', 'Base', 'User', 'm')";
            database.update(sqlStr);
            database.commit();
        }
    }

    /**
     * Insert arbitrary AuthTokens into the database, bypassing AuthTokenAccessor.
     * Username property defaults to "baseUser".
     * 
     * @param personID is the person ID to insert
     * @param firstname is the first name of the new Person
     * @param lastname is the last name of the new Person
     * @param gender is the gender of the new person, 'm' or 'f'
     */
    private void insertAuthToken(String authToken) {
        try {
            String sqlStr =
                "insert into authtoken\n" + 
                "   (username, authtoken)\n" + 
                "values ('baseUser', ?)";
            try (Database database = new Database()) {
                PreparedStatement statement = database.prepareStatement(sqlStr);
                statement.setString(1, authToken);
                database.update(statement);
                database.commit();
            }
        } catch (Exception err) {
            System.out.println("An error occured in insertAuthToken()");
            throw new Error(err.getMessage());
        }                     
    }

    /**
     * Runs insertAuthToken() with a bunch of filler data for get-testing
     */
    private void fillAuthTokens() {
        try {
            this.insertAuthToken("auth token 1");
            this.insertAuthToken("auth token 2");
            this.insertAuthToken("auth token 3");
            this.insertAuthToken("1234567890");
        } catch (Exception err) {
            System.out.println("An error occured in fillAuthTokens()");
            throw new Error(err.getMessage());
        }
    }

    private AuthToken[] getAllAuthTokens(Database database) throws DatabaseException {
        String sqlStr = "select * from authtoken";
        ArrayList<AuthToken> authTokens = database.query(sqlStr, (result) -> {
            String authtoken = result.getString(1);
            String username = result.getString(2);
            AuthToken authToken = new AuthToken(authtoken, username);
            return authToken;
        });
        return authTokens.toArray(new AuthToken[authTokens.size()]);
    }

    /**
     * Ensures an auth token can be acquired by its token string
     */
    @Test
    @DisplayName("Get existing auth token test -- by auth token")
    public void testGetExistingAuthTokenByAuthToken() {
        this.fillAuthTokens();
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken authToken = accessor.getByAuthToken("auth token 1");
            assertEquals("auth token 1", authToken.getAuthtoken());
            assertEquals("baseUser", authToken.getUsername());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures null is returned trying to get an auth token that does not exist
     */
    @Test
    @DisplayName("Get non-existing auth token test -- by auth token")
    public void testGetNonExistingAuthTokenByAuthToken() {
        this.fillAuthTokens();
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken authToken = accessor.getByAuthToken("auth token that does not exist");
            assertNull(authToken);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an auth token can be acquired by just a username
     */
    @Test
    @DisplayName("Get existing auth token test -- by username")
    public void testGetExistingAuthTokenByUsername() {
        this.fillAuthTokens();
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken[] authTokens = accessor.getByUsername("baseUser");
            assertEquals(4, authTokens.length);
            AuthToken numberToken = null;
            for (AuthToken authToken : authTokens) {
                if (authToken.getAuthtoken().equals("1234567890")) {
                    if (numberToken != null) {
                        fail("Duplicate entry found");
                    }
                    numberToken = authToken;
                }
            }
            assertEquals("baseUser", numberToken.getUsername());
            assertEquals("1234567890", numberToken.getAuthtoken());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures an empty array is returned when a username doesn't have auth tokens on it
     */
    @Test
    @DisplayName("Get non-existing auth token test -- by username")
    public void testGetNonExistingAuthTokenByUsername() {
        this.fillAuthTokens();
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken[] authTokens = accessor.getByUsername("someNonexistantUser");
            assertEquals(0, authTokens.length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that arbitrary auth tokens can be checked that they exist in the database
     */
    @Test
    @DisplayName("Check auth token exist test")
    public void testCheckExists() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token1");
            this.insertAuthToken("token2");
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken token3NotInDB = new AuthToken("token3NotInDB", "baseUser");
            AuthToken[] authTokens = {token1, token2, token3NotInDB};
            boolean[] exists = accessor.exists(authTokens);

            boolean[] expectedExists = {true, true, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that an empty database contains no existing auth tokens
     */
    @Test
    @DisplayName("Check auth token exist test -- empty database")
    public void testCheckExistsWithEmptyDB() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken[] authTokens = {token1, token2};
            boolean[] exists = accessor.exists(authTokens);

            boolean[] expectedExists = {false, false};
            assertArrayEquals(expectedExists, exists);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures new auth tokens can be created
     */
    @Test
    @DisplayName("Create new auth token test")
    public void testCreateNewAuthToken() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken[] results = this.getAllAuthTokens(database);
            assertEquals(0, results.length);
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken[] authTokens = {token1, token2};
            accessor.create(authTokens);

            results = this.getAllAuthTokens(database);
            assertEquals(2, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that re-creating auth tokens errors
     */
    @Test
    @DisplayName("Create new auth token test -- error on re-create")
    public void testCreateNewAuthTokenErrors() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            AuthToken[] results = this.getAllAuthTokens(database);
            assertEquals(0, results.length);
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken[] authTokens = {token1, token2};
            accessor.create(authTokens);

            results = this.getAllAuthTokens(database);
            assertEquals(2, results.length);

            AuthToken token3 = new AuthToken("token3", "baseUser");
            AuthToken[] authTokensAnd3 = {token1, token2, token3};
            accessor.create(authTokensAnd3);

            fail("create() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot create auth tokens.*"));
            assertTrue(err.getMessage().matches("(A|.*a)uth[ ]?tokens (are|were)? already (taken|used).*"));
            assertTrue(err.getMessage().contains("'token1'"));
            assertTrue(err.getMessage().contains("'token2'"));
            assertFalse(err.getMessage().contains("'token3'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that auth tokens can be deleted
     */
    @Test
    @DisplayName("Delete auth token test")
    public void testDeleteAuthToken() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token1");
            this.insertAuthToken("token2");
            this.insertAuthToken("token3");
            
            AuthToken[] results = this.getAllAuthTokens(database);
            assertEquals(3, results.length);
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken[] authTokens = {token1, token2};
            accessor.delete(authTokens);

            results = this.getAllAuthTokens(database);
            assertEquals(1, results.length);
        } catch (BadAccessException err) {
            this.failNoTraceback(err);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that deleting auth tokens that don't exist throws errors
     */
    @Test
    @DisplayName("Delete auth token test -- auth tokens don't exist")
    public void testDeleteAuthTokenErrors() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token3");
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken token3 = new AuthToken("token3", "baseUser");
            AuthToken[] authTokens = {token1, token2, token3};
            accessor.delete(authTokens);

            fail("delete() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot delete auth tokens.*"));
            assertTrue(err.getMessage().matches("(A|.*a)uth[ ]?tokens (do not|did not|didn'?t|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'token1'"));
            assertTrue(err.getMessage().contains("'token2'"));
            assertFalse(err.getMessage().contains("'token3'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
    
    /**
     * Ensures that people can be updated
     */
    @Test
    @DisplayName("Update auth token test")
    public void testUpdateAuthToken() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token1");
            this.insertAuthToken("token2");
            String sqlStr =
                "insert into user (username, password, email, firstname, lastname, gender)\n" + 
                "values ('baseUser2', 'password', 'base2@email.test', 'Base2', 'User2', 'f')";
            database.update(sqlStr);
            database.commit();
            
            AuthToken token1 = accessor.getByAuthToken("token1");
            AuthToken token2 = accessor.getByAuthToken("token2");
            assertEquals("baseUser", token1.getUsername());
            assertEquals("baseUser", token2.getUsername());

            token2.setUsername("baseUser2");
            AuthToken[] authTokens = {token1, token2};
            accessor.update(authTokens);
            
            AuthToken token1New = accessor.getByAuthToken("token1");
            AuthToken token2New = accessor.getByAuthToken("token2");
            assertEquals("baseUser", token1New.getUsername());
            assertEquals("baseUser2", token2New.getUsername());
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
    @DisplayName("Update auth token test -- auth tokens don't exist")
    public void testUpdateAuthTokenErrors() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token3");
            String sqlStr =
                "insert into user (username, password, email, firstname, lastname, gender)\n" + 
                "values ('baseUser2', 'password', 'base2@email.test', 'Base2', 'User2', 'f')";
            database.update(sqlStr);
            database.commit();
            
            AuthToken token1 = new AuthToken("token1", "baseUser");
            AuthToken token2 = new AuthToken("token2", "baseUser");
            AuthToken token3 = new AuthToken("token3", "baseUser");

            token2.setUsername("baseUser2");
            AuthToken[] authTokens = {token1, token2, token3};
            accessor.update(authTokens);

            fail("update() should have thrown a BadAccessException");
        } catch (BadAccessException err) {
            assertTrue(err.getMessage().matches("(C|.*c)annot update auth tokens.*"));
            assertTrue(err.getMessage().matches("(A|.*a)uth[ ]?tokens (do not|did not|didn'?t|don'?t)? exist.*"));
            assertTrue(err.getMessage().contains("'token1'"));
            assertTrue(err.getMessage().contains("'token2'"));
            assertFalse(err.getMessage().contains("'token3'"));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensures that the auth token table can be cleared from the database
     */
    @Test
    @DisplayName("Clear auth token table test")
    public void testClearAuthTokens() {
        try (Database database = new Database()) {
            AuthTokenAccessor accessor = new AuthTokenAccessor(database);
            this.insertAuthToken("token1");
            this.insertAuthToken("token2");
            assertEquals(2, this.getAllAuthTokens(database).length);

            accessor.clear();

            assertEquals(0, this.getAllAuthTokens(database).length);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
}
