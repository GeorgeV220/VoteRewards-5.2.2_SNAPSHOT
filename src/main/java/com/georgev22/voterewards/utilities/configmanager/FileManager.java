package com.georgev22.voterewards.utilities.configmanager;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FileManager {

    private static FileManager instance;

    public static FileManager getInstance() {
        return instance == null ? instance = new FileManager() : instance;
    }

    private CFG config;
    private CFG data;
    private CFG messages;
    private CFG voteInventory;
    private CFG voteTopInventory;

    private FileManager() {
    }

    public void loadFiles(final JavaPlugin plugin) {
        this.messages = new CFG(plugin, "messages", false);
        this.config = new CFG(plugin, "config", true);
        this.data = new CFG(plugin, "data", true);
        this.voteInventory = new CFG(plugin, "inventories" + File.separator + "vote", true);
        this.voteTopInventory = new CFG(plugin, "inventories" + File.separator + "votetop", true);
    }

    public CFG getMessages() {
        return messages;
    }

    public CFG getConfig() {
        return config;
    }

    public CFG getData() {
        return data;
    }

    public CFG getVoteInventory() {
        return voteInventory;
    }

    public CFG getVoteTopInventory() {
        return voteTopInventory;
    }
}
