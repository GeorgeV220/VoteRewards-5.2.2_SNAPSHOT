package com.georgev22.voterewards.listeners;

import com.georgev22.externals.xseries.XMaterial;
import com.georgev22.externals.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.Callback;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerListeners implements Listener {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
        final Stopwatch sw = Stopwatch.createStarted();
        try {
            userVoteData.load(new Callback() {
                @Override
                public void onSuccess() {
                    //OFFLINE VOTING
                    if (OptionsUtil.OFFLINE.isEnabled() && !Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                        for (String serviceName : userVoteData.getOfflineServices()) {
                            new VoteUtils().processVote(event.getPlayer(), serviceName, false);
                        }
                        userVoteData.setOfflineServices(Lists.newArrayList());
                    }
                    userVoteData.save(true);

                    UserVoteData.getAllUsersMap().append(event.getPlayer().getUniqueId(), userVoteData.getUser());
                    //HOLOGRAMS
                    if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                        if (!HolographicDisplays.getHolograms().isEmpty()) {
                            for (Hologram hologram : HolographicDisplays.getHolograms()) {
                                HolographicDisplays.show(hologram, event.getPlayer());
                            }

                            HolographicDisplays.updateAll();
                        }
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        final long elapsedMillis = sw.elapsed(TimeUnit.MILLISECONDS);
        if (OptionsUtil.DEBUG_LOAD.isEnabled()) {
            Utils.debug(VoteRewardPlugin.getInstance(), "Elapsed time to load user data: " + elapsedMillis);
        }

        //UPDATER
        if (OptionsUtil.UPDATER.isEnabled()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }

        VoteUtils.reminderMap.append(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserVoteData.getUser(event.getPlayer().getUniqueId()).save(true);
        VoteUtils.reminderMap.remove(event.getPlayer());
    }


    private final Set<Action> clicks = EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!clicks.contains(event.getAction())) {
            return;
        }

        final Player player = event.getPlayer();

        final ItemStack item = player.getInventory().getItemInHand();

        if (item.getType() != XMaterial.matchXMaterial(Objects.requireNonNull(OptionsUtil.VOTEPARTY_CRATE_ITEM.getStringValue())).get()
                .parseMaterial()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!(meta != null && meta.hasDisplayName())) {
            return;
        }

        final String itemName = OptionsUtil.VOTEPARTY_CRATE_NAME.getStringValue();
        if (itemName == null) {
            return;
        }

        if (!meta.getDisplayName().equals(Utils.colorize(itemName))) {
            return;
        }

        final int amount = item.getAmount();

        if (amount == 1) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        } else {
            item.setAmount(amount - 1);
        }

        VotePartyUtils.getInstance().chooseRandom(OptionsUtil.VOTEPARTY_RANDOM.isEnabled(), player);

        if (OptionsUtil.VOTEPARTY_SOUND_CRATE.isEnabled()) {
            try {
                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).get().parseSound(),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_CRATE_OPEN_CHANNEL.getStringValue()),
                        1000, 1);
            } catch (NoClassDefFoundError error) {
                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).get().parseSound(),
                        1000, 1);
                if (OptionsUtil.DEBUG_USELESS.isEnabled()) {
                    Utils.debug(voteRewardPlugin, "========================================================");
                    Utils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    error.printStackTrace();
                    Utils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    Utils.debug(voteRewardPlugin, "========================================================");
                }
            }
        }

        event.setCancelled(true);

    }
}
