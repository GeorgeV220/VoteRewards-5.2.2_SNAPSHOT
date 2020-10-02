package com.georgev22.voterewards.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class LegacyWorldEdit implements WorldEditInterface {

    private Selection selection;

    public LegacyWorldEdit(Player player) {
        this.selection = Objects.requireNonNull(getWorldEdit()).getSelection(player);
        if (selection == null) {
            player.sendMessage("&c&l(!) &cPlease make a selection first!");
        }
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
