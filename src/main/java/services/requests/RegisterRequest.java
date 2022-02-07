package services.requests;

/** Contains request data for the RegisterService */
public class RegisterRequest extends GenericRequest {
    /** The username of the target User */
    public String username;
    /** The new password */
    public String password;
    /** The User's email */
    public String email;
    /** The User's first name */
    public String firstName;
    /** The User's last name */
    public String lastName;
    /** The User's gender */
    public String gender;
}
