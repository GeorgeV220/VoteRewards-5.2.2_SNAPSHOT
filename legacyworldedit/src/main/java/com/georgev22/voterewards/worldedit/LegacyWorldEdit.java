package com.georgev22.voterewards.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class LegacyWorldEdit implements WorldEditInterface {

    private final Selection selection;

    public LegacyWorldEdit(Player player) {
        this.selection = getWorldEdit().getSelection(player);
    }

    private WorldEditPlugin getWorldEdit() {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (p instanceof WorldEditPlugin)
            return (WorldEditPlugin) p;
        else
            return null;
    }

    public Location getMinimumPoint() {
        return selection.getMinimumPoint();
    }

    public Location getMaximumPoint() {
        return selection.getMaximumPoint();
    }


}
