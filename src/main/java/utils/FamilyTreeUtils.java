package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Random;

import com.google.gson.Gson;

import dataAccess.*;
import models.*;

public class FamilyTreeUtils extends GenericUtility {
    /** The default number of generations to generate when one isn't provided */
    static final int NUM_GENERATIONS = 4;
    
    /**
     * Creates a new FamilyTreeUtils instance with a database to work with
     * 
     * @param database is the database to work with
     */
    public FamilyTreeUtils(Database database) {
        super(database);
    }

    /**
     * Generates a fake family tree by generating/tying people together for a user
     * 
     * @param user is the user whose family should be generated
     * @param numGenerations is the number of generations to generate
     * @return a GenerationAttempt, containing information on what happened during the procedure
     * @throws BadAccessException when the user's information is not properly cleared/ready (lingering Person/Event references, particularly)
     * @throws DatabaseException whenever another database error occurs
     */
    public GenerationAttempt generateFamilyTree(User user, int numGenerations) throws DatabaseException, BadAccessException {
        GenerationAttempt attempt = new GenerationAttempt();
        attempt.setSuccess(false);
        
        Person userPerson = this.createUserPerson(user, attempt);

        // fill the family tree
        Constraints startingConstraints = new Constraints(numGenerations);
        this.recursiveFillPerson(userPerson, startingConstraints, attempt);
        // finally, tie with the user
        user.setPersonID(userPerson.getPersonID());

        // lets update that database!
        PersonAccessor personAcc = new PersonAccessor(this.database);
        personAcc.create(attempt.getCreatedPersons());
        EventAccessor eventAcc = new EventAccessor(this.database);
        eventAcc.create(attempt.getCreatedEvents());
        UserAccessor userAcc = new UserAccessor(this.database);
        User[] users = {user};
        userAcc.update(users);

        // return the necessary data
        attempt.setSuccess(true);
        return attempt;
    }
    
    /**
     * Generates a fake family tree with a default number of generations
     * 
     * @param user is the user whose family should be generated
     * @return a GenerationAttempt, containing information on what happened during the procedure
     * @throws BadAccessException when the user's information is not properly cleared/ready (lingering Person/Event references, particularly)
     * @throws DatabaseException whenever another database error occurs
     */
    public GenerationAttempt generateFamilyTree(User user) throws DatabaseException, BadAccessException {
        return this.generateFamilyTree(user, FamilyTreeUtils.NUM_GENERATIONS);
    }

    /**
     * Recursively "fills" a given Person, meaning:
     * 1. birth and death Events are created (only birth for user's person)
     * 2. parents are generated for and tied to the person (father/mother IDs)
     * 3. a marriage event is created for each set of parents added, and their spouse IDs are set to each other
     * 
     * @param personToFill is the Person to perform the recursive iteration for
     * @param constraints is the Constraints object which defines the valid ranges for newly generated data
     * @param attempt is the GenerationAttempt to record certain actions
     */
    private void recursiveFillPerson(Person personToFill, Constraints constraints, GenerationAttempt attempt) {
        // happy birthday!
        Event birth = this.createBirthEventFor(personToFill, constraints, attempt);
        constraints.recordBirth(birth);
        if (!constraints.isUserPerson()) {
            // less happy death day...
            Event death = this.createDeathEventFor(personToFill, constraints, attempt);
            constraints.recordDeath(death);
        }

        // recursively fill parents
        if (constraints.shouldGenerateParents()) {
            // ladies first!
            Person mother = this.createMotherFor(personToFill, attempt);
            Constraints motherConstraints = constraints.makeParentConstraints();
            this.recursiveFillPerson(mother, motherConstraints, attempt);

            // now for daddy
            Person father = this.createFatherFor(personToFill, attempt);
            Constraints fatherConstraints = constraints.makeParentConstraints();
            this.recursiveFillPerson(father, fatherConstraints, attempt);

            // marry them together!
            mother.setSpouseID(father.getPersonID());
            father.setSpouseID(mother.getPersonID());
            Event[] marriages = this.createMarriageEventsFor(mother, father, motherConstraints, fatherConstraints, attempt);
            motherConstraints.recordMarriage(marriages[0]);
            fatherConstraints.recordMarriage(marriages[1]);

            // oh yeah, they had that baby thing...
            personToFill.setMotherID(mother.getPersonID());
            personToFill.setFatherID(father.getPersonID());
        } else {
            personToFill.setMotherID(null);
            personToFill.setFatherID(null);
        }
    }

