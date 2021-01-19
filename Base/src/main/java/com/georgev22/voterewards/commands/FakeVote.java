package com.georgev22.voterewards.commands;

import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

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

    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                Utils.msg(sender, "&c&l(!) &cNot enough arguments");
            } else if (args.length == 1) {
                process(args[0], "fakeVote");
            } else {
                process(args[0], args[1]);
            }
            return true;
        }

        if (args.length == 0) {
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
