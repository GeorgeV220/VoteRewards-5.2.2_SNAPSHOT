package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.database.Backup;
import com.georgev22.voterewards.hooks.WorldEditHook;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class VoteRewards extends BukkitCommand {

    public VoteRewards() {
        super("voterewards");
        this.description = "VoteRewards command";
        this.usageMessage = "/voterewards";
        this.setPermission("voterewards.basic");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vr", "votereward", "voter", "vrewards"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length == 0) {
            Utils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            Utils.msg(sender, "&6/vr reload");
            Utils.msg(sender, "&6/vr backup");
            Utils.msg(sender, "&6/vr help [player, voteparty]");
            Utils.msg(sender, "&c&l==============");
            return true;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!)&c /vr clear <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());

            if (userVoteData.playerExists()) {
                try {
                    userVoteData.reset(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Utils.msg(sender, "&c&l(!)&c You cleared player " + target.getName());
            } else {
                Utils.msg(sender, "&c&l(!)&c Player " + target.getName() + " doesn't exist");
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            final FileManager fm = FileManager.getInstance();
            fm.getConfig().reloadFile();
            fm.getMessages().reloadFile();
            MessagesUtil.repairPaths(fm.getMessages());
            Utils.msg(sender, "&a&l(!) &aPlugin reloaded!");
        } else if (args[0].equalsIgnoreCase("backup")) {

            Bukkit.getScheduler().runTaskAsynchronously(VoteRewardPlugin.getInstance(), () -> {
                Utils.kickAll("Backup started!");
                Utils.disallowLogin(true, "Backup ongoing!");
                boolean saving = !VoteUtils.reminderMap.isEmpty();
                while (saving) {
                    if (VoteUtils.reminderMap.isEmpty())
                        saving = false;
                    if (!saving)
                        return;
                    ZonedDateTime zonedDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
                    new Backup("backup" + zonedDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy--h-mm-a"))).backup(new Callback() {
                        @Override
                        public void onSuccess() {
                            Utils.disallowLogin(false, "");
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                            Utils.disallowLogin(false, "");
                        }
                    });
                }

            });
        } else if (args[0].equalsIgnoreCase("restore")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!)&c /vr restore <file name>");
                Utils.msg(sender, "&c&l(!)&c Do not include file extension!");
                return true;
            }
            Utils.kickAll("Restore started!");
            Utils.disallowLogin(true, "Restore ongoing!");
            new Backup(args[1] + ".yml").restore(new Callback() {
                @Override
                public void onSuccess() {
                    Utils.disallowLogin(false, "");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    Utils.disallowLogin(false, "");
                }
            });

        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                Utils.msg(sender, "&c&l(!)&c /vr set <player> <data> <value>!");
                Utils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());
            if (args[2].equalsIgnoreCase("votes")) {
                userVoteData.setVotes(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " votes to " + args[3]);
            } else if (args[2].equalsIgnoreCase("voteparty")) {
                userVoteData.setVoteParties(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " voteparty crates to " + args[3]);
            } else if (args[2].equalsIgnoreCase("time")) {
                userVoteData.setLastVoted(Long.parseLong(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " last time vote to " + args[3]);
            } else if (args[2].equalsIgnoreCase("dailyvotes")) {
                userVoteData.setDailyVotes(Integer.parseInt(args[3]));
                Utils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " daily votes to " + args[3]);
            } else {
                Utils.msg(sender, "&c&l(!)&c /vr set <player> <data>!");
                Utils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes");
            }
            UserVoteData.getAllUsersMap().replace(target.getUniqueId(), userVoteData.user());
            userVoteData.save(true, new Callback() {
                @Override
                public void onSuccess() {
                    if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                        Utils.debug(VoteRewardPlugin.getInstance(),
                                "User " + userVoteData.user().getName() + " successfully saved!",
                                "Votes: " + userVoteData.user().getVotes(),
                                "Daily Votes: " + userVoteData.user().getDailyVotes(),
                                "Last Voted: " + Instant.ofEpochMilli(userVoteData.user().getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + userVoteData.user().getLastVoted(),
                                "Vote Parties: " + userVoteData.user().getVoteParties(),
                                "All time votes: " + userVoteData.user().getAllTimeVotes());
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            return true;
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!)&c /vr help [player, voteparty]");
                return true;
            }
            if (args[1].equalsIgnoreCase("player")) {
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
                Utils.msg(sender, "&6/vr clear <player>");
                Utils.msg(sender, "&6/vr set <player> <data> <value>");
                Utils.msg(sender, "&6/vr region add/remove <regionName>");
                Utils.msg(sender, "&c&l==============");
            } else if (args[1].equalsIgnoreCase("voteparty")) {
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, " ");
                Utils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
                Utils.msg(sender, "&6/vp start");
                Utils.msg(sender, "&6/vp claim");
                Utils.msg(sender, "&6/vp give <player> <amount>");
                Utils.msg(sender, "&c&l==============");
            } else {
                Utils.msg(sender, "&c&l(!)&c /vr help [player, voteparty]");
            }
        } else if (args[0].equalsIgnoreCase("region")) {
            if (args.length == 1) {
                Utils.msg(sender, "&c&l(!)&c /vr region <add/remove> <name>");
                return true;
            }

            if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    Utils.msg(sender, "&c&l(!)&c /vr region add <name>");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                    return true;
                }

                String regionName = args[2];

                WorldEditHook worldEditHook = new WorldEditHook(player);

                Location a = worldEditHook.getMinimumPoint();
                Location b = worldEditHook.getMaximumPoint();

                if (a == null || b == null) {
                    Utils.msg(sender, "&c&l(!)&c Please make a selection first!");
                    return true;
                }

                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();

                data.set("Regions." + regionName + ".minimumPos", a);
                data.set("Regions." + regionName + ".maximumPos", b);
                cfg.saveFile();

                Utils.msg(sender, "&a&l(!) &aAdded Location \na: " + a.getX() + "," + a.getY() + "," + a.getZ()
                        + "\nb: " + b.getX() + "," + b.getY() + "," + b.getZ());
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    Utils.msg(sender, "&c&l(!)&c /vr region remove <name>");
                    return true;
                }

                String regionName = args[2];
                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();

                data.set("Regions." + regionName, null);
                cfg.saveFile();

                Utils.msg(sender, "&c&l(!)&c Location " + regionName + " removed!");
            }

            return true;
        } else {
            Utils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            Utils.msg(sender, "&6/vr reload");
            Utils.msg(sender, "&6/vr help [player, voteparty]");
            Utils.msg(sender, "&c&l==============");
            return true;
        }
        return true;
    }

}
