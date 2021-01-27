package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.xseries.XSound;
import com.georgev22.externals.xseries.messages.Titles;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Instant;
import java.time.ZoneOffset;
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
        UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
        userVoteData.setVotes(userVoteData.getVotes() + 1);
        userVoteData.setLastVoted(System.currentTimeMillis());
        UserVoteData.getAllUsersMap().replace(offlinePlayer.getName(), UserVoteData.getUser(offlinePlayer.getUniqueId()).getVotes());

        if (Options.VOTE_TITLE.isEnabled()) {
            Titles.sendTitle(offlinePlayer.getPlayer(),
                    Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()),
                    Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (Options.WORLD.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName) != null) {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName));
            } else {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (!Options.DISABLE_SERVICES.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Services." + serviceName) != null) {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (Options.LUCKY.isEnabled()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt((int) Options.LUCKY_NUMBERS.getValue() + 1);
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Lucky")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(i)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Lucky." + s2 + ".commands"));
                }
            }
        }

        // PERMISSIONS REWARDS
        if (Options.PERMISSIONS.isEnabled()) {
            final ConfigurationSection section = voteRewardPlugin.getConfig()
                    .getConfigurationSection("Rewards.Permission");
            for (String s2 : section.getKeys(false)) {
                if (offlinePlayer.getPlayer().hasPermission("voterewards.permission" + s2)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Permission." + s2 + ".commands"));
                }
            }
        }

        // CUMULATIVE REWARDS
        if (Options.CUMULATIVE.isEnabled()) {
            int votes = userVoteData.getVotes();
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(votes)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                }
            }
        }

        // PLAY SOUND
        if (Options.SOUND.isEnabled()) {
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(),
                        XSound.matchXSound(String.valueOf(Options.SOUND_VOTE.getValue())).get().parseSound(), 1000, 1);
        }

        if (Options.DAILY.isEnabled()) {
            int votes = userVoteData.getDailyVotes();
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Daily")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(votes)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Daily." + s2 + ".commands"));
                }
            }
        }

        // VOTE PARTY START
        VotePartyUtils.getInstance().run(offlinePlayer, false);

        //HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            HolographicDisplays.updateAll();

        if (Options.DEBUG_VOTE_AFTER.isEnabled()) {
            Utils.debug(voteRewardPlugin,
                    "Vote for player " + offlinePlayer.getPlayer(),
                    "Votes: " + userVoteData.getVotes(),
                    "Last Voted: " + Instant.ofEpochMilli(userVoteData.getLastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        }
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
        UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
        userVoteData.load(new UserVoteData.Callback() {
            @Override
            public void onSuccess() {
                List<String> services = userVoteData.getOfflineServices();
                services.add(serviceName);
                userVoteData.setOfflineServices(services);
                userVoteData.save(true);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

}
