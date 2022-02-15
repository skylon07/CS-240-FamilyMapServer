package dataAccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import models.User;

/**
 * A collection of methods that give access to User models in the database.
 * It can create, delete, update, and find Users using a variety of methods.
 */
public class UserAccessor extends Accessor<User> {
    /**
     * Returns the User matching a username
     * 
     * @param username is the username to query by
     * @return the corresponding User, or null if not found
     * @throws DatabaseException when a database error occurs
     */
    public User getByUsername(String username) throws DatabaseException {
        String sqlStr = "select * from user where username == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, username);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<User> users = this.database.query(statement, (result) -> this.mapQueryResult(result));
        if (users.size() == 0) {
            return null;
        } else if (users.size() == 1) {
            return users.get(0);
        } else {
            // should never happen...
            throw new DatabaseException("Database returned multiple users for one username");
        }
    }

    /**
     * Returns the Users matching an email address
     * 
     * @param email is the email to query by
     * @return the corresponding Users as an array
     * @throws DatabaseException when a database error occurs
     */
    public User[] getByEmail(String email) throws DatabaseException {
        String sqlStr = "select * from user where email == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, email);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<User> users = this.database.query(statement, (result) -> this.mapQueryResult(result));
        return users.toArray(new User[users.size()]);
    }

    /**
     * Returns all Users in the database
     * 
     * @return an array of all Users in the database
     * @throws DatabaseException when a database error occurs
     */
    public User[] getAll() throws DatabaseException {
        String sqlStr = "select * from user";
        ArrayList<User> users = this.database.query(sqlStr, (result) -> this.mapQueryResult(result));
        return users.toArray(new User[users.size()]);
    }
    
