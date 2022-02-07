package services.responses;

/** Contains response data returned by the RegisterService */
public class RegisterResponse extends GenericResponse {
    /** The newly generated authtoken */
    public String authtoken;
    /** The username of the target User */
    public String username;
    /** The personID of the target User */
    public String personID;
}
