package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Used to handle all user's data and anything related to them.
 */
public class UserVoteData {
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

    /**
     * Load all users
     */
    public static void loadAllUsers() throws Exception {
        allUsersMap.putAll(voteRewardPlugin.getIDatabaseType().getAllUsers());
        if (OptionsUtil.DEBUG_LOAD.isEnabled())
            Utils.debug(voteRewardPlugin, getAllUsersMap().toString());
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

    private final User user;

    private UserVoteData(User user) {
        this.user = user;
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
     * Set player all time votes
     *
     * @param votes The amount of votes.
     */
    public void setAllTimeVotes(int votes) {
        user.append("totalvotes", votes);
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
        if (OptionsUtil.DEBUG_VOTES_OFFLINE.isEnabled())
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
        return user.getDailyVotes();
    }


    /**
     * Get player all time votes
     *
     * @return player all time votes
     */
    public int getAllTimeVotes() {
        return user.getAllTimeVotes();
    }

    /**
     * Get player total votes
     *
     * @return player total votes
     */
    public int getVotes() {
        return user.getVotes();
    }

    /**
     * Get player virtual crates
     *
     * @return player virtual crates
     */
    public int getVoteParty() {
        return user.getVoteParties();
    }

    /**
     * Get the last time when the player voted
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
     * Check if the player exists
     *
     * @return true if player exists or false when is not
     */
    public boolean playerExists() {
        return getAllUsersMap().containsKey(user.getUniqueID());
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
            Bukkit.getScheduler().runTask(voteRewardPlugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(b.replace("%player%", user.getName()))));
        }
    }

    /**
     * Load player data
     *
     * @param callback Callback
     */
    public void load(Callback callback) throws Exception {
        voteRewardPlugin.getIDatabaseType().load(user, callback);
    }

