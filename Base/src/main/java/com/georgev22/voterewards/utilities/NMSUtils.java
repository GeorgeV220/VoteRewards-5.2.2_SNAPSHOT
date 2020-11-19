package com.georgev22.voterewards.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMSUtils {
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("An error occurred while finding NMS class.", e);
        }
    }

    public static Class<?> getOBCClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("An error occurred while finding OBC class.", ex);
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getHandle(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return object.getClass().getMethod("getHandle").invoke(object);
    }
}
