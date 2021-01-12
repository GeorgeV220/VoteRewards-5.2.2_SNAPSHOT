package com.georgev22.voterewards.listeners;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.options.PartyOptions;
import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.georgev22.xseries.XMaterial;
import com.georgev22.xseries.XSound;
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

    private final VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        //HOLOGRAMS
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (!HolographicDisplays.getHolograms().isEmpty()) {
                for (Hologram hologram : HolographicDisplays.getHolograms()) {
                    HolographicDisplays.show(hologram, event.getPlayer());
                }

                HolographicDisplays.updateAll();
            }
        }

        UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
        final Stopwatch sw = Stopwatch.createStarted();
        userVoteData.load(new UserVoteData.Callback() {
            @Override
            public void onSuccess() {
                //OFFLINE VOTING
                if (VoteOptions.OFFLINE.isEnabled() && !Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                    for (String serviceName : userVoteData.getOfflineServices()) {
                        VoteUtils.processVote(event.getPlayer(), serviceName);
                    }
                    userVoteData.setOfflineServices(Lists.newArrayList());
                }
                userVoteData.save();

                if (!UserVoteData.getAllUsersMap().containsKey(event.getPlayer().getName())) {
                    UserVoteData.getAllUsersMap().put(event.getPlayer().getName(), userVoteData.getVotes());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        final long elapsedMillis = sw.elapsed(TimeUnit.MILLISECONDS);
        if (VoteOptions.DEBUG_LOAD.isEnabled()) {
            Utils.debug(VoteRewardPlugin.getInstance(), "Elapsed time to load user data: " + elapsedMillis);
        }

        //UPDATER
        if (VoteOptions.UPDATER.isEnabled()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }

        m.reminderMap.put(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserVoteData.getUser(event.getPlayer().getUniqueId()).save();
        m.reminderMap.remove(event.getPlayer());
        UserVoteData.getUserMap().remove(event.getPlayer().getUniqueId());
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

        if (item.getType() != XMaterial.matchXMaterial(Objects.requireNonNull(m.getConfig().getString("VoteParty.crate.item"))).get()
                .parseMaterial()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!(meta != null && meta.hasDisplayName())) {
            return;
        }

        final String itemName = m.getConfig().getString("VoteParty.crate.name");
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

        VotePartyUtils.getInstance().chooseRandom(PartyOptions.RANDOM.isEnabled(), p);

        if (PartyOptions.SOUND_CRATE.isEnabled()) {
            p.playSound(p.getLocation(), Objects.requireNonNull(XSound.matchXSound(Objects.requireNonNull(m.getConfig().getString("Sounds.Crate"))).get().parseSound()), 1000, 1);
        }

        event.setCancelled(true);

    }
}
