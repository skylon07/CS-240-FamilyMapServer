package services.responses;

import models.Person;

/** Contains response data returned by the PersonService */
public class PersonResponse extends GenericResponse {
    /** The JSON-able list of all Persons (for "all" requests) */
    public Person[] data;
    /** The "personID" property of the target Person */
    public String personID;
    /** The "associatedUsername" property of the target Person */
    public String associatedUsername;
    /** The "firstName" property of the target Person */
    public String firstName;
    /** The "lastName" property of the target Person */
    public String lastName;
    /** The "gender" property of the target Person */
    public String gender;
    /** The "fatherID" property of the target Person */
    public String fatherID;
    /** The "motherID" property of the target Person */
    public String motherID;
    /** The "spouseID" property of the target Person */
    public String spouseID;
}
