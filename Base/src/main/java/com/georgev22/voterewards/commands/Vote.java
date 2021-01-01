package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.voterewards.utilities.player.UserUtils;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Map;

public class Vote extends BukkitCommand {


    public Vote() {
        super("vote");
        this.description = "Vote command";
        this.usageMessage = "/vote";
        this.setPermission("voterewards.vote");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrv", "vrvote", "vvote"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            Utils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessages()[0]);
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = (Player) sender;
                Map<String, String> placeholders = Maps.newHashMap();
                UserUtils userUtils = UserUtils.getUser(player.getUniqueId());
                placeholders.put("%votes%", String.valueOf(userUtils.getVotes()));
                MessagesUtil.VOTE_COMMAND.msg(player, placeholders, true);

                if (VoteOptions.CUMULATIVE.isEnabled() && VoteOptions.CUMULATIVE_MESSAGE.isEnabled()) {
                    placeholders.replace("%votes%", String.valueOf(userUtils.votesUntilNextCumulativeVote()));
                    MessagesUtil.VOTE_COMMAND_CUMULATIVE.msg(player, placeholders, true);
                }
            }
        }.runTaskAsynchronously(VoteRewardPlugin.getInstance());
        return true;
    }
}
