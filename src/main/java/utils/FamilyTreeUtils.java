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
        private boolean isForUserPerson;
        private Event birth;
        private Event death;
        private Event marriage;
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

        public Constraints makeParentConstraints() {
            int newNumGenerations = this.generationsLeft - 1;
            Constraints parentConstraints = new Constraints(newNumGenerations);
            parentConstraints.isForUserPerson = false;
            parentConstraints.childConstraints = this;
            return parentConstraints;
        }

        public boolean shouldGenerateParents() {
            return this.generationsLeft != 0;
        }

        public boolean isUserPerson() {
            return this.isForUserPerson;
        }

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

        public void recordBirth(Event birth) {
            this.birth = birth;
        }

        public int getDeathYearUpper() {
            assert this.birth != null : "Birth not set before getting death date";
            // nobody must die at an age older than 120 years old
            return this.birth.getYear() + 120;
        }

        public int getDeathYearLower() {
            assert this.childConstraints.birth != null : "Child birth not set before getting parent death date";
            // parents must not die before their child is born
            return this.childConstraints.birth.getYear();
        }

        public void recordDeath(Event death) {
            this.death = death;
        }
        
        public int getMarriageYearUpper() {
            assert this.death != null : "Parent death not set before getting marriage date";
            // parents cannot be married after they die
            return this.death.getYear();
        }

        public int getMarriageYearLower() {
            assert this.birth != null : "Parent birth not set before getting marriage date";
            // parents must be at least 13 years old when they are married
            return this.birth.getYear() + 13;
        }
        
        public void recordMarriage(Event marriage) {
            this.marriage = marriage;
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

    private int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    private int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }
}
