package com.georgev22.voterewards.commands;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Vote extends BukkitCommand {


    public Vote() {
        super("vote");
        this.description = "Vote command";
        this.usageMessage = "/vote";
        this.setPermission("voterewards.vote");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrv", "vrvote", "vvote"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player player)) {
            Utils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessages()[0]);
            return true;
        }

        ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
        placeholders.append("%votes%", String.valueOf(userVoteData.getVotes()));
        MessagesUtil.VOTE_COMMAND.msg(player, placeholders, true);

        if (OptionsUtil.CUMULATIVE.isEnabled() && OptionsUtil.CUMULATIVE_MESSAGE.isEnabled()) {
            placeholders.append("%votes%", String.valueOf(userVoteData.votesUntilNextCumulativeVote()));
            MessagesUtil.VOTE_COMMAND_CUMULATIVE.msg(player, placeholders, true);
        }

        return true;
    }
}