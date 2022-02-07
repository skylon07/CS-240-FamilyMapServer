package services.responses;

/** Contains response data returned by the PersonService */
public class PersonResponse extends GenericResponse {
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
