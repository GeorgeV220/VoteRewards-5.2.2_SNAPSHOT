package com.georgev22.voterewards.utilities;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.georgev22.voterewards.VoteRewardPlugin;
import org.bukkit.entity.Player;

public class Updater {

    private VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    public Updater() {
        new Thread(() -> {
            final String BASE_URL = "https://git.georgev22.com/GeorgeV22/VoteRewards/raw/branch/master/version.md";
            m.getLogger().info("Checking for Updates ... ");

            String onlineVersion;

            try {
                System.setProperty("http.agent", "Chrome");
                HttpsURLConnection con = (HttpsURLConnection) new URL(BASE_URL).openConnection();

                con.setDoOutput(true);

                con.setRequestMethod("GET");

                onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            } catch (Exception ex) {

                m.getLogger().warning("Failed to check for an update on Git.");

                m.getLogger().warning("Either Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (!m.getDescription().getVersion().equalsIgnoreCase(onlineVersion)) {
                if (m.getDescription().getVersion().contains("CustomBuild")) {
                    return;
                } else if (onlineVersion.contains("Beta")) {
                    m.getLogger().warning("New beta version availiable!");

                    m.getLogger().warning("Beta Version: " + onlineVersion + ". You are running version: "
                            + m.getDescription().getVersion());
                    m.getLogger().warning("Update at: https://www.mc-market.org/resources/9094/");
                } else if (onlineVersion.contains("Alpha")) {
                    m.getLogger().warning("New Alpha version availiable!");

                    m.getLogger().warning("Alpha Version: " + onlineVersion + ". You are running version: "
                            + m.getDescription().getVersion());
                    m.getLogger().warning("Update at: https://www.mc-market.org/resources/9094/");
                } else {
                    m.getLogger().warning("New stable version availiable!");

                    m.getLogger().warning("Stable Version: " + onlineVersion + ". You are running version: "
                            + m.getDescription().getVersion());
                    m.getLogger().warning("Update at: https://www.mc-market.org/resources/9094/");
                }
            } else {
                if (m.getDescription().getVersion().contains("Beta")) {
                    m.getLogger().info("You are running the newest beta build.");
                } else if (m.getDescription().getVersion().contains("Alpha")) {
                    m.getLogger().info("You are running the newest alpha build.");
                } else {
                    m.getLogger().info("You are running the newest stable build.");
                }
            }
        }).run();

    }

    public Updater(Player player) {
        new Thread(() -> {
            final String BASE_URL = "https://git.georgev22.com/GeorgeV22/VoteRewards/raw/branch/master/version.md";
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
            if (!m.getDescription().getVersion().equalsIgnoreCase(onlineVersion)) {
                if (m.getDescription().getVersion().contains("CustomBuild")) {
                    return;
                } else if (onlineVersion.contains("Beta")) {
                    Utils.msg(player, "&e&lUpdater &8» &6New beta version availiable!");
                    Utils.msg(player, "&e&lUpdater &8» &6Beta Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + m.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://www.mc-market.org/resources/9094/");
                } else if (onlineVersion.contains("Alpha")) {
                    Utils.msg(player, "&e&lUpdater &8» &6New alpha version availiable!");
                    Utils.msg(player, "&e&lUpdater &8» &6Alpha Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + m.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://www.mc-market.org/resources/9094/");
                } else {
                    Utils.msg(player,
                            "&e&lUpdater &8» &6New stable version availiable!");
                    Utils.msg(player, "&e&lUpdater &8» &6Stable Version: &c"
                            + onlineVersion + ". &6You are running version: &c" + m.getDescription().getVersion());
                    Utils.msg(player, "&e&lUpdater &8» &6Update at: https://www.mc-market.org/resources/9094/");

                }
            } else {
                if (m.getDescription().getVersion().contains("Beta")) {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest beta build.");
                } else if (m.getDescription().getVersion().contains("Alpha")) {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest alpha build.");
                } else {
                    Utils.msg(player, "&e&lUpdater &8» &6You are running the newest stable build.");
                }
            }
        }).run();

    }

}