package com.georgev22.voterewards;

import com.georgev22.externals.me.lucko.helper.maven.LibraryLoader;
import com.georgev22.externals.me.lucko.helper.maven.MavenLibrary;
import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.database.Database;
import com.georgev22.voterewards.database.mongo.MongoDB;
import com.georgev22.voterewards.database.sql.mysql.MySQL;
import com.georgev22.voterewards.database.sql.postgresql.PostgreSQL;
import com.georgev22.voterewards.database.sql.sqlite.SQLite;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.georgev22.voterewards.utilities.maps.ConcurrentObjectMap;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Map;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "28.2-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    private Database database = null;
    private IDatabaseType iDatabaseType = null;
    private Connection connection = null;
    private MongoDB mongoDB = null;

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
        new LibraryLoader(this).loadAll(this);
    }

    @Override
    public void onEnable() {
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        if (Options.DEBUG_USELESS.isEnabled())
            Utils.debug(this, "onEnable Thread ID: " + Thread.currentThread().getId());
        Utils.registerListeners(new VotifierListener(), new PlayerListeners());

        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }

        if (Options.COMMAND_VOTEREWARDS.isEnabled())
            Utils.registerCommand("voterewards", new VoteRewards());
        if (Options.COMMAND_FAKEVOTE.isEnabled())
            Utils.registerCommand("fakevote", new FakeVote());
        if (Options.COMMAND_VOTE.isEnabled())
            Utils.registerCommand("vote", new Vote());
        if (Options.COMMAND_VOTES.isEnabled())
            Utils.registerCommand("votes", new Votes());
        if (Options.COMMAND_VOTEPARTY.isEnabled())
            Utils.registerCommand("voteparty", new VoteParty());
        if (Options.COMMAND_REWARDS.isEnabled())
            Utils.registerCommand("rewards", new Rewards());
        if (Options.COMMAND_VOTETOP.isEnabled())
            Utils.registerCommand("votetop", new VoteTop());
        if (Options.COMMAND_HOLOGRAM.isEnabled())
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

        if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads")) {
            new LeaderHeadsHook().register();
            Bukkit.getLogger().info("[VoteRewards] Hooked into LeaderHeads!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (data.get("Holograms") != null) {
                for (String b : data.getConfigurationSection("Holograms").getKeys(false)) {
                    HolographicDisplays.create(b, (Location) data.get("Holograms." + b + ".location"), data.getString("Holograms." + b + ".type"), false);
                }
            }
            Bukkit.getLogger().info("[VoteRewards] Hooked into HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
            Bukkit.getPluginManager().registerEvents(new AuthMe(), this);
            Bukkit.getLogger().info("[VoteRewards] Hooked into AuthMeReloaded!");
        }

        if (Options.UPDATER.isEnabled()) {
            new Updater();
        }

        Metrics metrics = new Metrics(this, 3179);
        if (metrics.isEnabled()) {
            Bukkit.getLogger().info("[VoteRewards] Metrics are enabled!");
        }

        if (Options.REMINDER.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
                for (Map.Entry<Player, Long> entry : reminderMap.entrySet()) {
                    Player player = entry.getKey();
                    Long reminderTimer = entry.getValue();
                    if (reminderTimer <= System.currentTimeMillis()) {
                        UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                        if (System.currentTimeMillis() >= userVoteData.getLastVote() + (24 * 60 * 60 * 1000)) {
                            ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
                            placeholders.append("%player%", player.getName());
                            MessagesUtil.REMINDER.msg(player, placeholders, true);
                        }
                        reminderMap.replace(player, System.currentTimeMillis() + (Options.REMINDER_SEC.getIntValue() * 1000));
                    }
                }
            }, 20, 20);
        }

        if (Options.DAILY.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                    if (System.currentTimeMillis() >= userVoteData.getLastVote() + (Options.DAILY_HOURS.getIntValue() * 60 * 60 * 1000)) {
                        if (userVoteData.getDailyVotes() != 0) {
                            userVoteData.setDailyVotes(0);
                        }
                    }
                }
            }, 20, 20);
        }

    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.save(false);
        }
        Bukkit.getScheduler().cancelTasks(this);
        HolographicDisplays.getHologramMap().forEach((name, hologram) -> HolographicDisplays.remove(name, false));
        if (Options.COMMAND_VOTEREWARDS.isEnabled())
            Utils.unRegisterCommand("voterewards");
        if (Options.COMMAND_FAKEVOTE.isEnabled())
            Utils.unRegisterCommand("fakevote");
        if (Options.COMMAND_VOTE.isEnabled())
            Utils.unRegisterCommand("vote");
        if (Options.COMMAND_VOTES.isEnabled())
            Utils.unRegisterCommand("votes");
        if (Options.COMMAND_VOTEPARTY.isEnabled())
            Utils.unRegisterCommand("voteparty");
        if (Options.COMMAND_REWARDS.isEnabled())
            Utils.unRegisterCommand("rewards");
        if (Options.COMMAND_VOTETOP.isEnabled())
            Utils.unRegisterCommand("votetop");
        if (Options.COMMAND_HOLOGRAM.isEnabled())
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
        if (Options.DEBUG_USELESS.isEnabled())
            Utils.debug(this, "Setup database Thread ID: " + Thread.currentThread().getId());
        switch (String.valueOf(Options.DATABASE_TYPE.getValue())) {
            case "MySQL": {
                if (connection == null || connection.isClosed()) {
                    database = new MySQL(
                            String.valueOf(Options.DATABASE_HOST.getValue()),
                            String.valueOf(Options.DATABASE_PORT.getValue()),
                            String.valueOf(Options.DATABASE_DATABASE.getValue()),
                            String.valueOf(Options.DATABASE_USER.getValue()),
                            String.valueOf(Options.DATABASE_PASSWORD.getValue()));
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Bukkit.getLogger().info("[VoteRewards] Database: MySQL");
                }
                break;
            }
            case "PostgreSQL": {
                if (connection == null || connection.isClosed()) {
                    database = new PostgreSQL(
                            String.valueOf(Options.DATABASE_HOST.getValue()),
                            String.valueOf(Options.DATABASE_PORT.getValue()),
                            String.valueOf(Options.DATABASE_DATABASE.getValue()),
                            String.valueOf(Options.DATABASE_USER.getValue()),
                            String.valueOf(Options.DATABASE_PASSWORD.getValue()));
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Bukkit.getLogger().info("[VoteRewards] Database: PostgreSQL");
                }
                break;
            }
            case "SQLite": {
                if (connection == null || connection.isClosed()) {
                    database = new SQLite(
                            getDataFolder(),
                            String.valueOf(Options.DATABASE_SQLITE.getValue()));
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable();
                    Bukkit.getLogger().info("[VoteRewards] Database: SQLite");
                }
                break;
            }
            case "MongoDB": {
                mongoDB = new MongoDB(
                        String.valueOf(Options.DATABASE_MONGO_HOST.getValue()),
                        Options.DATABASE_MONGO_PORT.getIntValue(),
                        String.valueOf(Options.DATABASE_MONGO_USER.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_PASSWORD.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_DATABASE.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_COLLECTION.getValue()));
                database = null;
                iDatabaseType = new UserVoteData.MongoDBUtils();
                Bukkit.getLogger().info("[VoteRewards] Database: MongoDB");
                break;
            }
            default: {
                Bukkit.getLogger().info("[VoteRewards] Database: File");
                database = null;
                iDatabaseType = new UserVoteData.FileUserUtils();
                break;
            }
        }

        UserVoteData.loadAllUsers();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.load(new Callback() {
                @Override
                public void onSuccess() {
                    if (Options.DEBUG_LOAD.isEnabled())
                        Utils.debug(VoteRewardPlugin.getInstance(), "Successfully loaded user " + userVoteData.getUser().getPlayer().getName());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }

        HolographicDisplays.updateAll();

        if (Options.PURGE_ENABLED.isEnabled())
            VoteUtils.purgeData();

        if (Options.MONTHLY_ENABLED.isEnabled())
            VoteUtils.monthlyReset();
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

    @Override
    @Nonnull
    public FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    /**
     * Creates the reminder map
     * <p>
     * creates a new, empty {@link ConcurrentObjectMap#ConcurrentObjectMap()}
     */
    public final ObjectMap<Player, Long> reminderMap = ObjectMap.newConcurrentObjectMap();


}
