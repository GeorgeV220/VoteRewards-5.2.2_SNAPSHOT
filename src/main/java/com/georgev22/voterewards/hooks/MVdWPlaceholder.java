package com.georgev22.voterewards.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;

public class MVdWPlaceholder {

    VoteRewardPlugin plugin = VoteRewardPlugin.getInstance();

    public void register() {
        FileManager fm = FileManager.getInstance();

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_player_votes",
                event -> String.valueOf(UserVoteData.getUser(event.getPlayer().getUniqueId()).getVotes()));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_player_all_time_votes",
                event -> String.valueOf(UserVoteData.getUser(event.getPlayer().getUniqueId()).getAllTimeVotes()));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_voteparty_total_votes",
                event -> String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_voteparty_votes_need",
                event -> String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_voteparty_votes_until",
                event -> String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                        - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_voteparty_bar", event -> Utils.getProgressBar(
                fm.getData().getFileConfiguration().getInt("VoteParty-Votes"),
                OptionsUtil.VOTEPARTY_VOTES.getIntValue(),
                OptionsUtil.VOTEPARTY_BARS.getIntValue(),
                OptionsUtil.VOTEPARTY_BAR_SYMBOL.getStringValue(),
                OptionsUtil.VOTEPARTY_COMPLETE_COLOR.getStringValue(),
                OptionsUtil.VOTEPARTY_NOT_COMPLETE_COLOR.getStringValue()));

        PlaceholderAPI.registerPlaceholder(plugin, "voterewards_top_voter", event -> VoteUtils.getTopPlayer(0));
    }
}
