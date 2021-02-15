package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        final ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        final FileManager fm = FileManager.getInstance();
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("voterewards.voteparty.start")) {
                    MessagesUtil.NO_PERMISSION.msg(sender);
                    return true;
                }
                VotePartyUtils.getInstance().run(null, true);
            } else if (args[0].equalsIgnoreCase("claim")) {
                if (!(sender instanceof Player)) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                    return true;
                }
                if (!sender.hasPermission("voterewards.voteparty.claim")) {
                    MessagesUtil.NO_PERMISSION.msg(sender);
                    return true;
                }
                final UserVoteData userVoteData = UserVoteData.getUser(((Player) sender).getUniqueId());
                if (userVoteData.getVoteParty() > 0) {
                    ((Player) sender).getInventory()
                            .addItem(VotePartyUtils.getInstance().crate(userVoteData.getVoteParty()));
                    placeholders.append("%crates%", String.valueOf(userVoteData.getVoteParty()));
                    userVoteData.setVoteParties(0);
                    MessagesUtil.VOTEPARTY_CLAIM.msg(sender, placeholders, true);
                    placeholders.clear();
                } else {
                    MessagesUtil.VOTEPARTY_NOTHINGTOCLAIM.msg(sender);
                }
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
                        placeholders.append("%amount%", "1");
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
                    placeholders.append("%amount%", "1");
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
                    placeholders.append("%amount%", "1");
                    MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                    placeholders.clear();
                    return true;
                }
                if (!Utils.isInt(args[2])) {
                    Utils.msg(sender, "&c&l(!) &cEnter a valid number");
                    return true;
                }
                ((Player) sender).getInventory().addItem(VotePartyUtils.getInstance().crate(Integer.parseInt(args[2])));
                placeholders.append("%amount%", args[2]);
                MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                placeholders.clear();
                return true;
            }
            return true;
        }
        placeholders
                .append("%votes%", String.valueOf(Options.VOTEPARTY_VOTES.getIntValue()
                        - fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%current%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%need%", String.valueOf(Options.VOTEPARTY_VOTES.getIntValue()));
        MessagesUtil.VOTEPARTY.msg(sender, placeholders, true);
        placeholders.clear();
        return true;
    }
}
