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
        this.setAliases(Arrays.asList("vrewards", "vrew"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!sender.hasPermission("voterewards.rewards")) {
            MessagesUtil.NO_PERMISSION.msg(sender);
            return true;
        }
        MessagesUtil.REWARDS.msg(sender);
        return true;
    }
}