    /**
     * Records data (number of created Persons/Events) to be returned by
     * the generateFamilyTree() method
     */
    public class GenerationAttempt {
        /** Indicates the success of the attempt */
        private boolean success;
        /** Represents the number of Persons created during generation */
        private ArrayList<Person> createdPersons;
        /** Represents the number of Events created during generation */
        private ArrayList<Event> createdEvents;

        /**
         * Creates a new, blank GenerationAttempt
         */
        public GenerationAttempt() {
            this.success = false;
            this.createdPersons = new ArrayList<>();
            this.createdEvents = new ArrayList<>();
        }

        public boolean getSuccess() {
            return this.success;
        }
        
        protected void setSuccess(boolean success) {
            this.success = success;
        }

        public int getNumPersonsCreated() {
            return this.createdPersons.size();
        }

        public Person[] getCreatedPersons() {
            return this.createdPersons.toArray(new Person[this.createdPersons.size()]);
        }

        /**
         * Tracks a newly created Person and remembers it
         * 
         * @param person is the new person that was just created
         */
        protected void trackCreatePerson(Person person) {
            this.createdPersons.add(person);
        }

        public int getNumEventsCreated() {
            return this.createdEvents.size();
        }

        public Event[] getCreatedEvents() {
            return this.createdEvents.toArray(new Event[this.createdEvents.size()]);
        }

        /**
         * Tracks a newly created Event and remembers it
         * 
         * @param event is the new event that was just created
         */
        protected void trackCreateEvent(Event event) {
            this.createdEvents.add(event);
        }
    }

    /**
     * Defines ranges and constraints of values for data when it is randomly
     * generated during construction of the family tree
     */
    private class Constraints {
        /** The number of generations left to create */
        private int generationsLeft;
        /** Indicates if the constraints apply to the User's Person object */
        private boolean isForUserPerson;
        /** The recorded birth event for this person */
        private Event birth;
        /** The recorded death event for this person */
        private Event death;
        /** The recorded marriage event for this person */
        @SuppressWarnings("unused") // could be used if future restrictions arise
        private Event marriage;
        /**  */
        private Constraints childConstraints;

        /**
         * Defines default starting constraints for the user's person
         * 
         * @param numGenerations is the initial number of generations to make
         */
        public Constraints(int numGenerations) {
            assert numGenerations >= 0;
            this.generationsLeft = numGenerations;
            this.isForUserPerson = true;
            this.birth = null;
            this.death = null;
            this.marriage = null;
            this.childConstraints = null;
        }

        /**
         * Generates a new Constraints object that corresponds to a parent of the current Constraints object
         * 
         * @return the new Constraints object
         */
        public Constraints makeParentConstraints() {
            int newNumGenerations = this.generationsLeft - 1;
            Constraints parentConstraints = new Constraints(newNumGenerations);
            parentConstraints.isForUserPerson = false;
            parentConstraints.childConstraints = this;
            return parentConstraints;
        }

        /**
         * Indicates if another generation of parents should be generated for this Constraint's Person
         * 
         * @return whether or not a new generation of parents should be created
         */
        public boolean shouldGenerateParents() {
            return this.generationsLeft != 0;
        }

        /**
         * Returns if this Constraints instance represents constraints for the User's person
         * 
         * @return wheter or not it represents the User's person
         */
        public boolean isUserPerson() {
            return this.isForUserPerson;
        }

