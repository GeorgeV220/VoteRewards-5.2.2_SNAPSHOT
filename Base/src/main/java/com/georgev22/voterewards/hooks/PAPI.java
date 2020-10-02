package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;

import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.utilities.Utils;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PAPI extends PlaceholderExpansion {

	VoteRewardPlugin plugin = VoteRewardPlugin.getInstance();

	@Override
	public String getIdentifier() {
		return "voterewards";
	}

	@Override
	public String getRequiredPlugin() {
		return "VoteRewards";
	}

	@Override
	public String getAuthor() {
		return "GeorgeV22, Shin1gamiX";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (player == null) {
			return "null";
		}

		if (identifier.equalsIgnoreCase("player_player_votes")) {
			return String.valueOf(UserVoteData.getUser(player.getUniqueId()).getVotes());
		}

		final FileManager fm = FileManager.getInstance();
		if (identifier.equalsIgnoreCase("total_votes")) {
			return String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes"));
		}
		if (identifier.equalsIgnoreCase("votes_until")) {
			return String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2)
					- fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0));
		}
		if (identifier.equalsIgnoreCase("votes_needed")) {
			return String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2));
		}

		if (identifier.equalsIgnoreCase("top_voter")) {
			return String.valueOf(Utils.getTopPlayer());
		}

		return null;
	}

}