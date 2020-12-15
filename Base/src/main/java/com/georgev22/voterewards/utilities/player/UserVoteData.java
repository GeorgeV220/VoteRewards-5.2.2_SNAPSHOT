package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.xseries.XSound;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Used to handle a user's votes and anything related to them. Files, votes and
 * any other data related to them.
 */
public class UserVoteData {

    /* Returns a copy of this uservotedata class for a specific user. */

    public static UserVoteData getUser(final UUID uuid) {
        return new UserVoteData(uuid);
    }

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private UserVoteData(final UUID uuid) {
        this.uuid = uuid;

        this.file = new File(VoteRewardPlugin.getInstance().getDataFolder(),
                "userdata" + File.separator + uuid.toString() + ".yml");

        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.reloadConfiguration();

    }

    private final File file;
    private final UUID uuid;
    private YamlConfiguration configuration = null;
    private OfflinePlayer voter;

    private OfflinePlayer getVoter() {
        return voter == null ? voter = Bukkit.getOfflinePlayer(uuid) : voter;
    }

    public void processVote(final String serviceName) {

        // ADD STATS
        if (this.voteRewardPlugin.database) {
            try {
                final String name = this.getVoter().getUniqueId().toString();
                final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
                        String.format("UPDATE `users` SET `votes` = '%d', `time` = '%d' WHERE `uuid` = '%s'",
                                this.getVotes() + 1, System.currentTimeMillis(), name));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.configuration.set("total-votes", this.getVotes() + 1);
            this.configuration.set("last-vote", System.currentTimeMillis());
            this.saveConfiguration();
        }
        //

        // TITLE
        if (VoteOptions.VOTE_TITLE.isEnabled()) {
            new TitleActionbarUtil(getVoter().getPlayer()).sendTitle(
                    Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", getVoter().getName()),
                    Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", getVoter().getName()));
        }
        //

        // WORLD REWARDS (WITH SERVICES)
        if (VoteOptions.WORLD.isEnabled()) {
            //Bukkit.broadcastMessage("Player: " + voter.getName() + " world: " + getWorld());
            if (this.voteRewardPlugin.getConfig().getString("Rewards.Worlds." + getWorld() + "." + serviceName) != null) {
                this.runCommands(this.voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + getWorld() + "." + serviceName));
            } else {
                this.runCommands(this.voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + getWorld() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (!VoteOptions.DISABLE_SERVICES.isEnabled()) {
            if (this.voteRewardPlugin.getConfig().getString("Rewards.Services." + serviceName) != null) {
                this.runCommands(this.voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                this.runCommands(this.voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (VoteOptions.LUCKY.isEnabled()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt(this.voteRewardPlugin.getConfig().getInt("Options.votes.lucky numbers") + 1);
            for (String s2 : this.voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Lucky")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(i)) {
                    this.runCommands(this.voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Lucky." + s2 + ".commands"));
                }
            }
        }

        // PERMISSIONS REWARDS
        if (VoteOptions.PERMISSIONS.isEnabled()) {
            final ConfigurationSection section = this.voteRewardPlugin.getConfig()
                    .getConfigurationSection("Rewards.Permission");
            for (String s2 : section.getKeys(false)) {
                if (this.getVoter().getPlayer().hasPermission("voterewards.permission" + s2)) {
                    this.runCommands(this.voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Permission." + s2 + ".commands"));
                }
            }
        }
        //

        // CUMULATIVE REWARDS
        if (VoteOptions.CUMULATIVE.isEnabled()) {
            int votes = this.getVotes();
            for (String s2 : this.voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(votes)) {
                    this.runCommands(this.voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                }
            }
        }
        //

        // PLAY SOUND
        if (VoteOptions.SOUND.isEnabled()) {
            if (getVoter().isOnline())
                getVoter().getPlayer().playSound(getVoter().getPlayer().getLocation(),
                        XSound.matchXSound(voteRewardPlugin.getConfig().getString("Sounds.Vote")).get().parseSound(), 1000, 1);
        }

        // VOTE PARTY START
        VotePartyUtils.getInstance().run(getVoter());

        //HOLOGRAM UPDATE
        HolographicDisplays.updateAll();
    }

    public void processOfflineVote(final String serviceName) {
        List<String> services = this.configuration.getStringList("offline vote.services");
        services.add(serviceName);
        this.configuration.set("offline vote.services", services);
        this.saveConfiguration();
    }

    /**
     * Run the commands from config
     *
     * @param s
     */
    private void runCommands(List<String> s) {
        if (s == null) {
            //Bukkit.broadcastMessage("NULL CONFIGURATION");
            return;
        }
        for (String b : s) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(b.replace("%player%", getVoter().getName())));
        }
    }

    /**
     * Return player votes
     *
     * @return integer
     */
    public int getVotes() {
        if (voteRewardPlugin.database) {
            try {

                PreparedStatement ps = voteRewardPlugin.getConnection()
                        .prepareStatement("SELECT votes FROM users WHERE UUID = ?");
                ps.setString(1, uuid.toString());

                // PreparedStatement ps = voteRewardPlugin.getConnection()
                // .prepareStatement("SELECT * FROM " + voter.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt("votes");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return this.configuration.getInt("total-votes", 0);
    }

    public void setVotes(final int votes) {
        if (voteRewardPlugin.database) {
            try {
                final String name = this.getVoter().getUniqueId().toString();
                final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
                        String.format("UPDATE `users` SET `votes` = '%d' WHERE `uuid` = '%s'", votes, name));

                // final PreparedStatement statement = this.voteRewardPlugin.getConnection()
                // .prepareStatement(String.format("UPDATE `" + name + "` SET `votes` = '%d'",
                // votes));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.configuration.set("total-votes", votes);
            this.saveConfiguration();
        }
    }

    private void reloadConfiguration() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfiguration() {
        try {
            this.configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set X' player Daily votes
     *
     * @param i int
     */
    private void setDailyVotes(final int i) {
        if (this.voteRewardPlugin.database) {
            try {
                final String name = this.getVoter().getUniqueId().toString();
                final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
                        String.format("UPDATE `users` SET `daily` = '%d' WHERE `uuid` = '%s'", i, name));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.configuration.set("daily-votes", i);
            this.saveConfiguration();
        }
    }

    /**
     * Set X' player VoteParty votes
     *
     * @param i int
     */
    public void setVoteParty(final int i) {
        if (this.voteRewardPlugin.database) {
            try {
                final String name = this.getVoter().getUniqueId().toString();
                final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
                        String.format("UPDATE `users` SET `voteparty` = '%d' WHERE `uuid` = '%s'", i, name));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.configuration.set("voteparty-votes", i);
            this.saveConfiguration();
        }
    }

    /**
     * Return X's player VoteParty votes
     *
     * @return int
     */
    public int getVoteParty() {
        if (this.voteRewardPlugin.database) {
            try {
                final PreparedStatement ps = this.voteRewardPlugin.getConnection()
                        .prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
                ps.setString(1, this.uuid.toString());
                final ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt("voteparty");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return this.configuration.getInt("voteparty-votes", 0);
    }

    /**
     * Get last vote
     *
     * @return int
     */
    public long getLastVote() {
        if (this.voteRewardPlugin.database) {
            try {
                final PreparedStatement ps = this.voteRewardPlugin.getConnection()
                        .prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
                ps.setString(1, this.uuid.toString());
                final ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getLong("time");
            } catch (Exception e) {
                return 0L;
            }
        }
        return this.configuration.getLong("last-vote");
    }

    /**
     * Set last vote
     *
     * @param i
     */
    public void setLastVote(long i) {
        if (this.voteRewardPlugin.database) {
            try {
                final String name = this.getVoter().getUniqueId().toString();
                final PreparedStatement statement = this.voteRewardPlugin.getConnection().prepareStatement(
                        String.format("UPDATE `users` SET `time` = '%d' WHERE `uuid` = '%s'", i, name));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.configuration.set("last-vote", i);
            this.saveConfiguration();
        }
    }

    /**
     * Get Daily votes
     *
     * @return int
     */
    private int getDailyVotes() {
        if (this.voteRewardPlugin.database) {
            try {
                final PreparedStatement ps = this.voteRewardPlugin.getConnection()
                        .prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
                ps.setString(1, this.uuid.toString());
                final ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt("daily");
            } catch (Exception e) {
                return 0;
            }
        }
        return this.configuration.getInt("daily-votes");
    }

    /**
     * Setup the user
     */
    public void setupUser() {
        if (voteRewardPlugin.database) {
            new BukkitRunnable() {
                @Override
                public void run() {

                    try {
                        final PreparedStatement ps = voteRewardPlugin.getConnection()
                                .prepareStatement(String.format("UPDATE `users` SET `name` = '%s' WHERE `uuid` = '%s'",
                                        getVoter().getName(), getVoter().getUniqueId()));
                        ps.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(voteRewardPlugin);
        }
        this.configuration.set("last-name", getVoter().getName());
        try {
            if (!playerExist()) {
                if (voteRewardPlugin.database) {
                    setupMySQLUser();
                } else {
                    this.configuration.set("total-votes", 0);
                    this.configuration.set("daily-votes", 0);
                    // this.configuration.set("voteparty-votes", 0);
                    this.configuration.set("voteparty", 0);
                    this.configuration.set("offline votes.service", Lists.newArrayList());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.saveConfiguration();
    }

    /**
     * Setup MySQL user
     */
    private void setupMySQLUser() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement1 = voteRewardPlugin.getConnection().prepareStatement(String.format(
                            "INSERT INTO users (`uuid`, `votes`, `time`, `voteparty, `offlinevote`) VALUES ('%s', '0', '0', '0', '" + Lists.newArrayList() + "');",
                            uuid.toString()));
                    statement1.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(voteRewardPlugin);
    }

    /**
     * Check if the player exist
     *
     * @return boolean
     * @throws Exception
     */
    public boolean playerExist() throws Exception {
        if (voteRewardPlugin.database) {
            PreparedStatement ps = voteRewardPlugin.getConnection()
                    .prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } else {
            return configuration.get("total-votes") != null;
        }
    }

    /**
     * Reset player
     *
     * @throws Exception
     */
    public void reset() throws Exception {
        if (voteRewardPlugin.database) {
            PreparedStatement ps = voteRewardPlugin.getConnection()
                    .prepareStatement("DELETE from users WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeQuery();
            this.setupMySQLUser();
        } else {
            this.configuration.set("total-votes", 0);
            this.configuration.set("daily-votes", 0);
            // this.configuration.set("voteparty-votes", 0);
            this.configuration.set("voteparty", 0);
            this.configuration.set("offline votes.service", Lists.newArrayList());
            this.saveConfiguration();
        }
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

    /**
     * Set offline services
     *
     * @param services
     */
    public void setOfflineServices(List<String> services) {
        this.configuration.set("offline vote.services", services);
        this.saveConfiguration();
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
                || (object instanceof UserVoteData && Objects.equals(this.uuid, ((UserVoteData) object).uuid));
    }
}