        /**
         * Gets the upper bound for the Person's birth year
         * 
         * @return the upper bound for the Person's birth year
         */
        public int getBirthYearUpper() {
            if (this.isForUserPerson) {
                // initial/user upper
                return 2008;
            } else {
                assert this.childConstraints.birth != null : "Child birth not set before parent birth";
                // parents must be born at least 13 years before their children
                return this.childConstraints.birth.getYear() - 13;
            }
        }

        /**
         * Gets the lower bound for the Person's birth year
         * 
         * @return the lower bound for the Person's birth year
         */
        public int getBirthYearLower() {
            if (this.isForUserPerson) {
                // initial/user lower
                return 1984;
            } else {
                assert this.childConstraints.birth != null : "Child birth not set before parent birth";
                // women (both parents...) must not give birth when older than 50 years old
                return this.childConstraints.birth.getYear() - 50;
            }
        }

        /**
         * Records the birth event when it is created
         * 
         * @param birth is the newly created birth event
         */
        public void recordBirth(Event birth) {
            this.birth = birth;
        }

        /**
         * Gets the upper bound for the Person's death year
         * Should only be used after the birth event is recorded
         * 
         * @return the upper bound for the Person's death year
         */
        public int getDeathYearUpper() {
            assert this.birth != null : "Birth not set before getting death date";
            // nobody must die at an age older than 120 years old
            return this.birth.getYear() + 120;
        }

        /**
         * Gets the lower bound for the Person's death year.
         * Should only be used after the birth event is recorded
         * 
         * @return the lower bound for the Person's death year
         */
        public int getDeathYearLower() {
            assert this.childConstraints.birth != null : "Child birth not set before getting parent death date";
            // parents must not die before their child is born
            return this.childConstraints.birth.getYear();
        }

        /**
         * Records the death event when it is created
         * 
         * @param death is the newly created death event
         */
        public void recordDeath(Event death) {
            this.death = death;
        }
        
        /**
         * Gets the upper bound for the Person's marriage year.
         * Should only be used after birth and death events are recorded
         * 
         * @return the upper bound for the Person's marriage year
         */
        public int getMarriageYearUpper() {
            assert this.death != null : "Parent death not set before getting marriage date";
            // parents cannot be married after they die
            return this.death.getYear();
        }

        /**
         * Gets the lower bound for the Person's marriage year
         * Should only be used after birth and death events are recorded
         * 
         * @return the lower bound for the Person's marriage year
         */
        public int getMarriageYearLower() {
            assert this.birth != null : "Parent birth not set before getting marriage date";
            // parents must be at least 13 years old when they are married
            return this.birth.getYear() + 13;
        }
        
        /**
         * Records the marriage event when it is created
         * 
         * @param marriage is the newly created marriage event
         */
        public void recordMarriage(Event marriage) {
            this.marriage = marriage;
        }
    }

    /**
     * Creates and tracks a new Person for the User we are generating for
     * 
     * @param user is the User whose Person this is
     * @param attempt is the GenerationAttempt to record the creation
     * @return the new Person instance
     */
    private Person createUserPerson(User user, GenerationAttempt attempt) {
        String newPersonID = Person.generateID();
        Person userPerson = new Person(
            newPersonID, user.getUsername(), user.getFirstName(),
            user.getLastName(), user.getGender()
        );
        attempt.trackCreatePerson(userPerson);
        return userPerson;
    }

    /**
     * Creates a birth event for a person inside the bounds of the constraints
     * 
     * @param person is the person to generate the event for
     * @param constraints is the constraints that defines valid birth event states
     * @param attempt is the GenerationAttempt to record the new event with
     * @return the new birth event
     */
    private Event createBirthEventFor(Person person, Constraints constraints, GenerationAttempt attempt) {
        String eventID = Event.generateID();
        Location location;
        try {
            location = this.getRandomLocation();
        } catch (FileNotFoundException err) {
            location = new Location();
            location.latitude = 0;
            location.longitude = 0;
            location.country = "(Location file not found)";
            location.city = "(Location file not found)";
        }
        int year = this.randomRange(constraints.getBirthYearLower(), constraints.getBirthYearUpper());
        
        Event birth = new Event(
            eventID,
            person.getAssociatedUsername(),
            person.getPersonID(),
            location.latitude,
            location.longitude,
            location.country,
            location.city,
            "Birth",
            year
        );
        attempt.trackCreateEvent(birth);
        return birth;
    }
    
