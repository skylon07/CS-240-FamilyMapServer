package services.requests;

/** Contains request data for the LoginService */
public class LoginRequest extends GenericRequest {
    /** The username of the target User */
    public String username;
    /** The user's password */
    public String password;
}
