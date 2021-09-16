package com.georgev22.voterewards.commands;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventory.inventories.VoteTopInventory;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class VoteTop extends BukkitCommand {


    public VoteTop() {
        super("votetop");
        this.description = "Votes command";
        this.usageMessage = "/votes";
        this.setPermission("voterewards.votetop");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vtop", "vrtop", "vvtop"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            sendMsg(sender);
        } else {
            if (OptionsUtil.EXPERIMENTAL_FEATURES.isEnabled() && OptionsUtil.VOTETOP_GUI.isEnabled()) {
                new VoteTopInventory().openTopPlayersInventory(((Player) sender).getPlayer(), !OptionsUtil.VOTETOP_GUI_TYPE.getStringValue().equalsIgnoreCase("monthly"));
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

        VoteUtils.getTopPlayers(OptionsUtil.VOTETOP_VOTERS.getIntValue()).forEach((key, value) -> {
            placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

            MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
        });

        if (OptionsUtil.VOTETOP_LINE.isEnabled())
            MessagesUtil.VOTE_TOP_LINE.msg(sender);

        if (OptionsUtil.VOTETOP_ALL_TIME_ENABLED.isEnabled())
            VoteUtils.getAllTimeTopPlayers(OptionsUtil.VOTETOP_ALL_TIME_VOTERS.getIntValue()).forEach((key, value) -> {
                placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

                MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
            });
        if (OptionsUtil.VOTETOP_FOOTER.isEnabled())
            MessagesUtil.VOTE_TOP_FOOTER.msg(sender);
        placeholders.clear();
    }
}
