package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

/**
 * @author GeorgeV22
 */
public class AuthMe implements Listener {

    @EventHandler
    public void onAuthLogin(LoginEvent event) throws IOException {
        if (OptionsUtil.OFFLINE.isEnabled()) {
            UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
            for (String serviceName : userVoteData.getOfflineServices()) {
                new VoteUtils(userVoteData.user()).processVote(serviceName, false);
            }
            userVoteData.setOfflineServices(Lists.newArrayList());
        }
    }

}
