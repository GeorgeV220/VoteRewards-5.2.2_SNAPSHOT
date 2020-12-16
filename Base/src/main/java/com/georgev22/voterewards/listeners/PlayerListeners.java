package com.georgev22.voterewards.listeners;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.options.PartyOptions;
import com.georgev22.voterewards.utilities.options.VoteOptions;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.xseries.XMaterial;
import com.georgev22.xseries.XSound;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class PlayerListeners implements Listener {

    private final VoteRewardPlugin plugin;

    public PlayerListeners(final VoteRewardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        UserVoteData.getUser(event.getPlayer().getUniqueId()).setupUser();
        if (VoteOptions.UPDATER.isEnabled()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (HolographicDisplays.getHolograms().isEmpty())
                return;
            for (Hologram hologram : HolographicDisplays.getHolograms()) {
                HolographicDisplays.show(hologram, event.getPlayer());
            }

            //HOLOGRAM UPDATE
            HolographicDisplays.updateAll();
        }
        //OFFLINE VOTING
        if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
            return;
        }
        if (VoteOptions.OFFLINE.isEnabled()) {
            UserVoteData userVoteData = UserVoteData.getUser(event.getPlayer().getUniqueId());
            for (String serviceName : userVoteData.getServices()) {
                userVoteData.processVote(serviceName);
            }
            userVoteData.setOfflineServices(Lists.newArrayList());
        }
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

        if (item.getType() != XMaterial.matchXMaterial(Objects.requireNonNull(plugin.getConfig().getString("VoteParty.crate.item"))).get()
                .parseMaterial()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!(meta != null && meta.hasDisplayName())) {
            return;
        }

        final String itemName = plugin.getConfig().getString("VoteParty.crate.name");
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

            p.playSound(p.getLocation(), Objects.requireNonNull(XSound.matchXSound(Objects.requireNonNull(plugin.getConfig().getString("Sounds.Crate"))).get().parseSound()), 1000, 1);
        }
        event.setCancelled(true);

    }
}
