package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.utilities.maps.ConcurrentObjectMap;
import com.georgev22.externals.utilities.maps.LinkedObjectMap;
import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.externals.xseries.XSound;
import com.georgev22.externals.xseries.messages.Titles;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MinecraftVersion;
import com.georgev22.voterewards.utilities.configmanager.CFG;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public record VoteUtils(User user) {

    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    /**
     * Process player vote
     *
     * @param serviceName the service name (dah)
     */
    public void processVote(String serviceName) {
        processVote(serviceName, OptionsUtil.VOTEPARTY.isEnabled());
    }

    /**
     * Process player vote
     *
     * @param serviceName  the service name (dah)
     * @param addVoteParty count the vote on voteparty
     * @since v4.7.0
     */
    public void processVote(String serviceName, boolean addVoteParty) {
        if (OptionsUtil.DEBUG_VOTES_REGULAR.isEnabled())
            Utils.debug(voteRewardPlugin, "VOTE OF: " + user.getName());
        UserVoteData userVoteData = UserVoteData.getUser(user.getUniqueId());
        userVoteData.setVotes(userVoteData.getVotes() + 1);
        userVoteData.setLastVoted(System.currentTimeMillis());
        userVoteData.setAllTimeVotes(userVoteData.getAllTimeVotes() + 1);
        userVoteData.setDailyVotes(userVoteData.getDailyVotes() + 1);
        UserVoteData.getAllUsersMap().replace(user.getUniqueId(), UserVoteData.getUser(user.getUniqueId()).user());

        if (OptionsUtil.VOTE_TITLE.isEnabled()) {
            Titles.sendTitle(user.getPlayer(),
                    Utils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", user.getName()),
                    Utils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", user.getName()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (OptionsUtil.WORLD.isEnabled()) {
            if (voteRewardPlugin.getConfig().getString("Rewards.Worlds." + user.getPlayer().getWorld() + "." + serviceName) != null && OptionsUtil.WORLD_SERVICES.isEnabled()) {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + user.getPlayer().getWorld().getName() + "." + serviceName));
            } else {
                userVoteData.runCommands(voteRewardPlugin.getConfig()
                        .getStringList("Rewards.Worlds." + user.getPlayer().getWorld().getName() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (OptionsUtil.SERVICES.isEnabled()) {
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
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Permission").getKeys(false)) {
                if (user.getPlayer().hasPermission("voterewards.permission." + s2)) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Permission." + s2 + ".commands"));
                }
            }
        }

        // CUMULATIVE REWARDS
        if (OptionsUtil.CUMULATIVE.isEnabled()) {
            for (String s2 : voteRewardPlugin.getConfig().getConfigurationSection("Rewards.Cumulative")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(userVoteData.getVotes())) {
                    userVoteData.runCommands(voteRewardPlugin.getConfig()
                            .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                }
            }
        }

        // PLAY SOUND
        if (OptionsUtil.SOUND.isEnabled()) {
            if (MinecraftVersion.getCurrentVersion().isBelow(MinecraftVersion.V1_12_R1)) {
                user.getPlayer().playSound(user.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_VOTE.getStringValue()).get().parseSound(),
                        1000, 1);
                if (OptionsUtil.DEBUG_USELESS.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "========================================================");
                    Utils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    Utils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    Utils.debug(voteRewardPlugin, "========================================================");
                }
            } else {
                user.getPlayer().playSound(user.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_VOTE.getStringValue()).get().parseSound(),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_VOTE_CHANNEL.getStringValue()),
                        1000, 1);
            }
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

        // VOTE PARTY
        if (addVoteParty)
            new VotePartyUtils(user.getOfflinePlayer()).run(false);

        //HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            HolographicDisplays.updateAll();

        if (OptionsUtil.DEBUG_VOTE_AFTER.isEnabled()) {
            Utils.debug(voteRewardPlugin,
                    "Vote for player " + user.getOfflinePlayer(),
                    "Votes: " + userVoteData.getVotes(),
                    "Last Voted: " + Instant.ofEpochMilli(userVoteData.getLastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        }
    }

    /**
     * Do not look at this:)
     * <p>
     * Process player offline vote
     *
     * @param serviceName service name (dah)
     * @throws Exception When something goes wrong
     */
    public void processOfflineVote(final String serviceName) throws Exception {
        UserVoteData userVoteData = UserVoteData.getUser(user.getUniqueId());
        userVoteData.load(new Callback() {
            @Override
            public void onSuccess() {
                List<String> services = userVoteData.getOfflineServices();
                services.add(serviceName);
                userVoteData.setOfflineServices(services);
                userVoteData.save(true, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (OptionsUtil.DEBUG_SAVE.isEnabled()) {
                            Utils.debug(voteRewardPlugin,
                                    "User " + user.getName() + " successfully saved!",
                                    "Votes: " + user.getVotes(),
                                    "Daily Votes: " + user.getDailyVotes(),
                                    "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted(),
                                    "Vote Parties: " + user.getVoteParties(),
                                    "All time votes: " + user.getAllTimeVotes());
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
                new VotePartyUtils(user.getOfflinePlayer()).run(false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Monthly reset the players stats
     *
     * @since v4.7.0
     */
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
                    userVoteData.reset(false);
                });
                dataConfiguration.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                cfg.saveFile();
            }
        }, 20L, OptionsUtil.MONTHLY_MINUTES.getLongValue() * 1200L);
    }

    /**
     * Purge players data if they don't have vote for a X days.
     *
     * @since v4.7.0
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

    /**
     * Get the top player in X place
     *
     * @param number the number of the place
     * @return X place player name
     */
    public static String getTopPlayer(int number) {
        try {
            return String.valueOf(getTopPlayers(number + 1).keySet().toArray()[number]).replace("[", "").replace("]", "");
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return getTopPlayer(0);
        }
    }

    public static void reminder() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(voteRewardPlugin, () -> reminderMap.forEach((key, value) -> {
            if (value <= System.currentTimeMillis()) {
                UserVoteData userVoteData = UserVoteData.getUser(key.getUniqueId());
                if (System.currentTimeMillis() >= userVoteData.getLastVote() + (24 * 60 * 60 * 1000)) {
                    ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
                    placeholders.append("%player%", key.getName());
                    MessagesUtil.REMINDER.msg(key, placeholders, true);
                }
                reminderMap.replace(key, System.currentTimeMillis() + (OptionsUtil.REMINDER_SEC.getIntValue() * 1000));
            }
        }), 20, 20);
    }

    /**
     * Creates the reminder map
     * <p>
     * creates a new, empty {@link ConcurrentObjectMap#ConcurrentObjectMap()}
     */
    public static final ObjectMap<Player, Long> reminderMap = ObjectMap.newConcurrentObjectMap();

}
