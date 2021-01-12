package com.georgev22.voterewards;

import com.georgev22.org.bstats.bukkit.MetricsLite;
import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.database.DB;
import com.georgev22.voterewards.database.DatabaseType;
import com.georgev22.voterewards.database.mysql.MySQL;
import com.georgev22.voterewards.database.sqlite.SQLite;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.options.VoteOptions;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    // Start Database
    public boolean database = false;
    private Connection connection = null;
    // Stop Database

    /**
     * Return the VoteRewardPlugin instance
     */
    public static VoteRewardPlugin getInstance() {
        return instance == null ? instance = VoteRewardPlugin.getPlugin(VoteRewardPlugin.class) : instance;
    }

    @Override
    public void onEnable() {
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();

        this.registerListeners(new VotifierListener(), new PlayerListeners());

        if (VoteOptions.COMMAND_VOTEREWARDS.isEnabled())
            this.registerCommand("voterewards", new VoteRewards());
        if (VoteOptions.COMMAND_FAKEVOTE.isEnabled())
            this.registerCommand("fakevote", new FakeVote());
        if (VoteOptions.COMMAND_VOTE.isEnabled())
            this.registerCommand("vote", new Vote());
        if (VoteOptions.COMMAND_VOTES.isEnabled())
            this.registerCommand("votes", new Votes());
        if (VoteOptions.COMMAND_VOTEPARTY.isEnabled())
            this.registerCommand("voteparty", new VoteParty());
        if (VoteOptions.COMMAND_REWARDS.isEnabled())
            this.registerCommand("rewards", new Rewards());
        if (VoteOptions.COMMAND_VOTETOP.isEnabled())
            this.registerCommand("votetop", new VoteTop());
        if (VoteOptions.COMMAND_HOLOGRAM.isEnabled())
            this.registerCommand("hologram", new Holograms());

        // Start database
        setupDatabase();
        // Stop database

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

        if (VoteOptions.UPDATER.isEnabled()) {
            new Updater();
        }

        MetricsLite metrics = new MetricsLite(this);
        if (metrics.isEnabled()) {
            Bukkit.getLogger().info("[VoteRewards] Metrics is enabled!");
        }

        if (VoteOptions.REMINDER.isEnabled()) {
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
                        reminderMap.replace(player, System.currentTimeMillis() + ((int) VoteOptions.REMINDER_SEC.getValue() * 1000));
                    }
                }
            }, 20, 20);
        }

        if (VoteOptions.DAILY.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                    if (System.currentTimeMillis() >= userVoteData.getLastVote() + ((int) VoteOptions.DAILY_HOURS.getValue() * 60 * 60 * 1000)) {
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
        Bukkit.getScheduler().cancelTasks(this);
        if (VoteOptions.COMMAND_VOTEREWARDS.isEnabled())
            this.unRegisterCommand("voterewards");
        if (VoteOptions.COMMAND_FAKEVOTE.isEnabled())
            this.unRegisterCommand("fakevote");
        if (VoteOptions.COMMAND_VOTE.isEnabled())
            this.unRegisterCommand("vote");
        if (VoteOptions.COMMAND_VOTES.isEnabled())
            this.unRegisterCommand("votes");
        if (VoteOptions.COMMAND_VOTEPARTY.isEnabled())
            this.unRegisterCommand("voteparty");
        if (VoteOptions.COMMAND_REWARDS.isEnabled())
            this.unRegisterCommand("rewards");
        if (VoteOptions.COMMAND_VOTETOP.isEnabled())
            this.unRegisterCommand("votetop");
        if (VoteOptions.COMMAND_HOLOGRAM.isEnabled())
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
            Field field = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
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
    private void setupDatabase() {
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (getConfig().getString("Options.database")) {
                    case "MySQL":
                        database = true;
                        try {
                            if (connection == null || connection.isClosed()) {
                                MySQL mySQL = (MySQL) new DB(DatabaseType.MySQL).connect(getConfig().getString("MySQL.IP"),
                                        getConfig().getString("MySQL.Port"), getConfig().getString("MySQL.Database"),
                                        getConfig().getString("MySQL.User"), getConfig().getString("MySQL.Password"));
                                connection = mySQL.openConnection();
                                createTable();
                                Bukkit.getLogger().info("[VoteRewards] Database: MySQL");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "SQLite":
                        database = true;
                        try {
                            if (connection == null || connection.isClosed()) {
                                SQLite sqLite = (SQLite) new DB(DatabaseType.SQLite).connect(getDataFolder(),
                                        getConfig().getString("SQLite.fileName"));
                                connection = sqLite.openConnection();
                                createTable();
                                Bukkit.getLogger().info("[VoteRewards] Database: SQLite");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Bukkit.getLogger().info("[VoteRewards] Database: File");
                        database = false;
                        break;
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
        }.runTaskAsynchronously(this);

    }

    /**
     * Create the user table
     */
    public void createTable() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `users` (\n  `uuid` varchar(255) DEFAULT NULL,\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL,\n  `daily` int(255) DEFAULT NULL,\n `services` varchar(10000) DEFAULT NULL\n)";
        PreparedStatement stmt = connection.prepareStatement(sqlCreate);
        stmt.execute();
    }

    /**
     * Get MySQL, SQLite Connection
     *
     * @return connection
     */
    public Connection getConnection() {
        return connection;
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
