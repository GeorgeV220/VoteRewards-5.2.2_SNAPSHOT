package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.utilities.player.UserUtils;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LeaderHeads extends OnlineDataCollector {

    public LeaderHeads() {
        super("votetop", "VoteRewards", BoardType.DEFAULT, "&3Vote Top", "votetopgui",
                Arrays.asList(null, null, "&e{amount} Votes", null));
    }

    @Override
    public Double getScore(Player player) {
        return (double) UserUtils.getUser(player.getUniqueId()).getVotes();
    }
}