package dataAccess;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

enum StatementType {
    STRING,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
}

public class Database {
    private Connection connection;

    public Database() {
        String DATABASE_PATH = "main" + File.separator + "database.sqlite";
        this.connection = null;
    }

    /**
     * Creates a new prepared statement from the database.
     * If a connection to the database doesn't exist, this method will create one
     * 
     * @param sqlToExec is a string containing SQL code to load into the statement
     * @return the loaded statement
     */
    public PreparedStatement prepareStatement(String sqlToExec) {
        // TODO
        return null;
    }

    /**
     * Executes the statement as a query call to the database
     * 
     * @param statement is the prepared statement to execute
     * @param resultMapper is a callback that takes a statement result and turns it into a model
     * @return the ResultSet containg the query results
     */
    public <ModelType> ModelType[] query(PreparedStatement statement, QueryCallback<ModelType> resultMapper) {
        // TODO
        return null;
    }

    /**
     * Executes the statement as an insert/update/delete call to the database
     * 
     * @param statement is the prepared statement to executs
     * @return the number of modified rows
     */
    public int update(PreparedStatement statement) {
        // TODO
        return 0;
    }

    /** Commits executed statements to the database */
    public void commit() {
        // TODO
    }

    /** Clears the effects of executed statements to the last commit (or the start of the connection) */
    public void rollback() {
        // TODO
    }
}

interface QueryCallback<ModelType> {
    public ModelType call(ResultSet currentResult);
}
