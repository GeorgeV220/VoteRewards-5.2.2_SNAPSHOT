package com.georgev22.voterewards.utilities.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private int votes = 0;
    private long lastVoted = 0;
    private int voteParties = 0;
    private List<String> services = new ArrayList<>();


    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public UUID getUniqueID() {
        return uuid;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public void setVoteParties(int voteParties) {
        this.voteParties = voteParties;
    }

    public void setLastVoted(long lastVoted) {
        this.lastVoted = lastVoted;
    }

    public int getVotes() {
        return votes;
    }

    public long getLastVoted() {
        return lastVoted;
    }

    public int getVoteParties() {
        return voteParties;
    }

    public List<String> getServices() {
        return services;
    }
}
