package com.georgev22.voterewards.utilities.inventory.inventories;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.inventory.CustomItemInventory;
import com.georgev22.voterewards.utilities.inventory.IPagedInventory;
import com.georgev22.voterewards.utilities.inventory.ItemBuilder;
import com.georgev22.voterewards.utilities.inventory.NavigationRow;
import com.georgev22.voterewards.utilities.inventory.handlers.PagedInventoryCustomNavigationHandler;
import com.georgev22.voterewards.utilities.inventory.navigationitems.*;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Beta
public class VoteInventory {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    public void openInventory(Player player) {
        UserVoteData userVoteData = UserVoteData.getUser(player);

        List<NavigationItem> navigationItemList = Lists.newArrayList();
        ObjectMap<Integer, ItemStack> objectMap = ObjectMap.newHashObjectMap();

        final FileManager fileManager = FileManager.getInstance();

        if (fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.navigation") != null)
            for (String s : fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.navigation").getKeys(false)) {
                ItemStack itemStack = ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "custom item.navigation." + s, fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.random colors")).build();
                CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                    @Override
                    public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                        for (String command : fileManager.getVoteInventory().getFileConfiguration().getStringList("custom item.navigation." + s + ".commands")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                        }
                    }
                };
                navigationItemList.add(navigationItem);
            }

        if (fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.gui") != null)
            for (String s : fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.gui").getKeys(false)) {
                String itemServiceName = fileManager.getVoteInventory().getFileConfiguration().getString("custom item.gui." + s + ".service name");
                if (!userVoteData.getServicesLastVote().containsKey(itemServiceName)) {
                    userVoteData.getServicesLastVote().append(itemServiceName, 0L);
                }
                ZonedDateTime zonedDateTime = Instant.ofEpochMilli(
                                userVoteData.getServicesLastVote().get(itemServiceName))
                        .atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));

                ItemStack itemStack = ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "custom item.gui." + s,
                        ObjectMap.newHashObjectMap()
                                .append("%check%", userVoteData.getServicesLastVote().getLong(itemServiceName) + (OptionsUtil.DAILY_HOURS.getIntValue() * 3600000) >= System.currentTimeMillis() ? "You can't vote" : "You can vote")
                                .append("%lastVoteInTimeMillis%", String.valueOf(userVoteData.getServicesLastVote().getLong(itemServiceName)))
                                .append("%lastVote%", String.valueOf(zonedDateTime))
                                .append("%lastVoteDay%", String.valueOf(zonedDateTime.getDayOfMonth()))
                                .append("%lastVoteMonthName%", zonedDateTime.getMonth().name())
                                .append("%lastVoteMonth%", String.valueOf(zonedDateTime.getMonthValue()))
                                .append("%lastVoteYear%", String.valueOf(zonedDateTime.getYear()))
                                .append("%lastVoteHour%", String.valueOf(zonedDateTime.getHour()))
                                .append("%lastVoteMinute%", String.valueOf(zonedDateTime.getMinute()))
                                .append("%lastVoteSecond%", String.valueOf(zonedDateTime.getSecond())),
                        ObjectMap.newHashObjectMap(),
                        fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.random colors")).build();
                objectMap.append(Integer.parseInt(s), itemStack);
            }

        IPagedInventory pagedInventory = voteRewardPlugin.getInventoryAPI()
                .createPagedInventory(
                        new NavigationRow(
                                new NextNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "navigation.next", fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.random colors")).build(), fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.next.slot", 6)),
                                new PreviousNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "navigation.back", fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.random colors")).build(), fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.back.slot", 2)),
                                new CloseNavigationItem(ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "navigation.cancel", fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.random colors")).build(), fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.cancel.slot", 4)),
                                navigationItemList.toArray(new NavigationItem[0])));

        CustomItemInventory customItemInventory = new CustomItemInventory(Utils.colorize(fileManager.getVoteInventory().getFileConfiguration().getString("name")), objectMap, 54);

        Inventory inventory = customItemInventory.getInventory();

        pagedInventory.addPage(inventory);

        pagedInventory.open(player, 0, fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.enabled"), fileManager.getVoteInventory().getFileConfiguration().getString("animation.type").equalsIgnoreCase("wave"));
    }

}
