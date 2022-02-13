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
                this.connection.rollback();
                this.connection.close();
                this.connection = null;
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }
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
            try (PreparedStatement statement = this.connection.prepareStatement(sqlToExec)) {
                return statement;
            }
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
     * @return an array of models that matched the query
     * @throws DatabaseException when a SQLException occurs
     */
    // suppress warnings generated from conversion of generic Object[] to ModelType[];
    // this is because models.toArray(new ModelType[]) cannot be used;
    // generic arrays can't be created for template types
    @SuppressWarnings("unchecked")
    public <ModelType> ModelType[] query(PreparedStatement statement, QueryCallback<ModelType> resultMapper) throws DatabaseException {
        try {
            ResultSet resultsIter = statement.executeQuery();
            ArrayList<ModelType> models = new ArrayList<ModelType>();
            while (resultsIter.next()) {
                ModelType model = resultMapper.call(resultsIter);
                models.add(model);
            }
            return (ModelType[]) models.toArray();
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
    }
    /**
     * The functional interface for callbacks passed to the Database.query function
     */
    interface QueryCallback<ModelType> {
        public ModelType call(ResultSet currentResult);
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
            String DATABASE_PATH = "main" + File.separator + "database.sqlite";
            this.connection = DriverManager.getConnection(DATABASE_PATH);
            // allows greater control of the transaction
            // (specifically, commit() and rollback())
            this.connection.setAutoCommit(false);
        }
    }
}
