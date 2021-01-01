package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to handle all user's votes and anything related to them.
 */
public class UserVoteData {

    private static final Map<UUID, User> userMap = Maps.newHashMap();

    /**
     * Returns all loaded users
     *
     * @return all loaded users
     */
    public static Map<UUID, User> getUserMap() {
        return userMap;
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param uuid
     * @return a copy of this UserVoteData class for a specific user.
     */
    public static UserVoteData getUser(UUID uuid) {
        if (userMap.get(uuid) == null) {
            userMap.put(uuid, new User(uuid));
        }
        return new UserVoteData(userMap.get(uuid));
    }

    /**
     * Returns all the users on userdata/ folder
     *
     * @return all the users on userdata/ folder
     */
    public static Map<String, Integer> getAllUsers() {
        Map<String, Integer> map = new ConcurrentHashMap<>();

        File[] files = new File(VoteRewardPlugin.getInstance().getDataFolder(), "userdata").listFiles();

        for (File file : Objects.requireNonNull(files)) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            if (cfg.get("last-name") == null)
                continue;
            map.put(cfg.getString("last-name"), cfg.getInt("total-votes"));
        }

        return map;
    }

    private final User user;
    private final UserUtils userUtils;
    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private UserVoteData(User user) {
        this.user = user;
        this.userUtils = UserUtils.getUser(user.getUniqueID());
    }

    /**
     * Set player votes
     *
     * @param votes
     */
    public void setVotes(int votes) {
        user.setVotes(votes);
        if (voteRewardPlugin.database) {
            return;
        }
        userUtils.setVotes(user.getVotes());
    }

    /**
     * Set the last time when a player voted
     *
     * @param lastVoted
     */
    public void setLastVoted(long lastVoted) {
        user.setLastVoted(lastVoted);
        if (voteRewardPlugin.database) {
            return;
        }
        userUtils.setLastVote(lastVoted);
    }

    /**
     * Set how many virtual crates a player have
     *
     * @param voteParties
     */
    public void setVoteParties(int voteParties) {
        user.setVoteParties(voteParties);
        if (voteRewardPlugin.database) {
            return;
        }
        userUtils.setVoteParty(voteParties);
    }

    /**
     * Set the offline services
     *
     * @param services
     */
    public void setOfflineServices(List<String> services) {
        user.setServices(services);
        if (voteRewardPlugin.database) {
            return;
        }
        userUtils.setOfflineServices(services);
    }

    /**
     * Get player total votes
     * Check UserUtils#getVotes()
     *
     * @return player total votes
     */
    public int getVotes() {
        return user.getVotes();
    }

    /**
     * Get player virtual crates
     * Check UserUtils#getVoteParty()
     *
     * @return player virtual crates
     */
    public int getVoteParty() {
        return user.getVoteParties();
    }

    /**
     * Get the last time when the player voted
     * Check UserUtils#getLastVote()
     *
     * @return the last time when the player voted
     */
    public long getLastVote() {
        return user.getLastVoted();
    }

    /**
     * Get all services that the player have voted
     * when he was offline
     *
     * @return services
     */
    public List<String> getOfflineServices() {
        return user.getServices();
    }

    /**
     * Returns the user object class
     *
     * @return user object class
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the UserUtils class object
     *
     * @return the UserUtils class object
     */
    public UserUtils getUserVoteData() {
        return userUtils;
    }

    /**
     * Check if the player exists
     *
     * @return true if player exists or false when is not
     */
    public boolean playerExists() {
        if (voteRewardPlugin.database) {
            try {
                return SQLUserUtils.playerExists(user.getUniqueID());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return false;
            }
        }
        return userUtils.playerExist();
    }

    /**
     * Get the total votes until the next cumulative reward
     *
     * @return Integer total votes until the next cumulative reward
     */
    public int votesUntilNextCumulativeVote() {
        if (voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative") == null) {
            return 0;
        }
        int votesUntil = 0;
        for (String b : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative").getKeys(false)) {
            int cumulative = Integer.parseInt(b);
            if (cumulative <= getVotes()) {
                continue;
            }
            votesUntil = cumulative - getVotes();
            break;
        }
        return votesUntil;
    }