    /**
     * Save all player's data
     *
     * @param async True if you want to save async
     */
    public void save(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
                try {
                    voteRewardPlugin.getIDatabaseType().save(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            try {
                voteRewardPlugin.getIDatabaseType().save(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reset player's stats
     */
    public void reset() {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            try {
                voteRewardPlugin.getIDatabaseType().reset(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Delete player from database
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
         * Save all player's data
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
                            "WHERE `uuid` = '" + user.getUniqueID() + "'");
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
         * Reset all player's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class is not found
         */
        public void reset(User user) throws SQLException, ClassNotFoundException {
            user.append("votes", 0)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0)
                    .append("totalvotes", 0);
            save(user);
            Utils.debug(voteRewardPlugin, "User " + user.getName() + " has been reset!");
        }

        /**
         * Remove player's data from database.
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class is not found
         */
        public void delete(User user) throws SQLException, ClassNotFoundException {
            voteRewardPlugin.getDatabase().updateSQL("DELETE FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueID().toString() + "';");
            Utils.debug(voteRewardPlugin, "User " + user.getName() + " deleted from the database!");
            allUsersMap.remove(user.getUniqueID());
        }

        /**
         * Load all player's data
         *
         * @param callback Callback
         */
        public void load(User user, Callback callback) {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    try {
                        ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueID().toString() + "'");
                        while (resultSet.next()) {
                            user.append("votes", resultSet.getInt("votes"))
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
         * Check if the player exists
         *
         * @return true if player exists or false when is not
         */
        public boolean playerExists(User user) throws SQLException, ClassNotFoundException {
            return voteRewardPlugin.getDatabase().querySQL("SELECT * FROM " + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + " WHERE `uuid` = '" + user.getUniqueID().toString() + "'").next();
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
                                    "('" + user.getUniqueID().toString() + "', '" + Bukkit.getOfflinePlayer(user.getUniqueID()).getName() + "','0', '0', '0', '0', '" + Lists.newArrayList().toString().replace("[", "").replace("]", "").replace(" ", "") + "', '0'" + ");");
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
        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = ObjectMap.newConcurrentObjectMap();
            ResultSet resultSet = voteRewardPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "`");
            while (resultSet.next()) {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(resultSet.getString("uuid")));
                userVoteData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        map.append(userVoteData.getUser().getUniqueID(), userVoteData.getUser());
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
         * Save all player's data
         */
        public void save(User user) {
            BasicDBObject query = new BasicDBObject();
            query.append("uuid", user.getUniqueID().toString());

            BasicDBObject updateObject = new BasicDBObject();
            updateObject.append("$set", new BasicDBObject()
                    .append("uuid", user.getUniqueID().toString())
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
         * Load player data
         *
         * @param user     User
         * @param callback Callback
         */
        public void load(User user, Callback callback) {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    BasicDBObject searchQuery = new BasicDBObject();
                    searchQuery.append("uuid", user.getUniqueID().toString());
                    FindIterable<Document> findIterable = voteRewardPlugin.getMongoDB().getCollection().find(searchQuery);
                    Document document = findIterable.first();
                    user.append("votes", document.getInteger("votes"))
                            .append("daily", document.getInteger("daily"))
                            .append("voteparty", document.getInteger("voteparty"))
                            .append("last", document.getLong("last-vote"))
                            .append("services", document.getList("services", String.class))
                            .append("totalvotes", document.getInteger("totalvotes"));
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
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback callback) {
            if (!playerExists(user)) {
                voteRewardPlugin.getMongoDB().getCollection().insertOne(new Document()
                        .append("uuid", user.getUniqueID().toString())
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
         * Check if the player exists
         *
         * @return true if player exists or false when is not
         */
        public boolean playerExists(User user) {
            long count = voteRewardPlugin.getMongoDB().getCollection().count(new BsonDocument("uuid", new BsonString(user.getUniqueID().toString())));
            return count > 0;
        }

        /**
         * Reset player stats
         */
        public void reset(User user) {
            user.append("votes", 0)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0)
                    .append("totalvotes", 0);
            save(user);
            Utils.debug(voteRewardPlugin, "User " + user.getName() + " has been reset!");
        }

        /**
         * Remove player's data from database.
         */
        public void delete(User user) {
            BasicDBObject theQuery = new BasicDBObject();
            theQuery.put("uuid", user.getUniqueID().toString());
            DeleteResult result = voteRewardPlugin.getMongoDB().getCollection().deleteMany(theQuery);
            if (result.getDeletedCount() > 0) {
                if (OptionsUtil.DEBUG_DELETE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "User " + user.getName() + " deleted from the database!");
                }
                allUsersMap.remove(user.getUniqueID());
            }
        }


        /**
         * Get all players from the database
         *
         * @return all the players from the database
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
                            map.append(userVoteData.getUser().getUniqueID(), userVoteData.getUser());
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
         * Save all player's data
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
        }

        /**
         * Load player data
         *
         * @param user     User object
         * @param callback Callback
         */
        public void load(User user, Callback callback) throws IOException {
            setupUser(user, new Callback() {
                @Override
                public void onSuccess() {
                    user.append("votes", yamlConfiguration.getInt("votes"))
                            .append("last", yamlConfiguration.getLong("time"))
                            .append("services", yamlConfiguration.getStringList("services"))
                            .append("voteparty", yamlConfiguration.getInt("voteparty"))
                            .append("daily", yamlConfiguration.getInt("daily"))
                            .append("totalvotes", yamlConfiguration.getInt("totalvotes"));
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
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback callback) throws IOException {
            if (new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata").mkdirs()) {
                if (OptionsUtil.DEBUG_CREATE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "Folder userdata has been created!");
                }
            }
            this.file = new File(VoteRewardPlugin.getInstance().getDataFolder(),
                    "userdata" + File.separator + user.getUniqueID().toString() + ".yml");
            if (!playerExists(user)) {
                try {
                    if (this.file.createNewFile()) {
                        if (OptionsUtil.DEBUG_CREATE.isEnabled()) {
                            Utils.debug(voteRewardPlugin, "File " + file.getName() + " for the user " + Bukkit.getOfflinePlayer(user.getUniqueID()).getName() + " has been created!");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailure(e.getCause());
                }
                user.append("votes", 0)
                        .append("last", 0L)
                        .append("services", Lists.newArrayList())
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("totalvotes", 0);
            }
            yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            save(user);
            callback.onSuccess();
        }

        /**
         * Reset player stats
         */
        public void reset(User user) throws IOException {
            user.append("votes", 0)
                    .append("services", Lists.newArrayList())
                    .append("voteparty", 0)
                    .append("daily", 0)
                    .append("totalvotes", 0);
            save(user);
            Utils.debug(voteRewardPlugin, "User " + user.getName() + " has been reset!");

        }

        /**
         * Remove player's data from file.
         */
        public void delete(User user) {
            if (file.delete()) {
                if (OptionsUtil.DEBUG_DELETE.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "File " + file.getName() + " deleted!");
                }
                UserVoteData.getAllUsersMap().remove(user.getUniqueID());
            }
        }

        /**
         * Check if the player exists
         *
         * @return true if player exists or false when is not
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
                        map.append(userVoteData.getUser().getUniqueID(), userVoteData.getUser());
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
