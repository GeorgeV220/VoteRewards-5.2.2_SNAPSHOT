package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.PermissionsUtil;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

public class Vote extends BukkitCommand {


    public Vote() {
        super("vote");
        this.description = "Vote command";
        this.usageMessage = "/vote";
        this.setPermission("voterewards.vote");
        this.setAliases(Arrays.asList("vrv", "vrvote", "vvote"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!sender.hasPermission(PermissionsUtil.VOTE)) {
            MessagesUtil.NO_PERMISSION.msg(sender);
            return true;
        }
        Player player = (Player) sender;
        Map<String, String> placeholders = Maps.newHashMap();
        placeholders.put("%votes%", String.valueOf(UserVoteData.getUser(player.getUniqueId()).getVotes()));
        MessagesUtil.VOTE_COMMAND.msg(sender, placeholders, true);
        return true;
    }
}
