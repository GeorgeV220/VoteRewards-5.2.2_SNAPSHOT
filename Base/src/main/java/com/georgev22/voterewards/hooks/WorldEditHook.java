package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.worldedit.LegacyWorldEdit;
import com.georgev22.voterewards.worldedit.NewWorldEdit;
import com.georgev22.voterewards.worldedit.WorldEditInterface;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author GeorgeV22
 */
public class WorldEditHook {

    private WorldEditInterface worldEditInterface;

    public WorldEditHook(Player player) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        if (version.contains("1_8") || version.contains("1_9") || version.contains("1_11") || version.contains("1_12")) {
            worldEditInterface = new LegacyWorldEdit(player);
        } else {
            worldEditInterface = new NewWorldEdit(player);
        }
    }

    public Location getMinimumPoint() {
        return worldEditInterface.getMinimumPoint();
    }

    public Location getMaximumPoint() {
        return worldEditInterface.getMaximumPoint();
    }
}
