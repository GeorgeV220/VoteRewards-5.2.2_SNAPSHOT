package com.georgev22.voterewards.database.sqlite;

import com.georgev22.voterewards.database.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends Database {

    private final String fileName;
    private final File path;

    public SQLite(final File path, final String fileName) {
        this.fileName = fileName;
        this.path = path;
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }
        Class.forName("org.sqlite.JDBC");
        String connectionURL = "jdbc:sqlite:" + path.getPath() + "/" + this.fileName + ".db";

        connection = DriverManager.getConnection(connectionURL);
        connection.createStatement().setQueryTimeout(Integer.MAX_VALUE);
        return connection;
    }
}
