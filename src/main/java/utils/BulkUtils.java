package utils;

import dataAccess.*;
import models.*;

// TODO: run javadoc to catch missing docstrings, like here
public class BulkUtils extends GenericUtility {
    public BulkUtils(Database database) {
        super(database);
    }

    public void clearDatabase() throws DatabaseException {
        // clear Users
        UserAccessor userAcc = new UserAccessor(this.database);
        userAcc.clear();
        
        // clear Persons
        PersonAccessor personAcc = new PersonAccessor(this.database);
        personAcc.clear();
        
        // clear Events
        EventAccessor eventAcc = new EventAccessor(this.database);
        eventAcc.clear();

        // clear AuthTokens
        AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(this.database);
        authTokenAcc.clear();
    }

    public void deleteUser(User user) {
        // TODO
    }

    public void loadIntoDatabase(User[] users, Person[] persons, Event[] events, AuthToken[] authTokens) throws BadAccessException, DatabaseException {
        // add users
        UserAccessor userAcc = new UserAccessor(this.database);
        userAcc.create(users);

        // add persons
        PersonAccessor personAcc = new PersonAccessor(this.database);
        personAcc.create(persons);

        // add events
        EventAccessor eventAccessor = new EventAccessor(this.database);
        eventAccessor.create(events);

        // add auth tokens
        AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(this.database);
        authTokenAcc.create(authTokens);
    }
}
