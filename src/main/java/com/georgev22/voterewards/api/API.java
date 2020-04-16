package com.georgev22.voterewards.api;

import com.georgev22.voterewards.playerdata.UserVoteData;
import com.georgev22.voterewards.playerdata.VotePartyUtils;

import org.bukkit.entity.Player;

public class API {

	private static API instance = null;

	/**
	 * Return the API instance
	 */
	public static API getInstance() {
		return instance == null ? instance = new API() : instance;
	}

	/**
	 * Get the votes of a player
	 *
	 * @param player
	 * @return
	 */
	public Integer getVotes(Player player) {
		return UserVoteData.getUser(player.getUniqueId()).getVotes();
	}

	/**
	 * Get the voteparty of a player
	 *
	 * @param player
	 * @return
	 */
	public Integer getVoteParty(Player player) {
		return UserVoteData.getUser(player.getUniqueId()).getVoteParty();
	}

	/**
	 * Set voteparty votes of a player
	 *
	 * @param player
	 * @param vp
	 */
	public void setVoteParty(Player player, int vp) {
		UserVoteData.getUser(player.getUniqueId()).setVoteParty(vp);
	}

	/**
	 * Set the votes of a player
	 *
	 * @param player
	 * @param votes
	 */
	public void setVotes(Player player, int votes) {
		UserVoteData.getUser(player.getUniqueId()).setVotes(votes);
	}

	/**
	 * Reset a player
	 *
	 * @param player
	 */
	public void reset(Player player) {
		try {
			UserVoteData.getUser(player.getUniqueId()).reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if a player exist
	 *
	 * @param player
	 * @return playerExist
	 */
	public boolean playerExist(Player player) {
		try {
			return UserVoteData.getUser(player.getUniqueId()).playerExist();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Setup user
	 *
	 * @param player
	 */
	public void setupUser(Player player) {
		UserVoteData.getUser(player.getUniqueId()).setupUser();
	}

	@Deprecated
	public void deleteUser(Player player) {
		throw new UnsupportedOperationException("Not supporter yet");
	}

	/**
	 * Start VoteParty
	 */
	@Deprecated
	public void startVoteParty() {
		VotePartyUtils.getInstance().start();
	}

}
