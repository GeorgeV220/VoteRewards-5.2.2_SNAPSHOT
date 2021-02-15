package com.georgev22.voterewards.listeners;

import com.georgev22.externals.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/*
 *
 * This class handles votes going through the server.
 *
 */
public class VotifierListener implements Listener {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @EventHandler
    public void onVote(VotifierEvent e) {
        final Vote vote = e.getVote();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(vote.getUsername());

        if (!offlinePlayer.isOnline()) {
            if (Options.OFFLINE.isEnabled()) {
                try {
                    VoteUtils.processOfflineVote(offlinePlayer, vote.getServiceName());
                } catch (Exception ioException) {
                    ioException.printStackTrace();
                }
            }
            return;
        }

        VoteUtils.processVote(offlinePlayer, vote.getServiceName());
        ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        placeholders.append("%player%", vote.getUsername()).append("%servicename%", vote.getServiceName());
        if (Options.MESSAGE_VOTE.isEnabled())
            MessagesUtil.VOTE.msgAll(placeholders, true);

        placeholders.clear();
        if (Options.SOUND.isEnabled()) {
            offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), XSound.matchXSound(String.valueOf(Options.SOUND_VOTE.getValue())).get().parseSound(), 1000, 1);
        }
    }

}
