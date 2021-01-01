package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserUtils;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Map;

public class Votes extends BukkitCommand {


    public Votes() {
        super("votes");
        this.description = "Votes command";
        this.usageMessage = "/votes";
        this.setPermission("voterewards.vote");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrvs", "vrvotes", "vvotes"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, String> placeholders = Maps.newHashMap();
                if (args.length == 0) {
                    if (!(sender instanceof Player)) {
                        Utils.msg(sender, "/votes <player>");
                        return;
                    }
                    placeholders.put("%player%", sender.getName());
                    placeholders.put("%votes%",
                            String.valueOf(UserUtils.getUser(((Player) sender).getUniqueId()).getVotes()));
                    MessagesUtil.VOTES.msg(sender, placeholders, true);
                    placeholders.clear();
                    return;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                placeholders.put("%player%", target.getName());
                placeholders.put("%votes%", String.valueOf(UserUtils.getUser(target.getUniqueId()).getVotes()));
                MessagesUtil.VOTES.msg(sender, placeholders, true);
                placeholders.clear();
            }
        }.runTaskAsynchronously(VoteRewardPlugin.getInstance());
        return true;
    }
}
