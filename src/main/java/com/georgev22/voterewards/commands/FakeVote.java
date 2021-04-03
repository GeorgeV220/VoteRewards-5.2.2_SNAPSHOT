package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FakeVote extends BukkitCommand {


    public FakeVote() {
        super("fakevote");
        this.description = "FakeVote command";
        this.usageMessage = "/fakevote";
        this.setPermission("voterewards.fakevote");
        this.setPermissionMessage(Utils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
        this.setAliases(Arrays.asList("vrfake", "vrfakevote", "vfake", "vfakevote"));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Utils.msg(sender, "&c&l(!) &cNot enough arguments");
                return true;
            }
            process(sender.getName(), "fakeVote");
        } else if (args.length == 1) {
            process(args[0], "fakeVote");
        } else {
            process(args[0], args[1]);
        }

        return true;
    }

    private void process(String userName, String serviceName) {
        Vote vote = new Vote();
        vote.setUsername(userName);
        vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        vote.setAddress("localhost");
        vote.setServiceName(serviceName);
        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));

    }
}
