package com.georgev22.voterewards.utilities.inventory.inventories;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.externals.xseries.XMaterial;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.inventory.CustomItemInventory;
import com.georgev22.voterewards.utilities.inventory.IPagedInventory;
import com.georgev22.voterewards.utilities.inventory.ItemBuilder;
import com.georgev22.voterewards.utilities.inventory.NavigationRow;
import com.georgev22.voterewards.utilities.inventory.handlers.PagedInventoryCustomNavigationHandler;
import com.georgev22.voterewards.utilities.inventory.navigationitems.*;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class VoteTopInventory {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    public void openTopPlayersInventory(Player player, String inventoryName, ObjectMap<String, Integer> stringIntegerObjectMap, OptionsUtil optionsUtil) {

        List<NavigationItem> navigationItemList = Lists.newArrayList();

        if (voteRewardPlugin.getConfig().getConfigurationSection("Options.gui.custom item.navigation") != null)
            for (String s : voteRewardPlugin.getConfig().getConfigurationSection("Options.gui.custom item.navigation").getKeys(false)) {
                ItemBuilder itemBuilder =
                        new ItemBuilder(new ItemStack(Material.valueOf(voteRewardPlugin.getConfig().getString("Options.gui.custom item.navigation." + s + ".item"))))
                                .title(Utils.colorize(voteRewardPlugin.getConfig().getString("Options.gui.custom item.navigation." + s + ".title")))
                                .amount(voteRewardPlugin.getConfig().getInt("Options.gui.custom item.navigation." + s + ".amount"))
                                .showAllAttributes(voteRewardPlugin.getConfig().getBoolean("Options.gui.custom item.navigation." + s + ".show all attributes"))
                                .lores(voteRewardPlugin.getConfig().getStringList("Options.gui.custom item.navigation." + s + ".lores"))
                                .glow(voteRewardPlugin.getConfig().getBoolean("Options.gui.custom item.navigation." + s + ".glow"));
                ItemStack itemStack = itemBuilder.build();
                CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                    @Override
                    public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                        for (String command : voteRewardPlugin.getConfig().getStringList("Options.gui.custom item.navigation." + s + ".commands")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                        }
                    }
                };
                navigationItemList.add(navigationItem);
            }

        IPagedInventory pagedInventory = voteRewardPlugin.getInventoryAPI()
                .createPagedInventory(
                        new NavigationRow(
                                new NextNavigationItem(OptionsUtil.GUI_NAVIGATION_ITEMS_NEXT.getItemStack(false), voteRewardPlugin.getConfig().getInt("Options.gui.navigation items.next.slot", 6)),
                                new PreviousNavigationItem(OptionsUtil.GUI_NAVIGATION_ITEMS_BACK.getItemStack(false), voteRewardPlugin.getConfig().getInt("Options.gui.navigation items.back.slot", 2)),
                                new CloseNavigationItem(OptionsUtil.GUI_NAVIGATION_ITEMS_CANCEL.getItemStack(false), voteRewardPlugin.getConfig().getInt("Options.gui.navigation items.cancel.slot", 4)),
                                navigationItemList.toArray(new NavigationItem[0])));

        List<Inventory> inventoryList = Lists.newArrayList();

        UserVoteData userManager = UserVoteData.getUser(player.getUniqueId());


        ObjectMap<Integer, ItemStack> objectMap = ObjectMap.newHashObjectMap();

        if (voteRewardPlugin.getConfig().getConfigurationSection("Options.gui.custom item.gui") != null)
            for (String s : voteRewardPlugin.getConfig().getConfigurationSection("Options.gui.custom item.gui").getKeys(false)) {
                ItemBuilder itemBuilder =
                        new ItemBuilder(new ItemStack(Material.valueOf(voteRewardPlugin.getConfig().getString("Options.gui.custom item.gui." + s + ".item"))))
                                .title(Utils.colorize(voteRewardPlugin.getConfig().getString("Options.gui.custom item.gui." + s + ".title")))
                                .amount(voteRewardPlugin.getConfig().getInt("Options.gui.custom item.gui." + s + ".amount"))
                                .showAllAttributes(voteRewardPlugin.getConfig().getBoolean("Options.gui.custom item.gui." + s + ".show all attributes"))
                                .lores(voteRewardPlugin.getConfig().getStringList("Options.gui.custom item.gui." + s + ".lores"))
                                .glow(voteRewardPlugin.getConfig().getBoolean("Options.gui.custom item.gui." + s + ".glow"));
                ItemStack itemStack = itemBuilder.build();
                objectMap.append(Integer.parseInt(s), itemStack);
            }

        CustomItemInventory customItemInventory = new CustomItemInventory(inventoryName, objectMap, 54);

        Inventory inventory = customItemInventory.getInventory();

        int i = 0;

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

            inventory.setItem(i, new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial())
                    .skull(entry.getKey())
                    .showAllAttributes(false)
                    .amount(1)
                    .title(Utils.colorize(
                            Utils.placeHolder(optionsUtil.getItemStack(false).getItemMeta().getDisplayName(),
                                    ObjectMap.newHashObjectMap()
                                            .append("%displayName%", entry.getKey()),
                                    true)))
                    .lores(Utils.colorize(
                            Utils.placeHolder(optionsUtil.getItemStack(false).getItemMeta().getLore(),
                                    ObjectMap.newHashObjectMap()
                                            .append("%votes%", String.valueOf(entry.getValue())),
                                    true)))
                    .build());

            i++;
        }
        inventoryList.forEach(pagedInventory::addPage);
        pagedInventory.open(player, 0, OptionsUtil.GUI_ANIMATION.isEnabled(), OptionsUtil.GUI_ANIMATION_TYPE.getStringValue().equalsIgnoreCase("wave"));

    }

}
