package dataAccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import models.Person;

/**
 * A collection of methods that give access to Person models in the database.
 * It can create, delete, update, and find Persons using a variety of methods.
 */
public class PersonAccessor extends Accessor<Person> {
    /**
     * Returns the Person matching a Person ID
     * 
     * @param personID is the Person ID to query by
     * @return the corresponding Person, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public Person getByID(String personID) throws DatabaseException {
        String sqlStr = "select * from person where personID == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, personID);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<Person> people = this.database.query(statement, (result) -> this.mapQueryResult(result));
        if (people.size() == 0) {
            return null;
        } else if (people.size() == 1) {
            return people.get(0);
        } else {
            // should never happen...
            throw new DatabaseException("Database returned multiple people for one personID");
        }
    }

    /**
     * Returns all Persons in the database
     * 
     * @return an array of all Persons in the database
     * @throws DatabaseException when a database error occurs
     */
    public Person[] getAll() throws DatabaseException {
        String sqlStr = "select * from person";
        ArrayList<Person> people = this.database.query(sqlStr, (result) -> this.mapQueryResult(result));
        return people.toArray(new Person[people.size()]);
    }

    @Override
    public void create(Person[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> usedPersonIDs = new ArrayList<>();
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            boolean exists = existingModels[personIdx];
            if (exists) {
                Person person = models[personIdx];
                usedPersonIDs.add(person.getPersonID());
            }
        }
        if (usedPersonIDs.size() > 0) {
            String errMsg = "Cannot create people; some person IDs were already taken: ";
            for (String personID : usedPersonIDs) {
                errMsg += "'" + personID + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("insert into person\n");
        sqlStr.append("   (personID, associatedUsername, firstname, lastname,\n");
        sqlStr.append("    gender, fatherID, motherID, spouseID)\n");
        sqlStr.append("values\n");
        boolean firstPerson = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstPerson) {
                sqlStr.append(", ");
            }
            sqlStr.append("(?, ?, ?, ?, ?, ?, ?, ?)");
            firstPerson = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            Person person = models[personIdx];
            
            int numFields = 8;
            int personIDIdx             = personIdx * numFields + 1;
            int associatedUsernameIdx   = personIdx * numFields + 2;
            int firstnameIdx            = personIdx * numFields + 3;
            int lastnameIdx             = personIdx * numFields + 4;
            int genderIdx               = personIdx * numFields + 5;
            int fatherIDIdx             = personIdx * numFields + 6;
            int motherIDIdx             = personIdx * numFields + 7;
            int spouseIDIdx             = personIdx * numFields + 8;

            try {
                statement.setString(personIDIdx, person.getPersonID());
                statement.setString(associatedUsernameIdx, person.getAssociatedUsername());
                statement.setString(firstnameIdx, person.getFirstName());
                statement.setString(lastnameIdx, person.getLastName());
                statement.setString(genderIdx, person.getGender());
                statement.setString(fatherIDIdx, person.getFatherID());
                statement.setString(motherIDIdx, person.getMotherID());
                statement.setString(spouseIDIdx, person.getSpouseID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public void delete(Person[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistPersonIDs = new ArrayList<>();
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            boolean exists = existingModels[personIdx];
            if (!exists) {
                Person person = models[personIdx];
                nonExistPersonIDs.add(person.getPersonID());
            }
        }
        if (nonExistPersonIDs.size() > 0) {
            String errMsg = "Cannot delete people; some person IDs did not exist: ";
            for (String personID : nonExistPersonIDs) {
                errMsg += "'" + personID + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("delete from person where\n");
        boolean firstPerson = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstPerson) {
                sqlStr.append(" or ");
            }
            sqlStr.append("personID == ?");
            firstPerson = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            Person person = models[personIdx];
            
            int numFields = 1;
            int personIDIdx = personIdx * numFields + 1;

            try {
                statement.setString(personIDIdx, person.getPersonID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public void update(Person[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistPersonIDs = new ArrayList<>();
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            boolean exists = existingModels[personIdx];
            if (!exists) {
                Person person = models[personIdx];
                nonExistPersonIDs.add(person.getPersonID());
            }
        }
        if (nonExistPersonIDs.size() > 0) {
            String errMsg = "Cannot update people; some person IDs did not exist: ";
            for (String personID : nonExistPersonIDs) {
                errMsg += "'" + personID + "' ";
            }
            throw new BadAccessException(errMsg);
        }
        
        StringBuilder associatedUsernameStr = new StringBuilder();
        associatedUsernameStr.append("associatedUsername = case\n");
        StringBuilder firstnameStr = new StringBuilder();
        firstnameStr.append("firstname = case\n");
        StringBuilder lastnameStr = new StringBuilder();
        lastnameStr.append("lastname = case\n");
        StringBuilder genderStr = new StringBuilder();
        genderStr.append("gender = case\n");
        StringBuilder fatherIDStr = new StringBuilder();
        fatherIDStr.append("fatherID = case\n");
        StringBuilder motherIDStr = new StringBuilder();
        motherIDStr.append("motherID = case\n");
        StringBuilder spouseIDStr = new StringBuilder();
        spouseIDStr.append("spouseID = case\n");
        StringBuilder whereClauseStr = new StringBuilder();
        whereClauseStr.append("where personID in (");
        boolean firstPerson = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstPerson) {
                associatedUsernameStr.append("\n");
                firstnameStr.append("\n");
                lastnameStr.append("\n");
                genderStr.append("\n");
                fatherIDStr.append("\n");
                motherIDStr.append("\n");
                spouseIDStr.append("\n");
                whereClauseStr.append(", ");
            }
            associatedUsernameStr.append("when personID == ? then ?");
            firstnameStr.append("when personID == ? then ?");
            lastnameStr.append("when personID == ? then ?");
            genderStr.append("when personID == ? then ?");
            fatherIDStr.append("when personID == ? then ?");
            motherIDStr.append("when personID == ? then ?");
            spouseIDStr.append("when personID == ? then ?");
            whereClauseStr.append("?");
            firstPerson = false;
        }
        associatedUsernameStr.append("else associatedUsername end,\n");
        firstnameStr.append("else firstname end,\n");
        lastnameStr.append("else lastname end,\n");
        genderStr.append("else gender end,\n");
        fatherIDStr.append("else fatherID end,\n");
        motherIDStr.append("else motherID end,\n");
        spouseIDStr.append("else spouseID end\n");
        whereClauseStr.append(")\n");

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("update person set\n");
        sqlStr.append(associatedUsernameStr.toString());
        sqlStr.append(firstnameStr.toString());
        sqlStr.append(lastnameStr.toString());
        sqlStr.append(genderStr.toString());
        sqlStr.append(fatherIDStr.toString());
        sqlStr.append(motherIDStr.toString());
        sqlStr.append(spouseIDStr.toString());
        sqlStr.append(whereClauseStr.toString());

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            Person person = models[personIdx];

            int numFieldsPerWhen = 2; // when personID == ? then ?
            int numFieldsPerProp = models.length * numFieldsPerWhen;
            int whenAssociatedUsernameIdx   = 0 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenFirstnameIdx            = 1 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenLastnameIdx             = 2 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenGenderIdx               = 3 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenFatherIDIdx             = 4 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenMotherIDIdx             = 5 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whenSpouseIDIdx             = 6 * numFieldsPerProp + personIdx * numFieldsPerWhen + 1;
            int whereClauseIdx              = 7 * numFieldsPerProp + personIdx + 1;

            try {
                statement.setString(whenAssociatedUsernameIdx, person.getPersonID());
                statement.setString(whenAssociatedUsernameIdx + 1, person.getAssociatedUsername());
                statement.setString(whenFirstnameIdx, person.getPersonID());
                statement.setString(whenFirstnameIdx + 1, person.getFirstName());
                statement.setString(whenLastnameIdx, person.getPersonID());
                statement.setString(whenLastnameIdx + 1, person.getLastName());
                statement.setString(whenGenderIdx, person.getPersonID());
                statement.setString(whenGenderIdx + 1, person.getGender());
                statement.setString(whenFatherIDIdx, person.getPersonID());
                statement.setString(whenFatherIDIdx + 1, person.getFatherID());
                statement.setString(whenMotherIDIdx, person.getPersonID());
                statement.setString(whenMotherIDIdx + 1, person.getMotherID());
                statement.setString(whenSpouseIDIdx, person.getPersonID());
                statement.setString(whenSpouseIDIdx + 1, person.getSpouseID());
                statement.setString(whereClauseIdx, person.getPersonID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public boolean[] exists(Person[] models) throws DatabaseException {
        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select personID from person where\n");
        boolean firstPerson = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstPerson) {
                sqlStr.append(" or ");
            }
            sqlStr.append("personID == ?");
            firstPerson = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            Person person = models[personIdx];
            
            int numFields = 1;
            int personIDIdx = personIdx * numFields + 1;

            try {
                statement.setString(personIDIdx, person.getPersonID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        boolean[] exists = new boolean[models.length];
        for (int personIdx = 0; personIdx < models.length; ++personIdx) {
            exists[personIdx] = false;
        }
        ArrayList<String> existingPersonIDs = this.database.query(statement, (result) -> result.getString(1));
        for (String personID : existingPersonIDs) {
            int personExistsIdx = -1;
            for (int personIdx = 0; personIdx < models.length; ++personIdx) {
                Person person = models[personIdx];
                if (person.getPersonID().equals(personID)) {
                    personExistsIdx = personIdx;
                    break;
                }
            }
            if (personExistsIdx != -1) {
                exists[personExistsIdx] = true;
            }
        }
        return exists;
    }

    @Override
    public void clear() throws DatabaseException {
        String sqlStr = "delete from person";
        this.database.update(sqlStr);
        this.database.commit();   
    }

    @Override
    protected Person mapQueryResult(ResultSet result) throws SQLException {
        String personID = result.getString(1);
        String associatedUsername = result.getString(2);
        String firstName = result.getString(3);
        String lastName = result.getString(4);
        String gender = result.getString(5);
        String fatherID = result.getString(6);
        String motherID = result.getString(7);
        String spouseID = result.getString(8);
        Person person = new Person(personID, associatedUsername, firstName, lastName,
                                   gender, fatherID, motherID, spouseID);
        return person;
    }
}
