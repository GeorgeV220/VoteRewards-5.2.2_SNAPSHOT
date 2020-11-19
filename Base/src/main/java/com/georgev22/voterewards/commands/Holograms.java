package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.HologramManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author GeorgeV22
 */
public class Holograms extends BukkitCommand {

    private HologramManager hologramManager = new HologramManager();

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

            Hologram hologram = hologramManager.getHologramMap().get(args[1]);
            if (hologram == null) {
                hologram = HologramsAPI.createHologram(VoteRewardPlugin.getInstance(), player.getLocation());
                hologramManager.getHologramMap().put(args[1], hologram);
            }

            for (String text : Utils.getArguments(args, 2)) {
                hologram.appendTextLine(text);
            }

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                Utils.msg(player, "/hologram remove <hologramName>");
                return true;
            }

            Hologram hologram = hologramManager.getHologramMap().remove(args[1]);

            if (hologram == null) {
                Utils.msg(player, "Hologram " + args[1] + " doesn't exist");
                return true;
            }

            hologram.delete();

        }
        return true;
    }

}
