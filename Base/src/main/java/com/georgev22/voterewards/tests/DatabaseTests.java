package com.georgev22.voterewards.tests;

import com.georgev22.voterewards.VoteRewardPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.PreparedStatement;
import java.util.UUID;

public class DatabaseTests implements Listener {

    VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    private static DatabaseTests instance = null;

    public static DatabaseTests getInstance() {
        return instance == null ? instance = new DatabaseTests() : instance;
    }

    public DatabaseTests() {
        // throw new AssertionError();
    }

    @Deprecated
    public void perPlayerTable(UUID uuid) throws Exception {
        Validate.notNull(uuid);
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `" + uuid.toString()
                + "` (\n  `name` varchar(255) DEFAULT NULL,\n  `votes` int(255) DEFAULT NULL,\n  `time` varchar(255) DEFAULT NULL,\n  `voteparty` int(255) DEFAULT NULL\n)";
        PreparedStatement stmt = m.getConnection().prepareStatement(sqlCreate);
        stmt.execute();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        try {
            perPlayerTable(event.getPlayer().getUniqueId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
