package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Used to handle a user's votes and anything related to them. Files, votes and
 * any other data related to them.
 * <p>
 * This class will not get combined with UserUtils class
 */
public class UserVoteData {

    /**
     * Returns a copy of this uservotedata class for a specific user.
     *
     * @param uuid
     * @return a copy of this UserVoteData class for a specific user.
     */
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

    /**
     * Return player votes
     *
     * @return player total votes
     */
    public int getVotes() {
        return this.configuration.getInt("total-votes", 0);
    }

    public void setVotes(final int votes) {
        this.configuration.set("total-votes", votes);
    }

    private void reloadConfiguration() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfiguration() {
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
        this.configuration.set("daily-votes", i);
    }

    /**
     * Set X' player VoteParty votes
     *
     * @param i int
     */
    public void setVoteParty(final int i) {
        this.configuration.set("voteparty-votes", i);
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
        return this.configuration.getLong("last-vote");
    }

    /**
     * Set last vote
     *
     * @param i
     */
    public void setLastVote(long i) {
        this.configuration.set("last-vote", i);
    }

    /**
     * Get Daily votes
     *
     * @return int
     */
    private int getDailyVotes() {
        return this.configuration.getInt("daily-votes");
    }

    /**
     * Setup the user
     */
    public void setupUser() {
        this.configuration.set("last-name", getVoter().getName());
        if (!playerExist()) {
            this.configuration.set("total-votes", 0);
            this.configuration.set("daily-votes", 0);
            this.configuration.set("voteparty", 0);
            this.configuration.set("offline votes.service", Lists.newArrayList());
        }
        saveConfiguration();
    }

    /**
     * Check if the player exist
     *
     * @return boolean
     */
    public boolean playerExist() {
        return configuration.get("total-votes") != null;
    }

    /**
     * Reset player
     */
    public void reset() {
        this.configuration.set("total-votes", 0);
        this.configuration.set("daily-votes", 0);
        this.configuration.set("voteparty", 0);
        this.configuration.set("offline votes.service", Lists.newArrayList());
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

    /**
     * Set offline services
     *
     * @param services
     */
    public void setOfflineServices(List<String> services) {
        this.configuration.set("offline vote.services", services);
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