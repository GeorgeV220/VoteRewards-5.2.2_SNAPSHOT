package com.georgev22.voterewards.database;

import com.georgev22.voterewards.database.mysql.MySQL;
import com.georgev22.voterewards.database.sqlite.SQLite;

import java.io.File;

public class DB {

	private DatabaseType type;

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
		switch (type) {
		case MySQL:
			return new MySQL(hostname, port, database, username, password);
		default:
			break;
		}
		return null;
	}

	@Deprecated
	public Database connect(File path, String hostname) {
		switch (type) {
		case SQLite:
			return new SQLite(path, hostname);
		default:
			break;
		}
		return null;
	}

}
