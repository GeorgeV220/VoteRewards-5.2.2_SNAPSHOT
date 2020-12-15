package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteOptions;
import com.google.common.collect.Lists;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author GeorgeV22
 */
public class AuthMe implements Listener {

    public AuthMe() {
        Bukkit.getPluginManager().registerEvents(this, VoteRewardPlugin.getInstance());
    }

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
