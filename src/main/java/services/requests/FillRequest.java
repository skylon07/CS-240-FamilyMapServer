package services.requests;

/** Contains request data for the FillService */
public class FillRequest extends GenericRequest {
    /** The username of the target User */
    public String username;
    /** The number of generations to fill */
    public int generations;
}
