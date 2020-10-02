package com.georgev22.voterewards.utilities;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class TitleActionbarUtil {

    private NMSUtils nmsUtils = new NMSUtils();
    private Player player;

    public TitleActionbarUtil(Player player) {
        this.player = player;
    }

    public void sendTitle(String s, String b) {
        try {

            Object enumTitle = nmsUtils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            Object titleChat = nmsUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + s + "\"}");

            Constructor<?> titleConstructor = nmsUtils.getNMSClass("PacketPlayOutTitle").getConstructor(nmsUtils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], nmsUtils.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object titlePacket = titleConstructor.newInstance(enumTitle, titleChat, 30, 50, 30);

            Object subtitlePacket = null;
            nmsUtils.sendPacket(player, titlePacket);
            if (b != null) {
                Object enumSubtitle = nmsUtils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
                Object subtitleChat = nmsUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + b + "\"}");
                subtitlePacket = titleConstructor.newInstance(enumSubtitle, subtitleChat, 30, 40, 30);
                nmsUtils.sendPacket(player, subtitlePacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendActionbar(String s) {
        try {
            Constructor<?> constructor = nmsUtils.getNMSClass("PacketPlayOutChat").getConstructor(nmsUtils.getNMSClass("IChatBaseComponent"), byte.class);

            Object icbc = nmsUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + s + "\"}");
            Object packet = constructor.newInstance(icbc, (byte) 2);
            nmsUtils.sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
