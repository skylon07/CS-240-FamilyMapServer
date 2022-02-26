package services.requests;

import models.*;

/** Contains request data for the LoadService */
public class LoadRequest extends GenericRequest {
    /** The array of Users to load into the database */
    public User[] users;
    /** The array of Persons to load into the database */
    public Person[] persons;
    /** The array of Events to load into the database */
    public Event[] events;
}
