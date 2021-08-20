package com.georgev22.voterewards.utilities.inventory.inventories;

import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventory.ItemBuilder;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.google.common.annotations.Beta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@Beta
public class VoteInventory {

    public void openInventory(Player player) {
        UserVoteData userVoteData = UserVoteData.getUser(player);

        Inventory inventory = Bukkit.createInventory(null, 54, "Vote Inventory");

        for (Map.Entry<String, Long> b : userVoteData.getServicesLastVote().entrySet()) {
            String serviceName = b.getKey();
            Long serviceLastVote = b.getValue();

            ItemBuilder itemBuilder = new ItemBuilder(Material.DIRT);
            itemBuilder.title(serviceName);
            itemBuilder.lores(
                    "Last Vote:" + Instant.ofEpochMilli(serviceLastVote).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())),
                    serviceLastVote + (OptionsUtil.DAILY_HOURS.getIntValue() * 60 * 60 * 1000) > System.currentTimeMillis() ? "You cannot vote yet" : "You can vote again");

            inventory.addItem(itemBuilder.build());
        }

        player.openInventory(inventory);
    }

}
