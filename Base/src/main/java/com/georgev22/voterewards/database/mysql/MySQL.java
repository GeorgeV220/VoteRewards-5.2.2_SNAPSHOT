package com.georgev22.voterewards.database.mysql;

import com.georgev22.voterewards.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MySQL extends Database {

    private final String user, password, database, port, hostname;

    public static void main(String[] args) throws SQLException {
        //test
        Database database = new MySQL("", "", "", "", "");
        database.openConnection();
        ResultSet resultSet = database.queryPreparedSQL("SELECT * FROM users");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }
    }

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
    public Connection openConnection() throws SQLException {
        if (checkConnection()) {
            return connection;
        }

        final Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        prop.setProperty("useSSL", "false");
        prop.setProperty("autoReconnect", "true");
        prop.setProperty("connectTimeout", String.valueOf(Integer.MAX_VALUE));
        return connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, prop);
    }
}