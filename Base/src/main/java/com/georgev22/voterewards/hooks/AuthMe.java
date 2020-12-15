package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.google.common.collect.Lists;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author GeorgeV22
 */
public class AuthMe implements Listener {

    @EventHandler
    public void onAuthLogin(LoginEvent event) {
        if (VoteOptions.OFFLINE.isEnabled()) {
            UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
            for (String serviceName : userVoteData.getServices()) {
                userVoteData.processVote(serviceName);
            }
            userVoteData.setOfflineServices(Lists.newArrayList());
        }
    }

}
