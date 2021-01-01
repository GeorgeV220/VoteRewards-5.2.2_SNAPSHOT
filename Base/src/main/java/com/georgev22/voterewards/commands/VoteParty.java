package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserUtils;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Map;

public class VoteParty extends BukkitCommand {

    private final VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    public VoteParty() {
        super("voteparty");
        this.description = "VoteParty command";
        this.usageMessage = "/voteparty";
        this.setPermission("voterewards.voteparty");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vvp", "vp", "vrvp"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        final Map<String, String> placeholders = Maps.newHashMap();
        final FileManager fm = FileManager.getInstance();
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("voterewards.voteparty.start")) {
                    MessagesUtil.NO_PERMISSION.msg(sender);
                    return true;
                }
                VotePartyUtils.getInstance().start();
            } else if (args[0].equalsIgnoreCase("claim")) {
                if (!(sender instanceof Player)) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                    return true;
                }
                if (!sender.hasPermission("voterewards.voteparty.claim")) {
                    MessagesUtil.NO_PERMISSION.msg(sender);
                    return true;
                }
                final UserUtils userUtils = UserUtils.getUser(((Player) sender).getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (userUtils.getVoteParties() > 0) {
                            ((Player) sender).getInventory()
                                    .addItem(VotePartyUtils.getInstance().crate(userUtils.getVoteParties()));
                            placeholders.put("%crates%", String.valueOf(userUtils.getVoteParties()));
                            userUtils.setVoteParties(0);
                            MessagesUtil.VOTEPARTY_CLAIM.msg(sender, placeholders, true);
                            placeholders.clear();
                        } else {
                            MessagesUtil.VOTEPARTY_NOTHINGTOCLAIM.msg(sender);
                        }
                    }
                }.runTaskAsynchronously(m);
                return true;
            } else if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("voterewards.voteparty.give")) {
                    MessagesUtil.NO_PERMISSION.msg(sender);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    if (args.length < 2) {
                        Utils.msg(sender, "&c&l(!) &c/vp give <player>");
                        return true;
                    }
                    final Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        MessagesUtil.OFFLINE_PLAYER.msg(sender);
                        return true;
                    }
                    if (args.length == 2) {
                        target.getInventory().addItem(VotePartyUtils.getInstance().crate(1));
                        placeholders.put("%amount%", "1");
                        MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                        placeholders.clear();
                        return true;
                    }
                    if (!Utils.isInt(args[2])) {
                        Utils.msg(sender, "&c&l(!) &cEnter a valid number");
                        return true;
                    }
                    return true;
                }
                if (args.length == 1) {
                    ((Player) sender).getInventory().addItem(VotePartyUtils.getInstance().crate(1));
                    placeholders.put("%amount%", "1");
                    MessagesUtil.VOTEPARTY_GIVE.msg(sender, placeholders, true);
                    placeholders.clear();
                    return true;
                }
                final Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    MessagesUtil.OFFLINE_PLAYER.msg(sender);
                    return true;
                }
                if (args.length == 2) {
                    target.getInventory().addItem(VotePartyUtils.getInstance().crate(1));
                    placeholders.put("%amount%", "1");
                    MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                    placeholders.clear();
                    return true;
                }
                if (!Utils.isInt(args[2])) {
                    Utils.msg(sender, "&c&l(!) &cEnter a valid number");
                    return true;
                }
                ((Player) sender).getInventory().addItem(VotePartyUtils.getInstance().crate(Integer.parseInt(args[2])));
                placeholders.put("%amount%", args[2]);
                MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                placeholders.clear();
                return true;
            }
            return true;
        }
        placeholders.put("%votes%", String.valueOf(this.m.getConfig().getInt("VoteParty.votes")
                - fm.getData().getFileConfiguration().getInt("VoteParty-Votes")));
        placeholders.put("%current%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")));
        placeholders.put("%need%", String.valueOf(this.m.getConfig().getInt("VoteParty.votes")));
        MessagesUtil.VOTEPARTY.msg(sender, placeholders, true);
        placeholders.clear();
        return true;
    }
}
