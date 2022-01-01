package com.georgev22.voterewards.listeners;

import com.georgev22.api.externals.xseries.XMaterial;
import com.georgev22.api.externals.xseries.XSound;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.georgev22.api.utilities.Utils.*;

public class PlayerListeners implements Listener {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLogin(PlayerPreLoginEvent event) {
        if (MinecraftUtils.isLoginDisallowed())
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, MinecraftUtils.colorize(MinecraftUtils.getDisallowLoginMessage()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
        final Stopwatch sw = Stopwatch.createStarted();
        try {
            userVoteData.load(new Callback() {
                @Override
                public void onSuccess() {
                    //OFFLINE VOTING
                    if (OptionsUtil.OFFLINE.getBooleanValue() && !Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                        for (String serviceName : userVoteData.getOfflineServices()) {
                            try {
                                new VoteUtils(userVoteData.user()).processVote(serviceName, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        userVoteData.setOfflineServices(Lists.newArrayList());
                    }

                    UserVoteData.getAllUsersMap().append(userVoteData.user().getUniqueId(), userVoteData.user());
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
        if (OptionsUtil.DEBUG_LOAD.getBooleanValue()) {
            MinecraftUtils.debug(VoteRewardPlugin.getInstance(), "Elapsed time to load user data: " + elapsedMillis);
        }

        //UPDATER
        if (OptionsUtil.UPDATER.getBooleanValue()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }

        if (OptionsUtil.REMINDER.getBooleanValue())
            VoteUtils.reminderMap.append(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
        userVoteData.save(true, new Callback() {
            @Override
            public void onSuccess() {
                UserVoteData.getAllUsersMap().append(userVoteData.user().getUniqueId(), userVoteData.user());
                if (OptionsUtil.REMINDER.getBooleanValue())
                    VoteUtils.reminderMap.remove(event.getPlayer());
                if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                    MinecraftUtils.debug(voteRewardPlugin, "User " + event.getPlayer().getName() + " saved!",
                            userVoteData.user().toString());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }


    private final Set<Action> clicks = EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);

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

        if (!meta.getDisplayName().equals(MinecraftUtils.colorize(itemName))) {
            return;
        }

        final int amount = item.getAmount();

        if (amount == 1) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        } else {
            item.setAmount(amount - 1);
        }

        new VotePartyUtils(player).chooseRandom(OptionsUtil.VOTEPARTY_RANDOM.getBooleanValue());

        if (OptionsUtil.VOTEPARTY_SOUND_CRATE.getBooleanValue()) {
            if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).get().parseSound(),
                        1000, 1);
                if (OptionsUtil.DEBUG_USELESS.getBooleanValue()) {
                    MinecraftUtils.debug(voteRewardPlugin, "========================================================");
                    MinecraftUtils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    MinecraftUtils.debug(voteRewardPlugin, "SoundCategory doesn't exists in versions below 1.12");
                    MinecraftUtils.debug(voteRewardPlugin, "========================================================");
                }
            } else {
                player.getPlayer().playSound(player.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).get().parseSound(),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_CRATE_OPEN_CHANNEL.getStringValue()),
                        1000, 1);
            }
        }

        event.setCancelled(true);

    }
}
