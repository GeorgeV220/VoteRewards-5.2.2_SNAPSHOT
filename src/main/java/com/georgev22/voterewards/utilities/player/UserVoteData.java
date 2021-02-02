package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.database.Type;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.maps.ConcurrentObjectMap;
import com.georgev22.voterewards.utilities.maps.LinkedObjectMap;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Used to handle all user's votes and anything related to them.
 */
public class UserVoteData {

    private static final ConcurrentObjectMap<UUID, User> userMap = new ConcurrentObjectMap<>();
    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    /**
     * Returns all loaded users
     *
     * @return all loaded users
     */
    public static ConcurrentObjectMap<UUID, User> getUserMap() {
        return userMap;
    }

    private static final LinkedObjectMap<String, Integer> allUsersMap = new LinkedObjectMap<>();

    /**
     * Returns all the players in a map
     *
     * @return all the players
     */
    public static LinkedObjectMap<String, Integer> getAllUsersMap() {
        return allUsersMap;
    }

    /**
     * Load all users
     */
    public static void loadAllUsers() {
        if (voteRewardPlugin.getDatabaseType().equals(Type.SQL)) {
            try {
                allUsersMap.putAll(UserVoteData.SQLUserUtils.getAllUsers());
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.FILE)) {
            allUsersMap.putAll(UserVoteData.UserUtils.getAllUsers());
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.MONGO)) {
            allUsersMap.putAll(MongoDBUtils.getAllUsers());
        }
        Utils.debug(voteRewardPlugin, getAllUsersMap().toString());
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param uuid Player's Unique identifier
     * @return a copy of this UserVoteData class for a specific user.
     */
    public static UserVoteData getUser(UUID uuid) {
        if (userMap.get(uuid) == null) {
            userMap.append(uuid, new User(uuid));
        }
        return new UserVoteData(userMap.get(uuid));
    }

    private final User user;
    private UserUtils userUtils;
    private SQLUserUtils sqlUserUtils;
    private MongoDBUtils mongoDBUtils;