    /**
     * Creates a death event for a person inside the bounds of the constraints
     * 
     * @param person is the person to generate the event for
     * @param constraints is the constraints that defines valid death event states
     * @param attempt is the GenerationAttempt to record the new event with
     * @return the new death event
     */
    private Event createDeathEventFor(Person person, Constraints constraints, GenerationAttempt attempt) {
        String eventID = Event.generateID();
        Location location;
        try {
            location = this.getRandomLocation();
        } catch (FileNotFoundException err) {
            location = new Location();
            location.latitude = 0;
            location.longitude = 0;
            location.country = "(Location file not found)";
            location.city = "(Location file not found)";
        }
        int year = this.randomRange(constraints.getDeathYearLower(), constraints.getDeathYearUpper());
        
        Event death = new Event(
            eventID,
            person.getAssociatedUsername(),
            person.getPersonID(),
            location.latitude,
            location.longitude,
            location.country,
            location.city,
            "Death",
            year
        );
        attempt.trackCreateEvent(death);
        return death;
    }
    
    /**
     * Creates two marriage events for two Persons, assumed to be married, inside the bounds of the constraints
     * 
     * @param parent1 is either the mother or the father
     * @param parent2 is the mother if parent1 is the father, and vice versa
     * @param constraints1 is the constraints for parent1
     * @param constraints2 is the constraints for parent2
     * @param attempt is the GenerationAttempt to record the new event with
     * @return the pair of newly generated marriage events
     */
    private Event[] createMarriageEventsFor(Person parent1, Person parent2, Constraints constraints1, Constraints constraints2, GenerationAttempt attempt) {
        String eventID1 = Event.generateID();
        String eventID2 = Event.generateID();
        Location location;
        int year;

        try {
            location = this.getRandomLocation();
        } catch (FileNotFoundException err) {
            location = new Location();
            location.latitude = 0;
            location.longitude = 0;
            location.country = "(Location file not found)";
            location.city = "(Location file not found)";
        }
        int yearRangeLower = this.max(constraints1.getMarriageYearLower(), constraints2.getMarriageYearLower());
        int yearRangeUpper = this.min(constraints1.getMarriageYearUpper(), constraints2.getMarriageYearUpper());
        year = this.randomRange(yearRangeLower, yearRangeUpper);
        
        Event marriage1 = new Event(
            eventID1,
            parent1.getAssociatedUsername(),
            parent1.getPersonID(),
            location.latitude,
            location.longitude,
            location.country,
            location.city,
            "Marriage",
            year
        );
        attempt.trackCreateEvent(marriage1);
        Event marriage2 = new Event(
            eventID2,
            parent2.getAssociatedUsername(),
            parent2.getPersonID(),
            location.latitude,
            location.longitude,
            location.country,
            location.city,
            "Marriage",
            year
        );
        attempt.trackCreateEvent(marriage2);
        Event[] marriages = {marriage1, marriage2};
        return marriages;
    }
    
    /**
     * Creates a new mother given some child
     * 
     * @param child is the Person who should be treated as the child
     * @param attempt is the GenerationAttempt to record the new mother with
     * @return the new mother Person
     */
    private Person createMotherFor(Person child, GenerationAttempt attempt) {
        String newPersonID = Person.generateID();
        String firstName, lastName;
        try {
            firstName = this.getRandomFirstName();
            lastName = this.getRandomLastName();
        } catch (FileNotFoundException err) {
            firstName = "(first name file not found)";
            lastName = "(last name file not found)";
        }

        Person mother = new Person(
            newPersonID, child.getAssociatedUsername(),
            firstName, lastName, "f"
        );
        attempt.trackCreatePerson(mother);
        return mother;
    }
    
