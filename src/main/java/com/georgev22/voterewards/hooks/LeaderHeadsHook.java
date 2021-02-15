package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.utilities.player.UserVoteData;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LeaderHeadsHook {

    public void register() {
        new LeaderHeadsVoteTop();
        new LeaderHeadsAllTimeVotes();
    }

    private static class LeaderHeadsVoteTop extends OnlineDataCollector {

        public LeaderHeadsVoteTop() {
            super("votetop", "VoteRewards", BoardType.DEFAULT, "&3Vote Top", "votetopgui",
                    Arrays.asList(null, null, "&e{amount} Votes", null));
        }

        @Override
        public Double getScore(Player player) {
            return (double) UserVoteData.getUser(player.getUniqueId()).getVotes();
        }
    }

    private static class LeaderHeadsAllTimeVotes extends OnlineDataCollector {

        public LeaderHeadsAllTimeVotes() {
            super("votetop", "VoteRewards", BoardType.DEFAULT, "&3All Time Vote Top", "alltimevotetopgui",
                    Arrays.asList(null, null, "&e{amount} Votes", null));
        }

        @Override
        public Double getScore(Player player) {
            return (double) UserVoteData.getUser(player.getUniqueId()).getAllTimeVotes();
        }
    }
}