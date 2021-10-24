package com.georgev22.voterewards.hooks;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.worldedit.LegacyWorldEdit;
import com.georgev22.worldedit.NewWorldEdit;
import com.georgev22.worldedit.WorldEditInterface;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author GeorgeV22
 */
public class WorldEditHook {

    private final WorldEditInterface worldEditInterface;

    public WorldEditHook(Player player) {
        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelowOrEqual(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
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
