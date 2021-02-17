package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.xseries.XSound;
import com.georgev22.externals.xseries.messages.Titles;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.maps.LinkedObjectMap;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
        userVoteData.setAllTimeVotes(userVoteData.getAllTimeVotes() + 1);
        UserVoteData.getAllUsersMap().replace(offlinePlayer.getUniqueId(), UserVoteData.getUser(offlinePlayer.getUniqueId()).getUser());

        if (OptionsUtil.VOTE_TITLE.isEnabled()) {
            Titles.sendTitle(offlinePlayer.getPlayer(),
                    Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()),
                    Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", offlinePlayer.getName()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (OptionsUtil.WORLD.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName) != null) {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + "." + serviceName));
            } else {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + offlinePlayer.getPlayer().getWorld() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (!OptionsUtil.DISABLE_SERVICES.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Services." + serviceName) != null) {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (OptionsUtil.LUCKY.isEnabled()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt(OptionsUtil.LUCKY_NUMBERS.getIntValue() + 1);
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Lucky")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(i)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Lucky." + s2 + ".commands"));
                }
            }
        }

        // PERMISSIONS REWARDS
        if (OptionsUtil.PERMISSIONS.isEnabled()) {
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
        if (OptionsUtil.CUMULATIVE.isEnabled()) {
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
        if (OptionsUtil.SOUND.isEnabled()) {
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(),
                        XSound.matchXSound(OptionsUtil.SOUND_VOTE.getStringValue()).get().parseSound(), 1000, 1);
        }

        if (OptionsUtil.DAILY.isEnabled()) {
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

        if (OptionsUtil.DEBUG_VOTE_AFTER.isEnabled()) {
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
     * @throws Exception When something goes wrong
     */
    public static void processOfflineVote(OfflinePlayer offlinePlayer, final String serviceName) throws Exception {
        UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
        userVoteData.load(new Callback() {
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

    public static void monthlyReset() {
        Bukkit.getScheduler().runTaskTimer(voteRewardPlugin, () -> {
            if (OptionsUtil.DEBUG_USELESS.isEnabled())
                Utils.debug(voteRewardPlugin, "Monthly reset Thread ID: " + Thread.currentThread().getId());
            FileManager fileManager = FileManager.getInstance();
            CFG cfg = fileManager.getData();
            FileConfiguration dataConfiguration = cfg.getFileConfiguration();
            if (dataConfiguration.getInt("month") != Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()) {
                ObjectMap<UUID, User> objectMap = UserVoteData.getAllUsersMap();
                objectMap.forEach((uuid, user) -> {
                    UserVoteData userVoteData = UserVoteData.getUser(uuid);
                    userVoteData.reset();
                });
                dataConfiguration.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                cfg.saveFile();
            }
        }, 20L, OptionsUtil.MONTHLY_MINUTES.getLongValue() * 1200L);
    }

    /**
     * Purge players data if they don't have vote for a X days.
     */
    public static void purgeData() {
        Bukkit.getScheduler().runTaskTimer(voteRewardPlugin, () -> {
            if (OptionsUtil.DEBUG_USELESS.isEnabled())
                Utils.debug(voteRewardPlugin, "Purge data Thread ID: " + Thread.currentThread().getId());
            ObjectMap<UUID, User> objectMap = UserVoteData.getAllUsersMap();
            objectMap.forEach((uuid, user) -> {
                UserVoteData userVoteData = UserVoteData.getUser(uuid);
                long time = userVoteData.getLastVote() + (OptionsUtil.PURGE_DAYS.getLongValue() * 86400000);
                if (OptionsUtil.DEBUG_USELESS.isEnabled()) {
                    Utils.debug(voteRewardPlugin, Instant.ofEpochMilli(userVoteData.getLastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                    Utils.debug(voteRewardPlugin, Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                }
                if (time <= System.currentTimeMillis()) {
                    userVoteData.delete();
                }

            });
        }, 20L, OptionsUtil.PURGE_MINUTES.getLongValue() * 1200L);
    }

    /**
     * @param limit number of top monthly voters in a Map.
     * @return a {@link LinkedObjectMap} with {limit} top players.
     */
    public static LinkedObjectMap<String, Integer> getTopPlayers(int limit) {
        ObjectMap<String, Integer> objectMap = ObjectMap.newLinkedObjectMap();

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("votes"));
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @param limit number of top all time voters in a Map.
     * @return a {@link LinkedObjectMap} with {limit} top players.
     */
    public static LinkedObjectMap<String, Integer> getAllTimeTopPlayers(int limit) {
        ObjectMap<String, Integer> objectMap = ObjectMap.newLinkedObjectMap();

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("totalvotes"));
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    public static String getTopPlayer(int number) {
        try {
            return String.valueOf(getTopPlayers(number + 1).keySet().toArray()[number]).replace("[", "").replace("]", "");
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return getTopPlayer(0);
        }
    }

}
