package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

/**
 * @author GeorgeV22
 */
public class HolographicDisplays {

    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();
    private final static VoteRewardPlugin m = VoteRewardPlugin.getInstance();
    private static final ObjectMap<String, Hologram> hologramMap = ObjectMap.newConcurrentObjectMap();

    public static Hologram create(String name, Location location, String type, boolean save) {
        Hologram hologram = getHologramMap().get(name);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(VoteRewardPlugin.getInstance(), location);
            getHologramMap().append(name, hologram);
        }

        for (String line : fileManager.getConfig().getFileConfiguration().getStringList("Holograms." + type)) {
            hologram.appendTextLine(Utils.colorize(line));
        }

        if (save) {
            data.set("Holograms." + name + ".location", location);
            data.set("Holograms." + name + ".type", type);
            dataCFG.saveFile();
        }
        return hologram;
    }

    public static void remove(String name, boolean save) {
        Hologram hologram = getHologramMap().remove(name);

        hologram.delete();

        if (save) {
            data.set("Holograms." + name, null);
            dataCFG.saveFile();
        }
    }

    public static void show(String name, Player player) {
        Hologram hologram = getHologramMap().get(name);

        if (hologram == null) {
            Utils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }
        hologram.getVisibilityManager().showTo(player);
    }

    public static void hide(String name, Player player) {
        Hologram hologram = getHologramMap().get(name);

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
        return getHologramMap().values();
    }

    public static Hologram getHologram(String name) {
        return getHologramMap().get(name);
    }

    public static boolean hologramExists(String name) {
        return getHologramMap().get(name) != null;
    }

    public static Hologram updateHologram(Hologram hologram, String[] lines, ObjectMap<String, String> placeholders) {
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
        return hologram;
    }

    public static void updateAll() {
        if (data.get("Holograms") == null)
            return;
        for (String hologramName : data.getConfigurationSection("Holograms").getKeys(false)) {
            Hologram hologram = getHologram(hologramName);
            HolographicDisplays.updateHologram(hologram, m.getConfig().getStringList("Holograms." + data.getString("Holograms." + hologramName + ".type")).toArray(new String[0]), getPlaceholderMap());
            getPlaceholderMap().clear();
        }
    }

    public static ObjectMap<String, Hologram> getHologramMap() {
        return hologramMap;
    }

    public static ObjectMap<String, String> getPlaceholderMap() {
        final ObjectMap<String, String> map = ObjectMap.newHashObjectMap();
        int i = 1;
        for (Map.Entry<String, Integer> b : VoteUtils.getTopPlayers(Options.VOTETOP_VOTERS.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%top-" + i + "%", args[0]).append("%vote-" + i + "%", args[1]);
            i++;
        }
        map.append("%bar%", Utils.getProgressBar(
                data.getInt("VoteParty-Votes"),
                Options.VOTEPARTY_VOTES.getIntValue(),
                Options.VOTEPARTY_BARS.getIntValue(),
                (String) Options.VOTEPARTY_BAR_SYMBOL.getValue(),
                (String) Options.VOTEPARTY_COMPLETE_COLOR.getValue(),
                (String) Options.VOTEPARTY_NOT_COMPLETE_COLOR.getValue()));
        return map;
    }

}
