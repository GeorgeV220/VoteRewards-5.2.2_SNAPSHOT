package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.PermissionsUtil;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Votes implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(PermissionsUtil.VOTES)) {
			MessagesUtil.NO_PERMISSION.msg(sender);
			return true;
		}
		Map<String, String> placeholders = Maps.newHashMap();
		if (args.length == 0) {
			placeholders.put("%player%", sender.getName());
			placeholders.put("%votes%",
					String.valueOf(UserVoteData.getUser(((Player) sender).getUniqueId()).getVotes()));
			MessagesUtil.VOTES.msg(sender, placeholders, true);
			placeholders.clear();
			return true;
		}
		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		placeholders.put("%player%", target.getName());
		placeholders.put("%votes%", String.valueOf(UserVoteData.getUser(target.getUniqueId()).getVotes()));
		MessagesUtil.VOTES.msg(sender, placeholders, true);
		placeholders.clear();
		return true;
	}
}