    /**
     * Creates a new father given some child
     * 
     * @param child is the Person who should be treated as the child
     * @param attempt is the GenerationAttempt to record the new father with
     * @return the new father Person
     */
    private Person createFatherFor(Person child, GenerationAttempt attempt) {
        String newPersonID = Person.generateID();
        String firstName, lastName;
        try {
            firstName = this.getRandomFirstName();
        } catch (FileNotFoundException err) {
            firstName = "(first name file not found)";
        }
        lastName = child.getLastName();

        Person father = new Person(
            newPersonID, child.getAssociatedUsername(),
            firstName, lastName, "m"
        );
        attempt.trackCreatePerson(father);
        return father;
    }

    /**
     * Returns a new, random Location from the locations.json file
     * 
     * @return a new Location to use for an Event
     * @throws FileNotFoundException if the locations.json file path is not correct
     */
    private Location getRandomLocation() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/locations.json"));
        Gson gson = new Gson();
        Location[] locations = gson.fromJson(locationFile, LocationJSONList.class).data;
        return this.randomChoice(locations);
    }

    /**
     * Represents a location according to the structure of locations.json
     */
    private class Location {
        public String country;
        public String city;
        public float latitude;
        public float longitude;
    }

    /**
     * Represents the entire object returned from locations.json
     */
    private class LocationJSONList {
        public Location[] data;
    }

    /**
     * Returns a random first name from fnames.json
     * 
     * @return a random first name String
     * @throws FileNotFoundException if the fnames.json file path is not correct
     */
    private String getRandomFirstName() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/fnames.json"));
        Gson gson = new Gson();
        String[] firstNames = gson.fromJson(locationFile, FirstNameJSONList.class).data;
        return this.randomChoice(firstNames);
    }

    /**
     * Represents the entire object returned from fnames.json
     */
    private class FirstNameJSONList {
        public String[] data;
    }

    /**
     * Returns a random last name from fnames.json
     * 
     * @return a random last name String
     * @throws FileNotFoundException if the snames.json file path is not correct
     */
    private String getRandomLastName() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/snames.json"));
        Gson gson = new Gson();
        String[] lastNames = gson.fromJson(locationFile, LastNameJSONList.class).data;
        return this.randomChoice(lastNames);
    }

    /**
     * Represents the entire object returned from snames.json
     */
    private class LastNameJSONList {
        public String[] data;
    }

    /**
     * A utility helper function that returns a random object given a list of objects
     * 
     * @param <ObjType> is the type of the objects in the "objs" array
     * @param objs is the array of ObjTypes
     * @return a random object from the "objs" array
     */
    private <ObjType> ObjType randomChoice(ObjType[] objs) {
        Random rand = new Random();
        int idx = rand.nextInt(objs.length);
        return objs[idx];
    }

    /**
     * A utility helper function that returns a random integer between low and high.
     * Any value between low and high, including low and high, can be returned
     * 
     * @param low is the lower int bound
     * @param high is the upper int bound
     * @return a number equal to or greater than low, while also equal to or less than high
     */
    private int randomRange(int low, int high) {
        assert low <= high : "FamilyTreeUtils.randomRange() received backwards arguments";
        Random rand = new Random();
        int randBase = rand.nextInt(high + 1 - low);
        return randBase + low;
    }

    /**
     * A utility helper function that returns the higher of two integer values.
     * Negative numbers are treated as "lower" (ie -4 is less than -3)
     * 
     * @param a is an integer
     * @param b is another integer
     * @return the larger value of the two integers
     */
    private int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * A utility helper function that returns the lower of two integer values.
     * Negative numbers are treated as "lower" (ie -4 is less than -3)
     * 
     * @param a is an integer
     * @param b is another integer
     * @return the lower value of the two integers
     */
    private int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }
}
