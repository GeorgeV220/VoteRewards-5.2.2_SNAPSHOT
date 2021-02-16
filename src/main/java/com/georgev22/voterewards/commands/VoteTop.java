package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
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
        if (!(sender instanceof Player)) {
            sendMsg(sender);
        } else {
            if (OptionsUtil.VOTETOP_GUI.isEnabled() && Bukkit.getPluginManager().isPluginEnabled("LeaderHeads")) {

                if (OptionsUtil.VOTETOP_GUI_TYPE.getStringValue().equalsIgnoreCase("monthly")) {
                    ((Player) sender).chat("/votetopgui");
                } else {
                    ((Player) sender).chat("/alltimevotetopgui");
                }
            } else {
                sendMsg(sender);
            }
        }
        return true;
    }

    public void sendMsg(CommandSender sender) {
        ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();

        if (OptionsUtil.VOTETOP_HEADER.isEnabled())
            MessagesUtil.VOTE_TOP_HEADER.msg(sender);

        for (Map.Entry<String, Integer> b : VoteUtils.getTopPlayers(OptionsUtil.VOTETOP_VOTERS.getIntValue()).entrySet()) {
            String[] arg = String.valueOf(b).split("=");
            placeholders.append("%name%", arg[0]).append("%votes%", arg[1]);

            MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
        }

        if (OptionsUtil.VOTETOP_LINE.isEnabled())
            MessagesUtil.VOTE_TOP_LINE.msg(sender);

        if (OptionsUtil.VOTETOP_ALL_TIME_ENABLED.isEnabled())
            for (Map.Entry<String, Integer> b : VoteUtils.getAllTimeTopPlayers(OptionsUtil.VOTETOP_ALL_TIME_VOTERS.getIntValue()).entrySet()) {
                String[] arg = String.valueOf(b).split("=");
                placeholders.append("%name%", arg[0]).append("%votes%", arg[1]);

                MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
            }
        if (OptionsUtil.VOTETOP_FOOTER.isEnabled())
            MessagesUtil.VOTE_TOP_FOOTER.msg(sender);
        placeholders.clear();
    }
}
