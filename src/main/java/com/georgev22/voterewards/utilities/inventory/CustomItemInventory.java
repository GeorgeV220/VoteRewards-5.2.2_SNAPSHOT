package com.georgev22.voterewards.utilities.inventory;

import com.georgev22.externals.utilities.maps.ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record CustomItemInventory(String inventoryName, ObjectMap<Integer, ItemStack> objectMap, int inventorySize) {

    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        for (Map.Entry<Integer, ItemStack> itemStackEntry : objectMap.entrySet()) {
            inventory.setItem(itemStackEntry.getKey(), itemStackEntry.getValue());
        }
        return inventory;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public Integer getInventorySize() {
        return inventorySize;
    }
}
