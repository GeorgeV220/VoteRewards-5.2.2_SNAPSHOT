package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author GeorgeV22
 */
public class Holograms extends BukkitCommand {

    private final VoteRewardPlugin m = VoteRewardPlugin.getInstance();
    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();

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
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) return true;
        if (args.length == 0) {
            Utils.msg(sender, "&c&l(!) &cNot enough arguments!");
            return true;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                Utils.msg(player, "&c&l(!) &cUsage: /hologram create <hologramName> <type>");
                return true;
            }

            if (HolographicDisplays.hologramExists(args[1])) {
                Utils.msg(sender, "&c&l(!) &cHologram already exists!");
                return true;
            }

            if (m.getConfig().get("Holograms." + args[2]) != null) {
                Utils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                return true;
            }

            HolographicDisplays.show(HolographicDisplays.updateHologram(HolographicDisplays.create(
                    args[1],
                    player.getLocation(),
                    args[2], true),
                    m.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap()),
                    player);

            HolographicDisplays.getPlaceholderMap().clear();

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                Utils.msg(player, "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return true;
            }

            if (!HolographicDisplays.hologramExists(args[1])) {
                Utils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                return true;
            }

            HolographicDisplays.remove(args[1]);
        } else if (args[0].equalsIgnoreCase("update")) {
            if (HolographicDisplays.hologramExists(args[1])) {
                Hologram hologram = HolographicDisplays.getHologramMap().get(args[1]);
                //String[] lines = {"%top1%", "%top2%", "%top3%", "%top4%", "%top5%"};
                HolographicDisplays.updateHologram(hologram, m.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap());
                HolographicDisplays.getPlaceholderMap().clear();
                HolographicDisplays.hide(hologram, player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(m, () -> HolographicDisplays.show(hologram, player), 20);
                Utils.msg(player, args[1] + " " + args[2]);
            } else {
                Utils.msg(player, "&c&l(!) &cHologram doesn't exists!");
            }
        }
        return true;
    }
}
