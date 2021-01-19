package com.georgev22.voterewards.database;

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

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeQuery();
    }

    public int updatePreparedSQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeUpdate();
    }

    public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        return statement.executeQuery(query);
    }

    public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (!checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        return statement.executeUpdate(query);
    }

    /**
     * Create the user table
     */
    public void createTable() throws SQLException, ClassNotFoundException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `users` (\n  `uuid` varchar(255) DEFAULT NULL,\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL,\n  `daily` int(255) DEFAULT NULL,\n `services` varchar(10000) DEFAULT NULL\n)";
        updatePreparedSQL(sqlCreate);
    }
}