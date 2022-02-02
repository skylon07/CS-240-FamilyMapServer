package models;

/**
 * A representation of a person who has an account with the Family Map
 * application. This stores all the necessary data about the user to allow
 * them to interact with the application.
 */
public class User {
    /** A unique username for the user */
    private String username;
    /** The user's password, stored in glorious plaintext for any curious DB admins */
    private String password;
    /** The user's (unique) email address */
    private String email;
    /** The first name of the user */
    private String firstName;
    /** The last name of the user */
    private String lastName;
    /** The user's gender, either "f" or "m" */
    private String gender;
    /** The unique Person ID assigned to this user's Person representation */
    private String personID;

    /**
     * Creates a User by defining each property directly
     * 
     * @param username the user's username
     * @param password the user's password
     * @param email the user's email
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param gender the user's gender: "f" or "m"
     * @param personID the Person ID to tie to the user
     */
    public User(String username, String password, String email,
                String firstName, String lastName, String gender, String personID) {
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setGender(gender);
        this.setPersonID(personID);
    }

	public String getUsername() {
        return this.username;
    }
    
	public void setUsername(String username) {
        this.username = username;
    }

	public String getPassword() {
        return this.password;
    }
    
	public void setPassword(String password) {
        this.password = password;
    }

	public String getEmail() {
        return this.email;
    }
    
	public void setEmail(String email) {
        this.email = email;
    }

	public String getFirstName() {
        return this.firstName;
    }
    
	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getLastName() {
        return this.lastName;
    }
    
	public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public String getGender() {
        return this.gender;
    }
    
	public void setGender(String gender) {
        this.gender = gender;
    }

	public String getPersonID() {
        return this.personID;
    }
    
	public void setPersonID(String personID) {
        this.personID = personID;
    }
}
