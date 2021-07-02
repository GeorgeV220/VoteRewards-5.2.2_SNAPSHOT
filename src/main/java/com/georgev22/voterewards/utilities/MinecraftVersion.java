package com.georgev22.voterewards.utilities;

import org.bukkit.Bukkit;

import java.io.File;

public enum MinecraftVersion {
    UNKNOWN,
    V1_8_R1,
    V1_8_R2,
    V1_8_R3,
    V1_9_R1,
    V1_9_R2,
    V1_10_R1,
    V1_11_R1,
    V1_12_R1,
    V1_13_R1,
    V1_13_R2,
    V1_14_R1,
    V1_15_R1,
    V1_16_R1,
    V1_16_R2,
    V1_16_R3,
    V1_17_R1;

    private static MinecraftVersion currentVersion;

    public boolean isAboveOrEqual(MinecraftVersion minecraftVersion) {
        return this.ordinal() >= minecraftVersion.ordinal();
    }

    public boolean isAbove(MinecraftVersion minecraftVersion) {
        return this.ordinal() > minecraftVersion.ordinal();
    }

    public boolean isBelowOrEqual(MinecraftVersion minecraftVersion) {
        return this.ordinal() <= minecraftVersion.ordinal();
    }

    public boolean isBelow(MinecraftVersion minecraftVersion) {
        return this.ordinal() < minecraftVersion.ordinal();
    }

    public static MinecraftVersion getCurrentVersion() {
        return currentVersion;
    }

    static {
        try {
            File serverVersion = new File("server.properties");
            File bukkitVersion = new File("bukkit.yml");
            if (serverVersion.exists() && bukkitVersion.exists()) {
                currentVersion = valueOf(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].toUpperCase());
            } else {
                currentVersion = UNKNOWN;
            }
        } catch (Exception var2) {
            currentVersion = UNKNOWN;
        }

    }
}
