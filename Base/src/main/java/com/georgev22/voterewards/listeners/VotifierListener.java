package com.georgev22.voterewards.listeners;

import com.cryptomorin.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.UserVoteData;
import com.georgev22.voterewards.utilities.VoteOptions;
import com.google.common.collect.Maps;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

/*
 *
 * This class handles votes going thru the server.
 *
 */
public class VotifierListener implements Listener {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @EventHandler
    public void onVote(VotifierEvent e) {

        final Vote vote = e.getVote();
        final Player target = Bukkit.getPlayerExact(vote.getUsername());

        if (target == null) {
            return;
        }

        UserVoteData user = UserVoteData.getUser(target.getUniqueId());
        user.processVote(vote.getServiceName());
        Map<String, String> placeholders = Maps.newHashMap();
        placeholders.put("%player%", vote.getUsername());
        placeholders.put("%servicename%", vote.getServiceName());
        if (VoteOptions.MESSAGE.isEnabled())
            MessagesUtil.VOTE.msgAll(placeholders, true);

        placeholders.clear();
        if (VoteOptions.SOUND.isEnabled()) {
            target.playSound(target.getLocation(), XSound.matchXSound(voteRewardPlugin.getConfig().getString("Sounds.Vote")).get().parseSound(), 1000, 1);
        }
    }

}
