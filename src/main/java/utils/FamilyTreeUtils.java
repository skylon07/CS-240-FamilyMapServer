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
    static final int NUM_GENERATIONS = 4;
    
    public FamilyTreeUtils(Database database) {
        super(database);
    }

    /**
     * Generates a fake family tree by generating/tying people together for a user
     * 
     * @param user is the user whose family should be generated
     * @param numGenerations is the number of generations to generate
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
     */
    public GenerationAttempt generateFamilyTree(User user) throws DatabaseException, BadAccessException {
        return this.generateFamilyTree(user, FamilyTreeUtils.NUM_GENERATIONS);
    }

    private void recursiveFillPerson(Person personToFill, Constraints constraints, GenerationAttempt attempt) {
        // first up, make a birth date
        Event birth = this.createBirthEventFor(personToFill, constraints, attempt);
        // next up, make a death date
        Event death = null;
        if (constraints.shouldMakeDeathEvent()) {
            death = this.createDeathEventFor(personToFill, constraints, attempt, birth);
        }
        // lastly, make a marriage date
        Event marriage = null;
        if (constraints.shouldMakeMarriageEvent() && death != null) {
            marriage = this.createMarriageEventFor(personToFill, constraints, attempt, birth, death);
            constraints.recordMarriageEvent(marriage);
        }

        // continue making parents if more generations are needed
        if (constraints.getNumGensLeft() > 0) {
            int newGensLeft = constraints.getNumGensLeft() - 1;

            // ladies first!
            Person mother = this.createMotherFor(personToFill, attempt);
            Constraints motherConstraints = new Constraints(newGensLeft, birth);
            this.recursiveFillPerson(mother, motherConstraints, attempt);
            
            // now for daddy
            Person father = this.createFatherFor(personToFill, attempt);
            Constraints fatherConstraints = new Constraints(newGensLeft, birth, motherConstraints.getMarriageEvent());
            this.recursiveFillPerson(father, fatherConstraints, attempt);

            mother.setSpouseID(father.getPersonID());
            father.setSpouseID(mother.getPersonID());

            personToFill.setFatherID(father.getPersonID());
            personToFill.setMotherID(mother.getPersonID());
        } else {
            personToFill.setFatherID(null);
            personToFill.setMotherID(null);
        }
    }

    public class GenerationAttempt {
        private boolean success;
        private ArrayList<Person> createdPersons;
        private ArrayList<Event> createdEvents;

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

        protected void trackCreatePerson(Person person) {
            this.createdPersons.add(person);
        }

        public int getNumEventsCreated() {
            return this.createdEvents.size();
        }

        public Event[] getCreatedEvents() {
            return this.createdEvents.toArray(new Event[this.createdEvents.size()]);
        }

        protected void trackCreateEvent(Event event) {
            this.createdEvents.add(event);
        }
    }

    private class Constraints {
        private int generationsLeft;
        private Event childBirthday;
        private boolean isUserPerson;
        private Event marriageEvent;

        /**
         * Defines default starting constraints for the user's person
         * 
         * @param numGenerations is the initial number of generations to make
         */
        public Constraints(int numGenerations) {
            this.generationsLeft = numGenerations;
            this.childBirthday = null;
            this.isUserPerson = true;
            this.marriageEvent = null;
        }

        /**
         * Defines constraints for the first generated parent
         * 
         * @param numGenerations is the number of generations left
         * @param childBirthday is the parent's childs birthday (which defines constraints)
         */
        public Constraints(int numGenerations, Event childBirthday) {
            this.generationsLeft = numGenerations;
            this.childBirthday = childBirthday;
            this.isUserPerson = false;
            this.marriageEvent = null;
        }

        /**
         * Defines constraints for the second generated parent
         * 
         * @param numGenerations is the number of generations left
         * @param childBirthday is the parent's childs birthday (which defines constraints)
         * @param marriageEvent is the spouse's marriage event to copy
         */
        public Constraints(int numGenerations, Event childBirthday, Event marriageEvent) {
            this.generationsLeft = numGenerations;
            this.childBirthday = childBirthday;
            this.isUserPerson = false;
            this.marriageEvent = marriageEvent;
        }

        public int getNumGensLeft() {
            return this.generationsLeft;
        }

        public boolean shouldMakeDeathEvent() {
            return !this.isUserPerson;
        }

        public boolean shouldMakeMarriageEvent() {
            return !this.isUserPerson;
        }

        public Event getMarriageEvent() {
            return this.marriageEvent;
        }

        public void recordMarriageEvent(Event marriageEvent) {
            this.marriageEvent = marriageEvent;
        }

        public int getBirthYearUpper() {
            if (this.childBirthday == null) {
                // initial/user upper
                return 2008;
            }
            // parents must be born at least 13 years before their children
            return this.childBirthday.getYear() - 13;
        }

        public int getBirthYearLower() {
            if (this.childBirthday == null) {
                // initial/user lower
                return 1984;
            }
            // women (both parents...) must not give birth when older than 50 years old
            return this.childBirthday.getYear() - 50;
        }

        public int getMarriageYearUpper(Event deathEvent) {
            // parents cannot be married after they die
            return deathEvent.getYear();
        }

        public int getMarriageYearLower(Event birthEvent) {
            // parents must be at least 13 years old when they are married
            return birthEvent.getYear() + 13;
        }

        public int getDeathYearUpper(Event birthEvent) {
            // nobody must die at an age older than 120 years old
            return birthEvent.getYear() + 120;
        }

        public int getDeathYearLower(Event birthEvent) {
            // parents must not die before their child is born
            return this.childBirthday.getYear();
        }
    }

    private Person createUserPerson(User user, GenerationAttempt attempt) {
        String newPersonID = Person.generateID();
        Person userPerson = new Person(
            newPersonID, user.getUsername(), user.getFirstName(),
            user.getLastName(), user.getGender()
        );
        attempt.trackCreatePerson(userPerson);
        return userPerson;
    }

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
    
    private Event createDeathEventFor(Person person, Constraints constraints, GenerationAttempt attempt, Event birth) {
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
        int year = this.randomRange(constraints.getDeathYearLower(birth), constraints.getDeathYearUpper(birth));
        
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
    
    private Event createMarriageEventFor(Person person, Constraints constraints, GenerationAttempt attempt, Event birth, Event death) {
        String eventID = Event.generateID();
        Location location;
        int year;

        Event spouseMarriage = constraints.getMarriageEvent();
        boolean spouseDefinedMarriage = spouseMarriage != null;
        if (spouseDefinedMarriage) {
            // copy marriage event
            location = new Location();
            location.latitude = spouseMarriage.getLatitude();
            location.longitude = spouseMarriage.getLongitude();
            location.country = spouseMarriage.getCountry();
            location.city = spouseMarriage.getCity();
            year = spouseMarriage.getYear();
        } else {
            // generate our own
            try {
                location = this.getRandomLocation();
            } catch (FileNotFoundException err) {
                location = new Location();
                location.latitude = 0;
                location.longitude = 0;
                location.country = "(Location file not found)";
                location.city = "(Location file not found)";
            }
            year = this.randomRange(constraints.getMarriageYearLower(birth), constraints.getMarriageYearUpper(death));
        }
        
        Event marriage = new Event(
            eventID,
            person.getAssociatedUsername(),
            person.getPersonID(),
            location.latitude,
            location.longitude,
            location.country,
            location.city,
            "Marriage",
            year
        );
        attempt.trackCreateEvent(marriage);
        return marriage;
    }
    
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


    private class Location {
        public String country;
        public String city;
        public float latitude;
        public float longitude;
    }

    private Location getRandomLocation() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/locations.json"));
        Gson gson = new Gson();
        Location[] locations = gson.fromJson(locationFile, LocationJSONList.class).data;
        return this.randomChoice(locations);
    }

    private String getRandomFirstName() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/fnames.json"));
        Gson gson = new Gson();
        String[] firstNames = gson.fromJson(locationFile, FirstNameJSONList.class).data;
        return this.randomChoice(firstNames);
    }

    private String getRandomLastName() throws FileNotFoundException {
        FileReader locationFile = new FileReader(new File("json/snames.json"));
        Gson gson = new Gson();
        String[] lastNames = gson.fromJson(locationFile, LastNameJSONList.class).data;
        return this.randomChoice(lastNames);
    }

    private class LocationJSONList {
        public Location[] data;
    }

    private class FirstNameJSONList {
        public String[] data;
    }

    private class LastNameJSONList {
        public String[] data;
    }

    private <ObjType> ObjType randomChoice(ObjType[] objs) {
        Random rand = new Random();
        int idx = rand.nextInt(objs.length);
        return objs[idx];
    }

    private int randomRange(int low, int high) {
        assert low <= high : "FamilyTreeUtils.randomRange() received backwards arguments";
        Random rand = new Random();
        int randBase = rand.nextInt(high + 1 - low);
        return randBase + low;
    }
}
