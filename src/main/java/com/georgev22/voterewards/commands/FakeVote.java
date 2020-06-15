package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.PermissionsUtil;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FakeVote extends BukkitCommand {


	public FakeVote() {
		super("fakevote");
		this.description = "FakeVote command";
		this.usageMessage = "/fakevote";
		this.setPermission("voterewards.fakevote");
		this.setAliases(Arrays.asList("vrfake", "vrfakevote", "vfake", "vfakevote"));
	}

	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
			return true;
		}
		if (!sender.hasPermission(PermissionsUtil.FAKE_VOTE)) {
			MessagesUtil.NO_PERMISSION.msg(sender);
			return true;
		}

		if (args.length == 0) {
			Vote vote = new Vote();
			vote.setUsername(sender.getName());
			vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
			vote.setAddress(((Player) sender).getAddress().getAddress().getHostAddress());
			vote.setServiceName("fakeVote");
			Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
			return true;
		}
		OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
		Vote vote = new Vote();
		vote.setUsername(target.getName());
		vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
		vote.setAddress("localhost");
		vote.setServiceName("fakeVote");
		Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));

		return true;
	}
}