    @Override
    public void create(User[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> takenUsernames = new ArrayList<>();
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            boolean exists = existingModels[userIdx];
            if (exists) {
                User user = models[userIdx];
                takenUsernames.add(user.getUsername());
            }
        }
        if (takenUsernames.size() > 0) {
            String errMsg = "Cannot create users; some usernames were already taken: ";
            for (String username : takenUsernames) {
                errMsg += "'" + username + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("insert into user\n");
        sqlStr.append("   (username, password, email, firstname, lastname, gender, personID)\n");
        sqlStr.append("values\n");
        boolean firstUser = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstUser) {
                sqlStr.append(", ");
            }
            sqlStr.append("(?, ?, ?, ?, ?, ?, ?)");
            firstUser = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            User user = models[userIdx];
            
            int numFields = 7;
            int usernameIdx     = userIdx * numFields + 1;
            int passwordIdx     = userIdx * numFields + 2;
            int emailIdx        = userIdx * numFields + 3;
            int firstnameIdx    = userIdx * numFields + 4;
            int lastnameIdx     = userIdx * numFields + 5;
            int genderIdx       = userIdx * numFields + 6;
            int personIDIdx     = userIdx * numFields + 7;

            try {
                statement.setString(usernameIdx, user.getUsername());
                statement.setString(passwordIdx, user.getPassword());
                statement.setString(emailIdx, user.getEmail());
                statement.setString(firstnameIdx, user.getFirstName());
                statement.setString(lastnameIdx, user.getLastName());
                statement.setString(genderIdx, user.getLastName());
                statement.setString(personIDIdx, user.getPersonID());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public void delete(User[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistUsernames = new ArrayList<>();
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            boolean exists = existingModels[userIdx];
            if (!exists) {
                User user = models[userIdx];
                nonExistUsernames.add(user.getUsername());
            }
        }
        if (nonExistUsernames.size() > 0) {
            String errMsg = "Cannot delete users; some usernames did not exist: ";
            for (String username : nonExistUsernames) {
                errMsg += "'" + username + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("delete from user where\n");
        boolean firstUser = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstUser) {
                sqlStr.append(" or ");
            }
            sqlStr.append("username == ?");
            firstUser = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            User user = models[userIdx];
            
            int numFields = 1;
            int usernameIdx = userIdx * numFields + 0;

            try {
                statement.setString(usernameIdx, user.getUsername());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public void update(User[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistUsernames = new ArrayList<>();
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            boolean exists = existingModels[userIdx];
            if (!exists) {
                User user = models[userIdx];
                nonExistUsernames.add(user.getUsername());
            }
        }
        if (nonExistUsernames.size() > 0) {
            String errMsg = "Cannot update users; some usernames did not exist: ";
            for (String username : nonExistUsernames) {
                errMsg += "'" + username + "' ";
            }
            throw new BadAccessException(errMsg);
        }
        
        StringBuilder passwordStr = new StringBuilder();
        passwordStr.append("password = case\n");
        StringBuilder emailStr = new StringBuilder();
        emailStr.append("email = case\n");
        StringBuilder firstnameStr = new StringBuilder();
        firstnameStr.append("firstname = case\n");
        StringBuilder lastnameStr = new StringBuilder();
        lastnameStr.append("lastname = case\n");
        StringBuilder genderStr = new StringBuilder();
        genderStr.append("gender = case\n");
        StringBuilder personIDStr = new StringBuilder();
        personIDStr.append("personID = case\n");
        StringBuilder whereClauseStr = new StringBuilder();
        whereClauseStr.append("where username in (");
        boolean firstUser = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstUser) {
                passwordStr.append("\n");
                emailStr.append("\n");
                firstnameStr.append("\n");
                lastnameStr.append("\n");
                genderStr.append("\n");
                personIDStr.append("\n");
                whereClauseStr.append(", ");
            }
            passwordStr.append("when username == ? then ?");
            emailStr.append("when username == ? then ?");
            firstnameStr.append("when username == ? then ?");
            lastnameStr.append("when username == ? then ?");
            genderStr.append("when username == ? then ?");
            personIDStr.append("when username == ? then ?");
            whereClauseStr.append("?");
            firstUser = false;
        }
        passwordStr.append("else password end,\n");
        emailStr.append("else email end,\n");
        firstnameStr.append("else firstname end,\n");
        lastnameStr.append("else lastname end,\n");
        genderStr.append("else gender end,\n");
        personIDStr.append("else personID end\n");
        whereClauseStr.append(")\n");

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("update user set\n");
        sqlStr.append(passwordStr.toString());
        sqlStr.append(emailStr.toString());
        sqlStr.append(firstnameStr.toString());
        sqlStr.append(lastnameStr.toString());
        sqlStr.append(genderStr.toString());
        sqlStr.append(personIDStr.toString());
        sqlStr.append(whereClauseStr.toString());

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            User user = models[userIdx];

            int numFieldsPerWhen = 2; // username == ? then ?
            int numFieldsPerProp = models.length * numFieldsPerWhen;
            int whenPasswordIdx     = 0 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whenEmailIdx        = 1 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whenFirstnameIdx    = 2 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whenLastnameIdx     = 3 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whenGenderIdx       = 4 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whenPersonIDIdx     = 5 * numFieldsPerProp + userIdx * numFieldsPerWhen + 1;
            int whereClauseIdx      = 6 * numFieldsPerProp + userIdx + 1;

            try {
                statement.setString(whenPasswordIdx, user.getUsername());
                statement.setString(whenPasswordIdx + 1, user.getPassword());
                statement.setString(whenEmailIdx, user.getUsername());
                statement.setString(whenEmailIdx + 1, user.getEmail());
                statement.setString(whenFirstnameIdx, user.getUsername());
                statement.setString(whenFirstnameIdx + 1, user.getFirstName());
                statement.setString(whenLastnameIdx, user.getUsername());
                statement.setString(whenLastnameIdx + 1, user.getLastName());
                statement.setString(whenGenderIdx, user.getUsername());
                statement.setString(whenGenderIdx + 1, user.getGender());
                statement.setString(whenPersonIDIdx, user.getUsername());
                statement.setString(whenPersonIDIdx + 1, user.getPersonID());
                statement.setString(whereClauseIdx, user.getUsername());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
        this.database.commit();
    }

    @Override
    public void clear() throws DatabaseException {
        String sqlStr = "delete from user";
        this.database.update(sqlStr);
        this.database.commit(); 
    }

    @Override
    public boolean[] exists(User[] models) throws DatabaseException {
        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select username from user where\n");
        boolean firstUser = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstUser) {
                sqlStr.append(" or ");
            }
            sqlStr.append("username == ?");
            firstUser = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            User user = models[userIdx];
            
            int numFields = 1;
            int usernameIdx = userIdx * numFields + 0;

            try {
                statement.setString(usernameIdx, user.getUsername());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        boolean[] exists = new boolean[models.length];
        for (int userIdx = 0; userIdx < models.length; ++userIdx) {
            exists[userIdx] = false;
        }
        ArrayList<String> existingUsernames = this.database.query(statement, (result) -> result.getString(0));
        for (String username : existingUsernames) {
            int userExistsIdx = -1;
            for (int userIdx = 0; userIdx < models.length; ++userIdx) {
                User user = models[userIdx];
                if (user.getUsername() == username) {
                    userExistsIdx = userIdx;
                    break;
                }
            }
            if (userExistsIdx != -1) {
                exists[userExistsIdx] = true;
            }
        }
        return exists;
    }

    @Override
    protected User mapQueryResult(ResultSet result) throws SQLException {
        String username = result.getString(0);
        String password = result.getString(1);
        String email = result.getString(2);
        String firstName = result.getString(3);
        String lastName = result.getString(4);
        String gender = result.getString(5);
        String personID = result.getString(6);
        User user = new User(username, password, email, firstName, lastName, gender, personID);
        return user;
    }
}
