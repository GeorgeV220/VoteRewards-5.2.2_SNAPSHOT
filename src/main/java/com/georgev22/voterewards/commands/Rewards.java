package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class Rewards implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("voterewards.rewards")) {
            MessagesUtil.NO_PERMISSION.msg(sender);
            return true;
        }
        MessagesUtil.REWARDS.msg(sender);
        return true;
    }
}
