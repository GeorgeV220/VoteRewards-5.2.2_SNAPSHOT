package com.georgev22.voterewards.listeners;

import com.cryptomorin.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.*;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;
import java.util.Set;

public class PlayerListeners implements Listener {

    private final VoteRewardPlugin plugin;

    public PlayerListeners(final VoteRewardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        UserVoteData.getUser(event.getPlayer().getUniqueId()).setupUser();
        if (plugin.getConfig().getBoolean("Options.updater")) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }
    }

    /*@EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase("[VoteRewards]")) {
            if (e.getLine(1).isEmpty()) {
            }

        }
    }*/

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

        if (item.getType() != MaterialUtil.valueOf(plugin.getConfig().getString("VoteParty.crate.item"))
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

            // SoundUtil.valueOf(plugin.getConfig().getString("Sounds.Crate")).getSound()

            XSound.matchXSound(plugin.getConfig().getString("Sounds.Crate"))
                    .ifPresent(sound -> p.playSound(p.getLocation(), sound.parseSound(), 1000, 1));

        }
        event.setCancelled(true);

    }
}