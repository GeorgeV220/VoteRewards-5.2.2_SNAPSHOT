package com.georgev22.voterewards.database.mongo;

import com.georgev22.voterewards.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class MongoAtlas extends Database {

    public static void main(String[] args) throws SQLException {
        //test
        Database database = new MongoAtlas("", "", "", "", "");
        database.openConnection();
        ResultSet resultSet = database.queryPreparedSQL("SELECT * FROM users");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }
    }

    private final String user, password, database, port, hostname;

    public MongoAtlas(String hostname, String port, String database, String user, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = user;
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
        //prop.setProperty("database", database);
        //prop.setProperty("useSSL", "false");
        //prop.setProperty("autoReconnect", "true");
        //prop.setProperty("connectTimeout", String.valueOf(Integer.MAX_VALUE));
        return connection = DriverManager.getConnection("jdbc:mongodb://" + this.hostname + ":" + this.port + "/" + database, prop);
    }
}
