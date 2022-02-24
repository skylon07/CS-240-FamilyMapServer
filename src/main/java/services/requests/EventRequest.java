package services.requests;

/** Contains request data for the EventService */
public class EventRequest extends AuthorizedRequest {
    /** The Event ID of the target Event */
    public String eventID;
    /** An indication that all Events should be returned instead of just one */
    boolean all;
}
