package com.nubebuster.skydata;

import java.sql.*;
import java.util.Optional;

public class MYSQLDatabase {

    private final String hostname, database, user, password;
    private final int port;
    private Connection con;

    public MYSQLDatabase(String host, int port, String database, String user, String password) {
        hostname = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    private void openConnection()
            throws SQLException {
        closeConnection();// make sure the connection is not already open
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Using old mysql driver");
            try {
                Class.forName("com.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            } catch (Exception e2) {
                System.err.println("Could not find mysql drivers!");
                e2.printStackTrace();
            }
        }
        con = DriverManager.getConnection(
                "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?allowMultiQueries=true", user, password);
        getConnection().prepareStatement("USE " + database).execute();
    }

    public void closeConnection() {
        try {
            if (con != null && !con.isClosed())
                con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a connection is present and open, otherwise creates and opens  a connection
     */
    private void checkConnection() {
        try {
            if (con == null || !con.isValid(0))
                openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new connection if there is not a valid connection present
     *
     * @return connection
     */
    public Connection getConnection() {
        checkConnection();
        return con;
    }

    /**
     * @param sqlQuery the query to fetch data
     * @return an {@link java.util.Optional<ResultSet>} of the {@link java.sql.ResultSet ResultSet} from the query
     */
    public Optional<ResultSet> fetch(String sqlQuery) throws SQLException {
        return Optional.of(getConnection().prepareStatement(sqlQuery).executeQuery());
    }

    /**
     * @param sqlQuery the update query to execute
     * @return whether execution was successful
     */
    public boolean executeUpdate(String sqlQuery) throws SQLException {
        return getConnection().prepareStatement(sqlQuery).execute();
    }

    /**
     * @param sqlQuery the update query to execute
     * @return whether execution was successful
     */
    public PreparedStatement createStatement(String sqlQuery) throws SQLException {
        return getConnection().prepareStatement(sqlQuery);
    }
}
