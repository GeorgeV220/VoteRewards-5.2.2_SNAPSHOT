package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserUtils {

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
     * Returns a copy of this UserUtils class for a specific user.
     *
     * @param uuid
     * @return a copy of this UserUtils class for a specific user.
     */
    public static UserUtils getUser(UUID uuid) {
        if (userMap.get(uuid) == null) {
            userMap.put(uuid, new User(uuid));
        }
        return new UserUtils(userMap.get(uuid));
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
    private final UserVoteData userVoteData;
    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private UserUtils(User user) {
        this.user = user;
        this.userVoteData = UserVoteData.getUser(user.getUniqueID());
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
        userVoteData.setVotes(user.getVotes());
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
        userVoteData.setLastVote(lastVoted);
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
        userVoteData.setVoteParty(voteParties);
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
        userVoteData.setOfflineServices(services);
    }

    /**
     * Get player total votes
     * Check UserVoteData#getVotes()
     *
     * @return player total votes
     */
    public int getVotes() {
        return user.getVotes();
    }

    /**
     * Get player virtual crates
     * Check UserVoteData#getVoteParty()
     *
     * @return player virtual crates
     */
    public int getVoteParties() {
        return user.getVoteParties();
    }

    /**
     * Get the last time when the player voted
     * Check UserVoteData#getLastVote()
     *
     * @return the last time when the player voted
     */
    public long getLastVoted() {
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
     * Returns the UserVoteData class object
     *
     * @return the UserVoteData class object
     */
    public UserVoteData getUserVoteData() {
        return userVoteData;
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
        return userVoteData.playerExist();
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
        userVoteData.setupUser();
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
        userVoteData.saveConfiguration();
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
        userVoteData.reset();
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
            UserUtils userUtils = UserUtils.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + userUtils.getVotes() + "', " +
                            "`time` = '" + userUtils.getLastVoted() + "', " +
                            "`voteparty` = '" + userUtils.getVoteParties() + "', " +
                            "`services` = '" + userUtils.getOfflineServices().toString().replace("[", "").replace("]", "") + "' " +
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
            UserUtils userUtils = UserUtils.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement(
                    "UPDATE `users` " +
                            "SET `votes` = '" + 0 + "', " +
                            "`time` = '" + 0 + "', " +
                            "`voteparty` = '" + 0 + "', " +
                            "`services` = '" + Lists.newArrayList().toString().replace("[", "").replace("]", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            userUtils.setVotes(0);
            userUtils.setLastVoted(0);
            userUtils.setOfflineServices(Lists.newArrayList());
            userUtils.setVoteParties(0);
            preparedStatement.executeUpdate();
        }

        /**
         * Load all player's data
         *
         * @param uuid Player's Unique ID
         * @throws SQLException When something goes wrong
         */
        public static void load(UUID uuid) throws SQLException {
            UserUtils userUtils = UserUtils.getUser(uuid);
            PreparedStatement preparedStatement = voteRewardPlugin.getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid.toString() + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userUtils.setVotes(resultSet.getInt("votes"));
                userUtils.setLastVoted(resultSet.getLong("time"));
                userUtils.setOfflineServices(new ArrayList<>(Arrays.asList(resultSet.getString("services").split(","))));
                userUtils.setVoteParties(resultSet.getInt("voteparty"));
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
            UserUtils.getUserMap().put(uuid, new User(uuid));
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
}
