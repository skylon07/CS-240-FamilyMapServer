package services.requests;

/** Contains request data for the EventService */
public class EventRequest extends AuthorizedRequest {
    /** The Event ID of the target Event */
    public String eventID;
}
