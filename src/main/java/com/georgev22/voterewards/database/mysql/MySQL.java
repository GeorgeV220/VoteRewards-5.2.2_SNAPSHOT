package com.georgev22.voterewards.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.georgev22.voterewards.database.Database;

public class MySQL extends Database {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    public MySQL(String hostname, String port, String username, String password) {
        this(hostname, port, null, username, password);
    }

    public MySQL(String hostname, String port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }

        String connectionURL = "jdbc:mysql://" + this.hostname + ":" + this.port;
        if (database != null) {
            connectionURL = connectionURL + "/" + this.database + "?autoReconnect=true&useUnicode=yes";
        }

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":3306/" + database
                + "?autoReconnect=true&user=" + user + "&password=" + password);
        connection.createStatement().setQueryTimeout(Integer.MAX_VALUE);
        return connection;
    }
}