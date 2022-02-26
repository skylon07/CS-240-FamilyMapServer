package utils;

import dataAccess.*;
import models.User;

// TODO: run javadoc to catch missing docstrings, like here
public class BulkUtils extends GenericUtility {
    public BulkUtils(Database database) {
        super(database);
    }

    public void clearDatabase() throws DatabaseException {
        // clear Users
        UserAccessor userAcc = new UserAccessor(database);
        userAcc.clear();
        
        // clear Persons
        PersonAccessor personAcc = new PersonAccessor(database);
        personAcc.clear();
        
        // clear Events
        EventAccessor eventAcc = new EventAccessor(database);
        eventAcc.clear();

        // clear AuthTokens
        AuthTokenAccessor authTokenAcc = new AuthTokenAccessor(database);
        authTokenAcc.clear();
    }

    public void deleteUser(User user) {
        // TODO
    }

    public void loadJSONToDatabase(String json) {
        // TODO
    }
}
