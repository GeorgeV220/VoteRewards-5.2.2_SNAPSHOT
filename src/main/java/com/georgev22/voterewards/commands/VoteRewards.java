package com.georgev22.voterewards.commands;

import org.bukkit.entity.Player;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.playerdata.UserVoteData;

import java.util.Objects;

import org.bukkit.Bukkit;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class VoteRewards implements CommandExecutor {

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!sender.hasPermission("voterewards.basic")) {
			MessagesUtil.NO_PERMISSION.msg(sender);
			return true;
		}
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
				data.setVotes(Integer.valueOf(args[3]));
				Utils.msg(sender, "VOTES SET MSG HERE");

			} else if (args[2].equalsIgnoreCase("voteparty")) {
				data.setVoteParty(Integer.valueOf(args[3]));
				Utils.msg(sender, "VOTEPARTY SET MSG HERE");

			} else if (args[2].equalsIgnoreCase("time")) {
				data.setLastVote(Integer.valueOf(args[3]));
				Utils.msg(sender, "TIME SET MSG HERE");

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
		}
		return true;
	}

}
