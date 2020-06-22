package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.Arrays;

public class Rewards extends BukkitCommand {


    public Rewards() {
        super("rewards");
        this.description = "rewards command";
        this.usageMessage = "/rewards";
        this.setPermission("voterewards.rewards");
        this.setPermissionMessage(MessagesUtil.NO_PERMISSION.getMessages()[0]);
        this.setAliases(Arrays.asList("vrewards", "vrew"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        MessagesUtil.REWARDS.msg(sender);
        return true;
    }
}
