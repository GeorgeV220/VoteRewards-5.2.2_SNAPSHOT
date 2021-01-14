package com.georgev22.voterewards.database;

import java.sql.*;

public abstract class Database {

    protected Connection connection;

    protected Database() {
        this.connection = null;
    }

    public abstract Connection openConnection() throws SQLException;

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

    public ResultSet queryPreparedSQL(String query) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeQuery();
    }

    public int updatePreparedSQL(String query) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeUpdate();
    }

    public ResultSet querySQL(String query) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        return statement.executeQuery(query);
    }

    public int updateSQL(String query) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        return statement.executeUpdate(query);
    }
}