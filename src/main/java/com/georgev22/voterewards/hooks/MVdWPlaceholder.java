package com.georgev22.voterewards.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.Utils;
import org.bukkit.Bukkit;

public class MVdWPlaceholder {
	private final VoteRewardPlugin plugin;

	public MVdWPlaceholder(VoteRewardPlugin plugin) {
		this.plugin = plugin;
	}

	public void hook() {
		MVdWPlaceholderAPI();
	}

	private void MVdWPlaceholderAPI() {
		if (!Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			return;
		}

		FileManager fm = FileManager.getInstance();

		PlaceholderAPI.registerPlaceholder(this.plugin, "voterewards_total_votes",
				event -> String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")));

		PlaceholderAPI.registerPlaceholder(this.plugin, "voterewards_votes_needed",
				event -> String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2)));

		PlaceholderAPI.registerPlaceholder(this.plugin, "voterewards_votes_until",
				event -> String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2)
						- fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)));

		PlaceholderAPI.registerPlaceholder(this.plugin, "voterewards_vote_top", event -> Utils.getTopPlayer());
	}
}
