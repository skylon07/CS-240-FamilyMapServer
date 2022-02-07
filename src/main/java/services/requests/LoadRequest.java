package services.requests;

/** Contains request data for the LoadService */
public class LoadRequest extends GenericRequest {
    /** The JSON String of Users to load into the database */
    public String users;
    /** The JSON String of Persons to load into the database */
    public String persons;
    /** The JSON String of Events to load into the database */
    public String events;
}
