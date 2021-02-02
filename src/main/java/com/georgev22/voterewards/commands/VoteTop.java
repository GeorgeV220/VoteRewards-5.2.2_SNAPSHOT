package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Options;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.maps.HashObjectMap;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class VoteTop extends BukkitCommand {


    public VoteTop() {
        super("votetop");
        this.description = "Votes command";
        this.usageMessage = "/votes";
        this.setPermission("voterewards.votetop");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vtop", "vrtop", "vvtop"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        FileManager fm = FileManager.getInstance();
        FileConfiguration conf = fm.getConfig().getFileConfiguration();

        if (conf.getBoolean("Options.gui") && Bukkit.getPluginManager().isPluginEnabled("LeaderHeads")) {
            if (!(sender instanceof Player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return true;
            }
            Player player = (Player) sender;
            player.chat("/votetopgui");
            return true;
        }

        ObjectMap<String, String> placeholders = new HashObjectMap<>();

        if (Options.VOTETOP_HEADER.isEnabled())
            MessagesUtil.VOTE_TOP_HEADER.msg(sender);

        for (Map.Entry<String, Integer> b : Utils.getTopPlayers((Integer) Options.VOTETOP_VOTERS.getValue()).entrySet()) {
            String[] arg = String.valueOf(b).split("=");
            placeholders.append("%name%", arg[0]).append("%votes%", arg[1]);

            MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
        }
        if (Options.VOTETOP_FOOTER.isEnabled())
            MessagesUtil.VOTE_TOP_FOOTER.msg(sender);
        placeholders.clear();
        return true;
    }
}
