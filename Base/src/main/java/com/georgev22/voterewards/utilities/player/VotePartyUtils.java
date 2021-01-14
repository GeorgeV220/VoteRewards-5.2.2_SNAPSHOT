package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Regions;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.xseries.XMaterial;
import com.georgev22.xseries.XSound;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VotePartyUtils {

    private static VotePartyUtils instance;

    public static VotePartyUtils getInstance() {
        return instance == null ? instance = new VotePartyUtils() : instance;
    }

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private final Set<OfflinePlayer> players = Sets.newHashSet();

    public void run(OfflinePlayer player) {
        if (!Options.VOTEPARTY.isEnabled()) {
            return;
        }

        final FileManager fm = FileManager.getInstance();

        final FileConfiguration dataFile = fm.getData().getFileConfiguration();

        dataFile.set("VoteParty-Votes", dataFile.getInt("VoteParty-Votes", 0) + 1);
        fm.getData().saveFile();

        int maxVotes = (int) Options.VOTEPARTY_VOTES.getValue();
        int currentVotes = dataFile.getInt("VoteParty-Votes", 0);

        final Map<String, String> placeholders = Maps.newHashMap();
        if (maxVotes - currentVotes > 0) {
            placeholders.put("%votes%", Utils.formatNumber(maxVotes - currentVotes));
            MessagesUtil.VOTEPARTY_VOTES_NEED.msgAll(placeholders, true);
            placeholders.clear();
        }

        if (Options.VOTEPARTY_PARTICIPATE.isEnabled()) {
            players.add(player);
        } else {
            this.players.addAll(Bukkit.getOnlinePlayers());
        }

        if (currentVotes < maxVotes) {
            return;

        }
        if (Options.VOTEPARTY_COOLDOWN.isEnabled()) {
            placeholders.put("%secs%",
                    String.valueOf(Options.VOTEPARTY_COOLDOWN_SECONDS.getValue()));
            MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);
            placeholders.clear();

            for (Player all : Bukkit.getOnlinePlayers())
                if (!players.contains(all)) {
                    MessagesUtil.VOTEPARTY_NOT_PARTICIPATED.msg(all);
                }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (OfflinePlayer offlinePlayer : players) {
                        if (Options.VOTEPARTY_CRATE.isEnabled()) {
                            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                                if (isInLocation(offlinePlayer.getPlayer().getLocation())) {
                                    UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
                                    userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                                    MessagesUtil.VOTEPARTY_UNCLAIM.msg(offlinePlayer.getPlayer());
                                } else
                                    offlinePlayer.getPlayer().getInventory().addItem(crate(1));
                            }
                        } else {
                            chooseRandom(Options.VOTEPARTY_RANDOM.isEnabled(), offlinePlayer);
                        }
                        if ((offlinePlayer != null && offlinePlayer.isOnline()) && Options.VOTEPARTY_SOUND_START.isEnabled()) {
                            offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), XSound
                                            .matchXSound(String.valueOf(Options.SOUND_VOTEPARTY_START.getValue())).get().parseSound(),
                                    1000, 1);
                        }
                    }
                    players.clear();
                }
            }.runTaskLaterAsynchronously(voteRewardPlugin, (long) Options.VOTEPARTY_COOLDOWN_SECONDS.getValue() * 20L);

        } else {
            for (OfflinePlayer offlinePlayer : players) {
                if (Options.VOTEPARTY_CRATE.isEnabled()) {
                    if (offlinePlayer.getPlayer() != null) {
                        if (isInLocation(offlinePlayer.getPlayer().getLocation())) {
                            UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
                            userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                            MessagesUtil.VOTEPARTY_UNCLAIM.msg(offlinePlayer.getPlayer());
                        } else
                            offlinePlayer.getPlayer().getInventory().addItem(crate(1));
                    }

                } else {
                    chooseRandom(Options.VOTEPARTY_RANDOM.isEnabled(), offlinePlayer);
                }

            }
            players.clear();
        }

        dataFile.set("VoteParty-Votes", 0);
        fm.getData().saveFile();
    }

    /**
     * Choose random voteparty rewards
     */
    public void chooseRandom(boolean enable, OfflinePlayer offlinePlayer) {
        List<String> list = Options.VOTEPARTY_REWARDS.getStringList();
        if (enable) {
            Random random = new Random();
            int selector = random.nextInt(list.size());
            runCommands(list.get(selector).replace("%player%", offlinePlayer.getName()));
        } else {
            for (String s : list) {
                runCommands(s.replace("%player%", offlinePlayer.getName()));
            }
        }
    }

    /**
     * Start the VoteParty
     */
    public void start() {
        final FileManager fm = FileManager.getInstance();

        final Map<String, String> placeholders = Maps.newHashMap();

        if (Options.VOTEPARTY_COOLDOWN.isEnabled()) {
            placeholders.put("%secs%",
                    String.valueOf(Options.VOTEPARTY_COOLDOWN_SECONDS.getValue()));
            MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);
            placeholders.clear();

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (OfflinePlayer offlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (Options.VOTEPARTY_CRATE.isEnabled()) {
                            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                                if (isInLocation(offlinePlayer.getPlayer().getLocation())) {
                                    UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
                                    userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                                    MessagesUtil.VOTEPARTY_UNCLAIM.msg(offlinePlayer.getPlayer());
                                } else
                                    offlinePlayer.getPlayer().getInventory().addItem(crate(1));

                            }
                        } else {
                            chooseRandom(Options.VOTEPARTY_RANDOM.isEnabled(), offlinePlayer);
                        }
                        if ((offlinePlayer != null && offlinePlayer.isOnline()) && Options.VOTEPARTY_SOUND_START.isEnabled()) {
                            offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), XSound
                                            .matchXSound(String.valueOf(Options.VOTEPARTY_SOUND_START.getValue())).get().parseSound(),
                                    1000, 1);
                        }
                    }
                }
            }.runTaskLaterAsynchronously(voteRewardPlugin,
                    (long) Options.VOTEPARTY_COOLDOWN_SECONDS.getValue() * 20L);

        }

    }

    public boolean isInLocation(Location location) {
        CFG cfg = FileManager.getInstance().getData();
        FileConfiguration data = cfg.getFileConfiguration();
        if (!Options.VOTEPARTY_REGIONS.isEnabled()) {
            return false;
        }
        if (data.getConfigurationSection("Regions") == null || data.getConfigurationSection("Regions").getKeys(false).isEmpty()) {
            return false;
        }
        for (String s : data.getConfigurationSection("Regions").getKeys(false)) {
            Location a = (Location) data.get("Regions." + s + ".minimumPos");
            Location b = (Location) data.get("Regions." + s + ".maximumPos");
            Regions regions = new Regions(a, b);
            return regions.locationIsInRegion(location);
        }
        return false;
    }

    public ItemStack crate(int amount) {
        ItemStack itemStack = new ItemStack(
                Objects.requireNonNull(
                        XMaterial.matchXMaterial(
                                String.valueOf(Objects.requireNonNull(Options.VOTEPARTY_CRATE_ITEM.getValue()))).get().parseMaterial()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Utils.colorize(String.valueOf(Options.VOTEPARTY_CRATE_NAME.getValue())));
        itemMeta.setLore(Utils.colorize(Options.VOTEPARTY_CRATE_LORES.getStringList()));
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);
        return itemStack;
    }

    /**
     * Run the commands from config
     *
     * @param s
     */
    private void runCommands(String s) {
        Bukkit.getScheduler().runTask(VoteRewardPlugin.getInstance(), () -> {
            if (s == null)
                return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(s));
        });
    }
}
