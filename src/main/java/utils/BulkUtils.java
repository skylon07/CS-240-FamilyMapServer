package utils;

import dataAccess.*;
import models.*;

/**
 * Contains helpful functions that involve operating on all models in the database
 */
public class BulkUtils extends GenericUtility {
    /**
     * Creates a new BulkUtils instance, given some database to work on
     * 
     * @param database is the database to work on
     */
    public BulkUtils(Database database) {
        super(database);
    }

    /**
     * Resets all data in the database. I mean everything. Like, drop all tables kind of clearing.
     * 
     * @throws DatabaseException whenever the database doesn't feel like working...
     */
    public void clearDatabase() throws DatabaseException {
        this.database.reset();
    }

    /**
     * Removes anything that has an "associatedUsername" pointing to the user
     * 
     * @param user is the user to clear associated data for
     * @throws BadAccessException when the user doesn't exist
     * @throws DatabaseException when the database is sick and doesn't feel like working
     */
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

    /**
     * Loads data for each table into the database. Equivelant for calling create() on each Accessor.
     * This function does not remove any data from the database.
     * 
     * @param users is the array of User instances to create
     * @param persons is the array of Person instances to create
     * @param events is the array of Event instances to create
     * @param authTokens is the array of AuthToken instances to create
     * @throws DatabaseException when the database throws up (a sql error, that is)
     */
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
