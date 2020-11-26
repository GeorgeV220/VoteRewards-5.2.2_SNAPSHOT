package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.holograms.HologramUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

/**
 * @author GeorgeV22
 */
public class Holograms extends BukkitCommand {

    public Holograms() {
        super("hologram");
        this.description = "hologram command";
        this.usageMessage = "/hologram";
        this.setPermission("voterewards.hologram");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrhologram", "vrh"));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length == 0) {
            Utils.msg(sender, "Arguments");
            return true;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 1) {
                Utils.msg(player, "/hologram create <hologramName> <lines>");
                return true;
            }

            if (HologramUtils.hologramExists(args[1])) {
                Utils.msg(sender, "Hologram already exists!");
                return true;
            }

            HologramUtils.show(HologramUtils.create(args[1], player.getLocation(), Utils.getArguments(args, 2), true), player);

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                Utils.msg(player, "/hologram remove <hologramName>");
                return true;
            }

            if (!HologramUtils.hologramExists(args[1])) {
                Utils.msg(sender, "Hologram doesn't exists!");
                return true;
            }

            HologramUtils.remove(args[1]);
        } else if (args[0].equalsIgnoreCase("update")) {
            if (HologramUtils.hologramExists(args[1])) {
                Hologram hologram = HologramUtils.getHologramManager().getHologramMap().get(args[1]);
                if (hologram == null) {
                    Bukkit.broadcastMessage("null");
                    return true;
                }
                String[] lines = {"%top1%", "%top2%", "%top3%", "%top4%", "%top5%"};
                final Map<String, String> map = Maps.newHashMap();
                map.put("%top1%", Utils.getTopPlayer());
                map.put("%top2%", Utils.getTopPlayer(2));
                map.put("%top3%", Utils.getTopPlayer(3));
                map.put("%top4%", Utils.getTopPlayer(4));
                map.put("%top5%", Utils.getTopPlayer(5));
                HologramUtils.updateHologram(hologram, lines, map);
                map.clear();
            }
        }
        return true;
    }

}
