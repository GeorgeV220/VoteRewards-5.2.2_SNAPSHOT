package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Rewards extends BukkitCommand {


    public Rewards() {
        super("rewards");
        this.description = "rewards command";
        this.usageMessage = "/rewards";
        this.setPermission("voterewards.rewards");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrewards", "vrew"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        MessagesUtil.REWARDS.msg(sender);
        return true;
    }
}
