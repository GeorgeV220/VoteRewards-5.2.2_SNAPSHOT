package com.georgev22.voterewards.utilities.database;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.OptionsUtil;

import java.sql.*;

public abstract class Database {

    protected Connection connection;

    protected Database() {
        this.connection = null;
    }

    public abstract Connection openConnection() throws SQLException, ClassNotFoundException;

    public boolean isConnectionValid() {
        return connection != null;
    }

    public boolean isClosed() throws SQLException {
        return connection.isClosed();
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
        if (!isClosed()) {
            openConnection();
        }

        return connection.prepareStatement(query).executeQuery();
    }

    public int updatePreparedSQL(String query) throws SQLException, ClassNotFoundException {
        if (!isClosed()) {
            openConnection();
        }

        return connection.prepareStatement(query).executeUpdate();
    }

    public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
        if (!isClosed()) {
            openConnection();
        }

        return connection.createStatement().executeQuery(query);
    }

    public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (!isClosed()) {
            openConnection();
        }

        return connection.createStatement().executeUpdate(query);
    }

    public void checkColumn(String tableName, String column, String type) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + tableName + "`;");
        ResultSetMetaData metaData = resultSet.getMetaData();
        int rowCount = metaData.getColumnCount();

        boolean isMyColumnPresent = false;
        for (int i = 1; i <= rowCount; i++) {
            if (column.equals(metaData.getColumnName(i))) {
                isMyColumnPresent = true;
            }
        }

        if (!isMyColumnPresent) {
            statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD `" + column + "` " + type + ";");
        }
    }

    /**
     * Create the user table
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    public void createTable() throws SQLException, ClassNotFoundException {
        updateSQL("CREATE TABLE IF NOT EXISTS `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` (\n  `uuid` VARCHAR(38) DEFAULT NULL,\n" +
                " `name` VARCHAR(18) DEFAULT NULL,\n" +
                " `votes` INT(10) DEFAULT 0,\n" +
                " `time` VARCHAR(20) DEFAULT 0,\n" +
                " `voteparty` INT(10) DEFAULT 0,\n" +
                " `daily` BIGINT(10) DEFAULT 0,\n" +
                " `services` VARCHAR(10000) DEFAULT NULL,\n" +
                " `totalvotes` INT(10) DEFAULT 0\n)");
        ObjectMap<String, String> tableMap = ObjectMap.newHashObjectMap();
        tableMap.append("uuid", "VARCHAR(38) DEFAULT NULL")
                .append("name", "VARCHAR(18) DEFAULT NULL")
                .append("votes", "INT(10) DEFAULT 0")
                .append("time", "VARCHAR(20) DEFAULT 0")
                .append("voteparty", "INT(10) DEFAULT 0")
                .append("daily", "BIGINT(10) DEFAULT 0")
                .append("services", "VARCHAR(10000) DEFAULT NULL")
                .append("totalvotes", "INT(10) DEFAULT 0");
        tableMap.forEach((columnName, type) -> {
            try {
                checkColumn(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), columnName, type);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
}