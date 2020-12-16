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
import org.bukkit.command.CommandMap;
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

        this.registerListeners(new VotifierListener(), new PlayerListeners(this));

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
            MVdWPlaceholder MVdWPlaceholderAPI = new MVdWPlaceholder(this);
            MVdWPlaceholderAPI.hook();
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
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
                    if (System.currentTimeMillis() >= userVoteData.getLastVote() + 24 * 60 * 60 * 1000) {
                        Map<String, String> placeholders = Maps.newHashMap();
                        placeholders.put("%player%", player.getName());
                        MessagesUtil.REMINDER.msg(player, placeholders, true);
                    }
                }
            }, 20, (int) VoteOptions.REMINDER_SEC.getValue() * 20);
        }
    }

    @Override
    public void onDisable() {
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
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(commandName, command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     */
    private void setupDatabase() {
        new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                String b = getConfig().getString("Options.database");
                switch (b) {
                    case "MySQL":
                        try {
                            if (connection == null || connection.isClosed()) {
                                MySQL mySQL = (MySQL) new DB(DatabaseType.MySQL).connect(getConfig().getString("MySQL.IP"),
                                        getConfig().getString("MySQL.Port"), getConfig().getString("MySQL.Database"),
                                        getConfig().getString("MySQL.User"), getConfig().getString("MySQL.Password"));
                                connection = mySQL.openConnection();
                                if (!connection.isClosed()) {
                                    database = true;
                                }
                                createTable();
                            }
                        } catch (Exception e) {
                            database = false;
                            e.printStackTrace();
                        }
                        break;
                    case "SQLite":
                        try {
                            if (connection == null || connection.isClosed()) {
                                SQLite sqLite = (SQLite) new DB(DatabaseType.SQLite).connect(getDataFolder(),
                                        getConfig().getString("SQLite.fileName"));
                                connection = sqLite.openConnection();
                                if (!connection.isClosed()) {
                                    database = true;
                                }
                                createTable();
                            }
                        } catch (Exception e) {
                            database = false;
                            e.printStackTrace();
                        }
                        break;
                    default:
                        database = false;
                        break;
                }
            }
        }.runTaskAsynchronously(this);

    }

    /**
     * Create the user table
     */
    public void createTable() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `users` (\n  `uuid` varchar(255) DEFAULT NULL,\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL\n)";
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

}
