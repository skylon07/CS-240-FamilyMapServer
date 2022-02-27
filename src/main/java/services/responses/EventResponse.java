package services.responses;

import models.Event;

/** Contains response data returned by the EventService */
public class EventResponse extends GenericResponse {
    /** The JSON-able list of all Events (for "all" requests) */
    public Event[] data;
    /** The "eventID" property of the target Event */
    public String eventID;
    /** The "associatedUsername" property of the target Event */
    public String associatedUsername;
    /** The "personID" property of the target Event */
    public String personID;
    /** The "latitude" property of the target Event */
    public float latitude;
    /** The "longitude" property of the target Event */
    public float longitude;
    /** The "country" property of the target Event */
    public String country;
    /** The "city" property of the target Event */
    public String city;
    /** The "eventType" property of the target Event */
    public String eventType;
    /** The "year" property of the target Event */
    public int year;
}
