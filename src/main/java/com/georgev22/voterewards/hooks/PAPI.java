package com.georgev22.voterewards.hooks;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.api.utilities.Utils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPI extends PlaceholderExpansion {

    VoteRewardPlugin plugin = VoteRewardPlugin.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "voterewards";
    }

    @Override
    public String getRequiredPlugin() {
        return "VoteRewards";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GeorgeV22, Shin1gamiX";
    }

    @Override
    public @NotNull String getVersion() {
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
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (StringUtils.startsWithIgnoreCase(identifier, "top_playerName_")) {
            return VoteUtils.getTopPlayer(Integer.parseInt(identifier.split("_")[2]) - 1);
        }

        if (StringUtils.startsWithIgnoreCase(identifier, "top_playerVotes_")) {
            return String.valueOf(UserVoteData.getUser(Bukkit.getOfflinePlayer(VoteUtils.getTopPlayer(Integer.parseInt(identifier.split("_")[2]) - 1))).user().getVotes());
        }

        if (identifier.equalsIgnoreCase("player_votes")) {
            return String.valueOf(UserVoteData.getUser(offlinePlayer.getUniqueId()).getVotes());
        }
        if (identifier.equalsIgnoreCase("player_all_time_votes")) {
            return String.valueOf(UserVoteData.getUser(offlinePlayer.getUniqueId()).getAllTimeVotes());
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

        if (identifier.equalsIgnoreCase("voteparty_votes_full")) {
            return OptionsUtil.VOTEPARTY_PLAYERS.getBooleanValue() & VotePartyUtils.isWaitingForPlayers() ? MinecraftUtils.colorize(Utils.placeHolder(
                    MessagesUtil.VOTEPARTY_WAITING_FOR_MORE_PLAYERS_PLACEHOLDER.getMessages()[0],
                    ObjectMap.newHashObjectMap()
                            .append("%online%", Bukkit.getOnlinePlayers().size())
                            .append("%need%", OptionsUtil.VOTEPARTY_PLAYERS_NEED.getIntValue()), true)) : MinecraftUtils.colorize(Utils.placeHolder(
                    MessagesUtil.VOTEPARTY_PLAYERS_FULL_PLACEHOLDER.getMessages()[0],
                    ObjectMap.newHashObjectMap()
                            .append("%until%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                                    - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)))
                            .append("%total%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                            .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue())), true));
        }

        if (identifier.equalsIgnoreCase("voteparty_bar")) {
            return MinecraftUtils.getProgressBar(
                    fm.getData().getFileConfiguration().getInt("VoteParty-Votes"),
                    OptionsUtil.VOTEPARTY_VOTES.getIntValue(),
                    OptionsUtil.VOTEPARTY_BARS.getIntValue(),
                    OptionsUtil.VOTEPARTY_BAR_SYMBOL.getStringValue(),
                    OptionsUtil.VOTEPARTY_COMPLETE_COLOR.getStringValue(),
                    OptionsUtil.VOTEPARTY_NOT_COMPLETE_COLOR.getStringValue());
        }

        return null;
    }

}