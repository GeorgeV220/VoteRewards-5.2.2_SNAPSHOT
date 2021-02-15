package com.georgev22.voterewards.listeners;

import com.georgev22.externals.xseries.XMaterial;
import com.georgev22.externals.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.Options;
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
                    if (Options.OFFLINE.isEnabled() && !Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                        for (String serviceName : userVoteData.getOfflineServices()) {
                            VoteUtils.processVote(event.getPlayer(), serviceName);
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
        if (Options.DEBUG_LOAD.isEnabled()) {
            Utils.debug(VoteRewardPlugin.getInstance(), "Elapsed time to load user data: " + elapsedMillis);
        }

        //UPDATER
        if (Options.UPDATER.isEnabled()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }

        voteRewardPlugin.reminderMap.append(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserVoteData.getUser(event.getPlayer().getUniqueId()).save(true);
        voteRewardPlugin.reminderMap.remove(event.getPlayer());
    }


    private final Set<Action> clicks = EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!clicks.contains(event.getAction())) {
            return;
        }

        final Player p = event.getPlayer();

        final ItemStack item = p.getInventory().getItemInHand();

        if (item.getType() != XMaterial.matchXMaterial(String.valueOf(Objects.requireNonNull(Options.VOTEPARTY_CRATE_ITEM.getValue()))).get()
                .parseMaterial()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!(meta != null && meta.hasDisplayName())) {
            return;
        }

        final String itemName = String.valueOf(Options.VOTEPARTY_CRATE_NAME.getValue());
        if (itemName == null) {
            return;
        }

        if (!meta.getDisplayName().equals(Utils.colorize(itemName))) {
            return;
        }

        final int amount = item.getAmount();

        if (amount == 1) {
            p.getInventory().clear(p.getInventory().getHeldItemSlot());
        } else {
            item.setAmount(amount - 1);
        }

        VotePartyUtils.getInstance().chooseRandom(Options.VOTEPARTY_RANDOM.isEnabled(), p);

        if (Options.VOTEPARTY_SOUND_CRATE.isEnabled()) {
            p.playSound(p.getLocation(), XSound.matchXSound(String.valueOf(Options.SOUND_VOTEPARTY_START.getValue())).get().parseSound(), 1000, 1);
        }

        event.setCancelled(true);

    }
}