    /**
     * Run the commands from config
     *
     * @param s the list with all the commands
     */
    public void runCommands(List<String> s) {
        for (String b : s) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(b.replace("%player%", user.getPlayer().getName())));
                }
            }.runTask(voteRewardPlugin);
        }
    }

    /**
     * Load all player's data
     */
    public void load() {
        if (voteRewardPlugin.database) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        SQLUserUtils.setupUser(user.getUniqueID());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(voteRewardPlugin);
            return;
        }
        userUtils.setupUser();
    }

    /**
     * Save all player's data
     */
    public void save() {
        if (voteRewardPlugin.database) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        SQLUserUtils.save(user.getUniqueID());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(voteRewardPlugin);
            return;
        }
        userUtils.saveConfiguration();
    }

    /**
     * Reset player's stats
     */
    public void reset() {
        if (voteRewardPlugin.database) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        SQLUserUtils.reset(user.getUniqueID());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(voteRewardPlugin);
            return;
        }
        userUtils.reset();
    }

    /**
     * All SQL Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happened
     */
    public static class SQLUserUtils {

        private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

        /**
         * Save all player's data
         *
         * @param uuid Player's Unique ID
         * @throws SQLException When something goes wrong
         */
        public static void save(UUID uuid) throws SQLException {
            UserVoteData userVoteData = UserVoteData.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + userVoteData.getVotes() + "', " +
                            "`time` = '" + userVoteData.getLastVote() + "', " +
                            "`voteparty` = '" + userVoteData.getVoteParty() + "', " +
                            "`services` = '" + userVoteData.getOfflineServices().toString().replace("[", "").replace("]", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            preparedStatement.executeUpdate();
        }

        /**
         * Reset all player's data
         *
         * @param uuid Player's Unique ID
         * @throws SQLException When something goes wrong
         */
        public static void reset(UUID uuid) throws SQLException {
            UserVoteData userVoteData = UserVoteData.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + 0 + "', " +
                            "`time` = '" + 0 + "', " +
                            "`voteparty` = '" + 0 + "', " +
                            "`services` = '" + Lists.newArrayList().toString().replace("[", "").replace("]", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            userVoteData.setVotes(0);
            userVoteData.setLastVoted(0);
            userVoteData.setOfflineServices(Lists.newArrayList());
            userVoteData.setVoteParties(0);
            preparedStatement.executeUpdate();
        }

        /**
         * Load all player's data
         *
         * @param uuid Player's Unique ID
         * @throws SQLException When something goes wrong
         */
        public static void load(UUID uuid) throws SQLException {
            UserVoteData userVoteData = UserVoteData.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid.toString() + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userVoteData.setVotes(resultSet.getInt("votes"));
                userVoteData.setLastVoted(resultSet.getLong("time"));
                userVoteData.setOfflineServices(new ArrayList<>(Arrays.asList(resultSet.getString("services").split(","))));
                userVoteData.setVoteParties(resultSet.getInt("voteparty"));
            }
            resultSet.close();
        }

        /**
         * Check if player's exists
         *
         * @param uuid Player's Unique ID
         * @return true if player exists and false when is not
         * @throws SQLException When something goes wrong
         */
        public static boolean playerExists(UUID uuid) throws SQLException {
            PreparedStatement ps = voteRewardPlugin.getConnection()
                    .prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }

        /**
         * Setup the user data to the database
         *
         * @param uuid Player's Unique ID
         * @throws SQLException When something goes wrong
         */
        public static void setupUser(UUID uuid) throws SQLException {
            if (!playerExists(uuid)) {
                PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                        "INSERT INTO `users` (`uuid`, `name`, `votes`, `time`, `voteparty`, `services`)" +
                                " VALUES " +
                                "('" + uuid.toString() + "', '" + Bukkit.getOfflinePlayer(uuid).getName() + "','0', '0', '0', '" + Lists.newArrayList().toString().replace("[", "").replace("]", "") + "');");
                preparedStatement.executeUpdate();
            }
            UserVoteData.getUserMap().put(uuid, new User(uuid));
            load(uuid);
        }

        /**
         * Get all players from the database
         *
         * @return all the players from the database
         * @throws SQLException When something goes wrong
         */
        public static Map<String, Integer> getAllUsers() throws SQLException {
            Map<String, Integer> map = Maps.newHashMap();
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement("SELECT * FROM `users`");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString("name"), resultSet.getInt("votes"));
            }
            return map;
        }
    }


    /**
     * All User Utils for the user
     */
    public static class UserUtils {

        /**
         * Returns a copy of this UserUtils class for a specific user.
         *
         * @param uuid
         * @return a copy of this UserUtils class for a specific user.
         */
        public static UserUtils getUser(final UUID uuid) {
            return new UserUtils(uuid);
        }

        private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

        private UserUtils(final UUID uuid) {
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

        /**
         * Return player votes
         *
         * @return player total votes
         */
        public int getVotes() {
            return this.configuration.getInt("total-votes", 0);
        }

        public void setVotes(final int votes) {
            this.configuration.set("total-votes", votes);
        }

        private void reloadConfiguration() {
            this.configuration = YamlConfiguration.loadConfiguration(file);
        }

        public void saveConfiguration() {
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
            this.configuration.set("daily-votes", i);
        }

        /**
         * Set X' player VoteParty votes
         *
         * @param i int
         */
        public void setVoteParty(final int i) {
            this.configuration.set("voteparty-votes", i);
        }

        /**
         * Return X's player VoteParty votes
         *
         * @return int
         */
        public int getVoteParty() {
            return this.configuration.getInt("voteparty-votes", 0);
        }

        /**
         * Get last vote
         *
         * @return int
         */
        public long getLastVote() {
            return this.configuration.getLong("last-vote");
        }

        /**
         * Set last vote
         *
         * @param i
         */
        public void setLastVote(long i) {
            this.configuration.set("last-vote", i);
        }

        /**
         * Get Daily votes
         *
         * @return int
         */
        private int getDailyVotes() {
            return this.configuration.getInt("daily-votes");
        }

        /**
         * Setup the user
         */
        public void setupUser() {
            this.configuration.set("last-name", getVoter().getName());
            if (!playerExist()) {
                this.configuration.set("total-votes", 0);
                this.configuration.set("daily-votes", 0);
                this.configuration.set("voteparty", 0);
                this.configuration.set("offline votes.service", Lists.newArrayList());
            }
            saveConfiguration();
        }

        /**
         * Check if the player exist
         *
         * @return boolean
         */
        public boolean playerExist() {
            return configuration.get("total-votes") != null;
        }

        /**
         * Reset player
         */
        public void reset() {
            this.configuration.set("total-votes", 0);
            this.configuration.set("daily-votes", 0);
            this.configuration.set("voteparty", 0);
            this.configuration.set("offline votes.service", Lists.newArrayList());
            saveConfiguration();
        }

        /**
         * Get player World
         *
         * @return String worldName
         */
        public String getWorld() {
            if (getVoter().isOnline()) {
                return getVoter().getPlayer().getWorld().getName();
            } else {
                return "world";
            }
        }

        /**
         * Get offline vote services
         *
         * @return String list with the service names
         */
        public List<String> getServices() {
            return this.configuration.getStringList("offline vote.services");
        }

        /**
         * Set offline services
         *
         * @param services
         */
        public void setOfflineServices(List<String> services) {
            this.configuration.set("offline vote.services", services);
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
                    || (object instanceof UserUtils && Objects.equals(this.uuid, ((UserUtils) object).uuid));
        }
    }
}
