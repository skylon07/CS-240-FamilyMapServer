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

    public void deleteUserAndAssociatedData(User user) throws DatabaseException, BadAccessException {
        // delete associated events
        EventAccessor eventAcc = new EventAccessor(this.database);
        Event[] associatedEvents = eventAcc.getAllForUser(user.getUsername());
        try {
            eventAcc.delete(associatedEvents);
        } catch (BadAccessException err) {
            assert false : "Complete list of associated events contained events that did not exist";
        }
        
        // before deleting people, we have to de-reference the User personID
        // (because it is a foreign key reference)
        UserAccessor userAcc = new UserAccessor(this.database);
        user.setPersonID(null);
        User[] users = {user};
        try {
            userAcc.update(users);
        } catch (BadAccessException err) {
            throw err; // you shouldn't have given me a user that doesn't exist!
        }

        // okay, now we can delete people
        PersonAccessor personAcc = new PersonAccessor(this.database);
        Person[] associatedPersons = personAcc.getAllForUser(user.getUsername());
        personAcc.delete(associatedPersons);

        // finally, delete the actual User object
        userAcc.delete(users);
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
