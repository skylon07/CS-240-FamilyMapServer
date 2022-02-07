package dataAccess;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

/**
 * This is the main interface to the SQLite database. This layer of abstraction
 * allows accessors to modify/query the database without worrying (as much
 * as possible) about directly handling JDBC data types
 */
public class Database {
    /** The current connection object for the database */
    private Connection connection;

    /** 
     * Creates a database with no connection at first. The connection will be
     * checked/generated when any of the Database methods are called.
     */
    public Database() {
        this.connection = null;
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
        // TODO
        return null;
    }

    /**
     * Executes the statement as a query call to the database
     * 
     * @param <ModelType> is the model type expected from the query
     * @param statement is the prepared statement to execute
     * @param resultMapper is a callback that takes a statement result and turns it into a model instance
     * @return an array of models that matched the query
     * @throws DatabaseException when a SQLException occurs
     */
    public <ModelType> ModelType[] query(PreparedStatement statement, QueryCallback<ModelType> resultMapper) throws DatabaseException {
        // TODO
        return null;
    }

    /**
     * Executes the statement as an insert/update/delete call to the database
     * 
     * @param statement is the prepared statement to executs
     * @return the number of modified rows
     * @throws DatabaseException when a SQLException occurs
     */
    public int update(PreparedStatement statement) throws DatabaseException {
        // TODO
        return 0;
    }

    /**
     * Commits executed statements to the database
     * 
     * @throws DatabaseException when a SQLException occurs
     */
    public void commit() throws DatabaseException {
        // TODO
    }

    /**
     * Clears the effects of executed statements to the last commit (or the start of the connection)
     * 
     * @throws DatabaseException when a SQLException occurs
     */
    public void rollback() throws DatabaseException {
        // TODO
    }

    /**
     * Creates a new connection object and stores it as this.connection.
     * Creating the connection lazily (as opposed to in the constructor)
     * avoids throwing SQLExceptions when constructing.
     */
    private void createConnection() {
        String DATABASE_PATH = "main" + File.separator + "database.sqlite";
        // TODO
    }
}

interface QueryCallback<ModelType> {
    public ModelType call(ResultSet currentResult);
}
