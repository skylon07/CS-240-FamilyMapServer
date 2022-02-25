package dataAccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import models.AuthToken;

/**
 * A collection of methods that give access to AuthToken models in the database.
 * It can create, delete, update, and find AuthTokens using a variety of methods.
 */
public class AuthTokenAccessor extends Accessor<AuthToken> {
    /**
     * Creates an AuthTokenAccessor with a given database
     * 
     * @param database is the database to use
     */
    public AuthTokenAccessor(Database database) {
        super(database);
    }

    /**
     * Gets the AuthToken given its auth token
     * 
     * @param authToken is the auth token string of the AuthToken
     * @return the AuthToken that this auth token string represents
     * @throws DatabaseException when a database error occurs
     */
    public AuthToken getByAuthToken(String authToken) throws DatabaseException {
        String sqlStr = "select * from authtoken where authtoken == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, authToken);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<AuthToken> authTokens = this.database.query(statement, (result) -> this.mapQueryResult(result));
        if (authTokens.size() == 0) {
            return null;
        } else if (authTokens.size() == 1) {
            return authTokens.get(0);
        } else {
            // should never happen...
            throw new DatabaseException("Database returned multiple users for one username");
        }
    }

    /**
     * Gets the authentication tokens tied to a AuthToken by a username
     * 
     * @param username is the username of the AuthToken to query by
     * @return the AuthTokens associated to the authToken
     * @throws DatabaseException when a database error occurs
     */
    public AuthToken[] getByUsername(String username) throws DatabaseException {
        String sqlStr = "select * from authtoken where username == ?";
        PreparedStatement statement = this.database.prepareStatement(sqlStr);
        try {
            statement.setString(1, username);
        } catch (SQLException err) {
            throw new DatabaseException(err);
        }
        ArrayList<AuthToken> authTokens = this.database.query(statement, (result) -> this.mapQueryResult(result));
        return authTokens.toArray(new AuthToken[authTokens.size()]);
    }
    
