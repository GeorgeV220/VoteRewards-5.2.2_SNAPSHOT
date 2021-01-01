package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.voterewards.utilities.player.UserUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author GeorgeV22
 */
public class AuthMe implements Listener {

    @EventHandler
    public void onAuthLogin(LoginEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (VoteOptions.OFFLINE.isEnabled()) {
                    UserUtils userUtils = UserUtils.getUser(event.getPlayer().getUniqueId());
                    for (String serviceName : userUtils.getOfflineServices()) {
                        VoteUtils.processVote(event.getPlayer(), serviceName);
                    }
                    userUtils.setOfflineServices(Lists.newArrayList());
                }
            }
        }.runTaskAsynchronously(VoteRewardPlugin.getInstance());
    }

}
