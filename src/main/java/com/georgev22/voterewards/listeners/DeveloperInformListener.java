package com.georgev22.voterewards.listeners;

import java.util.List;
import java.util.UUID;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.OptionsUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.collect.Lists;


public class DeveloperInformListener implements Listener {

    private final List<ObjectMap.Pair<String, UUID>> inform = Lists.newArrayList(
            ObjectMap.Pair.create("Shin1gamiX", UUID.fromString("7cc1d444-fe6f-4063-a426-b62fdfea7dab")),
            ObjectMap.Pair.create("GeorgeV22", UUID.fromString("a4f5cd7f-362f-4044-931e-7128b4e6bad9"))
    );

    @EventHandler
    private void onJoin(final PlayerJoinEvent e) {
        final OfflinePlayer player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();

        final ObjectMap.Pair<String, UUID> pair = ObjectMap.Pair.create(name, uuid);

        boolean found = false;

        for (ObjectMap.Pair<String, UUID> loop : this.inform) {
            if (loop.getKey().equals(pair.getKey())) {
                found = true;
                break;
            }
            if (loop.getValue().equals(pair.getValue())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(VoteRewardPlugin.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }

            MinecraftUtils.msg(player.getPlayer(), joinMessage, ObjectMap.newHashObjectMap()
                    .append("%player%", e.getPlayer().getName())
                    .append("%version%", VoteRewardPlugin.getInstance().getDescription().getVersion())
                    .append("%package%", VoteRewardPlugin.getInstance().getClass().getPackage().getName())
                    .append("%name%", VoteRewardPlugin.getInstance().getDescription().getName())
                    .append("%author%", String.join(", ", VoteRewardPlugin.getInstance().getDescription().getAuthors()))
                    .append("%main%", VoteRewardPlugin.getInstance().getDescription().getMain())
                    .append("%javaversion%", System.getProperty("java.version"))
                    .append("%serverversion%", MinecraftUtils.MinecraftVersion.getCurrentVersion().name()), false);
        }, 20L * 10L);

    }

    private final static List<String> joinMessage = Lists.newArrayList(

            "",

            "",

            "&7Hey &f%player%&7, details are listed below.",

            "&7Version: &c%version%",

            "&7Java Version: &c%javaversion%",

            "&7Server Version: &c%serverversion%",

            "&7Name: &c%name%",

            "&7Author: &c%author%",

            "&7Main package: &c%package%",

            "&7Main path: &c%main%",

            "&7Experimental Features: &c" + OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue(),

            ""

    );

}
