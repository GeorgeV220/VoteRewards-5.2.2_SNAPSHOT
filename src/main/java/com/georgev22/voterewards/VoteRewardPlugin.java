package com.georgev22.voterewards;

import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.*;
import com.georgev22.voterewards.utilities.configmanager.CFG;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.database.Database;
import com.georgev22.voterewards.utilities.database.mongo.MongoDB;
import com.georgev22.voterewards.utilities.database.sql.mysql.MySQL;
import com.georgev22.voterewards.utilities.database.sql.postgresql.PostgreSQL;
import com.georgev22.voterewards.utilities.database.sql.sqlite.SQLite;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.georgev22.voterewards.utilities.inventory.PagedInventoryAPI;
import com.georgev22.voterewards.utilities.maven.LibraryLoader;
import com.georgev22.voterewards.utilities.maven.MavenLibrary;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.11")
public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    private Database database = null;
    private IDatabaseType iDatabaseType = null;
    private Connection connection = null;
    private MongoDB mongoDB = null;
    private PagedInventoryAPI api = null;

    /**
     * Return the VoteRewardPlugin instance
     *
     * @return VoteRewardPlugin instance
     */
    public static VoteRewardPlugin getInstance() {
        return instance == null ? instance = VoteRewardPlugin.getPlugin(VoteRewardPlugin.class) : instance;
    }

    @Override
    public void onLoad() {
        if (MinecraftVersion.getCurrentVersion().isBelow(MinecraftVersion.V1_16_R1))
            new LibraryLoader(this).loadAll();
        else
            Utils.debug(this, "Minecraft Version: " + MinecraftVersion.getCurrentVersion().name());
    }

    @Override
    public void onEnable() {
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        api = new PagedInventoryAPI(this);
        if (OptionsUtil.DEBUG_USELESS.isEnabled())
            Utils.debug(this, "onEnable Thread ID: " + Thread.currentThread().getId());
        Utils.registerListeners(new VotifierListener(), new PlayerListeners());

        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }

        if (OptionsUtil.COMMAND_VOTEREWARDS.isEnabled())
            Utils.registerCommand("voterewards", new VoteRewards());
        if (OptionsUtil.COMMAND_FAKEVOTE.isEnabled())
            Utils.registerCommand("fakevote", new FakeVote());
        if (OptionsUtil.COMMAND_VOTE.isEnabled())
            Utils.registerCommand("vote", new Vote());
        if (OptionsUtil.COMMAND_VOTES.isEnabled())
            Utils.registerCommand("votes", new Votes());
        if (OptionsUtil.COMMAND_VOTEPARTY.isEnabled())
            Utils.registerCommand("voteparty", new VoteParty());
        if (OptionsUtil.COMMAND_REWARDS.isEnabled())
            Utils.registerCommand("rewards", new Rewards());
        if (OptionsUtil.COMMAND_VOTETOP.isEnabled())
            Utils.registerCommand("votetop", new VoteTop());
        if (OptionsUtil.COMMAND_HOLOGRAM.isEnabled())
            Utils.registerCommand("hologram", new Holograms());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
            } catch (Exception throwable) {
                throwable.printStackTrace();
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPI().register();
            Bukkit.getLogger().info("[VoteRewards] Hooked into PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            new MVdWPlaceholder().register();
            Bukkit.getLogger().info("[VoteRewards] Hooked into MVdWPlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (data.get("Holograms") != null) {
                data.getConfigurationSection("Holograms").getKeys(false).forEach(s -> HolographicDisplays.create(s, (Location) data.get("Holograms." + s + ".location"), data.getString("Holograms." + s + ".type"), false));
            }
            HolographicDisplays.setHook(true);
            Bukkit.getLogger().info("[VoteRewards] Hooked into HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
            Bukkit.getPluginManager().registerEvents(new AuthMe(), this);
            Bukkit.getLogger().info("[VoteRewards] Hooked into AuthMeReloaded!");
        }

        if (OptionsUtil.UPDATER.isEnabled()) {
            new Updater();
        }

        Metrics metrics = new Metrics(this, 3179);
        if (metrics.isEnabled()) {
            Bukkit.getLogger().info("[VoteRewards] Metrics are enabled!");
        }

        if (OptionsUtil.DAILY.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> Bukkit.getOnlinePlayers().forEach(player -> {
                UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                if (System.currentTimeMillis() >= userVoteData.getLastVote() + (OptionsUtil.DAILY_HOURS.getIntValue() * 60 * 60 * 1000)) {
                    if (userVoteData.getDailyVotes() < 0) {
                        userVoteData.setDailyVotes(0);
                    }
                }
            }), 20, 20);
        }

    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.save(false, new Callback() {
                @Override
                public void onSuccess() {
                    if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                        Utils.debug(VoteRewardPlugin.getInstance(),
                                "User " + userVoteData.user().getName() + " successfully saved!",
                                "Votes: " + userVoteData.user().getVotes(),
                                "Daily Votes: " + userVoteData.user().getDailyVotes(),
                                "Last Voted: " + Instant.ofEpochMilli(userVoteData.user().getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + userVoteData.user().getLastVoted(),
                                "Vote Parties: " + userVoteData.user().getVoteParties(),
                                "All time votes: " + userVoteData.user().getAllTimeVotes());
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        });
        Bukkit.getScheduler().cancelTasks(this);
        if (HolographicDisplays.isHooked())
            HolographicDisplays.getHologramMap().forEach((name, hologram) -> HolographicDisplays.remove(name, false));
        if (OptionsUtil.COMMAND_VOTEREWARDS.isEnabled())
            Utils.unRegisterCommand("voterewards");
        if (OptionsUtil.COMMAND_FAKEVOTE.isEnabled())
            Utils.unRegisterCommand("fakevote");
        if (OptionsUtil.COMMAND_VOTE.isEnabled())
            Utils.unRegisterCommand("vote");
        if (OptionsUtil.COMMAND_VOTES.isEnabled())
            Utils.unRegisterCommand("votes");
        if (OptionsUtil.COMMAND_VOTEPARTY.isEnabled())
            Utils.unRegisterCommand("voteparty");
        if (OptionsUtil.COMMAND_REWARDS.isEnabled())
            Utils.unRegisterCommand("rewards");
        if (OptionsUtil.COMMAND_VOTETOP.isEnabled())
            Utils.unRegisterCommand("votetop");
        if (OptionsUtil.COMMAND_HOLOGRAM.isEnabled())
            Utils.unRegisterCommand("hologram");
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (mongoDB != null) {
            mongoDB.getMongoClient().close();
        }
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        if (OptionsUtil.DEBUG_USELESS.isEnabled())
            Utils.debug(this, "Setup database Thread ID: " + Thread.currentThread().getId());
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL" -> {
                if (connection == null || connection.isClosed()) {
                    database = new MySQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Utils.debug(this, "Database: MySQL");
                }
            }
            case "PostgreSQL" -> {
                if (connection == null || connection.isClosed()) {
                    database = new PostgreSQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Utils.debug(this, "Database: PostgreSQL");
                }
            }
            case "SQLite" -> {
                if (connection == null || connection.isClosed()) {
                    database = new SQLite(
                            getDataFolder(),
                            OptionsUtil.DATABASE_SQLITE.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Utils.debug(this, "Database: SQLite");
                }
            }
            case "MongoDB" -> {
                mongoDB = new MongoDB(
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getIntValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue());
                database = null;
                iDatabaseType = new UserVoteData.MongoDBUtils();
                Utils.debug(this, "Database: MongoDB");
            }
            case "File" -> {
                database = null;
                iDatabaseType = new UserVoteData.FileUserUtils();
                Utils.debug(this, "Database: File");
            }
            default -> {
                Utils.debug(this, "Please use one of the available databases", "Available databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
                database = null;
                iDatabaseType = null;
            }
        }

        UserVoteData.loadAllUsers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            try {
                userVoteData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        if (OptionsUtil.DEBUG_LOAD.isEnabled())
                            Utils.debug(VoteRewardPlugin.getInstance(), "Successfully loaded user " + userVoteData.user().getOfflinePlayer().getName());
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

        HolographicDisplays.updateAll();

        if (OptionsUtil.PURGE_ENABLED.isEnabled())
            VoteUtils.purgeData();

        if (OptionsUtil.MONTHLY_ENABLED.isEnabled())
            VoteUtils.monthlyReset();

        if (OptionsUtil.REMINDER.isEnabled()) {
            VoteUtils.reminder();
        }
    }

    /**
     * Get Database open connection
     *
     * @return connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get Database class instance
     *
     * @return {@link Database} class instance
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Return Database Type
     *
     * @return Database Type
     */
    public IDatabaseType getIDatabaseType() {
        return iDatabaseType;
    }

    /**
     * Return MongoDB instance when
     * MongoDB is in use.
     * <p>
     * Returns null if MongoDB is not in use
     *
     * @return {@link MongoDB} instance
     */
    public MongoDB getMongoDB() {
        return mongoDB;
    }

    public PagedInventoryAPI getInventoryAPI() {
        return api;
    }

    @Override
    @NotNull
    public FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    @Override
    public void saveConfig() {
        FileManager.getInstance().getConfig().saveFile();
    }


}
