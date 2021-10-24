package com.georgev22.voterewards.listeners;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

/*
 *
 * This class handles votes going through the server.
 *
 */
public class VotifierListener implements Listener {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @EventHandler
    public void onVote(VotifierEvent e) throws IOException {
        final Vote vote = e.getVote();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(vote.getUsername());
        if (OptionsUtil.DEBUG_VOTE_PRE.isEnabled())
            MinecraftUtils.debug(voteRewardPlugin, "Pre process of vote " + vote);

        UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
        if (!offlinePlayer.isOnline()) {

            if (OptionsUtil.DEBUG_USELESS.isEnabled())
                MinecraftUtils.debug(voteRewardPlugin, "Player " + offlinePlayer.getName() + " is offline!");
            if (OptionsUtil.OFFLINE.isEnabled()) {
                try {
                    if (OptionsUtil.DEBUG_USELESS.isEnabled())
                        MinecraftUtils.debug(voteRewardPlugin, "Process " + vote.getUsername() + " vote with " + vote.getServiceName() + " service name!");
                    new VoteUtils(userVoteData.user()).processOfflineVote(vote.getServiceName());
                } catch (Exception ioException) {
                    ioException.printStackTrace();
                }
            }
            return;
        }

        new VoteUtils(userVoteData.user()).processVote(vote.getServiceName());
        ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        placeholders.append("%player%", vote.getUsername()).append("%servicename%", vote.getServiceName());
        if (OptionsUtil.MESSAGE_VOTE.isEnabled())
            MessagesUtil.VOTE.msgAll(placeholders, true);

        placeholders.clear();
    }

}
