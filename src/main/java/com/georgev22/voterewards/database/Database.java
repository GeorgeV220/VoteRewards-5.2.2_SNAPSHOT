package com.georgev22.voterewards.database;

import com.georgev22.voterewards.utilities.Options;

import java.sql.*;

public abstract class Database {

    protected Connection connection;

    protected Database() {
        this.connection = null;
    }

    public abstract Connection openConnection() throws SQLException, ClassNotFoundException;

    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean closeConnection() throws SQLException {
        if (connection == null) {
            return false;
        }
        connection.close();
        return true;
    }

    public ResultSet queryPreparedSQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        return connection.prepareStatement(query).executeQuery();
    }

    public int updatePreparedSQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        return connection.prepareStatement(query).executeUpdate();
    }

    public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        return connection.createStatement().executeQuery(query);
    }

    public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        return connection.createStatement().executeUpdate(query);
    }

    /**
     * Create the user table
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    public void createTable() throws SQLException, ClassNotFoundException {
        updateSQL("CREATE TABLE IF NOT EXISTS `" + Options.DATABASE_TABLE_NAME.getValue() + "` (\n  `uuid` varchar(255) DEFAULT NULL,\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL,\n  `daily` int(255) DEFAULT NULL,\n `services` varchar(10000) DEFAULT NULL\n)");
    }
}