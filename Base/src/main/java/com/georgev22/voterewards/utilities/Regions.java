package com.georgev22.voterewards.utilities;

import org.bukkit.Location;

import java.util.UUID;

public class Regions {

    private final UUID worldUniqueId;

    private final double maxX;
    private final double maxY;
    private final double maxZ;

    private final double minX;
    private final double minY;
    private final double minZ;

    public Regions(Location firstPoint, Location secondPoint) {
        worldUniqueId = firstPoint.getWorld().getUID();

        maxX = Math.max(firstPoint.getX(), secondPoint.getX());
        maxY = Math.max(firstPoint.getY(), secondPoint.getY());
        maxZ = Math.max(firstPoint.getZ(), secondPoint.getZ());

        minX = Math.min(firstPoint.getX(), secondPoint.getX());
        minY = Math.min(firstPoint.getY(), secondPoint.getY());
        minZ = Math.min(firstPoint.getZ(), secondPoint.getZ());
    }

    public boolean locationIsInRegion(Location loc) {
        return loc.getWorld().getUID().equals(worldUniqueId) && loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

}