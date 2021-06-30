package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

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

        if (identifier.equalsIgnoreCase("player_votes")) {
            return String.valueOf(UserVoteData.getUser(player.getUniqueId()).getVotes());
        }
        if (identifier.equalsIgnoreCase("player_all_time_votes")) {
            return String.valueOf(UserVoteData.getUser(player.getUniqueId()).getAllTimeVotes());
        }
        final FileManager fm = FileManager.getInstance();
        if (identifier.equalsIgnoreCase("voteparty_total_votes")) {
            return String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes"));
        }

        if (identifier.equalsIgnoreCase("voteparty_votes_until")) {
            return String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                    - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0));
        }
        if (identifier.equalsIgnoreCase("voteparty_votes_need")) {
            return OptionsUtil.VOTEPARTY_VOTES.getStringValue();
        }

        if (identifier.equalsIgnoreCase("voteparty_bar")) {
            return Utils.getProgressBar(
                    fm.getData().getFileConfiguration().getInt("VoteParty-Votes"),
                    OptionsUtil.VOTEPARTY_VOTES.getIntValue(),
                    OptionsUtil.VOTEPARTY_BARS.getIntValue(),
                    OptionsUtil.VOTEPARTY_BAR_SYMBOL.getStringValue(),
                    OptionsUtil.VOTEPARTY_COMPLETE_COLOR.getStringValue(),
                    OptionsUtil.VOTEPARTY_NOT_COMPLETE_COLOR.getStringValue());
        }

        if (identifier.equalsIgnoreCase("top_voter")) {
            return VoteUtils.getTopPlayer(0);
        }

        return "null";
    }

}