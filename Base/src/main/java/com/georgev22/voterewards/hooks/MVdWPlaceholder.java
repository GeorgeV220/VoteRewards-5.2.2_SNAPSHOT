package com.georgev22.voterewards.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserVoteData;

public class MVdWPlaceholder {

    VoteRewardPlugin plugin = VoteRewardPlugin.getInstance();

    public void hook() {
        FileManager fm = FileManager.getInstance();

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_player_votes",
                event -> String.valueOf(UserVoteData.getUser(event.getPlayer().getUniqueId()).getVotes()));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_total_votes",
                event -> String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_votes_needed",
                event -> String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2)));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_votes_until",
                event -> String.valueOf(fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2)
                        - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_top_voter", event -> Utils.getTopPlayer(0));
    }
}