    @Override
     public void create(AuthToken[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> usedAuthTokens = new ArrayList<>();
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            boolean exists = existingModels[tokenIdx];
            if (exists) {
                AuthToken authToken = models[tokenIdx];
                usedAuthTokens.add(authToken.getAuthtoken());
            }
        }
        if (usedAuthTokens.size() > 0) {
            String errMsg = "Cannot create auth tokens; some auth tokens were already used: ";
            for (String usedAuthToken : usedAuthTokens) {
                // this is safe, since it will only display auth tokens that
                // the client already had access to anyway
                errMsg += "'" + usedAuthToken + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("insert into authtoken\n");
        sqlStr.append("   (username, authtoken)\n");
        sqlStr.append("values\n");
        boolean firstAuthToken = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstAuthToken) {
                sqlStr.append(", ");
            }
            sqlStr.append("(?, ?)");
            firstAuthToken = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            AuthToken authToken = models[tokenIdx];
            
            int numFields = 2;
            int usernameIdx     = tokenIdx * numFields + 1;
            int authtokenIdx    = tokenIdx * numFields + 2;

            try {
                statement.setString(usernameIdx,    authToken.getUsername());
                statement.setString(authtokenIdx,   authToken.getAuthtoken());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public void delete(AuthToken[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistAuthTokens = new ArrayList<>();
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            boolean exists = existingModels[tokenIdx];
            if (!exists) {
                AuthToken authToken = models[tokenIdx];
                nonExistAuthTokens.add(authToken.getAuthtoken());
            }
        }
        if (nonExistAuthTokens.size() > 0) {
            String errMsg = "Cannot delete auth tokens; some auth tokens did not exist: ";
            for (String authToken : nonExistAuthTokens) {
                // this is safe, since it will only display auth tokens that
                // the client already had access to anyway
                errMsg += "'" + authToken + "' ";
            }
            throw new BadAccessException(errMsg);
        }

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("delete from authtoken where\n");
        boolean firstAuthToken = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstAuthToken) {
                sqlStr.append(" or ");
            }
            sqlStr.append("authtoken == ?");
            firstAuthToken = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            AuthToken authToken = models[tokenIdx];
            
            int numFields = 1;
            int authTokenIdx = tokenIdx * numFields + 1;

            try {
                statement.setString(authTokenIdx, authToken.getAuthtoken());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public void update(AuthToken[] models) throws BadAccessException, DatabaseException {
        if (models.length == 0) {
            return;
        }

        boolean[] existingModels = this.exists(models);
        ArrayList<String> nonExistAuthTokens = new ArrayList<>();
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            boolean exists = existingModels[tokenIdx];
            if (!exists) {
                AuthToken authToken = models[tokenIdx];
                nonExistAuthTokens.add(authToken.getAuthtoken());
            }
        }
        if (nonExistAuthTokens.size() > 0) {
            String errMsg = "Cannot update auth tokens; some auth tokens did not exist: ";
            for (String authToken : nonExistAuthTokens) {
                // this is safe, since it will only display auth tokens that
                // the client already had access to anyway
                errMsg += "'" + authToken + "' ";
            }
            throw new BadAccessException(errMsg);
        }
        
        StringBuilder usernameStr = new StringBuilder();
        usernameStr.append("username = case\n");
        StringBuilder whereClauseStr = new StringBuilder();
        whereClauseStr.append("where authtoken in (");
        boolean firstAuthToken = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstAuthToken) {
                usernameStr.append("\n");
                whereClauseStr.append(", ");
            }
            usernameStr.append("when authtoken == ? then ?");
            whereClauseStr.append("?");
            firstAuthToken = false;
        }
        usernameStr.append("else username end\n");
        whereClauseStr.append(")\n");

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("update authtoken set\n");
        sqlStr.append(usernameStr.toString());
        sqlStr.append(whereClauseStr.toString());

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            AuthToken authToken = models[tokenIdx];

            int numFieldsPerWhen = 2; // when authtoken == ? then ?
            int numFieldsPerProp = models.length * numFieldsPerWhen;
            int whenUsernameIdx     = 0 * numFieldsPerProp + tokenIdx * numFieldsPerWhen + 1;
            int whereClauseIdx      = 1 * numFieldsPerProp + tokenIdx + 1;

            try {
                statement.setString(whenUsernameIdx,        authToken.getAuthtoken());
                statement.setString(whenUsernameIdx + 1,    authToken.getUsername());
                statement.setString(whereClauseIdx,         authToken.getAuthtoken());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        this.database.update(statement);
    }

    @Override
    public boolean[] exists(AuthToken[] models) throws DatabaseException {
        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select authtoken from authtoken where\n");
        boolean firstAuthToken = true;
        for (int i = 0; i < models.length; ++i) {
            if (!firstAuthToken) {
                sqlStr.append(" or ");
            }
            sqlStr.append("authtoken == ?");
            firstAuthToken = false;
        }

        PreparedStatement statement = this.database.prepareStatement(sqlStr.toString());
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            AuthToken authToken = models[tokenIdx];
            
            int numFields = 1;
            int authtokenIdx = tokenIdx * numFields + 1;

            try {
                statement.setString(authtokenIdx, authToken.getAuthtoken());
            } catch (SQLException err) {
                throw new DatabaseException(err);
            }
        }

        boolean[] exists = new boolean[models.length];
        for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
            exists[tokenIdx] = false;
        }
        ArrayList<String> existingAuthTokens = this.database.query(statement, (result) -> result.getString(1));
        for (String authtokenStr : existingAuthTokens) {
            int authTokenExists = -1;
            for (int tokenIdx = 0; tokenIdx < models.length; ++tokenIdx) {
                AuthToken authToken = models[tokenIdx];
                if (authToken.getAuthtoken().equals(authtokenStr)) {
                    authTokenExists = tokenIdx;
                    break;
                }
            }
            if (authTokenExists != -1) {
                exists[authTokenExists] = true;
            }
        }
        return exists;
    }

    @Override
    public void clear() throws DatabaseException {
        String sqlStr = "delete from authtoken";
        this.database.update(sqlStr);
    }

    @Override
    protected AuthToken mapQueryResult(ResultSet result) throws SQLException {
        String authtoken = result.getString(1);
        String username = result.getString(2);
        AuthToken authToken = new AuthToken(authtoken, username);
        return authToken;
    }
}