    private UserVoteData(User user) {
        this.user = user;
        if (voteRewardPlugin.getDatabaseType().equals(Type.SQL)) {
            this.sqlUserUtils = SQLUserUtils.getUser(user.getUniqueID());
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.FILE)) {
            this.userUtils = UserUtils.getUser(user.getUniqueID());
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.MONGO)) {
            this.mongoDBUtils = MongoDBUtils.getUser(user.getUniqueID());
        }
    }

    /**
     * Set player votes
     *
     * @param votes The amount of votes.
     */
    public void setVotes(int votes) {
        user.append("votes", votes);
    }

    /**
     * Set the last time when a player voted
     *
     * @param lastVoted Last time the player voted.
     */
    public void setLastVoted(long lastVoted) {
        user.append("last", lastVoted);
    }

    /**
     * Set how many virtual crates a player have
     *
     * @param voteParties The amount of vote party crates.
     */
    public void setVoteParties(int voteParties) {
        user.append("voteparty", voteParties);
    }

    /**
     * Set the offline services
     *
     * @param services The services that player voted when he was offline.
     */
    public void setOfflineServices(List<String> services) {
        if (Options.DEBUG_VOTES_OFFLINE.isEnabled())
            Utils.debug(voteRewardPlugin, "Offline Voting Debug", services.toString());
        user.append("services", services);
    }

    /**
     * Set daily votes
     *
     * @param votes The amount of daily votes
     */
    public void setDailyVotes(int votes) {
        user.append("daily", votes);
    }

    /**
     * Get user daily votes
     *
     * @return user daily votes
     */
    public int getDailyVotes() {
        return user.getInteger("daily");
    }

    /**
     * Get player total votes
     * Check UserUtils#getVotes()
     *
     * @return player total votes
     */
    public int getVotes() {
        return user.getInteger("votes");
    }

    /**
     * Get player virtual crates
     * Check UserUtils#getVoteParty()
     *
     * @return player virtual crates
     */
    public int getVoteParty() {
        return user.getInteger("voteparty");
    }

    /**
     * Get the last time when the player voted
     * Check UserUtils#getLastVote()
     *
     * @return the last time when the player voted
     */
    public long getLastVote() {
        return user.getLong("last", 0L);
    }

    /**
     * Get all services that the player have voted
     * when he was offline
     *
     * @return services
     */
    public List<String> getOfflineServices() {
        return user.getList("services", String.class);
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
     * @param callback Callback
     */
    public void load(Callback callback) {
        if (voteRewardPlugin.getDatabaseType().equals(Type.SQL)) {
            sqlUserUtils.load(callback);
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.FILE)) {
            userUtils.setupUser();
            callback.onSuccess();
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.MONGO)) {
            mongoDBUtils.load(callback);
        }
    }

    /**
     * Save all player's data
     *
     * @param async True if you want to save async
     */
    public void save(boolean async) {
        if (voteRewardPlugin.getDatabaseType().equals(Type.SQL)) {
            if (async) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            sqlUserUtils.save();
                        } catch (SQLException | ClassNotFoundException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(voteRewardPlugin);
            } else {
                try {
                    sqlUserUtils.save();
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
            }
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.FILE)) {
            userUtils.saveConfiguration();
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.MONGO)) {
            if (async) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        mongoDBUtils.save();
                    }
                }.runTaskAsynchronously(voteRewardPlugin);
            } else {
                mongoDBUtils.save();
            }

        }
    }

    /**
     * Reset player's stats
     */
    public void reset() {
        if (voteRewardPlugin.getDatabaseType().equals(Type.SQL)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        sqlUserUtils.reset();
                    } catch (SQLException | ClassNotFoundException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(voteRewardPlugin);
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.FILE)) {
            userUtils.reset();
        } else if (voteRewardPlugin.getDatabaseType().equals(Type.MONGO)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    mongoDBUtils.reset();
                }
            }.runTaskAsynchronously(voteRewardPlugin);
        }
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
            this.user = userMap.get(uuid) == null ? UserVoteData.getUser(uuid).getUser() : userMap.get(uuid);
        }

        /**
         * Save all player's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class not found
         */
        public void save() throws SQLException, ClassNotFoundException {
            voteRewardPlugin.getDatabase().updateSQL(
                    "UPDATE `" + Options.DATABASE_TABLE_NAME.getValue() + "` " +
                            "SET `votes` = '" + user.getInteger("votes") + "', " +
                            "`time` = '" + user.getLong("last", 0L) + "', " +
                            "`voteparty` = '" + user.getInteger("voteparty") + "', " +
                            "`daily` = '" + user.getInteger("daily") + "', " +
                            "`services` = '" + user.getList("services", String.class).toString().replace("[", "").replace("]", "").replace(" ", "") + "' " +
                            "WHERE `uuid` = '" + uuid.toString() + "'");
            if (Options.DEBUG_SAVE.isEnabled()) {
                Utils.debug(voteRewardPlugin,
                        "User " + user.getPlayer().getName() + " successfully saved!",
                        "Votes: " + user.getInteger("votes"),
                        "Daily Votes: " + user.getInteger("daily"),
                        "Last Voted: " + Instant.ofEpochMilli(user.getLong("last", 0L)).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())),
                        "Vote Parties: " + user.getInteger("voteparty"));
            }
        }

        /**
         * Reset all player's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class is not found
         */
        public void reset() throws SQLException, ClassNotFoundException {
            user.append("votes", 0)
                    .append("last", 0L)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0);
            save();
        }

        /**
         * Load all player's data
         *
         * @param callback Callback
         */
        public void load(Callback callback) {
            setupUser(new Callback() {
                @Override
                public void onSuccess() {
                    try {
                        ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + Options.DATABASE_TABLE_NAME.getValue() + "` WHERE `uuid` = '" + uuid.toString() + "'");
                        while (resultSet.next()) {
                            user.append("votes", resultSet.getInt("votes"))
                                    .append("last", resultSet.getLong("time"))
                                    .append("services", resultSet.getString("services").replace(" ", "").isEmpty() ? Lists.newArrayList() : new ArrayList<>(Arrays.asList(resultSet.getString("services").split(","))))
                                    .append("voteparty", resultSet.getInt("voteparty"))
                                    .append("daily", resultSet.getInt("daily"));
                            if (Options.DEBUG_LOAD.isEnabled()) {
                                Utils.debug(voteRewardPlugin,
                                        "User " + user.getPlayer().getName() + " successfully loaded!",
                                        "Votes: " + user.getInteger("votes", 0),
                                        "Daily Votes: " + user.getInteger("daily", 0),
                                        "Last Voted: " + Instant.ofEpochMilli(user.getLong("last", 0L)).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())),
                                        "Vote Parties: " + user.getInteger("voteparty"));
                            }
                        }
                        callback.onSuccess();
                    } catch (SQLException | ClassNotFoundException throwables) {
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
         *
         * @param callback Callback
         */
        public void setupUser(Callback callback) {
            try {
                if (!playerExists()) {
                    voteRewardPlugin.getDatabase().updateSQL(
                            "INSERT INTO `" + Options.DATABASE_TABLE_NAME.getValue() + "` (`uuid`, `name`, `votes`, `time`, `daily`, `voteparty`, `services`)" +
                                    " VALUES " +
                                    "('" + uuid.toString() + "', '" + Bukkit.getOfflinePlayer(uuid).getName() + "','0', '0', '0', '0', '" + Lists.newArrayList().toString().replace("[", "").replace("]", "").replace(" ", "") + "');");
                }
                callback.onSuccess();
            } catch (SQLException | ClassNotFoundException throwables) {
                callback.onFailure(throwables.getCause());
            }
        }

        /**
         * Get all players from the database
         *
         * @return all the players from the database
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When the class is not found
         */
        public static ObjectMap<String, Integer> getAllUsers() throws SQLException, ClassNotFoundException {
            ObjectMap<String, Integer> map = new LinkedObjectMap<>();
            ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + Options.DATABASE_TABLE_NAME.getValue() + "`");
            while (resultSet.next()) {
                map.append(resultSet.getString("name"), resultSet.getInt("votes"));
            }
            return map;
        }
    }

    /**
     * All Mongo Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happened
     */
    public static class MongoDBUtils {

        private final User user;
        private final UUID uuid;

        /**
         * Returns a copy of this MongoDBUtils class for a specific user.
         *
         * @param uuid Player's unique identifier.
         * @return a copy of this MongoDBUtils class for a specific user.
         */
        public static MongoDBUtils getUser(final UUID uuid) {
            return new MongoDBUtils(uuid);
        }

        private MongoDBUtils(UUID uuid) {
            this.uuid = uuid;
            this.user = userMap.get(uuid) == null ? UserVoteData.getUser(uuid).getUser() : userMap.get(uuid);
        }

        /**
         * Save all player's data
         */
        public void save() {
            BasicDBObject query = new BasicDBObject();
            query.append("uuid", user.getUniqueID().toString());

            BasicDBObject updateObject = new BasicDBObject();
            updateObject.append("$set", new BasicDBObject()
                    .append("uuid", user.getUniqueID().toString())
                    .append("name", user.getPlayer().getName())
                    .append("votes", user.getInteger("votes", 0))
                    .append("voteparty", user.getInteger("voteparty", 0))
                    .append("daily", user.getInteger("daily", 0))
                    .append("last-vote", user.getLong("last", 0L))
                    .append("services", user.getList("services", String.class)));

            voteRewardPlugin.getMongoDB().getCollection().updateOne(query, updateObject);
        }

        /**
         * Load player data
         *
         * @param callback Callback
         */
        public void load(Callback callback) {
            setupUser(new Callback() {
                @Override
                public void onSuccess() {
                    BasicDBObject searchQuery = new BasicDBObject();
                    searchQuery.append("uuid", uuid.toString());
                    FindIterable<Document> findIterable = voteRewardPlugin.getMongoDB().getCollection().find(searchQuery);
                    Document document = findIterable.first();
                    user.append("votes", document.getInteger("votes"))
                            .append("daily", document.getInteger("daily"))
                            .append("voteparty", document.getInteger("voteparty"))
                            .append("last", document.getLong("last-vote"))
                            .append("services", document.getList("services", String.class));
                    callback.onSuccess();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    callback.onFailure(throwable.getCause());
                }
            });
        }

        /**
         * Setup the user
         *
         * @param callback Callback
         */
        public void setupUser(Callback callback) {
            if (!playerExists()) {
                voteRewardPlugin.getMongoDB().getCollection().insertOne(new Document()
                        .append("uuid", user.getUniqueID().toString())
                        .append("name", user.getPlayer().getName())
                        .append("votes", 0)
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("last-vote", (long) 0)
                        .append("services", Lists.newArrayList()));
            }
            callback.onSuccess();
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
         * Reset player stats
         */
        public void reset() {
            user.append("votes", 0)
                    .append("last", 0L)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0);
            save();
        }


        /**
         * Get all players from the database
         *
         * @return all the players from the database
         */
        public static ObjectMap<String, Integer> getAllUsers() {
            ObjectMap<String, Integer> map = new LinkedObjectMap<>();
            FindIterable<Document> iterable = voteRewardPlugin.getMongoDB().getCollection().find();
            iterable.forEach((Block<Document>) document -> map.append(document.getString("name"), document.getInteger("votes")));
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
         * @param uuid Player's unique identifier.
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
        public static ObjectMap<String, Integer> getAllUsers() {
            ObjectMap<String, Integer> map = new LinkedObjectMap<>();

            File[] files = new File(VoteRewardPlugin.getInstance().getDataFolder(), "userdata").listFiles();

            if (files == null) {
                return map;
            }

            for (File file : files) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                if (cfg.get("last-name") == null)
                    continue;
                map.append(cfg.getString("last-name"), cfg.getInt("total-votes"));
            }

            return map;
        }

        private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();
        private final User user;

        private UserUtils(final UUID uuid) {
            this.uuid = uuid;

            if (new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata").mkdirs()) {
                if (Options.DEBUG_CREATE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "Folder userdata has been created!");
                }
            }

            this.file = new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata" + File.separator + uuid.toString() + ".yml");

            if (!this.file.exists()) {
                try {
                    if (this.file.createNewFile()) {
                        if (Options.DEBUG_CREATE.isEnabled()) {
                            Utils.debug(voteRewardPlugin, "File " + file.getName() + " for the user " + Bukkit.getOfflinePlayer(uuid).getName() + " has been created!");
                        }
                    }
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
            this.configuration.set("total-votes", user.getInteger("votes", 0));
            this.configuration.set("daily-votes", user.getInteger("daily", 0));
            this.configuration.set("voteparty", user.getInteger("voteparty", 0));
            this.configuration.set("last-vote", user.getLong("last", 0L));
            this.configuration.set("offline vote.services", user.getList("services", String.class));
            try {
                this.configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Options.DEBUG_SAVE.isEnabled()) {
                Utils.debug(voteRewardPlugin,
                        "User " + user.getPlayer().getName() + " successfully saved!",
                        "Votes: " + user.getInteger("votes", 0),
                        "Daily Votes: " + user.getInteger("daily", 0),
                        "Last Voted: " + Instant.ofEpochMilli(user.getLong("last", 0L)).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())),
                        "Vote Parties: " + user.getInteger("voteparty", 0));
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
            return this.configuration.getLong("last-vote", 0L);
        }

        /**
         * Get Daily votes
         *
         * @return int
         */
        private int getDailyVotes() {
            return this.configuration.getInt("daily-votes", 0);
        }

        /**
         * Setup the user
         */
        public void setupUser() {
            this.configuration.set("last-name", getVoter().getName());
            if (!playerExists()) {
                if (Options.DEBUG_LOAD.isEnabled())
                    Utils.debug(voteRewardPlugin,
                            "User " + getVoter().getName() + " doesn't exists!",
                            "Creating new User");
                user.append("votes", 0)
                        .append("last", 0L)
                        .append("services", Lists.newArrayList())
                        .append("voteparty", 0)
                        .append("daily", 0);
            } else {
                user.append("votes", getVotes())
                        .append("last", getLastVote())
                        .append("services", getServices())
                        .append("voteparty", getVoteParty())
                        .append("daily", getDailyVotes());
                if (Options.DEBUG_LOAD.isEnabled()) {
                    Utils.debug(voteRewardPlugin,
                            "User " + user.getPlayer().getName() + " successfully loaded!",
                            "Votes: " + user.getInteger("votes", 0),
                            "Daily Votes: " + user.getInteger("daily", 0),
                            "Last Voted: " + Instant.ofEpochMilli(user.getLong("last", 0L)).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())),
                            "Vote Parties: " + user.getInteger("voteparty", 0));
                }
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
         * Reset player stats
         */
        public void reset() {
            user.append("votes", 0)
                    .append("last", 0L)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0);
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
