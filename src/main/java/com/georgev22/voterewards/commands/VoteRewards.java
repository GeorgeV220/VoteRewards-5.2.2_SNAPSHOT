package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

public class VoteRewards extends BukkitCommand {

    private VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    public VoteRewards() {
        super("voterewards");
        this.description = "VoteRewards command";
        this.usageMessage = "/voterewards";
        this.setPermission("voterewards.basic");
        this.setPermissionMessage(MessagesUtil.NO_PERMISSION.getMessages()[0]);
        this.setAliases(Arrays.asList("vr", "votereward", "voter", "vrewards"));
    }

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            Utils.msg(sender, "&c&l(!) &cCommands &c&l(!)");
            Utils.msg(sender, "&6/vr reload");
            Utils.msg(sender, "&6/vr help [player, voteparty]");
            Utils.msg(sender, "&c&l==============");
            return true;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!) &c/vr clear <player>");
                return true;
            }
            final Player target = Bukkit.getPlayerExact(args[1]);
            final UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());
            try {
                if (userVoteData.playerExist()) {
                    userVoteData.reset();
                    Utils.msg(sender, "&c&l(!) &cYou cleared player " + target.getName());
                } else {
                    Utils.msg(sender, "&c&l(!) &cPlayer " + target.getName() + " doesn't exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            final FileManager fm = FileManager.getInstance();
            fm.getConfig().reloadFile();
            fm.getMessages().reloadFile();
            Utils.msg(sender, "&a&l(!) &aPlugin reloaded!");
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                Utils.msg(sender, "&c&l(!) &c/vr set <player> <votes|voteparty|time> <amount>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (Objects.isNull(target.getPlayer())) {
                return true;
            }
            UserVoteData data = UserVoteData.getUser(target.getUniqueId());
            if (args[2].equalsIgnoreCase("votes")) {
                data.setVotes(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " votes to " + args[3]);
            } else if (args[2].equalsIgnoreCase("voteparty")) {
                data.setVoteParty(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " voteparty crates to " + args[3]);
            } else if (args[2].equalsIgnoreCase("time")) {
                data.setLastVote(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " last time vote to " + args[3]);
            }
            return true;
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!) &c/vr help [player, voteparty]");
                return true;
            }
            if (args[1].equalsIgnoreCase("player")) {
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, "&c&l(!) &cCommands &c&l(!)");
                Utils.msg(sender, "&6/vr clear <player>");
                Utils.msg(sender, "&6/vr set <player> <votes|voteparty|time> <amount>");
                Utils.msg(sender, "&6/vr region <add|remove> <regionName>");
                Utils.msg(sender, "&c&l==============");
            } else if (args[1].equalsIgnoreCase("voteparty")) {
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, "&c&l(!) &cCommands &c&l(!)");
                Utils.msg(sender, "&6/vp start");
                Utils.msg(sender, "&6/vp claim");
                Utils.msg(sender, "&6/vp give <player> <amount>");
                Utils.msg(sender, "&c&l==============");
            } else {
                Utils.msg(sender, "&c&l(!) &c/vr help [player, voteparty]");
            }
        } else if (args[0].equalsIgnoreCase("region")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!) &c/vr region <add|remove> <name>");
                return true;
            }

            if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    Utils.msg(sender, "&c&l(!) &c/vr region add <name>");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                    return true;
                }

                Player player = (Player) sender;

                String regionName = args[2];
                Selection selection = m.getWorldEdit().getSelection(player);

                if (selection == null) {
                    Utils.msg(sender, "&c&l(!) &cPlease make a selection first!");
                    return true;
                }

                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();

                Location a = selection.getMinimumPoint();
                Location b = selection.getMaximumPoint();

                data.set("Regions." + regionName + ".minimumPos", a);
                data.set("Regions." + regionName + ".maximumPos", b);
                cfg.saveFile();

                Utils.msg(sender, "&a&l(!) &aAdded Location \na: " + a.getX() + "," + a.getY() + "," + a.getZ()
                        + "\nb: " + b.getX() + "," + b.getY() + "," + b.getZ());
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    Utils.msg(sender, "&c&l(!) &c/vr region remove <name>");
                    return true;
                }

                String regionName = args[2];
                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();

                data.set("Regions." + regionName, null);
                cfg.saveFile();

                Utils.msg(sender, "&c&l(!) &cLocation " + regionName + " removed!");
            }

            return true;
        }
        return true;
    }

}
