package com.georgev22.voterewards.commands;

import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;

public class NPCCommand extends BukkitCommand {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private final NPCPool npcPool = NPCPool.builder(voteRewardPlugin)
            .spawnDistance(60)
            .actionDistance(30)
            .tabListRemoveTicks(20)
            .build();

    public NPCCommand() {
        super("vnpc");
        this.description = "rewards command";
        this.usageMessage = "/vnpc";
        this.setPermission("voterewards.npc");
        this.setPermissionMessage(MinecraftUtils.colorize(MessagesUtil.NO_PERMISSION.getMessages()[0]));
    }

    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) return true;

        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 1) {
                return true;
            }
            if (!(sender instanceof Player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return true;
            }
            Player player = (Player) sender;
            Profile profile = new Profile(VoteUtils.getTopPlayer(Integer.parseInt(args[1])));
            profile.complete();

            profile.setUniqueId(new UUID(new Random().nextLong(), 0));

            NPC.builder()
                    .profile(profile)
                    .location(player.getLocation())
                    .imitatePlayer(false)
                    .lookAtPlayer(true)
                    .build(npcPool);

            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully created npc!");
            return true;
        }

        return true;
    }
}