package services.requests;

/** Contains request data for the PersonService */
public class PersonRequest extends AuthorizedRequest {
    /** The Person ID of the target Person */
    public String personID;
    /** An indication that all Persons should be returned instead of just one */
    public boolean all;
}
