package com.georgev22.voterewards.utilities;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Updater {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();
    private final String localVersion = voteRewardPlugin.getDescription().getVersion();
    private final String BASE_URL = "https://raw.githubusercontent.com/GeorgeV220/VoteRewards/master/version.md";
    private String onlineVersion;

    public Updater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(voteRewardPlugin, () -> {
            voteRewardPlugin.getLogger().info("Checking for Updates ... ");
            try {
                System.setProperty("http.agent", "Chrome");
                HttpsURLConnection con = (HttpsURLConnection) new URL(BASE_URL).openConnection();

                con.setDoOutput(true);

                con.setRequestMethod("GET");

                onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            } catch (Exception ex) {

                MinecraftUtils.debug(voteRewardPlugin, "Failed to check for an update on Git.", "Either Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                MinecraftUtils.debug(voteRewardPlugin, "You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                MinecraftUtils.debug(voteRewardPlugin,
                        "New stable version available!",
                        "Version: " + onlineVersion + ". You are running version: " + localVersion,
                        "Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
            } else {
                MinecraftUtils.debug(voteRewardPlugin, "You are currently using the " + localVersion + " version which is under development.",
                        "Your version is " + localVersion,
                        "Latest released version is " + onlineVersion,
                        "If you have problems contact me on discord or github. Thank you for testing this version");
            }

        }, 20L, 20 * 7200);
    }

    public Updater(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            MinecraftUtils.msg(player, "&e&lUpdater &8» &6Checking for Updates ...");
            try {
                System.setProperty("http.agent", "Chrome");
                HttpsURLConnection con = (HttpsURLConnection) new URL(BASE_URL).openConnection();

                con.setDoOutput(true);

                con.setRequestMethod("GET");

                onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            } catch (Exception ex) {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &cFailed to check for an update on Git.");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &cEither Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                MinecraftUtils.msg(player,
                        "&e&lUpdater &8» &6New version available!");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Version: &c"
                        + onlineVersion + ". &6You are running version: &c" + localVersion);
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/VoteRewards/releases/");

            } else {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6You are currently using the &c" + localVersion + " &6version which is under development. If you have problems contact me on discord or github");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Your version is &c" + localVersion);
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Latest released version is &c" + onlineVersion);
            }
        });
    }


    private int compareVersions(@NotNull String version1, @NotNull String version2) {
        if (version1.contains("SNAPSHOT") | version1.contains("SNAPSHOT")) {
            return -1;
        }

        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

}