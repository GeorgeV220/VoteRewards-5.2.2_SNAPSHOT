package com.georgev22.voterewards.commands;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Votes extends BukkitCommand {


    public Votes() {
        super("votes");
        this.description = "Votes command";
        this.usageMessage = "/votes";
        this.setPermission("voterewards.votes");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrvs", "vrvotes", "vvotes"));
    }

    public boolean execute(@NotNull final CommandSender sender, final @NotNull String label, final String[] args) {
        if (!testPermission(sender)) return true;


        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MinecraftUtils.msg(sender, "/votes <player>");
                return true;
            }
            UserVoteData userVoteData = UserVoteData.getUser((OfflinePlayer) sender);
            MessagesUtil.VOTES.msg(sender, userVoteData.user().placeholders(), true);
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserVoteData userVoteData = UserVoteData.getUser(target);
        MessagesUtil.VOTES.msg(sender, userVoteData.user().placeholders(), true);

        return true;
    }
}
