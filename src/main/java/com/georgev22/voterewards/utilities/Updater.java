package com.georgev22.voterewards.utilities;

import com.georgev22.voterewards.VoteRewardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Updater {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    public Updater() {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            final String BASE_URL = "https://raw.githubusercontent.com/GeorgeV220/VoteRewards/master/version.md";
            voteRewardPlugin.getLogger().info("Checking for Updates ... ");

            String onlineVersion;

            try {
                System.setProperty("http.agent", "Chrome");
                HttpsURLConnection con = (HttpsURLConnection) new URL(BASE_URL).openConnection();

                con.setDoOutput(true);

                con.setRequestMethod("GET");

                onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            } catch (Exception ex) {

                voteRewardPlugin.getLogger().warning("Failed to check for an update on Git.");

                voteRewardPlugin.getLogger().warning("Either Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (!voteRewardPlugin.getDescription().getVersion().equalsIgnoreCase(onlineVersion)) {
                if (onlineVersion.contains("Beta")) {
                    voteRewardPlugin.getLogger().warning("New beta version available!");

                    voteRewardPlugin.getLogger().warning("Beta Version: " + onlineVersion + ". You are running version: "
                            + voteRewardPlugin.getDescription().getVersion());
                    voteRewardPlugin.getLogger().warning("Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
                } else if (onlineVersion.contains("Alpha")) {
                    voteRewardPlugin.getLogger().warning("New Alpha version available!");

                    voteRewardPlugin.getLogger().warning("Alpha Version: " + onlineVersion + ". You are running version: "
                            + voteRewardPlugin.getDescription().getVersion());
                    voteRewardPlugin.getLogger().warning("Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
                } else {
                    voteRewardPlugin.getLogger().warning("New stable version available!");

                    voteRewardPlugin.getLogger().warning("Stable Version: " + onlineVersion + ". You are running version: "
                            + voteRewardPlugin.getDescription().getVersion());
                    voteRewardPlugin.getLogger().warning("Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
                }
            } else {
                if (voteRewardPlugin.getDescription().getVersion().contains("Beta")) {
                    voteRewardPlugin.getLogger().info("You are running the newest beta build.");
                } else if (voteRewardPlugin.getDescription().getVersion().contains("Alpha")) {
                    voteRewardPlugin.getLogger().info("You are running the newest alpha build.");
                } else {
                    voteRewardPlugin.getLogger().info("You are running the newest stable build.");
                }
            }
        });
    }

    public Updater(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            final String BASE_URL = "https://raw.githubusercontent.com/GeorgeV220/VoteRewards/master/version.md";
            Utils.msg(player, "&e&lUpdater &8» &6Checking for Updates ...");

            String onlineVersion;

            try {
                System.setProperty("http.agent", "Chrome");
                HttpsURLConnection con = (HttpsURLConnection) new URL(BASE_URL).openConnection();

                con.setDoOutput(true);

                con.setRequestMethod("GET");

                onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            } catch (Exception ex) {
                Utils.msg(player, "&e&lUpdater &8» &cFailed to check for an update on Git.");
                Utils.msg(player, "&e&lUpdater &8» &cEither Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (!voteRewardPlugin.getDescription().getVersion().equalsIgnoreCase(onlineVersion)) {
                if (onlineVersion.contains("Beta")) {
                    Utils.msg(player, "&e&lUpdater &8» &6New beta version available!");
                    Utils.msg(player, "&e&lUpdater &8» &6Beta Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + voteRewardPlugin.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
                } else if (onlineVersion.contains("Alpha")) {
                    Utils.msg(player, "&e&lUpdater &8» &6New alpha version available!");
                    Utils.msg(player, "&e&lUpdater &8» &6Alpha Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + voteRewardPlugin.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
                } else {
                    Utils.msg(player,
                            "&e&lUpdater &8» &6New stable version available!");
                    Utils.msg(player, "&e&lUpdater &8» &6Stable Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + voteRewardPlugin.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/VoteRewards/releases/");

                }
            } else {
                if (voteRewardPlugin.getDescription().getVersion().contains("Beta")) {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest beta build.");
                } else if (voteRewardPlugin.getDescription().getVersion().contains("Alpha")) {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest alpha build.");
                } else {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest stable build.");
                }
            }
        });
    }

}