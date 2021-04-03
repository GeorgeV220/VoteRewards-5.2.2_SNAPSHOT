package com.georgev22.voterewards.hooks;

import com.georgev22.interfaces.WorldEditInterface;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.worldedit.LegacyWorldEdit;
import com.georgev22.worldedit.NewWorldEdit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author GeorgeV22
 */
public class WorldEditHook {

    private final WorldEditInterface worldEditInterface;

    public WorldEditHook(Player player) {
        if (Utils.isLegacy()) {
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
