package com.georgev22.voterewards;

import com.georgev22.api.configmanager.CFG;
import com.georgev22.api.database.Database;
import com.georgev22.api.database.mongo.MongoDB;
import com.georgev22.api.database.sql.mysql.MySQL;
import com.georgev22.api.database.sql.postgresql.PostgreSQL;
import com.georgev22.api.database.sql.sqlite.SQLite;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.maven.LibraryLoader;
import com.georgev22.api.maven.MavenLibrary;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.api.utilities.MinecraftUtils.MinecraftVersion;
import com.georgev22.api.inventory.PagedInventoryAPI;
import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.DeveloperInformListener;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.*;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;

import static com.georgev22.api.utilities.Utils.*;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-io", artifactId = "commons-io", version = "2.11.0")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.15")
public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    private Database database = null;
    private IDatabaseType iDatabaseType = null;
    private Connection connection = null;
    private MongoDB mongoDB = null;
    private PagedInventoryAPI api = null;
    private PAPI placeholdersAPI = null;

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
            new LibraryLoader(this.getClass(), this.getDataFolder()).loadAll();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("\n" +
                " __     __             __                _______                                                     __           \n" +
                "|  \\   |  \\           |  \\              |       \\                                                   |  \\          \n" +
                "| $$   | $$  ______  _| $$_     ______  | $$$$$$$\\  ______   __   __   __   ______    ______    ____| $$  _______ \n" +
                "| $$   | $$ /      \\|   $$ \\   /      \\ | $$__| $$ /      \\ |  \\ |  \\ |  \\ |      \\  /      \\  /      $$ /       \\\n" +
                " \\$$\\ /  $$|  $$$$$$\\\\$$$$$$  |  $$$$$$\\| $$    $$|  $$$$$$\\| $$ | $$ | $$  \\$$$$$$\\|  $$$$$$\\|  $$$$$$$|  $$$$$$$\n" +
                "  \\$$\\  $$ | $$  | $$ | $$ __ | $$    $$| $$$$$$$\\| $$    $$| $$ | $$ | $$ /      $$| $$   \\$$| $$  | $$ \\$$    \\ \n" +
                "   \\$$ $$  | $$__/ $$ | $$|  \\| $$$$$$$$| $$  | $$| $$$$$$$$| $$_/ $$_/ $$|  $$$$$$$| $$      | $$__| $$ _\\$$$$$$\\\n" +
                "    \\$$$    \\$$    $$  \\$$  $$ \\$$     \\| $$  | $$ \\$$     \\ \\$$   $$   $$ \\$$    $$| $$       \\$$    $$|       $$\n" +
                "     \\$      \\$$$$$$    \\$$$$   \\$$$$$$$ \\$$   \\$$  \\$$$$$$$  \\$$$$$\\$$$$   \\$$$$$$$ \\$$        \\$$$$$$$ \\$$$$$$$ \n" +
                "                                                                                                                  \n");
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        api = new PagedInventoryAPI(this);
        if (OptionsUtil.DEBUG_USELESS.getBooleanValue())
            MinecraftUtils.debug(this, "onEnable Thread ID: " + Thread.currentThread().getId());
        MinecraftUtils.registerListeners(this, new VotifierListener(), new PlayerListeners(), new DeveloperInformListener());

        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }

        if (OptionsUtil.COMMAND_VOTEREWARDS.getBooleanValue())
            MinecraftUtils.registerCommand("voterewards", new VoteRewards());
        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            MinecraftUtils.registerCommand("fakevote", new FakeVote());
        if (OptionsUtil.COMMAND_VOTE.getBooleanValue())
            MinecraftUtils.registerCommand("vote", new Vote());
        if (OptionsUtil.COMMAND_VOTES.getBooleanValue())
            MinecraftUtils.registerCommand("votes", new Votes());
        if (OptionsUtil.COMMAND_VOTEPARTY.getBooleanValue())
            MinecraftUtils.registerCommand("voteparty", new VoteParty());
        if (OptionsUtil.COMMAND_REWARDS.getBooleanValue())
            MinecraftUtils.registerCommand("rewards", new Rewards());
        if (OptionsUtil.COMMAND_VOTETOP.getBooleanValue())
            MinecraftUtils.registerCommand("votetop", new VoteTop());
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue())
            MinecraftUtils.registerCommand("hologram", new Holograms());
        if (OptionsUtil.COMMAND_NPC.getBooleanValue())
            MinecraftUtils.registerCommand("vnpc", new NPCCommand());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
            } catch (Exception throwable) {
                throwable.printStackTrace();
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholdersAPI = new PAPI();
            if (placeholdersAPI.register())
                Bukkit.getLogger().info("[VoteRewards] Hooked into PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (data.get("Holograms") != null) {
                data.getConfigurationSection("Holograms").getKeys(false)
                        .forEach(s -> HolographicDisplays.create(s, (Location) data.get("Holograms." + s + ".location"),
                                data.getString("Holograms." + s + ".type"), false));
            }
            HolographicDisplays.setHook(true);
            Bukkit.getLogger().info("[VoteRewards] Hooked into HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
            Bukkit.getPluginManager().registerEvents(new AuthMe(), this);
            Bukkit.getLogger().info("[VoteRewards] Hooked into AuthMeReloaded!");
        }

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            new Updater();
        }

        new Metrics(this, 3179);
        if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
            Bukkit.getLogger().info("[VoteRewards] Metrics are enabled!");
        }

        if (MinecraftVersion.getCurrentVersion().isBelow(MinecraftVersion.V1_12_R1)) {
            MinecraftUtils.debug(this, "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run VoteRewards on this Minecraft version(" + MinecraftVersion.getCurrentVersion().name().toLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }
    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    Bukkit.getLogger().info("[VoteRewards] Unhooked from PlaceholderAPI!");
                }
            }
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.save(false, new Callback() {
                @Override
                public void onSuccess() {
                    if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                        MinecraftUtils.debug(VoteRewardPlugin.getInstance(),
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
        if (OptionsUtil.COMMAND_VOTEREWARDS.getBooleanValue())
            MinecraftUtils.unRegisterCommand("voterewards");
        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            MinecraftUtils.unRegisterCommand("fakevote");
        if (OptionsUtil.COMMAND_VOTE.getBooleanValue())
            MinecraftUtils.unRegisterCommand("vote");
        if (OptionsUtil.COMMAND_VOTES.getBooleanValue())
            MinecraftUtils.unRegisterCommand("votes");
        if (OptionsUtil.COMMAND_VOTEPARTY.getBooleanValue())
            MinecraftUtils.unRegisterCommand("voteparty");
        if (OptionsUtil.COMMAND_REWARDS.getBooleanValue())
            MinecraftUtils.unRegisterCommand("rewards");
        if (OptionsUtil.COMMAND_VOTETOP.getBooleanValue())
            MinecraftUtils.unRegisterCommand("votetop");
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue())
            MinecraftUtils.unRegisterCommand("hologram");
        if (OptionsUtil.COMMAND_NPC.getBooleanValue())
            MinecraftUtils.unRegisterCommand("vnpc");
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
        if (OptionsUtil.DEBUG_USELESS.getBooleanValue())
            MinecraftUtils.debug(this, "Setup database Thread ID: " + Thread.currentThread().getId());
        ObjectMap<String, ObjectMap.Pair<String, String>> map = ObjectMap.newHashObjectMap()
                .append("uuid", ObjectMap.Pair.create("VARCHAR(38)", "NULL"))
                .append("name", ObjectMap.Pair.create("VARCHAR(18)", "NULL"))
                .append("votes", ObjectMap.Pair.create("INT(10)", "0"))
                .append("time", ObjectMap.Pair.create("BIGINT(30)", "0"))
                .append("voteparty", ObjectMap.Pair.create("INT(10)", "0"))
                .append("daily", ObjectMap.Pair.create("INT(10)", "0"))
                .append("services", ObjectMap.Pair.create("TEXT", "NULL"))
                .append("servicesLastVote", ObjectMap.Pair.create("TEXT", "NULL"))
                .append("totalvotes", ObjectMap.Pair.create("INT(10)", "0"));
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL": {
                if (connection == null || connection.isClosed()) {
                    database = new MySQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: MySQL");
                }
                break;
            }
            case "PostgreSQL": {
                if (connection == null || connection.isClosed()) {
                    database = new PostgreSQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: PostgreSQL");
                }
                break;
            }
            case "SQLite": {
                if (connection == null || connection.isClosed()) {
                    database = new SQLite(
                            getDataFolder(),
                            OptionsUtil.DATABASE_SQLITE.getStringValue());
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: SQLite");
                }
                break;
            }
            case "MongoDB": {
                mongoDB = new MongoDB(
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getIntValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue());
                database = null;
                iDatabaseType = new UserVoteData.MongoDBUtils();
                MinecraftUtils.debug(this, "Database: MongoDB");
                break;
            }
            case "File": {
                database = null;
                iDatabaseType = new UserVoteData.FileUserUtils();
                MinecraftUtils.debug(this, "Database: File");
                break;
            }
            default: {
                database = null;
                iDatabaseType = null;
                setEnabled(false);
                throw new RuntimeException("Please use one of the available databases\nAvailable databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
            }
        }

        UserVoteData.loadAllUsers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            try {
                userVoteData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        UserVoteData.getAllUsersMap().append(userVoteData.user().getUniqueId(), userVoteData.user());
                        if (OptionsUtil.DEBUG_LOAD.getBooleanValue())
                            MinecraftUtils.debug(VoteRewardPlugin.getInstance(), "Successfully loaded user " + userVoteData.user().getOfflinePlayer().getName());
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

        if (OptionsUtil.PURGE_ENABLED.getBooleanValue())
            VoteUtils.purgeData();

        if (OptionsUtil.MONTHLY_ENABLED.getBooleanValue())
            VoteUtils.monthlyReset();

        if (OptionsUtil.REMINDER.getBooleanValue()) {
            VoteUtils.reminder();
        }

        if (OptionsUtil.DAILY.getBooleanValue()) {
            VoteUtils.dailyReset();
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
     * Return MongoDB instance when MongoDB is in use.
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
