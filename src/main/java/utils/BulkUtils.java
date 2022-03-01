package utils;

import dataAccess.*;
import models.*;

// TODO: run javadoc to catch missing docstrings, like here
public class BulkUtils extends GenericUtility {
    public BulkUtils(Database database) {
        super(database);
    }

    public void clearDatabase() throws DatabaseException {
        this.database.reset();
    }

    public void deleteUsersAssociatedData(User user) throws DatabaseException, BadAccessException {
        // we DON'T want to delete the user or auth tokens
        
        // delete associated events
        EventAccessor eventAcc = new EventAccessor(this.database);
        Event[] associatedEvents = eventAcc.getAllForUser(user.getUsername());
        try {
            eventAcc.delete(associatedEvents);
        } catch (BadAccessException err) {
            throw new AssertionError("Complete list of associated events contained events that did not exist");
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
        try {
            personAcc.delete(associatedPersons);
        } catch (BadAccessException err) {
            throw new AssertionError("Complete list of persons included persons that didn't exist");
        }
    }

    public void loadIntoDatabase(User[] users, Person[] persons, Event[] events, AuthToken[] authTokens) throws DatabaseException {
        this.database.load(() -> {
            try {
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
            } catch (BadAccessException err) {
                throw new DatabaseException(err.getMessage());
            }
        });
    }
}
