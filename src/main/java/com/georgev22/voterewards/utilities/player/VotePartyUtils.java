package com.georgev22.voterewards.utilities.player;

import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.externals.xseries.XMaterial;
import com.georgev22.api.externals.xseries.XSound;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.utilities.Utils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.*;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VotePartyUtils {

    private final OfflinePlayer offlinePlayer;

    public VotePartyUtils(@Nullable OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;

    }

    private static final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private static final List<OfflinePlayer> players = Lists.newArrayList();

    private static final ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();

    private static final FileManager fm = FileManager.getInstance();

    private static final FileConfiguration dataFile = fm.getData().getFileConfiguration();

    private static boolean isWaitingForPlayers = false;

    /**
     * Checks if voteparty is waiting for more players.
     *
     * @return if voteparty is waiting for more players in order to start.
     * @since v5.0
     */
    public static boolean isWaitingForPlayers() {
        return isWaitingForPlayers;
    }

    /**
     * @param start Set true to start the voteparty without the required votes.
     */
    public void run(boolean start) {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            if ((!start & OptionsUtil.VOTEPARTY_PLAYERS.getBooleanValue()) & Bukkit.getOnlinePlayers().size() > OptionsUtil.VOTEPARTY_PLAYERS_NEED.getIntValue()) {
                MessagesUtil.VOTEPARTY_NOT_ENOUGH_PLAYERS.msgAll();
                isWaitingForPlayers = true;
                return;
            } else {
                isWaitingForPlayers = false;
            }

            if (!start) {
                dataFile.set("VoteParty-Votes", dataFile.getInt("VoteParty-Votes", 0) + 1);
                fm.getData().saveFile();
            }

            int votesThatNeed = OptionsUtil.VOTEPARTY_VOTES.getIntValue();
            int currentVotes = dataFile.getInt("VoteParty-Votes", 0);

            if (!start) {
                if (OptionsUtil.MESSAGE_VOTEPARTY.getBooleanValue()) {
                    if (votesThatNeed - currentVotes > 0) {
                        placeholders.append("%votes%", Utils.formatNumber(votesThatNeed - currentVotes));
                        MessagesUtil.VOTEPARTY_VOTES_NEED.msgAll(placeholders, true);
                        placeholders.clear();
                    }
                }

                if (offlinePlayer != null & OptionsUtil.VOTEPARTY_PARTICIPATE.getBooleanValue()) {
                    if (!players.contains(offlinePlayer))
                        players.add(offlinePlayer);
                }

                if (currentVotes < votesThatNeed) {
                    return;
                }
            }

            //RESET VOTEPARTY COUNT
            if (!start) {
                dataFile.set("VoteParty-Votes", 0);
                fm.getData().saveFile();
            }

            //PARTICIPATE MESSAGE
            if (OptionsUtil.VOTEPARTY_PARTICIPATE.getBooleanValue() & !start) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!players.contains(player)) {
                        MessagesUtil.VOTEPARTY_NOT_PARTICIPATED.msg(player);
                    }
                });
            } else {
                players.addAll(Bukkit.getOnlinePlayers());
            }

            //COOLDOWN
            if (OptionsUtil.VOTEPARTY_COOLDOWN.getBooleanValue()) {
                placeholders.append("%secs%",
                        String.valueOf(OptionsUtil.VOTEPARTY_COOLDOWN_SECONDS.getIntValue()));
                MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);

                try {
                    TimeUnit.SECONDS.sleep(OptionsUtil.VOTEPARTY_COOLDOWN_SECONDS.getIntValue());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //REWARD
            players.forEach(player -> {
                UserVoteData userVoteData = UserVoteData.getUser(player);
                if (OptionsUtil.VOTEPARTY_CRATE.getBooleanValue()) {
                    if (player.isOnline()) {
                        if (OptionsUtil.VOTEPARTY_SOUND_START.getBooleanValue()) {
                            if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
                                if (OptionsUtil.DEBUG_USELESS.getBooleanValue()) {
                                    MinecraftUtils.debug(voteRewardPlugin, "========================================================");
                                    MinecraftUtils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                                    MinecraftUtils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                                    MinecraftUtils.debug(voteRewardPlugin, "========================================================");
                                }
                                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                                .matchXSound(OptionsUtil.SOUND_VOTEPARTY_START.getStringValue()).get().parseSound(),
                                        1000, 1);
                            } else {
                                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                                .matchXSound(OptionsUtil.SOUND_VOTEPARTY_START.getStringValue()).get().parseSound(),
                                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_VOTEPARTY_START_CHANNEL.getStringValue()),
                                        1000, 1);
                            }
                        }
                        if (isInLocation(player.getPlayer().getLocation())) {
                            userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                            MessagesUtil.VOTEPARTY_UNCLAIMED.msg(player.getPlayer());
                        } else {
                            player.getPlayer().getInventory().addItem(crate(1));
                        }
                    } else {
                        userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                    }
                } else {
                    chooseRandom(OptionsUtil.VOTEPARTY_RANDOM.getBooleanValue());
                }
            });

            placeholders.clear();
            players.clear();
        });
    }

    /**
     * Choose a random voteparty reward
     *
     * @param enable The boolean of random rewards
     */
    public void chooseRandom(boolean enable) {
        List<String> list = OptionsUtil.VOTEPARTY_REWARDS.getStringList();
        if (enable) {
            Random random = new Random();
            int selector = random.nextInt(list.size());
            MinecraftUtils.runCommand(voteRewardPlugin, list.get(selector).replace("%player%", offlinePlayer.getName()));
        } else {
            for (String s : list) {
                MinecraftUtils.runCommand(voteRewardPlugin, s.replace("%player%", offlinePlayer.getName()));
            }
        }
    }

    /**
     * Returns true if a specific location is inside a region.
     *
     * @param location Location to check if is inside on a region.
     * @return true if a specific location is inside a region.
     */
    public boolean isInLocation(Location location) {
        CFG cfg = FileManager.getInstance().getData();
        FileConfiguration data = cfg.getFileConfiguration();
        if (!OptionsUtil.VOTEPARTY_REGIONS.getBooleanValue()) {
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

    /**
     * Returns a custom item crate
     *
     * @param amount item amount
     * @return a custom item crate
     */
    public static @NotNull ItemStack crate(int amount) {
        ItemStack itemStack = new ItemStack(
                Objects.requireNonNull(
                        XMaterial.matchXMaterial(
                                Objects.requireNonNull(OptionsUtil.VOTEPARTY_CRATE_ITEM.getStringValue())).get().parseMaterial()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(MinecraftUtils.colorize(OptionsUtil.VOTEPARTY_CRATE_NAME.getStringValue()));
        itemMeta.setLore(MinecraftUtils.colorize(OptionsUtil.VOTEPARTY_CRATE_LORES.getStringList()));
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);
        return itemStack;
    }

}
