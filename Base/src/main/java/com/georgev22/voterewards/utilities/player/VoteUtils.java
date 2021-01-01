package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.xseries.XSound;
import com.georgev22.xseries.messages.Titles;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VoteUtils {

    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    /**
     * Process player vote
     *
     * @param offlinePlayer the player who voted
     * @param serviceName   the service name (dah)
     */
    public static void processVote(OfflinePlayer offlinePlayer, String serviceName) {
        UserUtils userUtils = UserUtils.getUser(offlinePlayer.getUniqueId());
        userUtils.setVotes(userUtils.getVotes() + 1);
        userUtils.setLastVoted(System.currentTimeMillis());
        userUtils.save();

        if (VoteOptions.VOTE_TITLE.isEnabled()) {
            Titles.sendTitle(offlinePlayer.getPlayer(),
                    Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()),
                    Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (VoteOptions.WORLD.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName) != null) {
                userUtils.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName));
            } else {
                userUtils.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (!VoteOptions.DISABLE_SERVICES.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Services." + serviceName) != null) {
                userUtils.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                userUtils.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (VoteOptions.LUCKY.isEnabled()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt(voteRewardPlugin.getConfig().getInt("Options.votes.lucky numbers") + 1);
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Lucky")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(i)) {
                    userUtils.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Lucky." + s2 + ".commands"));
                }
            }
        }

        // PERMISSIONS REWARDS
        if (VoteOptions.PERMISSIONS.isEnabled()) {
            final ConfigurationSection section = voteRewardPlugin.getConfig()
                    .getConfigurationSection("Rewards.Permission");
            for (String s2 : section.getKeys(false)) {
                if (offlinePlayer.getPlayer().hasPermission("voterewards.permission" + s2)) {
                    userUtils.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Permission." + s2 + ".commands"));
                }
            }
        }

        // CUMULATIVE REWARDS
        if (VoteOptions.CUMULATIVE.isEnabled()) {
            int votes = userUtils.getVotes();
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(votes)) {
                    userUtils.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                }
            }
        }

        // PLAY SOUND
        if (VoteOptions.SOUND.isEnabled()) {
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(),
                        XSound.matchXSound(voteRewardPlugin.getConfig().getString("Sounds.Vote")).get().parseSound(), 1000, 1);
        }

        // VOTE PARTY START
        VotePartyUtils.getInstance().run(offlinePlayer);

        //HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            HolographicDisplays.updateAll();
    }

    /**
     * Do not look at this:)
     * <p>
     * Process player offline vote
     *
     * @param offlinePlayer player who voted
     * @param serviceName   service name (dah)
     */
    public static void processOfflineVote(OfflinePlayer offlinePlayer, final String serviceName) {
        UserUtils userUtils = UserUtils.getUser(offlinePlayer.getUniqueId());
        List<String> services = userUtils.getOfflineServices();
        services.add(serviceName);
        userUtils.setOfflineServices(services);
        userUtils.save();
    }

}
