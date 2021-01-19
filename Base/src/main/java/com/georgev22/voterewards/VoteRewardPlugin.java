package com.georgev22.voterewards;

import com.georgev22.me.lucko.helper.maven.LibraryLoader;
import com.georgev22.me.lucko.helper.maven.MavenLibrary;
import com.georgev22.org.bstats.bukkit.MetricsLite;
import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.database.Database;
import com.georgev22.voterewards.database.Type;
import com.georgev22.voterewards.database.mongo.MongoDB;
import com.georgev22.voterewards.database.mysql.MySQL;
import com.georgev22.voterewards.database.sqlite.SQLite;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "28.2-jre")
public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    // Start Database
    private Database database = null;
    private Type databaseType = null;
    private Connection connection = null;
    private MongoDB mongoDB = null;
    // Stop Database

    /**
     * Return the VoteRewardPlugin instance
     */
    public static VoteRewardPlugin getInstance() {
        return instance == null ? instance = VoteRewardPlugin.getPlugin(VoteRewardPlugin.class) : instance;
    }

    @Override
    public void onLoad() {
        LibraryLoader.loadAll(this);
    }

    @Override
    public void onEnable() {
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();

        this.registerListeners(new VotifierListener(), new PlayerListeners());

        if (Options.COMMAND_VOTEREWARDS.isEnabled())
            this.registerCommand("voterewards", new VoteRewards());
        if (Options.COMMAND_FAKEVOTE.isEnabled())
            this.registerCommand("fakevote", new FakeVote());
        if (Options.COMMAND_VOTE.isEnabled())
            this.registerCommand("vote", new Vote());
        if (Options.COMMAND_VOTES.isEnabled())
            this.registerCommand("votes", new Votes());
        if (Options.COMMAND_VOTEPARTY.isEnabled())
            this.registerCommand("voteparty", new VoteParty());
        if (Options.COMMAND_REWARDS.isEnabled())
            this.registerCommand("rewards", new Rewards());
        if (Options.COMMAND_VOTETOP.isEnabled())
            this.registerCommand("votetop", new VoteTop());
        if (Options.COMMAND_HOLOGRAM.isEnabled())
            this.registerCommand("hologram", new Holograms());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPI().register();
            Bukkit.getLogger().info("[VoteRewards] Hooked into PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            new MVdWPlaceholder().hook();
            Bukkit.getLogger().info("[VoteRewards] Hooked into MVdWPlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads")) {
            new LeaderHeads();
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

        MetricsLite metrics = new MetricsLite(this);
        if (metrics.isEnabled()) {
            Bukkit.getLogger().info("[VoteRewards] Metrics is enabled!");
        }

        if (Options.REMINDER.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
                for (Map.Entry<Player, Long> entry : reminderMap.entrySet()) {
                    Player player = entry.getKey();
                    Long reminderTimer = entry.getValue();
                    if (reminderTimer <= System.currentTimeMillis()) {
                        UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                        if (System.currentTimeMillis() >= userVoteData.getLastVote() + (24 * 60 * 60 * 1000)) {
                            Map<String, String> placeholders = Maps.newHashMap();
                            placeholders.put("%player%", player.getName());
                            MessagesUtil.REMINDER.msg(player, placeholders, true);
                        }
                        reminderMap.replace(player, System.currentTimeMillis() + ((int) Options.REMINDER_SEC.getValue() * 1000));
                    }
                }
            }, 20, 20);
        }

        if (Options.DAILY.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                    if (System.currentTimeMillis() >= userVoteData.getLastVote() + ((int) Options.DAILY_HOURS.getValue() * 60 * 60 * 1000)) {
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
        if (Options.COMMAND_VOTEREWARDS.isEnabled())
            this.unRegisterCommand("voterewards");
        if (Options.COMMAND_FAKEVOTE.isEnabled())
            this.unRegisterCommand("fakevote");
        if (Options.COMMAND_VOTE.isEnabled())
            this.unRegisterCommand("vote");
        if (Options.COMMAND_VOTES.isEnabled())
            this.unRegisterCommand("votes");
        if (Options.COMMAND_VOTEPARTY.isEnabled())
            this.unRegisterCommand("voteparty");
        if (Options.COMMAND_REWARDS.isEnabled())
            this.unRegisterCommand("rewards");
        if (Options.COMMAND_VOTETOP.isEnabled())
            this.unRegisterCommand("votetop");
        if (Options.COMMAND_HOLOGRAM.isEnabled())
            this.unRegisterCommand("hologram");
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register listeners
     *
     * @param listeners
     */
    private void registerListeners(Listener... listeners) {
        final PluginManager pm = Bukkit.getPluginManager();
        for (final Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    /**
     * Register a command given an executor and a name.
     *
     * @param commandName
     * @param command
     */
    private void registerCommand(final String commandName, final Command command) {
        try {
            Field field = getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            Object result = field.get(getServer().getPluginManager());
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            commandMap.register(commandName, command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * unregister a command
     *
     * @param commandName
     */
    private void unRegisterCommand(String commandName) {
        try {
            Field field1 = getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            field1.setAccessible(true);
            Object result = field1.get(getServer().getPluginManager());
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Field field = Utils.isLegacy() ? commandMap.getClass().getDeclaredField("knownCommands") : commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            Object map = field.get(commandMap);
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            Command command = commandMap.getCommand(commandName);
            knownCommands.remove(command.getName());
            for (String alias : command.getAliases()) {
                knownCommands.remove(alias);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     */
    private void setupDatabase() throws SQLException, ClassNotFoundException {
        switch (String.valueOf(Options.DATABASE_TYPE.getValue())) {
            case "MySQL": {
                if (connection == null || connection.isClosed()) {
                    database = new MySQL(
                            String.valueOf(Options.DATABASE_HOST.getValue()),
                            String.valueOf(Options.DATABASE_PORT.getValue()),
                            String.valueOf(Options.DATABASE_DATABASE.getValue()),
                            String.valueOf(Options.DATABASE_USER.getValue()),
                            String.valueOf(Options.DATABASE_PASSWORD.getValue()));
                    databaseType = Type.SQL;
                    connection = database.openConnection();
                    database.createTable();
                    Bukkit.getLogger().info("[VoteRewards] Database: MySQL");
                }
                break;
            }
            case "SQLite": {
                if (connection == null || connection.isClosed()) {
                    database = new SQLite(
                            getDataFolder(),
                            String.valueOf(Options.DATABASE_SQLITE.getValue()));
                    databaseType = Type.SQL;
                    connection = database.openConnection();
                    database.createTable();
                    Bukkit.getLogger().info("[VoteRewards] Database: SQLite");
                }
                break;
            }
            case "MongoDB": {
                mongoDB = new MongoDB(
                        String.valueOf(Options.DATABASE_MONGO_HOST.getValue()),
                        (Integer) Options.DATABASE_MONGO_PORT.getValue(),
                        String.valueOf(Options.DATABASE_MONGO_USER.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_PASSWORD.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_DATABASE.getValue()),
                        String.valueOf(Options.DATABASE_MONGO_COLLECTION.getValue()));
                databaseType = Type.MONGO;
                Bukkit.getLogger().info("[VoteRewards] Database: MongoDB");
                break;
            }
            default: {
                Bukkit.getLogger().info("[VoteRewards] Database: File");
                database = null;
                databaseType = Type.FILE;
                break;
            }
        }

        UserVoteData.loadAllUsers();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.load(new UserVoteData.Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
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

    public Database getDatabase() {
        return database;
    }

    public Type getDatabaseType() {
        return databaseType;
    }

    public MongoDB getMongoDB() {
        return mongoDB;
    }

    @Override
    public FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    /**
     * Creates the reminder map
     *
     * @return a new, empty {@code ConcurrentMap}
     */
    public final Map<Player, Long> reminderMap = Maps.newConcurrentMap();


}
