package models;

/**
 * A representation of some event that people were a part of. Each Event gives
 * a description of where/when it happened, what person was present, their
 * associated username, and what kind of event it is.
 */
public class Event {
    /** The unique identifier for this Event */
    private String eventID;
    /** The username of the User whose family tree includes this event */
    private String associatedUsername;
    /** The Person ID of the Person tied to this event */
    private String personID;
    /** The latitude of the event's location */
    private float latitude;
    /** The longitude of the event's location */
    private float longitude;
    /** The country where the event occurred */
    private String country;
    /** The city where the event occurred */
    private String city;
    /** A short description of what kind of event this is */
    private String eventType;
    /** The year this event occurred */
    private int year;

    /**
     * Creates an Event by defining each property directly (converting doubles to floats)
     * 
     * @param eventID is the identifier for the event
     * @param associatedUsername is the username to tie the event to
     * @param personID is the Person ID this event belongs to
     * @param latitude is the event location's latitude
     * @param longitude is the event location's longitude
     * @param country is the event location's country
     * @param city  isthe event location's city
     * @param eventType is the type of the event
     * @param year is the year the event happened
     */
    public Event(String eventID, String associatedUsername, String personID,
                 double latitude, double longitude, String country, String city,
                 String eventType, int year) {
        this(eventID, associatedUsername, personID, (float) latitude, (float) longitude, country, city, eventType, year);
    }

    /**
     * Creates an Event by defining each property directly
     * 
     * @param eventID is the identifier for the event
     * @param associatedUsername is the username to tie the event to
     * @param personID is the Person ID this event belongs to
     * @param latitude is the event location's latitude
     * @param longitude is the event location's longitude
     * @param country is the event location's country
     * @param city  isthe event location's city
     * @param eventType is the type of the event
     * @param year is the year the event happened
     */
    public Event(String eventID, String associatedUsername, String personID,
                 float latitude, float longitude, String country, String city,
                 String eventType, int year) {
        this.setEventID(eventID);
        this.setAssociatedUsername(associatedUsername);
        this.setPersonID(personID);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setCountry(country);
        this.setCity(city);
        this.setEventType(eventType);
        this.setYear(year);
    }

	public String getEventID() {
        return this.eventID;
    }

	public void setEventID(String eventID) {
        this.eventID = eventID;
    }

	public String getAssociatedUsername() {
        return this.associatedUsername;
    }

	public void setAssociatedUsername(String associatedUsername) {
        this.associatedUsername = associatedUsername;
    }

	public String getPersonID() {
        return this.personID;
    }

	public void setPersonID(String personID) {
        this.personID = personID;
    }

	public float getLatitude() {
        return this.latitude;
    }

	public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

	public void setLatitude(double latitude) {
        this.latitude = (float) latitude;
    }

	public float getLongitude() {
        return this.longitude;
    }

	public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

	public void setLongitude(double longitude) {
        this.longitude = (float) longitude;
    }

	public String getCountry() {
        return this.country;
    }

	public void setCountry(String country) {
        this.country = country;
    }

	public String getCity() {
        return this.city;
    }

	public void setCity(String city) {
        this.city = city;
    }

	public String getEventType() {
        return this.eventType;
    }

	public void setEventType(String eventType) {
        this.eventType = eventType;
    }

	public int getYear() {
        return this.year;
    }

	public void setYear(int year) {
        this.year = year;
    }
}
