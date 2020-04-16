package com.georgev22.voterewards.playerdata;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.SoundUtil;
import com.georgev22.voterewards.utilities.TitleActionbarUtil;
import com.georgev22.voterewards.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Used to handle a user's votes and anything related to them. Files, votes and
 * any other data related to them.
 */
public class UserVoteData {

	/* Returns a copy of this uservotedata class for a specific user. */

	public static UserVoteData getUser(final UUID uuid) {
		return new UserVoteData(uuid);
	}

	private VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

	private UserVoteData(final UUID uuid) {
		this.uuid = uuid;

		this.file = new File(VoteRewardPlugin.getInstance().getDataFolder(),
				"userdata" + File.separator + uuid.toString() + ".yml");

		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.reloadConfiguration();

	}

	private final File file;
	private final UUID uuid;
	private YamlConfiguration configuration = null;
	private OfflinePlayer voter;

	private OfflinePlayer getVoter() {
		return voter == null ? voter = Bukkit.getOfflinePlayer(uuid) : voter;
	}

	public void processVote(final String serviceName) {

		// ADD STATS
		if (this.voteRewardPlugin.database) {
			try {
				final String name = this.getVoter().getUniqueId().toString();
				final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
						String.format("UPDATE `users` SET `votes` = '%d', `time` = '%d' WHERE `uuid` = '%s'",
								this.getVotes() + 1, System.currentTimeMillis(), name));
				statement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.configuration.set("total-votes", this.getVotes() + 1);
			this.configuration.set("last-vote", System.currentTimeMillis());
			this.saveConfiguration();
		}
		this.setDailyVotes(this.getDailyVotes() + 1);
		//

		// TITLE
		if (VoteOptions.VOTE_TITLE.isEnabled()) {
			new TitleActionbarUtil(getVoter().getPlayer()).sendTitle(
					Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", voter.getName()),
					Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", voter.getName()));
		}
		//

		// SERVICE REWARDS
		if (this.voteRewardPlugin.getConfig().getString("Rewards.Services." + serviceName) != null) {
			for (final String b : this.voteRewardPlugin.getConfig()
					.getStringList("Rewards.Services" + serviceName + ".commands")) {
				this.runCommands(b.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
			}
		} else {
			for (final String b : this.voteRewardPlugin.getConfig()
					.getStringList("Rewards.Services.default.commands")) {
				this.runCommands(b.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
			}
		}
		//

		// DAILY REWARDS
		if (VoteOptions.DAILY.isEnabled()) {
			final Calendar now = Calendar.getInstance();
			final Calendar start = Calendar.getInstance();
			start.setTime(new Date(this.getLastVote()));
			final long diffHours = (start.getTimeInMillis() - now.getTimeInMillis()) / 3600000L;
			if (diffHours >= this.voteRewardPlugin.getConfig().getLong("Options.daily.hours")) {
				this.setDailyVotes(1);
			}
			for (String s : this.voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Daily").getKeys(false)) {
				if (Integer.valueOf(s).equals(this.getDailyVotes())) {
					for (final String b2 : this.voteRewardPlugin.getConfig()
							.getConfigurationSection("Rewards.Daily." + s + ".commands").getKeys(false)) {
						this.runCommands(b2.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
					}
				}
			}
		}
		//

		if (VoteOptions.SOUND.isEnabled()) {
			voter.getPlayer().playSound(voter.getPlayer().getLocation(),
					SoundUtil.getSound(voteRewardPlugin.getConfig().getString("Sounds.Vote")).get(), 1000, 1);
		}

		// LUCKY REWARDS
		if (VoteOptions.LUCKY.isEnabled()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			int i = random.nextInt(101);
			for (String s2 : this.voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Lucky")
					.getKeys(false)) {
				if (Integer.valueOf(s2).equals(i)) {
					for (String b3 : this.voteRewardPlugin.getConfig()
							.getStringList("Rewards.Lucky." + s2 + ".commands")) {
						this.runCommands(b3.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
					}
				}
			}
		}

		// PERMISSIONS REWARDS
		if (VoteOptions.PERMISSIONS.isEnabled()) {
			final ConfigurationSection section = this.voteRewardPlugin.getConfig()
					.getConfigurationSection("Rewards.Permission");
			for (String s2 : section.getKeys(false)) {
				if (this.getVoter().getPlayer().hasPermission("voterewards.permission" + s2)) {
					for (final String b3 : this.voteRewardPlugin.getConfig()
							.getStringList("Rewards.Permission." + s2 + ".commands")) {
						this.runCommands(b3.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
					}
				}
			}
		}
		//

		// CUMULATIVE REWARDS
		if (VoteOptions.CUMULATIVE.isEnabled()) {
			int votes = this.getVotes();
			for (String s2 : this.voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative")
					.getKeys(false)) {
				if (Integer.valueOf(s2).equals(votes)) {
					for (String b3 : this.voteRewardPlugin.getConfig()
							.getStringList("Rewards.Cumulative." + s2 + ".commands")) {
						this.runCommands(b3.replace("%player%", Objects.requireNonNull(this.getVoter().getName())));
					}
				}
			}
		}
		//

		// VOTE PARTY START
		VotePartyUtils.getInstance().run(getVoter());

	}

	/**
	 * Run the commands from config
	 *
	 * @param s
	 */
	private void runCommands(String s) {
		if (s == null)
			return;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(s));
	}

	/**
	 * Return player votes
	 *
	 * @return integer
	 */
	public int getVotes() {
		if (voteRewardPlugin.database) {
			try {

				PreparedStatement ps = voteRewardPlugin.getConnection()
						.prepareStatement("SELECT votes FROM users WHERE UUID = ?");
				ps.setString(1, uuid.toString());

				// PreparedStatement ps = voteRewardPlugin.getConnection()
				// .prepareStatement("SELECT * FROM " + voter.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getInt("votes");
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		return this.configuration.getInt("total-votes", 0);
	}

	public void setVotes(final int votes) {
		if (voteRewardPlugin.database) {
			try {
				final String name = this.getVoter().getUniqueId().toString();
				final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
						String.format("UPDATE `users` SET `votes` = '%d' WHERE `uuid` = '%s'", votes, name));

				// final PreparedStatement statement = this.voteRewardPlugin.getConnection()
				// .prepareStatement(String.format("UPDATE `" + name + "` SET `votes` = '%d'",
				// votes));
				statement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.configuration.set("total-votes", votes);
			this.saveConfiguration();
		}
	}

	private void reloadConfiguration() {
		this.configuration = YamlConfiguration.loadConfiguration(file);
	}

	private void saveConfiguration() {
		try {
			this.configuration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set X' player Daily votes
	 *
	 * @param i int
	 */
	private void setDailyVotes(final int i) {
		if (this.voteRewardPlugin.database) {
			try {
				final String name = this.getVoter().getUniqueId().toString();
				final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
						String.format("UPDATE `users` SET `daily` = '%d' WHERE `uuid` = '%s'", i, name));
				statement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.configuration.set("daily-votes", i);
			this.saveConfiguration();
		}
	}

	/**
	 * Set X' player VoteParty votes
	 *
	 * @param i int
	 */
	public void setVoteParty(final int i) {
		if (this.voteRewardPlugin.database) {
			try {
				final String name = this.getVoter().getUniqueId().toString();
				final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
						String.format("UPDATE `users` SET `voteparty` = '%d' WHERE `uuid` = '%s'", i, name));
				statement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.configuration.set("voteparty-votes", i);
			this.saveConfiguration();
		}
	}

	/**
	 * Return X's player VoteParty votes
	 *
	 * @return int
	 */
	public int getVoteParty() {
		if (this.voteRewardPlugin.database) {
			try {
				final PreparedStatement ps = this.voteRewardPlugin.getConnection()
						.prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
				ps.setString(1, this.uuid.toString());
				final ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getInt("voteparty");
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		return this.configuration.getInt("voteparty-votes", 0);
	}

	/**
	 * Get last vote
	 *
	 * @return int
	 */
	public long getLastVote() {
		if (this.voteRewardPlugin.database) {
			try {
				final PreparedStatement ps = this.voteRewardPlugin.getConnection()
						.prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
				ps.setString(1, this.uuid.toString());
				final ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getLong("time");
			} catch (Exception e) {
				return 0L;
			}
		}
		return this.configuration.getLong("last-vote");
	}

	/**
	 * 
	 * Set last vote
	 * 
	 * @param i
	 */
	public void setLastVote(long i) {
		if (this.voteRewardPlugin.database) {
			try {
				final String name = this.getVoter().getUniqueId().toString();
				final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
						String.format("UPDATE `users` SET `time` = '%d' WHERE `uuid` = '%s'", i, name));
				statement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.configuration.set("last-vote", i);
			this.saveConfiguration();
		}
	}

	/**
	 * Get Daily votes
	 *
	 * @return int
	 */
	private int getDailyVotes() {
		if (this.voteRewardPlugin.database) {
			try {
				final PreparedStatement ps = this.voteRewardPlugin.getConnection()
						.prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
				ps.setString(1, this.uuid.toString());
				final ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getInt("daily");
			} catch (Exception e) {
				return 0;
			}
		}
		return this.configuration.getInt("daily-votes");
	}

	/**
	 * Setup the user
	 */
	public void setupUser() {
		if (voteRewardPlugin.database) {
			new BukkitRunnable() {
				@Override
				public void run() {

					try {
						final PreparedStatement ps = voteRewardPlugin.getConnection()
								.prepareStatement(String.format("UPDATE `users` SET `name` = '%s' WHERE `uuid` = '%s'",
										voter.getName(), voter.getUniqueId()));
						ps.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(voteRewardPlugin);
		}
		this.configuration.set("last-name", getVoter().getName());
		try {
			if (!playerExist()) {
				if (voteRewardPlugin.database) {
					setupMySQLUser();
				} else {
					this.configuration.set("total-votes", 0);
					this.configuration.set("daily-votes", 0);
					// this.configuration.set("voteparty-votes", 0);
					this.configuration.set("voteparty", 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.saveConfiguration();
	}

	/**
	 * Setup MySQL user
	 */
	private void setupMySQLUser() {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					PreparedStatement statement1 = voteRewardPlugin.getConnection().prepareStatement(String.format(
							"INSERT INTO users (`uuid`, `votes`, `time`, `voteparty`) VALUES ('%s', '0', '0', '0');",
							uuid.toString()));
					statement1.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(voteRewardPlugin);
	}

	/**
	 * Check if the player exist
	 *
	 * @return boolean
	 * @throws Exception
	 */
	public boolean playerExist() throws Exception {
		if (voteRewardPlugin.database) {
			PreparedStatement ps = voteRewardPlugin.getConnection()
					.prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} else {
			return configuration.get("total-votes") != null;
		}
	}

	/**
	 * Reset player
	 *
	 * @throws Exception
	 */
	public void reset() throws Exception {
		if (voteRewardPlugin.database) {
			PreparedStatement ps = voteRewardPlugin.getConnection()
					.prepareStatement("DELETE from users WHERE uuid = ?");
			ps.setString(1, uuid.toString());
			ps.executeQuery();
			this.setupMySQLUser();
		} else {
			this.configuration.set("total-votes", 0);
			this.configuration.set("daily-votes", 0);
			// this.configuration.set("voteparty-votes", 0);
			this.configuration.set("voteparty", 0);
			this.saveConfiguration();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		return (this == object)
				|| (object instanceof UserVoteData && Objects.equals(this.uuid, ((UserVoteData) object).uuid));
	}

}