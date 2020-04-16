package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.PermissionsUtil;
import com.google.common.collect.Maps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Vote implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(PermissionsUtil.VOTE)) {
			MessagesUtil.NO_PERMISSION.msg(sender);
			return true;
		}
		Player player = (Player) sender;
		Map<String, String> placeholders = Maps.newHashMap();
		placeholders.put("%votes%", String.valueOf(UserVoteData.getUser(player.getUniqueId()).getVotes()));
		MessagesUtil.VOTE_COMMAND.msg(sender, placeholders, true);
		return true;
	}
}
