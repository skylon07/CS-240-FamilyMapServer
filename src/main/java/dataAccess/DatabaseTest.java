package dataAccess;

import java.sql.*;
import java.util.ArrayList;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataAccess.Database.QueryCallback;


/**
 * Contains the test cases that ensure the Database class runs correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTest {
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
     * Creates a new database, resetting if indicated
     * 
     * @param reset indicates if the database should be reset after being created
     * @return the new database, reset, and prepared for testing
     * @throws DatabaseException whenever Database.reset() does
     */
    private Database createDatabase(boolean reset) throws DatabaseException {
        Database db = new Database();
        if (reset) {
            db.reset();
        }
        return db;
    }

    /**
     * Overloaded form of createDatabase(), where reset defaults to true
     * 
     * @return the new database, prepared for testing
     * @throws DatabaseException whenever Database.reset() does
     */
    private Database createDatabase() throws DatabaseException {
        return this.createDatabase(true);
    }

    /**
     * Signals the Database class to use the testing database for testing
     */
    @BeforeAll
    static public void useTestDB() {
        Database.useTestDB();
    }
    
    /**
     * Ensures that a blank database can be created/destructed with no further actions
     */
    @Test
    @Order(1) // execute this test before others
    @DisplayName("Database construction/destruction test")
    public void testConstructorAndDestructor() {
        Database database;
        try {
            database = this.createDatabase(false);
            database.close();
        } catch (Exception err) {
            this.failNoTraceback(err);
        }
        database = null;
    }

    /**
     * Tests that the database can clear data
     */
    @Test
    @Order(2) // execute this test before others
    @DisplayName("Database clearing/resetting test")
    public void testDBClear() {
        try (Database database = this.createDatabase(false)) {
            database.reset();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            ArrayList<String> people = database.query(
                database.prepareStatement("select personID from person"),
                mapFirstToString
            );
            ArrayList<String> events = database.query(
                database.prepareStatement("select eventID from event"),
                mapFirstToString
            );
            ArrayList<String> tokens = database.query(
                database.prepareStatement("select authtoken from authtoken"),
                mapFirstToString
            );
            assertEquals(0, users.size());
            assertEquals(0, people.size());
            assertEquals(0, events.size());
            assertEquals(0, tokens.size());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Ensure that the database loads a connection lazily
     */
    @Test
    @DisplayName("Database connection loading test")
    public void testConnectionLoadsLazily() {
        try (Database database = this.createDatabase(false)) {
            assertNull(database.getActiveConnection());
            database.reset();
            assertNull(database.getActiveConnection());
            database.close();
            assertNull(database.getActiveConnection());
        } catch (Exception err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that an "empty" statement can be created
     */
    @Test
    @DisplayName("Database 'empty' statement creation test")
    public void testStatementPreparation() {
        try (Database database = this.createDatabase()) {
            // ...because PreparedStatements error with empty strings
            String nonEmptyStringThatDoesntDoAnything = "drop table if exists THiS_SHoULDnT_EVeR_ExIST";
            PreparedStatement statement = database.prepareStatement(nonEmptyStringThatDoesntDoAnything);
            assertNotNull(statement);
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that the database can commit a transaction
     */
    @Test
    @DisplayName("Transaction commit test")
    public void testCommitTransaction() {
        try (Database database = this.createDatabase()) {
            String sqlStr = 
                "insert into user(\n" + 
                "    username,  			firstName,	email,\n" +
                "    password,  			lastName, 	gender\n" +
                ") values(\n" +
                "    'test1',			    'Tester',	'test1@test.test',\n" +
                "    'my test password',    'TestGuy', 	'm'\n" +
                ")";
            database.execute(database.prepareStatement(sqlStr));
            database.commit();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            assertEquals(1, users.size());
            assertEquals("test1", users.get(0));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that the database can rollback a transaction
     */
    @Test
    @DisplayName("Transaction rollback test")
    public void testRollbackTransaction() {
        try (Database database = this.createDatabase()) {
            String sqlStr = 
                "insert into user(\n" + 
                "    username,  			firstName,	email,\n" +
                "    password,  			lastName, 	gender\n" +
                ") values(\n" +
                "    'test1',			    'Tester',	'test1@test.test',\n" +
                "    'my test password',    'TestGuy', 	'm'\n" +
                ")";
            database.execute(database.prepareStatement(sqlStr));
            database.rollback();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            assertEquals(0, users.size());
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that the database can insert data
     */
    @Test
    @DisplayName("Database data insertion test")
    public void testInsertingData() {
        try (Database database = this.createDatabase()) {
            String[] sqlStatements = {
                // partially initialize user
                "insert into user(\n" + 
                "    username,  			firstName,	email,\n" +
                "    password,  			lastName, 	gender\n" +
                ") values(\n" +
                "    'test1',			    'Tester',	'test1@test.test',\n" +
                "    'my test password',    'TestGuy', 	'm'\n" +
                ")",

                // initialize an associated person
                "insert into person(\n" +
                "    personID,              firstName,	gender,\n" +
                "    associatedUsername,    lastName\n" +
                ") values(\n" +
                "    'person1',			    'Tester',	'm',\n" +
                "    'test1', 			    'TestGuy'\n" +
                ")",

                // finish user initialization
                "update user\n" +
                "    set personID = 'person1'\n" +
                "    where username == 'test1'",
            };
            for (String statementStr : sqlStatements) {
                PreparedStatement statement = database.prepareStatement(statementStr);
                database.update(statement);
            }
            database.commit();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            ArrayList<String> people = database.query(
                database.prepareStatement("select personID from person"),
                mapFirstToString
            );
            assertEquals(1, users.size());
            assertEquals(1, people.size());
            assertEquals("test1", users.get(0));
            assertEquals("person1", people.get(0));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that the database can query data
     */
    @Test
    @DisplayName("Database data query test")
    public void testQueryData() {
        try (Database database = this.createDatabase()) {
            String sqlStr = 
                "insert into user(\n" + 
                "    username,  			firstName,	email,\n" +
                "    password,  			lastName, 	gender\n" +
                ") values(\n" +
                "    'test1',			    'Tester',	'test1@test.test',\n" +
                "    'my test password',    'TestGuy', 	'm'\n" +
                ")";
            database.execute(database.prepareStatement(sqlStr));
            database.commit();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            assertEquals(1, users.size());
            assertEquals("test1", users.get(0));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }

    /**
     * Tests that the database can execute SQL generally
     */
    @Test
    @DisplayName("Database generic execution test")
    public void testExecuteSQL() {
        try (Database database = this.createDatabase()) {
            String sqlStr = 
                "insert into user(\n" + 
                "    username,  			firstName,	email,\n" +
                "    password,  			lastName, 	gender\n" +
                ") values(\n" +
                "    'test1',			    'Tester',	'test1@test.test',\n" +
                "    'my test password',    'TestGuy', 	'm'\n" +
                ")";
            database.execute(database.prepareStatement(sqlStr));
            database.commit();

            QueryCallback<String> mapFirstToString = (result) -> {
                try {
                    return result.getString(1);
                } catch (SQLException err) {
                    return null;
                }
            };
            ArrayList<String> users = database.query(
                database.prepareStatement("select username from user"),
                mapFirstToString
            );
            assertEquals(1, users.size());
            assertEquals("test1", users.get(0));
        } catch (DatabaseException err) {
            this.failNoTraceback(err);
        }
    }
}
