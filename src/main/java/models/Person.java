package models;

/**
 * A collection of data describing a single person in their family tree in the
 * Family Map Application. This could represent a deceased person, a current
 * user of Family Map, or just someone who was present during an Event.
 */
public class Person {
    /** The unique identifier for this Person */
    private String personID;
    /** The username of the User this Person represents */
    private String associatedUsername;
    /** The first name of the person */
    private String firstName;
    /** The last name of the person */
    private String lastName;
    /** The person's gender, either "f" or "m" */
    private String gender;
    /** The Person ID of this person's father */
    private String fatherID;
    /** The Person ID of this person's mother */
    private String motherID;
    /** The Person ID of this person's spouse */
    private String spouseID;

    /**
     * Creates a Person by defining each property directly
     * 
     * @param personID the Person ID to use
     * @param associatedUsername the username of the User to tie to this person
     * @param firstName the person's first name
     * @param lastName the person's last name
     * @param gender the person's gender: "f" or "m"
     * @param fatherID the Person ID to use as this person's father (can be null)
     * @param motherID the Person ID to use as this person's mother (can be null)
     * @param spouseID the Person ID to use as this person's spouse (can be null)
     */
    public Person(String personID, String associatedUsername, String firstName, String lastName,
                  String gender, String fatherID, String motherID, String spouseID) {
        this.setPersonID(personID);
        this.setAssociatedUsername(associatedUsername);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setGender(gender);
        this.setFatherID(fatherID);
        this.setMotherID(motherID);
        this.setSpouseID(spouseID);
    }

    /**
     * Creates a Person by defining only required properties
     * (setting the rest to null)
     * 
     * @param personID
     * @param associatedUsername
     * @param firstName
     * @param lastName
     * @param gender
     */
    public Person(String personID, String associatedUsername,
                  String firstName, String lastName, String gender) {
        this(
            personID, associatedUsername, firstName, lastName, gender,
            null, null, null
        );
    }

	public String getPersonID() {
        return this.personID;
    }

	public void setPersonID(String personID) {
        this.personID = personID;
    }

	public String getAssociatedUsername() {
        return this.associatedUsername;
    }

	public void setAssociatedUsername(String associatedUsername) {
        this.associatedUsername = associatedUsername;
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

	public String getFatherID() {
        return this.fatherID;
    }

	public void setFatherID(String fatherID) {
        this.fatherID = fatherID;
    }

	public String getMotherID() {
        return this.motherID;
    }

	public void setMotherID(String motherID) {
        this.motherID = motherID;
    }

	public String getSpouseID() {
        return this.spouseID;
    }

	public void setSpouseID(String spouseID) {
        this.spouseID = spouseID;
    }
}
