package com.georgev22.voterewards.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class NewWorldEdit implements WorldEditInterface {

    private Location minimumPoint;
    private Location maximumPoint;

    public NewWorldEdit(Player player) {
        Region selection = null;
        try {
            selection = Objects.requireNonNull(getWorldEdit()).getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        if (selection == null) {
            return;
        }
        this.minimumPoint = BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint());
        this.maximumPoint = BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint());
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
