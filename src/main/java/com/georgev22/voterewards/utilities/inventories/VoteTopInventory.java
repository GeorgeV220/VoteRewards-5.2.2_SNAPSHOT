package com.georgev22.voterewards.utilities.inventories;

import com.georgev22.api.inventory.CustomItemInventory;
import com.georgev22.api.inventory.IPagedInventory;
import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.api.inventory.NavigationRow;
import com.georgev22.api.inventory.handlers.PagedInventoryCustomNavigationHandler;
import com.georgev22.api.inventory.navigationitems.*;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class VoteTopInventory {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    public void openTopPlayersInventory(Player player, boolean allTimeVotes) {

        List<NavigationItem> navigationItemList = Lists.newArrayList();
        final FileManager fileManager = FileManager.getInstance();

        if (fileManager.getVoteTopInventory().getFileConfiguration().getConfigurationSection("custom item.navigation") != null)
            for (String s : fileManager.getVoteTopInventory().getFileConfiguration().getConfigurationSection("custom item.navigation").getKeys(false)) {
                ItemStack itemStack = ItemBuilder.buildItemFromConfig(fileManager.getVoteTopInventory().getFileConfiguration(), "custom item.navigation." + s).build();
                CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                    @Override
                    public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                        for (String command : fileManager.getVoteTopInventory().getFileConfiguration().getStringList("custom item.navigation." + s + ".commands")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                        }
                    }
                };
                navigationItemList.add(navigationItem);
            }

        IPagedInventory pagedInventory = voteRewardPlugin.getInventoryAPI()
                .createPagedInventory(
                        new NavigationRow(
                                new NextNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteTopInventory().getFileConfiguration(), "navigation.next").build(), fileManager.getVoteTopInventory().getFileConfiguration().getInt("navigation.next.slot", 6)),
                                new PreviousNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteTopInventory().getFileConfiguration(), "navigation.back").build(), fileManager.getVoteTopInventory().getFileConfiguration().getInt("navigation.back.slot", 2)),
                                new CloseNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteTopInventory().getFileConfiguration(), "navigation.cancel").build(), fileManager.getVoteTopInventory().getFileConfiguration().getInt("navigation.cancel.slot", 4)),
                                navigationItemList.toArray(new NavigationItem[0])));

        List<Inventory> inventoryList = Lists.newArrayList();

        ObjectMap<Integer, ItemStack> objectMap = ObjectMap.newHashObjectMap();

        if (fileManager.getVoteTopInventory().getFileConfiguration().getConfigurationSection("custom item.gui") != null)
            for (String s : fileManager.getVoteTopInventory().getFileConfiguration().getConfigurationSection("custom item.gui").getKeys(false)) {
                String itemServiceName = fileManager.getVoteTopInventory().getFileConfiguration().getString("custom item.gui." + s + ".service name");
                ItemStack itemStack = ItemBuilder.buildItemFromConfig(fileManager.getVoteTopInventory().getFileConfiguration(), "custom item.gui." + s).build();
                objectMap.append(Integer.parseInt(s), itemStack);
            }

        CustomItemInventory customItemInventory = new CustomItemInventory(MinecraftUtils.colorize(fileManager.getVoteTopInventory().getFileConfiguration().getString("name")), objectMap, 54);

        Inventory inventory = customItemInventory.getInventory();

        int i = 0;
        ObjectMap<String, Integer> stringIntegerObjectMap = allTimeVotes ? VoteUtils.getPlayersByAllTimeVotes() : VoteUtils.getPlayersByVotes();
        for (Map.Entry<String, Integer> entry : stringIntegerObjectMap.entrySet()) {
            if (entry == null) {
                continue;
            }

            if (inventoryList.isEmpty()) {
                inventory = customItemInventory.getInventory();
                inventoryList.add(inventory);
            }

            if (i > 45) {
                i = 0;
                inventory = customItemInventory.getInventory();
                inventoryList.add(inventory);
            }

            for (Map.Entry<Integer, ItemStack> test : objectMap.entrySet()) {
                if (inventory.getItem(i) != null && i == test.getKey()) {
                    i++;
                }
            }
            inventory.setItem(i, ItemBuilder.buildItemFromConfig(
                    fileManager.getVoteTopInventory().getFileConfiguration(),
                    allTimeVotes ? "gui.player all" : "gui.player",
                    ObjectMap.newHashObjectMap()
                            .append("%votes%", String.valueOf(entry.getValue())),
                    ObjectMap.newHashObjectMap()
                            .append("%displayName%", entry.getKey())).skull(entry.getKey()).build());

            i++;
        }
        inventoryList.forEach(pagedInventory::addPage);
        pagedInventory.open(player, 0, fileManager.getVoteTopInventory().getFileConfiguration().getBoolean("animation.enabled"));

    }

}
