package com.georgev22.voterewards.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class NewWorldEdit implements WorldEditInterface {

    private final Location minimumPoint;
    private final Location maximumPoint;

    public NewWorldEdit(Player player) {
        Region selection = null;
        try {
            selection = getWorldEdit().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (Exception ignored) {
        }

        this.minimumPoint = selection != null ? BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint()) : null;
        this.maximumPoint = selection != null ? BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint()) : null;
    }

    public Location getMinimumPoint() {
        return this.minimumPoint;
    }

    public Location getMaximumPoint() {
        return this.maximumPoint;
    }

    private WorldEditPlugin getWorldEdit() {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (p instanceof WorldEditPlugin)
            return (WorldEditPlugin) p;
        else
            return null;
    }
}
