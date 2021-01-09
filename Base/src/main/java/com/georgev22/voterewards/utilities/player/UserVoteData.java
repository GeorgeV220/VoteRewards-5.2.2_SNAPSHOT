package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.options.VoteOptions;
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
    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    /**
     * Returns all loaded users
     *
     * @return all loaded users
     */
    public static Map<UUID, User> getUserMap() {
        return userMap;
    }

    private static final Map<String, Integer> allUsersMap = Maps.newConcurrentMap();

    /**
     * Returns all the players in a map
     *
     * @return all the players
     */
    public static Map<String, Integer> getAllUsersMap() {
        return allUsersMap;
    }

    /**
     * Load all users
     */
    public static void loadAllUsers() {
        if (voteRewardPlugin.database) {
            try {
                allUsersMap.putAll(UserVoteData.SQLUserUtils.getAllUsers());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            allUsersMap.putAll(UserVoteData.UserUtils.getAllUsers());
        }
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

    private final User user;
    private final UserUtils userUtils;
    private final SQLUserUtils sqlUserUtils;

    private UserVoteData(User user) {
        this.user = user;
        this.userUtils = UserUtils.getUser(user.getUniqueID());
        this.sqlUserUtils = SQLUserUtils.getUser(user.getUniqueID());
    }

    /**
     * Set player votes
     *
     * @param votes
     */
    public void setVotes(int votes) {
        user.setVotes(votes);
    }

    /**
     * Set the last time when a player voted
     *
     * @param lastVoted
     */
    public void setLastVoted(long lastVoted) {
        user.setLastVoted(lastVoted);
    }

    /**
     * Set how many virtual crates a player have
     *
     * @param voteParties
     */
    public void setVoteParties(int voteParties) {
        user.setVoteParties(voteParties);
    }

    /**
     * Set the offline services
     *
     * @param services
     */
    public void setOfflineServices(List<String> services) {
        if (VoteOptions.DEBUG.isEnabled())
            Utils.debug(voteRewardPlugin, "Services Debug", services.toString());
        user.setServices(services);
    }

    /**
     * Set daily votes
     *
     * @param votes
     */
    public void setDailyVotes(int votes) {
        user.setDailyVotes(votes);
    }

    /**
     * Get user daily votes
     *
     * @return user daily votes
     */
    public int getDailyVotes() {
        return user.getDailyVotes();
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
     * Returns the SQLUserUtils class object
     *
     * @return the SQLUserUtils class object
     */
    public SQLUserUtils getSQLUserUtils() {
        return sqlUserUtils;
    }

    /**
     * Check if the player exists
     *
     * @return true if player exists or false when is not
     */
    public boolean playerExists() {
        return getAllUsersMap().containsKey(Bukkit.getOfflinePlayer(user.getUniqueID()).getName());
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
     * Load player data
     *
     * @param callback
     */
    public void load(Callback callback) {
        if (voteRewardPlugin.database)
            sqlUserUtils.load(callback);
        else
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
                        sqlUserUtils.save();
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
                        sqlUserUtils.reset();
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

        public static SQLUserUtils getUser(UUID uuid) {
            return new SQLUserUtils(uuid);
        }

        private final User user;

        private final UUID uuid;

        private SQLUserUtils(UUID uuid) {
            this.uuid = uuid;
            this.user = userMap.get(uuid);
        }

        /**
         * Save all player's data
         *
         * @throws SQLException When something goes wrong
         */
        public void save() throws SQLException {
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + user.getVotes() + "', " +
                            "`time` = '" + user.getLastVoted() + "', " +
                            "`voteparty` = '" + user.getVoteParties() + "', " +
                            "`daily` = '" + user.getDailyVotes() + "', " +
                            "`services` = '" + user.getServices().toString().replace("[", "").replace("]", "").replace(" ", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            preparedStatement.executeUpdate();
        }

        /**
         * Reset all player's data
         *
         * @throws SQLException When something goes wrong
         */
        public void reset() throws SQLException {
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + 0 + "', " +
                            "`time` = '" + 0 + "', " +
                            "`voteparty` = '" + 0 + "', " +
                            "`daily` = '" + 0 + "', " +
                            "`services` = '" + Lists.newArrayList().toString().replace("[", "").replace("]", "").replace(" ", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            user.setVotes(0);
            user.setLastVoted(0);
            user.setServices(Lists.newArrayList());
            user.setVoteParties(0);
            user.setDailyVotes(0);
            preparedStatement.executeUpdate();
        }

        /**
         * Load all player's data
         *
         * @throws SQLException When something goes wrong
         */
        public void load(Callback callback) {
            setupUser(new Callback() {
                @Override
                public void onSuccess() {
                    try {
                        PreparedStatement preparedStatement = VoteRewardPlugin.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid.toString() + "'");
                        ResultSet resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            user.setVotes(resultSet.getInt("votes"));
                            user.setLastVoted(resultSet.getLong("time"));
                            user.setServices(resultSet.getString("services").replace(" ", "").isEmpty() ? Lists.newArrayList() : new ArrayList<>(Arrays.asList(resultSet.getString("services").split(","))));
                            user.setVoteParties(resultSet.getInt("voteparty"));
                            user.setDailyVotes(resultSet.getInt("daily"));
                            if (VoteOptions.DEBUG.isEnabled()) {
                                Utils.debug(VoteRewardPlugin.getInstance(),
                                        String.valueOf(user.getVotes()),
                                        String.valueOf(user.getDailyVotes()),
                                        String.valueOf(user.getLastVoted()),
                                        String.valueOf(user.getVoteParties()));
                            }
                        }
                        callback.onSuccess();
                    } catch (SQLException throwables) {
                        callback.onFailure(throwables.getCause());
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }

        /**
         * Check if the player exists
         *
         * @return true if player exists or false when is not
         */
        public boolean playerExists() {
            return getAllUsersMap().containsKey(Bukkit.getOfflinePlayer(user.getUniqueID()).getName());
        }

        /**
         * Setup the user data to the database
         */
        public void setupUser(Callback callback) {
            try {
                if (!playerExists()) {
                    PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                            "INSERT INTO `users` (`uuid`, `name`, `votes`, `time`, `daily`, `voteparty`, `services`)" +
                                    " VALUES " +
                                    "('" + uuid.toString() + "', '" + Bukkit.getOfflinePlayer(uuid).getName() + "','0', '0', '0', '0', '" + Lists.newArrayList().toString().replace("[", "").replace("]", "").replace(" ", "") + "');");
                    preparedStatement.executeUpdate();
                }
                callback.onSuccess();
            } catch (SQLException throwables) {
                callback.onFailure(throwables.getCause());
            }
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

        private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();
        private final User user;

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

            this.user = userMap.get(uuid) == null ? UserVoteData.getUser(uuid).getUser() : userMap.get(uuid);
        }

        private final File file;
        private final UUID uuid;
        private YamlConfiguration configuration = null;
        private OfflinePlayer voter;

        private OfflinePlayer getVoter() {
            return voter == null ? voter = Bukkit.getOfflinePlayer(uuid) : voter;
        }

        private void reloadConfiguration() {
            this.configuration = YamlConfiguration.loadConfiguration(file);
        }

        /**
         * Save the configuration
         */
        public void saveConfiguration() {
            this.configuration.set("total-votes", user.getVotes());
            this.configuration.set("daily-votes", user.getDailyVotes());
            this.configuration.set("voteparty", user.getVoteParties());
            this.configuration.set("last-vote", user.getLastVoted());
            this.configuration.set("offline vote.services", user.getServices());
            try {
                this.configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Return player votes
         *
         * @return player total votes
         */
        public int getVotes() {
            return this.configuration.getInt("total-votes", 0);
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
            if (!playerExists()) {
                if (VoteOptions.DEBUG.isEnabled())
                    Utils.debug(voteRewardPlugin, "Player " + getVoter().getName() + " doesn't exists!");
                user.setDailyVotes(0);
                user.setVotes(0);
                user.setLastVoted(0);
                user.setVoteParties(0);
                user.setServices(Lists.newArrayList());
            } else {
                if (VoteOptions.DEBUG.isEnabled())
                    Utils.debug(voteRewardPlugin, "Player " + getVoter().getName() + " exists!");
                user.setDailyVotes(getDailyVotes());
                user.setVotes(getVotes());
                user.setLastVoted(getLastVote());
                user.setVoteParties(getVoteParty());
                user.setServices(getServices());
            }
            saveConfiguration();
        }

        /**
         * Check if the player exists
         *
         * @return true if player exists or false when is not
         */
        public boolean playerExists() {
            return getAllUsersMap().containsKey(Bukkit.getOfflinePlayer(user.getUniqueID()).getName());
        }

        /**
         * Reset player
         */
        public void reset() {
            user.setVotes(0);
            user.setVoteParties(0);
            user.setLastVoted(0);
            user.setServices(Lists.newArrayList());
            user.setDailyVotes(0);
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


    public interface Callback {
        void onSuccess();

        void onFailure(Throwable throwable);
    }
}
