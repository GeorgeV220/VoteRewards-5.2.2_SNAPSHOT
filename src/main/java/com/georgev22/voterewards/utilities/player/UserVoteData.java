package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.utilities.maps.HashObjectMap;
import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Used to handle all user's data and anything related to them.
 */
public record UserVoteData(User user) {
    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private static final ObjectMap<UUID, User> allUsersMap = ObjectMap.newConcurrentObjectMap();

    /**
     * Returns all the players in a map
     *
     * @return all the players
     */
    public static ObjectMap<UUID, User> getAllUsersMap() {
        return allUsersMap;
    }

    public static ObjectMap<String, User> getAllUsersMapWithName() {
        ObjectMap<String, User> objectMap = new HashObjectMap<>();
        for (Map.Entry<UUID, User> entry : allUsersMap.entrySet()) {
            objectMap.append(entry.getValue().getName(), entry.getValue());
        }
        return objectMap;
    }

    /**
     * Load all users
     *
     * @throws Exception When something goes wrong
     */
    public static void loadAllUsers() throws Exception {
        allUsersMap.putAll(voteRewardPlugin.getIDatabaseType().getAllUsers());
        if (OptionsUtil.DEBUG_LOAD.isEnabled())
            Utils.debug(voteRewardPlugin, getAllUsersMap().toString());
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param offlinePlayer Offline player object
     * @return a copy of this UserVoteData class for a specific user.
     * @since v4.7.0
     */
    public static UserVoteData getUser(OfflinePlayer offlinePlayer) {
        return getUser(offlinePlayer.getUniqueId());
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param uuid Player's Unique identifier
     * @return a copy of this UserVoteData class for a specific user.
     */
    public static UserVoteData getUser(UUID uuid) {
        if (allUsersMap.get(uuid) == null) {
            allUsersMap.append(uuid, new User(uuid));
        }
        return new UserVoteData(allUsersMap.get(uuid));
    }

    public UserVoteData {
        if (OptionsUtil.DEBUG_USELESS.isEnabled())
            Utils.debug(voteRewardPlugin, user.toString());
    }

    /**
     * Set player name
     */
    public UserVoteData setName(String name) {
        user.append("name", name);
        return this;
    }

    /**
     * Set player votes
     *
     * @param votes The amount of votes.
     */
    public UserVoteData setVotes(int votes) {
        user.append("votes", votes);
        return this;
    }

    /**
     * Set player all time votes
     *
     * @param votes The amount of votes.
     */
    public UserVoteData setAllTimeVotes(int votes) {
        user.append("totalvotes", votes);
        return this;
    }

    /**
     * Set the last time when a player voted
     *
     * @param lastVoted Last time the player voted.
     */
    public UserVoteData setLastVoted(long lastVoted) {
        user.append("last", lastVoted);
        return this;
    }

    /**
     * Set how many virtual crates a player have
     *
     * @param voteParties The amount of vote party crates.
     */
    public UserVoteData setVoteParties(int voteParties) {
        user.append("voteparty", voteParties);
        return this;
    }

    /**
     * Set the offline services
     *
     * @param services The services that player voted when he was offline.
     */
    public UserVoteData setOfflineServices(List<String> services) {
        if (OptionsUtil.DEBUG_VOTES_OFFLINE.isEnabled())
            Utils.debug(voteRewardPlugin, "Offline Voting Debug", services.toString());
        user.append("services", services);
        return this;
    }

    /**
     * Set daily votes
     *
     * @param votes The amount of daily votes
     */
    public UserVoteData setDailyVotes(int votes) {
        user.append("daily", votes);
        return this;
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
     * Get user all time votes
     *
     * @return user all time votes
     */
    public int getAllTimeVotes() {
        return user.getAllTimeVotes();
    }

    /**
     * Get user total votes
     *
     * @return user total votes
     */
    public int getVotes() {
        return user.getVotes();
    }

    /**
     * Get user virtual crates
     *
     * @return user virtual crates
     */
    public int getVoteParty() {
        return user.getVoteParties();
    }

    /**
     * Get the last time when the user voted
     *
     * @return the last time when the user voted
     */
    public long getLastVote() {
        return user.getLastVoted();
    }

    /**
     * Get all services that the user have voted
     * when he was offline
     *
     * @return services
     */
    public List<String> getOfflineServices() {
        return user.getServices();
    }

    /**
     * Check if the user exists
     *
     * @return true if user exists or false when is not
     */
    public boolean playerExists() {
        return getAllUsersMap().containsKey(user.getUniqueId());
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
     * Check {@link Utils#runCommand(String)}
     *
     * @param s the list with all the commands
     */
    public void runCommands(List<String> s) {
        Utils.debug(voteRewardPlugin, "RUNNING COMMANDS FOR PLAYER: " + user.getName());
        for (String b : s) {
            Utils.runCommand(b.replace("%player%", user.getName()));
        }
    }

    /**
     * Load user data
     *
     * @param callback Callback
     * @throws Exception When something goes wrong
     */
    public void load(Callback callback) throws Exception {
        voteRewardPlugin.getIDatabaseType().load(user, callback);
    }

    /**
     * Save all user's data
     *
     * @param async True if you want to save async
     */
    public void save(boolean async, Callback callback) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
                try {
                    voteRewardPlugin.getIDatabaseType().save(user);
                    callback.onSuccess();
                } catch (Exception e) {
                    callback.onFailure(e.getCause());
                }
            });
        } else {
            try {
                voteRewardPlugin.getIDatabaseType().save(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e.getCause());
            }
        }
    }

    /**
     * Reset user's stats
     *
     * @param allTime Set it true to reset all time votes.
     */
    public void reset(boolean allTime) {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            try {
                voteRewardPlugin.getIDatabaseType().reset(user, allTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Delete user from database
     */
    public void delete() {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            try {
                voteRewardPlugin.getIDatabaseType().delete(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * All SQL Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happened
     */
    public static class SQLUserUtils implements IDatabaseType {

        private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

        /**
         * Save all user's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class not found
         */
        public void save(User user) throws SQLException, ClassNotFoundException {
            voteRewardPlugin.getDatabase().updateSQL(
                    "UPDATE `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` " +
                            "SET `votes` = '" + user.getVotes() + "', " +
                            "`name` = '" + user.getName() + "', " +
                            "`time` = '" + user.getLastVoted() + "', " +
                            "`voteparty` = '" + user.getVoteParties() + "', " +
                            "`daily` = '" + user.getDailyVotes() + "', " +
                            "`services` = '" + user.getServices().toString().replace("[", "").replace("]", "").replace(" ", "") + "', " +
                            "`totalvotes` = '" + user.getAllTimeVotes() + "' " +
                            "WHERE `uuid` = '" + user.getUniqueId() + "'");
            if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                Utils.debug(voteRewardPlugin,
                        "User " + user.getName() + " successfully saved!",
                        "Votes: " + user.getVotes(),
                        "Daily Votes: " + user.getDailyVotes(),
                        "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted(),
                        "Vote Parties: " + user.getVoteParties(),
                        "All time votes: " + user.getAllTimeVotes());
            }
        }

        /**
         * Remove user's data from database.
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class is not found
         */
        public void delete(User user) throws SQLException, ClassNotFoundException {
            voteRewardPlugin.getDatabase().updateSQL("DELETE FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueId().toString() + "';");
            Utils.debug(voteRewardPlugin, "User " + user.getName() + " deleted from the database!");
            allUsersMap.remove(user.getUniqueId());
        }

        /**
         * Load all user's data
         *
         * @param callback Callback
         */
        public void load(User user, Callback callback) {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    try {
                        ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueId().toString() + "'");
                        while (resultSet.next()) {
                            user.append("votes", resultSet.getInt("votes"))
                                    .append("name", resultSet.getString("name"))
                                    .append("last", resultSet.getLong("time"))
                                    .append("services", resultSet.getString("services").replace(" ", "").isEmpty() ? Lists.newArrayList() : new ArrayList<>(Arrays.asList(resultSet.getString("services").split(","))))
                                    .append("voteparty", resultSet.getInt("voteparty"))
                                    .append("daily", resultSet.getInt("daily"))
                                    .append("totalvotes", resultSet.getInt("totalvotes"));
                            if (OptionsUtil.DEBUG_LOAD.isEnabled()) {
                                Utils.debug(voteRewardPlugin,
                                        "User " + user.getName() + " successfully loaded!",
                                        "Votes: " + user.getVotes(),
                                        "Daily Votes: " + user.getDailyVotes(),
                                        "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted() + " " + user.getLastVoted(),
                                        "Vote Parties: " + user.getVoteParties(),
                                        "All time votes: " + user.getAllTimeVotes(),
                                        "Services: " + user.getServices().toString());
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
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(User user) throws SQLException, ClassNotFoundException {
            return voteRewardPlugin.getDatabase().querySQL("SELECT * FROM " + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + " WHERE `uuid` = '" + user.getUniqueId().toString() + "'").next();
        }

        /**
         * Setup the user data to the database
         *
         * @param callback Callback
         */
        public void setupUser(User user, Callback callback) {
            try {
                if (!playerExists(user)) {
                    voteRewardPlugin.getDatabase().updateSQL(
                            "INSERT INTO `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` (`uuid`, `name`, `votes`, `time`, `daily`, `voteparty`, `services`, `totalvotes`)" +
                                    " VALUES " +
                                    "('" + user.getUniqueId().toString() + "', '" + user.getOfflinePlayer().getName() + "','0', '0', '0', '0', '" + Lists.newArrayList().toString().replace("[", "").replace("]", "").replace(" ", "") + "', '0'" + ");");
                }
                callback.onSuccess();
            } catch (SQLException | ClassNotFoundException throwables) {
                callback.onFailure(throwables.getCause());
            }
        }

        /**
         * Get all users from the database
         *
         * @return all the users from the database
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When the class is not found
         */
        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = ObjectMap.newConcurrentObjectMap();
            ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "`");
            while (resultSet.next()) {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(resultSet.getString("uuid")));
                userVoteData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        map.append(userVoteData.user().getUniqueId(), userVoteData.user());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
            return map;
        }
    }

    /**
     * All Mongo Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happened
     */
    public static class MongoDBUtils implements IDatabaseType {

        /**
         * Save all user's data
         */
        public void save(User user) {
            BasicDBObject query = new BasicDBObject();
            query.append("uuid", user.getUniqueId().toString());

            BasicDBObject updateObject = new BasicDBObject();
            updateObject.append("$set", new BasicDBObject()
                    .append("uuid", user.getUniqueId().toString())
                    .append("name", user.getName())
                    .append("votes", user.getVotes())
                    .append("voteparty", user.getVoteParties())
                    .append("daily", user.getDailyVotes())
                    .append("last-vote", user.getLastVoted())
                    .append("services", user.getServices())
                    .append("totalvotes", user.getAllTimeVotes()));

            voteRewardPlugin.getMongoDB().getCollection().updateOne(query, updateObject);
            if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                Utils.debug(voteRewardPlugin,
                        "User " + user.getName() + " successfully saved!",
                        "Votes: " + user.getVotes(),
                        "Daily Votes: " + user.getDailyVotes(),
                        "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted(),
                        "Vote Parties: " + user.getVoteParties(),
                        "All time votes: " + user.getAllTimeVotes(),
                        "Services: " + user.getServices().toString());
            }
        }

        /**
         * Load user data
         *
         * @param user     User
         * @param callback Callback
         */
        public void load(User user, Callback callback) {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    BasicDBObject searchQuery = new BasicDBObject();
                    searchQuery.append("uuid", user.getUniqueId().toString());
                    FindIterable<Document> findIterable = voteRewardPlugin.getMongoDB().getCollection().find(searchQuery);
                    Document document = findIterable.first();
                    user.append("votes", document.getInteger("votes"))
                            .append("name", document.getString("name"))
                            .append("daily", document.getInteger("daily"))
                            .append("voteparty", document.getInteger("voteparty"))
                            .append("last", document.getLong("last-vote"))
                            .append("services", document.getList("services", String.class))
                            .append("totalvotes", document.getInteger("totalvotes"));
                    callback.onSuccess();
                    if (OptionsUtil.DEBUG_LOAD.isEnabled()) {
                        Utils.debug(voteRewardPlugin,
                                "User " + user.getName() + " successfully loaded!",
                                "Votes: " + user.getVotes(),
                                "Daily Votes: " + user.getDailyVotes(),
                                "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted() + " " + user.getLastVoted(),
                                "Vote Parties: " + user.getVoteParties(),
                                "All time votes: " + user.getAllTimeVotes(),
                                "Services: " + user.getServices().toString());
                    }
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
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback callback) {
            if (!playerExists(user)) {
                voteRewardPlugin.getMongoDB().getCollection().insertOne(new Document()
                        .append("uuid", user.getUniqueId().toString())
                        .append("name", user.getOfflinePlayer().getName())
                        .append("votes", 0)
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("last-vote", 0L)
                        .append("services", Lists.newArrayList())
                        .append("totalvotes", 0));
            }
            callback.onSuccess();
        }

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(User user) {
            long count = voteRewardPlugin.getMongoDB().getCollection().count(new BsonDocument("uuid", new BsonString(user.getUniqueId().toString())));
            return count > 0;
        }

        /**
         * Remove user's data from database.
         */
        public void delete(User user) {
            BasicDBObject theQuery = new BasicDBObject();
            theQuery.put("uuid", user.getUniqueId().toString());
            DeleteResult result = voteRewardPlugin.getMongoDB().getCollection().deleteMany(theQuery);
            if (result.getDeletedCount() > 0) {
                if (OptionsUtil.DEBUG_DELETE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "User " + user.getName() + " deleted from the database!");
                }
                allUsersMap.remove(user.getUniqueId());
            }
        }


        /**
         * Get all users from the database
         *
         * @return all the users from the database
         */
        public ObjectMap<UUID, User> getAllUsers() {
            ObjectMap<UUID, User> map = ObjectMap.newConcurrentObjectMap();
            FindIterable<Document> iterable = voteRewardPlugin.getMongoDB().getCollection().find();
            iterable.forEach((Block<Document>) document -> {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(document.getString("uuid")));
                try {
                    userVoteData.load(new Callback() {
                        @Override
                        public void onSuccess() {
                            map.append(userVoteData.user().getUniqueId(), userVoteData.user());
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return map;
        }

    }


    /**
     * All User Utils for the user
     */

    public static class FileUserUtils implements IDatabaseType {

        private File file = null;
        private YamlConfiguration yamlConfiguration = null;
        private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

        /**
         * Save all user's data
         *
         * @param user User object
         */
        @Override
        public void save(User user) throws IOException {
            this.yamlConfiguration.set("votes", user.getVotes());
            this.yamlConfiguration.set("name", user.getName());
            this.yamlConfiguration.set("time", user.getLastVoted());
            this.yamlConfiguration.set("services", user.getServices());
            this.yamlConfiguration.set("voteparty", user.getVoteParties());
            this.yamlConfiguration.set("daily", user.getDailyVotes());
            this.yamlConfiguration.set("totalvotes", user.getAllTimeVotes());
            this.yamlConfiguration.save(file);
            if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                Utils.debug(voteRewardPlugin,
                        "User " + user.getName() + " successfully saved!",
                        "Votes: " + user.getVotes(),
                        "Daily Votes: " + user.getDailyVotes(),
                        "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted(),
                        "Vote Parties: " + user.getVoteParties(),
                        "All time votes: " + user.getAllTimeVotes());
            }
        }

        /**
         * Load user data
         *
         * @param user     User object
         * @param callback Callback
         */
        public void load(User user, Callback callback) {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    user.append("votes", yamlConfiguration.getInt("votes"))
                            .append("name", yamlConfiguration.getString("name"))
                            .append("last", yamlConfiguration.getLong("time"))
                            .append("services", yamlConfiguration.getStringList("services"))
                            .append("voteparty", yamlConfiguration.getInt("voteparty"))
                            .append("daily", yamlConfiguration.getInt("daily"))
                            .append("totalvotes", yamlConfiguration.getInt("totalvotes"));
                    callback.onSuccess();
                    if (OptionsUtil.DEBUG_LOAD.isEnabled()) {
                        Utils.debug(voteRewardPlugin,
                                "User " + user.getName() + " successfully loaded!",
                                "Votes: " + user.getVotes(),
                                "Daily Votes: " + user.getDailyVotes(),
                                "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted() + " " + user.getLastVoted(),
                                "Vote Parties: " + user.getVoteParties(),
                                "All time votes: " + user.getAllTimeVotes(),
                                "Services: " + user.getServices().toString());
                    }
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
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback callback) {
            if (new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata").mkdirs()) {
                if (OptionsUtil.DEBUG_CREATE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "Folder userdata has been created!");
                }
            }
            this.file = new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata" + File.separator + user.getUniqueId().toString() + ".yml");
            if (!playerExists(user)) {
                try {
                    if (this.file.createNewFile()) {
                        if (OptionsUtil.DEBUG_CREATE.isEnabled()) {
                            Utils.debug(voteRewardPlugin, "File " + file.getName() + " for the user " + Bukkit.getOfflinePlayer(user.getUniqueId()).getName() + " has been created!");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailure(e.getCause());
                }
                user.append("votes", 0)
                        .append("name", user.getOfflinePlayer().getName())
                        .append("last", 0L)
                        .append("services", Lists.newArrayList())
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("totalvotes", 0);
                this.yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                try {
                    save(user);
                } catch (IOException exception) {
                    callback.onFailure(exception.getCause());
                }
            }
            this.yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            callback.onSuccess();
        }

        /**
         * Remove user's data from file.
         */
        public void delete(User user) {
            if (file.delete()) {
                if (OptionsUtil.DEBUG_DELETE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "File " + file.getName() + " deleted!");
                }
                UserVoteData.getAllUsersMap().remove(user.getUniqueId());
            }
        }

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(User user) {
            return file.exists();
        }

        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = ObjectMap.newLinkedObjectMap();

            File[] files = new File(voteRewardPlugin.getDataFolder(), "userdata").listFiles((dir, name) -> name.endsWith(".yml"));

            if (files == null) {
                return map;
            }

            for (File file : files) {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(file.getName().replace(".yml", "")));
                userVoteData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        map.append(userVoteData.user().getUniqueId(), userVoteData.user());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }

            return map;
        }
    }
}
