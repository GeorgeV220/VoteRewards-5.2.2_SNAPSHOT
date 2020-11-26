package com.georgev22.voterewards.utilities.holograms;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author GeorgeV22
 */
public class HologramUtils {

    private static final HologramManager hologramManager = new HologramManager();

    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();

    public static HologramManager getHologramManager() {
        return hologramManager;
    }

    public static Hologram create(String name, Location location, List<String> lines, boolean save) {
        return create(name, location, lines.toArray(new String[0]), save);
    }

    public static Hologram create(String name, Location location, String[] lines, boolean save) {
        Hologram hologram = hologramManager.getHologramMap().get(name);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(VoteRewardPlugin.getInstance(), location);
            hologramManager.getHologramMap().put(name, hologram);
        }

        for (String line : lines) {
            hologram.appendTextLine(line);
        }

        if (save) {
            data.set("Holograms." + name + ".location", location);
            data.set("Holograms." + name + ".lines", lines);
            dataCFG.saveFile();
        }
        return hologram;
    }

    public static void remove(String name) {
        Hologram hologram = hologramManager.getHologramMap().remove(name);

        hologram.delete();

        data.set("Holograms." + name, null);
        dataCFG.saveFile();

    }

    public static void show(String name, Player player) {
        Hologram hologram = hologramManager.getHologramMap().get(name);

        if (hologram == null) {
            Utils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }
        hologram.getVisibilityManager().showTo(player);
    }

    public static void hide(String name, Player player) {
        Hologram hologram = hologramManager.getHologramMap().get(name);

        if (hologram == null) {
            Utils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }

        hologram.getVisibilityManager().hideTo(player);
    }

    public static void show(Hologram hologram, Player player) {
        hologram.getVisibilityManager().showTo(player);
    }

    public static void hide(Hologram hologram, Player player) {
        hologram.getVisibilityManager().hideTo(player);
    }

    public static Collection<Hologram> getHolograms() {
        return hologramManager.getHologramMap().values();
    }

    public static boolean hologramExists(String name) {
        return hologramManager.getHologramMap().get(name) != null;
    }

    public static void updateHologram(Hologram hologram, String[] lines, Map<String, String> placeholders) {
        int i = 0;
        for (final String key : lines) {
            for (String placeholder : placeholders.keySet()) {
                if (key.contains(placeholder)) {
                    TextLine line = (TextLine) hologram.getLine(i);
                    line.setText(Utils.placeHolder(Utils.colorize(key), placeholders, true));
                    break;
                }
            }
            ++i;
        }
    }

}
