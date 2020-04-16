package com.georgev22.voterewards.tests;

import java.sql.PreparedStatement;
import java.util.UUID;

import org.apache.commons.lang.Validate;

import com.georgev22.voterewards.VoteRewardPlugin;

public class DatabaseTests {

	VoteRewardPlugin m = VoteRewardPlugin.getInstance();

	private static DatabaseTests instance = null;

	public static DatabaseTests getInstance() {
		return instance == null ? instance = new DatabaseTests() : instance;
	}

	public DatabaseTests() {
		// throw new AssertionError();
	}

	@Deprecated
	public void perPlayerTable(UUID uuid) throws Exception {
		Validate.notNull(uuid);
		String sqlCreate = String.format("CREATE TABLE IF NOT EXISTS `" + uuid.toString()
				+ "` (\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL\n)");
		PreparedStatement stmt = m.getConnection().prepareStatement(sqlCreate);
		stmt.execute();
	}

}
