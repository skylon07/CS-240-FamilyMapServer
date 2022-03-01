package services;

import dataAccess.Database;
import dataAccess.DatabaseException;
import dataAccess.EventAccessor;

import models.Event;
import models.User;

import services.requests.EventRequest;
import services.responses.EventResponse;

import utils.AuthUtils;

/**
 * This service provides functionality for the event getter endpoint.
 * It accepts calls through the HTTP GET method.
 */
public class EventService extends GenericService<EventRequest, EventResponse> {
    /**
     * Creates a new EventService by calling GenericService with this
     * service's name
     */
    public EventService() {
        super("EventService");
    }

    @Override
    public EventResponse onGet(EventRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        // check authorization
        AuthUtils authUtils = new AuthUtils(database);
        User authenticatedUser = authUtils.getAuthenticatedUser(request.authtoken);
        boolean userIsAuthenticated = authenticatedUser != null;
        if (!userIsAuthenticated) {
            return this.createUnauthenticatedResponse();
        }

        // determine branch
        if (request.all) {
            // get all persons
            EventAccessor eventAcc = new EventAccessor(database);
            Event[] allEvents = eventAcc.getAllForUser(authenticatedUser.getUsername());
            
            // generate response
            return this.createSuccessfulAllResponse(allEvents);
        } else {
            // get all persons
            EventAccessor eventAcc = new EventAccessor(database);
            Event matchingEvent = eventAcc.getByID(request.eventID);
            
            // generate response
            if (matchingEvent == null || !matchingEvent.getAssociatedUsername().equals(authenticatedUser.getUsername())) {
                return this.createInvalidEventResponse();
            } else {
                return this.createSuccessfulSingleResponse(matchingEvent);
            }
        }
    }

    private EventResponse createSuccessfulAllResponse(Event[] allEvents) {
        EventResponse response = new EventResponse();
        response.success = true;
        response.data = allEvents;
        return response;
    }

    private EventResponse createInvalidEventResponse() {
        EventResponse response = new EventResponse();
        response.success = false;
        response.message = "The eventID requested was not found for the user";
        return response;
    }

    private EventResponse createSuccessfulSingleResponse(Event matchingEvent) {
        EventResponse response = new EventResponse();
        response.success = true;
        // I feel like there's gotta be a better way to do this...
        // Like serializing the Event() object itself...
        // But whatever! Specs are specs
        response.eventID =              matchingEvent.getEventID();
        response.associatedUsername =   matchingEvent.getAssociatedUsername();
        response.personID =             matchingEvent.getPersonID();
        response.latitude =             matchingEvent.getLatitude();
        response.longitude =            matchingEvent.getLongitude();
        response.country =              matchingEvent.getCountry();
        response.city =                 matchingEvent.getCity();
        response.eventType =            matchingEvent.getEventType();
        response.year =                 matchingEvent.getYear();
        return response;
    }

    @Override
    protected EventResponse createSpecificErrorResponse(String errMsg) {
        return new EventResponse();
    }
}
