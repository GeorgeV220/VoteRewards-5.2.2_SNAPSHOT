package com.georgev22.voterewards.commands;

import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author GeorgeV22
 */
public class Holograms extends BukkitCommand {

    private final VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    public Holograms() {
        super("hologram");
        this.description = "hologram command";
        this.usageMessage = "/hologram";
        this.setPermission("voterewards.hologram");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrhologram", "vrh"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
            MinecraftUtils.msg(sender, "&c&l(!) &cHolographicDisplays is not enabled!");
            return true;
        }

        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!) &cNot enough arguments!");
            return true;
        }

        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram create <hologramName> <type>");
                return true;
            }

            if (HolographicDisplays.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                return true;
            }

            if (m.getConfig().get("Holograms." + args[2]) == null) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                return true;
            }

            HolographicDisplays.show(HolographicDisplays.updateHologram(HolographicDisplays.create(
                                    args[1],
                                    player.getLocation(),
                                    args[2], true),
                            m.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap()),
                    player);

            HolographicDisplays.getPlaceholderMap().clear();

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " with type " + args[2] + " successfully created!");

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return true;
            }

            if (!HolographicDisplays.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                return true;
            }

            HolographicDisplays.remove(args[1], true);

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully removed!");
        } else if (args[0].equalsIgnoreCase("update")) {
            if (HolographicDisplays.hologramExists(args[1])) {
                Hologram hologram = HolographicDisplays.getHologramMap().get(args[1]);
                HolographicDisplays.updateHologram(hologram, m.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap());
                HolographicDisplays.getPlaceholderMap().clear();
                HolographicDisplays.hide(hologram, player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(m, () -> HolographicDisplays.show(hologram, player), 20);
                MinecraftUtils.msg(player, args[1] + " " + args[2]);
                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully updated!");
            } else {
                MinecraftUtils.msg(player, "&c&l(!) &cHologram doesn't exists!");
            }
        }
        return true;
    }
}
