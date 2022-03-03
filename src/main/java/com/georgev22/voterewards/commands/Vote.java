package com.georgev22.voterewards.commands;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteInventory;
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
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrv", "vrvote", "vvote"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            MinecraftUtils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessages()[0]);
            return true;
        }

        Player player = (Player) sender;

        if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            new VoteInventory().openInventory(((Player) sender));
        }

        ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
        placeholders.append("%votes%", String.valueOf(userVoteData.getVotes()));
        MessagesUtil.VOTE_COMMAND.msg(player, placeholders, true);

        if (OptionsUtil.CUMULATIVE.getBooleanValue() && OptionsUtil.CUMULATIVE_MESSAGE.getBooleanValue()) {
            placeholders.append("%votes%", String.valueOf(userVoteData.votesUntilNextCumulativeVote()));
            MessagesUtil.VOTE_COMMAND_CUMULATIVE.msg(player, placeholders, true);
        }

        return true;
    }
}