package dataAccess;

import java.sql.*;
import java.util.ArrayList;

/**
 * This is the main interface to the SQLite database. This layer of abstraction
 * allows accessors to modify/query the database without worrying (as much
 * as possible) about directly handling JDBC data types
 */
public class Database implements AutoCloseable {
    /** Indicates if the testing database should be used or not */
    static private boolean shouldUseTestDB;

    static public void useTestDB() {
        Database.shouldUseTestDB = true;
    }

    /** The current connection object for the database */
    private Connection connection;
    /** A list of PreparedStatements that need to be closed */
    private ArrayList<PreparedStatement> openStatements;

    /** 
     * Creates a database with no connection at first. The connection will be
     * checked/generated when any of the Database methods are called.
     */
    public Database() {
        this.connection = null;
        this.openStatements = new ArrayList<>();
    }

    /**
     * A method called by the garbage collector; This is just a checker
     * to help ensure that connections are closed properly
     * 
     * @throws DatabaseException when an active connection still exists
    */
    protected void finalize() throws DatabaseException {
        if (this.connection != null) {
            System.out.println("A Database was deconstructed without closing!");
            throw new DatabaseException("A Database was deconstructed without closing!");
        }
    }

    /**
     * A deconstructor that destroys the active connection
     * (if one was ever created)
     * 
     * @throws DatabaseException if the connection throws a SQLException in rollback() or close()
     */
    public void close() throws DatabaseException {
        if (this.connection != null) {
            try {
                for (PreparedStatement statement : this.openStatements) {
                    // "calling the method when it is already closed has no effect"
                    statement.close();
                }
                this.connection.rollback();
                this.connection.close();
                this.connection = null;
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }
    }

    /**
     * Clears and resets all tables in the database, rolling back any current changes
     * 
     * @throws DatabaseException when a SQLException occurs
     */
    public void reset() throws DatabaseException {
        // ignore any pending changes
        if (this.connection != null) {
            this.rollback();
        }

        // generate statements
        // TODO: figure out a way to get these into a file and load
        //       into multiple statements (each statement can only execute
        //       one sql statement)
        String[] statements = {
            // clear tables
            "pragma foreign_keys = off",
            "drop table if exists user",
            "drop table if exists person",
            "drop table if exists event",
            "drop table if exists authtoken",
            "drop table if exists enum_gender",
            "pragma foreign_keys = on",

            // create tables
            "create table enum_gender(\n" + 
            "   gender     text    not null    primary key  \n" + 
            ")",

            "insert into enum_gender(gender) values\n" + 
            "   ('f'),\n" + 
            "   ('m')",

            "create table user (\n" + 
            "    username	text	not null	primary key,            \n" + 
            "    password 	text	not null,                           \n" + 
            "    email 		text	not null,                           \n" + 
            "    firstName	text	not null,                           \n" + 
            "    lastName	text	not null,                           \n" + 
            "    gender		text	not null,                           \n" + 
            "    personID	text				unique,                 \n" + 
            "    foreign key(gender)		references enum_gender,     \n" + 
            "    foreign key(personID)      references person(personID) \n" + 
            ")\n",

            "create table person(\n" + 
            "    personID			text	not null	primary key,                \n" + 
            "    associatedUsername	text	not null,                               \n" + 
            "    firstName			text	not null,                               \n" + 
            "    lastName			text 	not null,                               \n" + 
            "    gender				text	not null,                               \n" + 
            "    fatherID			text,                                           \n" + 
            "    motherID			text,                                           \n" + 
            "    spouseID			text,                                           \n" + 
            "    foreign key(gender)				references enum_gender,         \n" + 
            "    foreign key(associatedUsername)	references user(username),      \n" + 
            "    foreign key(fatherID)			    references person(personID),    \n" + 
            "    foreign key(motherID)			    references person(personID),    \n" + 
            "    foreign key(spouseID)			    references person(personID)     \n" + 
            ")\n",
            
            "create table event(\n" + 
            "    eventID				text	not null	primary key,        \n" + 
            "    associatedUsername	    text	not null,                       \n" + 
            "    personID			    text	not null,                       \n" + 
            "    latitude			    real	not null,                       \n" + 
            "    longitude			    real	not null,                       \n" + 
            "    country				text 	not null,                       \n" + 
            "    city				    text	not null,                       \n" + 
            "    eventType			    text 	not null,                       \n" + 
            "    year				    int		not null                        \n" + 
            "        check(year > 0),                                           \n" + 
            "    foreign key(associatedUsername)	references user(username),  \n" + 
            "    foreign key(personID)			    references person(personID)     \n" + 
            ")\n",

            "create table authtoken(\n" + 
            "    authtoken 	text	not null	primary key,        \n" + 
            "    username	text	not null,                       \n" + 
            "    foreign key(username)	references user(username)   \n" + 
            ")\n",
        };

        // execute sql code
        for (String statementStr : statements) {
            PreparedStatement statement = this.prepareStatement(statementStr);
            this.execute(statement);
        }
        this.commit();
    }

    /**
     * Returns the currently active connection, if any
     * 
     * @return the connection, if one is present, or null
     */
    public Connection getActiveConnection() {
        return this.connection;
    }

    /**
     * Creates a new prepared statement from the database.
     * If a connection to the database doesn't exist, this method will create one
     * 
     * @param sqlToExec is a string containing SQL code to load into the statement
     * @return the loaded statement
     * @throws DatabaseException when a SQLException occurs
     */
    public PreparedStatement prepareStatement(String sqlToExec) throws DatabaseException {
        try {
            this.initializeConnectionIfNoneExists();
            PreparedStatement statement = this.connection.prepareStatement(sqlToExec);
            this.openStatements.add(statement);
            return statement;
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
    }

    /**
     * Executes the statement as a query call to the database
     * 
     * @param <ModelType> is the model type expected from the query
     * @param statement is the prepared statement to execute
     * @param resultMapper is a callback that takes a statement result and turns it into a model instance
     * @return an ArrayList of models that matched the query
     * @throws DatabaseException when a SQLException occurs
     */
    public <ModelType> ArrayList<ModelType> query(PreparedStatement statement, QueryCallback<ModelType> resultMapper) throws DatabaseException {
        try {
            ResultSet resultsIter = statement.executeQuery();
            ArrayList<ModelType> models = new ArrayList<>();
            while (resultsIter.next()) {
                ModelType model = resultMapper.call(resultsIter);
                models.add(model);
            }
            return models;
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
    }

    /**
     * Executes the statement as a query call to the database
     * 
     * @param <ModelType> is the model type expected from the query
     * @param statement is the prepared statement to execute
     * @param resultMapper is a callback that takes a statement result and turns it into a model instance
     * @return an ArrayList of models that matched the query
     * @throws DatabaseException when a SQLException occurs
     */
    public <ModelType> ArrayList<ModelType> query(String statement, QueryCallback<ModelType> resultMapper) throws DatabaseException {
        return this.query(this.prepareStatement(statement), resultMapper);
    }

    /**
     * The functional interface for callbacks passed to the Database.query function
     */
    interface QueryCallback<ModelType> {
        public ModelType call(ResultSet currentResult) throws SQLException, DatabaseException;
    }

    /**
     * Executes the statement as an insert/update/delete call to the database
     * 
     * @param statement is the prepared statement to executs
     * @return the number of modified rows
     * @throws DatabaseException when a SQLException occurs
     */
    public int update(PreparedStatement statement) throws DatabaseException {
        try {
            return statement.executeUpdate();
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
    }

    /**
     * Executes the statement as an insert/update/delete call to the database
     * 
     * @param statement is the prepared statement to executs
     * @return the number of modified rows
     * @throws DatabaseException when a SQLException occurs
     */
    public int update(String statement) throws DatabaseException {
        return this.update(this.prepareStatement(statement));
    }

    /**
     * Executes the statement in its current state
     * 
     * @param statement is the prepared statement to execute
     * @return the value obtained by statement.execute(); "true if the first result is a ResultSet object; false if the first result is an update count or there is no result"
     * @throws DatabaseException when a SQLException occurs
     */
    public boolean execute(PreparedStatement statement) throws DatabaseException {
        try {
            return statement.execute();
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
    }

    /**
     * Executes the statement in its current state
     * 
     * @param statement is the prepared statement to execute
     * @return the value obtained by statement.execute(); "true if the first result is a ResultSet object; false if the first result is an update count or there is no result"
     * @throws DatabaseException when a SQLException occurs
     */
    public boolean execute(String statement) throws DatabaseException {
        return this.execute(this.prepareStatement(statement));
    }

    /**
     * Commits executed statements to the database
     * 
     * @throws DatabaseException when a SQLException occurs
     */
    public void commit() throws DatabaseException {
        if (this.connection != null) {
            try {
                this.connection.commit();
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        } else {
            throw new DatabaseException("Cannot commit(); No connection was opened");
        }
    }

    /**
     * Clears the effects of executed statements to the last commit (or the start of the connection)
     * 
     * @throws DatabaseException when a SQLException occurs
     */
    public void rollback() throws DatabaseException {
        if (this.connection != null) {
            try {
                this.connection.rollback();
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        } else {
            throw new DatabaseException("Cannot rollback(); No connection was opened");
        }
    }

    /**
     * Creates a new connection object and stores it as this.connection.
     * Creating the connection lazily (as opposed to in the constructor)
     * avoids throwing SQLExceptions when constructing.
     * 
     * @throws SQLException if the DriverManager cannot create a connection
     */
    private void initializeConnectionIfNoneExists() throws SQLException {
        if (this.connection == null) {
            String DATABASE_PATH;
            if (Database.shouldUseTestDB) {
                DATABASE_PATH = "jdbc:sqlite:database_forTesting.sqlite";
            } else {
                DATABASE_PATH = "jdbc:sqlite:database.sqlite";
            }
            this.connection = DriverManager.getConnection(DATABASE_PATH);
            // allows greater control of the transaction
            // (specifically, commit() and rollback())
            this.connection.setAutoCommit(false);
        }
    }
}
