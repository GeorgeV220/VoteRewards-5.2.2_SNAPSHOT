package com.georgev22.voterewards.database;

import com.georgev22.voterewards.database.mysql.MySQL;
import com.georgev22.voterewards.database.sqlite.SQLite;

import java.io.File;

public class DB {

    private final DatabaseType type;

    /**
     * Database type (MySQL, SQLite)
     *
     * @param type
     */

    public DB(DatabaseType type) {
        this.type = type;
    }

    /**
     * Connect to the Database (Host name can not be null)
     *
     * @param hostname
     * @param port
     * @param database
     * @param username
     * @param password
     * @return
     */

    public Database connect(String hostname, String port, String database, String username, String password) {
        if (type == DatabaseType.MySQL) {
            return new MySQL(hostname, port, database, username, password);
        }
        return null;
    }

    @Deprecated
    public Database connect(File path, String hostname) {
        if (type == DatabaseType.SQLite) {
            return new SQLite(path, hostname);
        }
        return null;
    }

}
